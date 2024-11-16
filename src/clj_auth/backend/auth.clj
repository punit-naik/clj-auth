(ns clj-auth.backend.auth
  (:require [buddy.sign.jwt :as jwt]
            [buddy.core.nonce :as nonce]
            [buddy.hashers :as hashers]
            [clj-auth.backend.db :as db]))

(def secret (nonce/random-bytes 32))

(defn register! [{:keys [username password] :as _user-data}]
  (let [hashed-password (hashers/derive password)
        result (db/create-user! username hashed-password)]
    (if (:error result)
      result
      {:success true
       :username username})))

(defn create-auth-token [claims]
  (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm}))

(defn authenticate [{:keys [username password]}]
  (when-let [user (db/get-user-by-username username)]
    (when (hashers/check password (:users/password user))
      (create-auth-token {:username username}))))

(defn verify-token [token]
  (try
    (jwt/decrypt token secret {:alg :a256kw :enc :a128gcm})
    (catch Exception _
      nil)))
