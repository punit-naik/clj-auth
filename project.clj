(defproject clj-auth "0.0.1"
  :description "Sample full-stack web SPA (clojure/clojurescript) project which implements auth using jwt"
  :url "https://github.com/punit-naik/clj-auth"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/clojurescript "1.11.60" :scope "provided"]

                 ;; Backend
                 [http-kit/http-kit "2.7.0"]
                 [metosin/reitit "0.6.0"]
                 [metosin/reitit-ring "0.6.0"]
                 [metosin/reitit-swagger "0.6.0"]
                 [metosin/reitit-swagger-ui "0.6.0"]
                 [metosin/reitit-spec "0.6.0"]
                 [metosin/muuntaja "0.6.8"]
                 [ring/ring-core "1.10.0"]
                 [buddy/buddy-auth "3.0.323"]
                 [buddy/buddy-hashers "2.0.167"]
                 [buddy/buddy-sign "3.5.351"]
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [com.github.seancorfield/honeysql "2.5.1103"]
                 [org.postgresql/postgresql "42.6.0"]
                 [ring-cors/ring-cors "0.1.13"]

                 ;; Frontend
                 [reagent "1.2.0"]
                 [re-frame "1.3.0"]
                 [day8.re-frame/http-fx "0.2.4"]
                 [cljs-ajax "0.8.4"]
                 [metosin/reitit-frontend "0.6.0"]]

  :source-paths ["src"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s"

  :clean-targets ^{:protect false} ["resources/public/js"
                                    "target"
                                    "node_modules"
                                    "package.json"
                                    "package-lock.json"
                                    "figwheel_server.log"]

  :plugins [[lein-cljsbuild "1.1.8"]
            [lein-figwheel "0.5.20"]]

  :main clj-auth.backend.core

  :figwheel {:http-server-root "public"
             :server-port 8280
             :server-ip "127.0.0.1"
             :css-dirs ["resources/public/css"]
             :ring-handler clj-auth.backend.handler/app
             :nrepl-port 7002}

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel {:on-jsload "clj-auth.frontend.core/mount-root"}
                :compiler {:main clj-auth.frontend.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/app.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map true
                           :optimizations :none
                           :npm-deps {:react "17.0.2"
                                      :react-dom "17.0.2"}
                           :install-deps true
                           :infer-externs true
                           :language-in :ecmascript-next
                           :language-out :ecmascript-next
                           :pretty-print true}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/app.js"
                           :main clj-auth.frontend.core
                           :optimizations :advanced
                           :npm-deps {:react "17.0.2"
                                      :react-dom "17.0.2"}
                           :install-deps true
                           :infer-externs true
                           :language-in :ecmascript-next
                           :language-out :ecmascript-next
                           :pretty-print false}}]}

  :profiles {:dev {:dependencies [[binaryage/devtools "1.0.7"]
                                [figwheel-sidecar "0.5.20"]]}
             :uberjar {:aot :all
                       :prep-tasks ["compile" 
                                    ["cljsbuild" "once" "min"]]
                       :omit-source true
                       :main clj-auth.backend.core
                       :uberjar-name "clj-auth.jar"}})
