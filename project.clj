(defproject om-components "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.cemerick/piggieback "0.1.3"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 ]

  :profiles { :dev {:dependencies [[devcards "0.1.1-SNAPSHOT"]]
                    :plugins [[lein-cljsbuild "1.0.3"]
                              [lein-figwheel "0.1.3-SNAPSHOT"]] }}

  :source-paths ["src"]

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :cljsbuild {
              :builds [{:id "devcards"
                        :source-paths ["devcards_src" "src"]
                        :compiler {
                                   :output-to "resources/public/devcards/js/compiled/om_components_devcards.js"
                                   :output-dir "resources/public/devcards/js/compiled/out"
                                   :optimizations :none
                                   :source-map true}}

                       #_{:id "app"
                          :source-paths ["src"]
                          :compiler {
                                     :output-to "resources/public/js/compiled/om_components.js"
                                     :output-dir "resources/public/js/compiled/out"
                                     :optimizations :none
                                     :source-map true}}]}

  :figwheel { :css-dirs ["resources/public/css"] })
