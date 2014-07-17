(ns om-components.core
  (:require-macros  [cljs.core.async.macros :refer  [go]])
    (:require
      [clojure.browser.repl :as repl]
     #_[devcards.core :as dc :include-macros true]
     [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]
      [goog.dom :as gdom]
      [goog.events :as events]
      [cljs.core.async :refer  [put! chan <!]]
     #_[sablono.core :as sab :include-macros true])
    #_(:require-macros
       [devcards.core :refer [defcard is are= are-not=]]))

(enable-console-print!)
(repl/connect "http://localhost:9000/repl")

;; utility

(defn selected-value
  [sel]
  "Get the selected value from a <select>"
  (let [idx (.-selectedIndex sel)]
    (.-value (aget (.-options sel) idx))))

(defn display
  [show]
  (if show
    #js {}
    #js {:display "none"})) 

;; component to display in a span

(defn span-display [data owner {:keys [key-name className] :as opts}]
  (om/component
    (dom/span #js {:className className} (pr-str (key-name data)))))

;; component to display in a div

(defn div-display [data owner {:keys [key-name className] :as opts}]
  (om/component
    (dom/div #js {:className className} (key-name data))))

;; display a select
(defn choice-display [data owner {:keys [key-name className values labels] :as opts}]
  (om/component
    (apply dom/select #js {:className className
                           :value (key-name data)
                           :ref "choice"
                           :onChange (fn []
                                       (this-as s
                                                (let [val (selected-value (om/get-node owner "choice"))]
                                                  (om/transact! data key-name (fn [] val)))))
                           }
       (map (fn [o l] (dom/option #js {:value o} (or l o))) values labels))))

;; datatable component!
(defn data-table [data owner {:keys [headings] :as opts}]
  "Data table: display data in a table provided definitions for columns.
     options: headings is a list of column definitions
     
     header definition:
       :key - The key to lookup in each data entry
       :label - The column lable
       :component - An om component handle the data (needs to follow the convention of taking the keyname and a map)
       :opts - A map of options passed to the cell component.
  "
  (reify
    om/IInitState
    (init-state [_]
      {:sort-column 0 :sort-fn >})
    om/IRenderState
    (render-state [_ {:keys [sort-column sort-fn]}]
      (let [sorted (sort-by (:key (headings sort-column)) sort-fn data)]
        (dom/div #js {:className "datatable"}
           (dom/div #js {:className "datatable-content"}
              (dom/table nil
                (dom/thead #js {:className "datatable-columns"}
                   (apply dom/tr #js {:className "datatable-first datatable-last"}
                     (map-indexed (fn [c h]
                       (dom/th #js {:onClick #(if (= sort-column c)
                                                (om/set-state! owner :sort-fn (if (= sort-fn <) > <))
                                                (om/set-state! owner :sort-column c))}
                               (:label h)
                               (dom/span #js {:className (str "pull-right glyphicon glyphicon-chevron-" (if (= sort-fn <) "down" "up"))
                                              :style (display (= sort-column c))})))
                        headings)))
                 (apply dom/tbody #js {:className "datatable-data"}
                   (map (fn [row cls] 
                     (apply dom/tr #js {:className cls}
                       (map (fn [c] 
                         (dom/td nil 
                           (dom/div #js {:className "datatable-liner"}
                             (om/build 
                               (or (:component c) span-display)
                               row
                               {:opts (merge (:opts c) {:key-name (:key c)})})))) headings))) sorted (cycle ["datatable-even" "datatable-odd"]))))))))))


;; editable (from om tutorial)

(defn handle-change [evt owner]
  (om/set-state! owner :edit-value (.. evt -target -value)))

(defn end-edit
  [text owner cursor edit-key cb]
  (om/set-state! owner :editing false)
  (om/transact! cursor edit-key (fn [] text))
  (.focus (om/get-node owner "val"))
  (when cb
    (cb text)))

(defn editable [data owner {:keys [key-name on-edit]}]
  (reify
    om/IInitState
    (init-state [_]
      {:editing false :edit-value ""})
    om/IRenderState
    (render-state [_ {:keys [editing edit-value]}]
      (let [text (get data key-name)]
        (dom/div #js {:className "editable"}
                 (dom/span
                   #js {:style (display (not editing))
                        :className "glyphicon glyphicon-edit pull-right"
                        :onClick (fn [_]
                                   (om/set-state! owner :edit-value text)
                                   (om/set-state! owner :editing true)
                                   (.focus (om/get-node owner "edit")))})
                 (dom/span #js {:style (display (not editing)) :ref "val"} text)
                 (dom/input
                   #js {:style (display editing)
                        :className "form-control"
                        :value edit-value
                        :ref "edit"
                        :onChange #(handle-change % owner)
                        :onKeyDown #(condp == (.-keyCode %)
                                       13 (end-edit edit-value owner data key-name on-edit)
                                       27 (end-edit text owner data key-name nil)
                                       true)
                        :onBlur (fn [e]
                                  (when (om/get-state owner :editing)
                                    (end-edit edit-value owner data key-name on-edit)))}))))))


;; Pretty percentage display

(def pi Math/PI)
(def twopi (* 2  Math/PI))
(def piovertwo (/ Math/PI 2))

(defn circle-y [c r t]
  (Math/round (+ c (* r (Math/sin (- (* twopi t) piovertwo)))))) 

(defn circle-x [c r t]
  (Math/round (+ c (* r (Math/cos (- (* twopi t) piovertwo))))))

(defn pretty-percent [data owner {:keys [key-name maxval]}]
  (reify
    om/IRender
    (render [_]
      (let [r 20
            cx 25
            cy 25
            c 0
            t (/ (key-name data) maxval)
            x (circle-x cx r t)
            y (circle-y cy r t)
            large (if (> t .5) 1 0)
            ]
          (dom/svg #js {:className "pretty-percent"
                        :width 50
                        :height 50}
                   (dom/path #js {:className "outer-ring"
                                  :d "M25,25m0,-20a20,20 0 1,1 0,40a20,20 0 1,1 0,-40"})
                   (dom/path #js {:className "indicator"
                                  :d (str "M25,25m0,-20A20,20 0 " large ",1 " x "," y)})
                   (dom/text #js {:x "50%" :y "54%"} (str (Math/round (* 100 t))))
                   #_(dom/circle #js {:cx 25 :cy 5 :r 2 :fill "red"})
                   #_(dom/circle #js {:cx x :cy y :r 2 :fill "green"})
                   )))))


;; draggable number editor

(def event->js
  {:onMouseDown (.-MOUSEDOWN events/EventType)
   :onMouseUp (.-MOUSEUP events/EventType)
   :onMouseMove (.-MOUSEMOVE events/EventType)})

(defn attach-to-window
  [event callback]
  (let [w (gdom/getWindow)
        e (event->js event)]
    (events/listen w e (fn [e] (callback e)))))

(defn detach-from-window
  ([event k]
   (let [w (gdom/getWindow)
         e (event->js event)]
     (events/unlistenByKey w e k)))
  ([event]
   (events/removeAll (gdom/getWindow) (event->js event))))


(defn clip-to
  [v min max]
  (Math/min max (Math/max v min)))

(defn smooth-value
  [data owner {:keys [key-name min-val max-val]}]
  (reify
    om/IInitState
    (init-state [_]
      {:dragging false
       :listeners []
       :update (chan)
       })
    om/IWillMount
    (will-mount [_]
      (let [upd (om/get-state owner :update)]
        (go (loop []
              (let [v (<! upd)]
                (om/transact! data key-name (fn [ov] (clip-to (- ov v) min-val max-val))))
              (recur)))))
    om/IRender
    (render [_]
      (dom/span #js {
                    :className "smooth-value"
                    :onMouseDown (fn [e] 
                                   (let [ch (om/get-state owner :update)]
                                     (om/set-state! owner :listeners [(attach-to-window :onMouseMove
                                                                                        (fn [ee] 
                                                                                          (let [dy (.-webkitMovementY (.-event_ ee))]
                                                                                            (put! ch dy)
                                                                                            (.preventDefault ee)
                                                                                            )))
                                                                      (attach-to-window :onMouseUp
                                                                                        (fn [e] 
                                                                                          (detach-from-window :onMouseMove)
                                                                                          #_(let [ll (om/get-state owner :listeners)]
                                                                                            (for [l ll]
                                                                                              (do
                                                                                                (detach-from-window :onMouseMove l))))))])))
                    }
               (key-name data)))))

