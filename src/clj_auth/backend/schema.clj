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