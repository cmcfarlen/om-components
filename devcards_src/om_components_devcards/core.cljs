(ns om-components-devcards.core
    (:require
     [devcards.core :as dc :include-macros true]
     [om-components.core :as om-components] 
     [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]
     #_[sablono.core :as sab :include-macros true])
    (:require-macros
     [devcards.core :refer [defcard is are= are-not=]]))

(enable-console-print!)

(devcards.core/start-devcard-ui!)
(devcards.core/start-figwheel-reloader!)

;; remember to run lein figwheel and then browse to
;; http://localhost:3449/devcards/index.html

(defcard devcard-intro
  (dc/markdown-card
   "# Devcards for om-components

    I can be found in `devcards_src/om_components_devcards/core.cljs`.

    If you add cards to this file, they will appear here on this page.

    You can add devcards to any file as long as you require
    `devcards.core` like so:

    ```
    (:require [devcards.core :as dc :include-macros true])
    ```

    As you add cards to other namspaces, those namspaces will
    be listed on the Devcards **home** page.

    <a href=\"https://github.com/bhauman/devcards/blob/master/example_src/devdemos/core.cljs\" target=\"_blank\">Here are some Devcard examples</a>"))

(def headings
  [
   {:key :one :label "One" :component om-components/div-display :opts {:className "one"}}
   {:key :two :label "Two" :component om-components/choice-display :opts {:className "two"
                                                                          :values ["ch1" "ch2" "ch3"]
                                                                          :labels ["Chapter 1" "Chapter 2" "Chapter 3"]
                                                                          }}
   {:key :three :label "Three" :component om-components/editable :opts {:on-edit (fn [txt] (println (str "changed to " txt)))}}
   ]
  )

(def data
  [{:one "a" :two "ch1" :three "c"}
   {:one "b" :two "ch2" :three "c"}
   {:one "c" :two "ch3" :three "c"}
   {:one "d" :two "ch1" :three "c"}
   {:one "e" :two "ch2" :three "c"}
   {:one "f" :two "ch3" :three "c"}
   ]
  )

(defcard om-data-table
  (dc/om-root-card om-components/data-table data {:opts {:headings headings}})
  )

(defcard om-editable
  (dc/om-root-card om-components/editable {:text "Edit Me"} {:opts {:key-name :text}})
  )

(def ppercent-atom (atom {:val 100 :val2 50}))

(defcard om-pretty-percent-edit2
  (dc/om-root-card 
    (fn [data owner]
      (om/component
        (dom/div nil
          (om/build om-components/editable data {:opts {:key-name :val}})
          (om/build om-components/pretty-percent data {:opts {:key-name :val :maxval 500}})
          (om/build om-components/smooth-value data {:opts {:key-name :val2 :min-val 0 :max-val 250}})
          (om/build om-components/pretty-percent data {:opts {:key-name :val2 :maxval 250}})))) ppercent-atom))

(defcard om-smooth-value-card
  (dc/om-root-card
    om-components/smooth-value {:val 100} {:opts {:key-name :val :min-val 0 :max-val 500}}))

