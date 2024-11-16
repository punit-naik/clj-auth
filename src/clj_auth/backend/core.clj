(ns clj-auth.backend.core
  (:require [clj-auth.backend.handler :as handler]
            [org.httpkit.server :as server]
            [clj-auth.backend.db :as db])
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

(defn init-db []
  (println "Initializing database...")
  (db/create-users-table!)
  (println "Database initialized successfully"))

(defn -main [& args]
  (init-db)
  (start-server))
