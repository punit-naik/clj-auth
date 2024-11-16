(ns clj-auth.backend.db
  (:require
   [next.jdbc :as jdbc]
   [clj-auth.backend.db.utils :as db.utils]))

(def db-spec
  {:dbtype "postgresql"
   :dbname "clj_auth"
   :host "localhost"
   :port 5438
   :user "clj_auth"
   :password "clj_auth_pass"})

(def datasource (jdbc/get-datasource db-spec))

(defn create-users-table!
  []
  (db.utils/execute!
   datasource
   {:create-table [:users :if-not-exists]
    :with-columns
    [[:id :serial [:primary-key]]
     [:username [:varchar 255] [:not nil] [:unique]]
     [:password [:varchar 255] [:not nil]]
     [:created_at :timestamp [:default [:raw "CURRENT_TIMESTAMP"]]]]}))

(defn get-user-by-username
  [username]
  (db.utils/query-one
   datasource
   {:select [:*]
    :from [:users]
    :where [:= :username username]}))

(defn create-user!
  [username password]
  (try
    (db.utils/insert!
     datasource
     :users
     {:username username
      :password password})
    (catch Exception e
      (if (= "23505" (.getSQLState e))
        {:error "Username already exists"}
        {:error (.getMessage e)}))))
