(ns clj-auth.backend.schema
  (:require [clojure.spec.alpha :as s]))

;; Request Specs
(s/def :user/username string?)
(s/def :user/password string?)

(s/def ::register-request
  (s/keys :req-un [:user/username :user/password]))

(s/def ::login-request
  (s/keys :req-un [:user/username :user/password]))

;; Response Specs
(s/def ::error string?)
(s/def ::success boolean?)
(s/def ::token string?)
(s/def ::message string?)

(s/def ::error-response
  (s/keys :req-un [::error]))

(s/def ::register-success-response
  (s/keys :req-un [::success :user/username]))

(s/def ::login-success-response
  (s/keys :req-un [::token]))

(s/def ::protected-response
  (s/keys :req-un [::message]))

;; Swagger Schemas
(def swagger-specs
  {:components
   {:schemas
    {:LoginRequest
     {:type "object"
      :required ["username" "password"]
      :properties
      {:username {:type "string" :description "User's username"}
       :password {:type "string" :format "password" :description "User's password"}}}
     
     :RegisterRequest
     {:type "object"
      :required ["username" "password"]
      :properties
      {:username {:type "string" :description "Desired username"}
       :password {:type "string" :format "password" :description "Desired password"}}}
     
     :LoginResponse
     {:type "object"
      :required ["token"]
      :properties
      {:token {:type "string" :description "JWT token for authentication"}}}

     :RegisterResponse
     {:type "object"
      :required ["success" "username"]
      :properties
      {:success {:type "boolean" :description "Registration success status"}
       :username {:type "string" :description "Registered username"}}}
     
     :ErrorResponse
     {:type "object"
      :required ["error"]
      :properties
      {:error {:type "string" :description "Error message"}}}
     
     :ProtectedResponse
     {:type "object"
      :required ["message"]
      :properties
      {:message {:type "string" :description "Protected resource message"}}}}}})
