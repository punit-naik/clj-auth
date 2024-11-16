(ns clj-auth.backend.auth
  (:require [buddy.sign.jwt :as jwt]
            [buddy.core.nonce :as nonce]
            [buddy.hashers :as hashers]))

(def secret (nonce/random-bytes 32))

;; In-memory user store (replace with database in production)
(def users (atom {}))

(defn register! [{:keys [username password] :as _user-data}]
  (if (get @users username)
    {:error "Username already taken"}
    (do
      (swap! users assoc username {:username username
                                 :password (hashers/derive password)})
      {:success true
       :username username})))

(defn create-auth-token [claims]
  (jwt/encrypt claims secret {:alg :a256kw :enc :a128gcm}))

(defn authenticate [{:keys [username password]}]
  (when-let [user (get @users username)]
    (when (hashers/check password (:password user))
      (create-auth-token {:username username}))))

(defn verify-token [token]
  (try
    (jwt/decrypt token secret {:alg :a256kw :enc :a128gcm})
    (catch Exception _
      nil)))
