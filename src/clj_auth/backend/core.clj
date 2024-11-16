(ns clj-auth.backend.core
  (:require [clj-auth.backend.handler :as handler]
            [org.httpkit.server :as server])
  (:gen-class))

(defonce server (atom nil))

(defn stop-server []
  (when-let [s @server]
    (s :timeout 100)
    (reset! server nil)
    (println "Server stopped")))

(defn start-server []
  (let [port 3000]
    (println "Starting server on port" port)
    (reset! server (server/run-server #'handler/app {:port port}))
    (println "Server started successfully on port" port)))

(defn -main [& args]
  (start-server))
