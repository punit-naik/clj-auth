(ns clj-auth.backend.handler
  (:require
   [reitit.ring :as ring]
   [reitit.ring.middleware.muuntaja :as muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [reitit.ring.coercion :as coercion]
   [reitit.coercion.spec :as spec-coercion]
   [muuntaja.core :as m]
   [clj-auth.backend.auth :as auth]
   [clj-auth.backend.schema :as schema]
   [ring.util.response :as response]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.not-modified :refer [wrap-not-modified]]
   [ring.middleware.cors :refer [wrap-cors]]
   [clojure.string :as str]))

(defn extract-token [authorization]
  (when authorization
    (let [auth-parts (str/split authorization #" ")]
      (when (= 2 (count auth-parts))
        (when (= "Bearer" (first auth-parts))
          (second auth-parts))))))

(def auth-middleware
  {:name ::auth
   :wrap (fn [handler]
           (fn [request]
             (if-let [token (some-> (get-in request [:headers "authorization"])
                                  extract-token)]
               (if-let [claims (auth/verify-token token)]
                 (handler (assoc request :identity claims))
                 {:status 401
                  :body {:error "Invalid or expired token"}})
               {:status 401
                :body {:error "No authorization token provided"}})))})

(defn index-handler [_]
  (-> (response/resource-response "index.html" {:root "public"})
      (response/content-type "text/html")))

(def static-routes
  [["/" {:get {:handler index-handler
               :no-doc true}}]
   ["/js/*" {:get {:handler (fn [req]
                             (response/resource-response (get-in req [:path-params :*]) {:root "public/js"}))
                   :no-doc true}}]
   ["/css/*" {:get {:handler (fn [req]
                              (response/resource-response (get-in req [:path-params :*]) {:root "public/css"}))
                    :no-doc true}}]])

(def api-routes
  [["/swagger.json"
    {:get {:no-doc true
           :openapi {:openapi "3.0.0"
                    :info {:title "CLJ-Auth API"
                          :description "Authentication API with JWT tokens"
                          :version "1.0.0"}
                    :components {:securitySchemes
                               {:bearer-auth
                                {:type "http"
                                 :scheme "bearer"
                                 :bearerFormat "JWT"}}}}
           :handler (swagger/create-swagger-handler)}}]
   
   ["/register"
    {:post {:summary "Register a new user"
            :parameters {:body ::schema/register-request}
            :responses {201 {:description "User registered successfully"
                           :body ::schema/register-success-response}
                      400 {:description "Invalid request"
                           :body ::schema/error-response}}
            :handler (fn [{:keys [body-params]}]
                      (let [result (auth/register! body-params)]
                        (if (:error result)
                          {:status 400
                           :body result}
                          {:status 201
                           :body result})))}}]
   
   ["/login"
    {:post {:summary "Login with username and password"
            :parameters {:body ::schema/login-request}
            :responses {200 {:description "Login successful"
                           :body ::schema/login-success-response}
                      401 {:description "Invalid credentials"
                           :body ::schema/error-response}}
            :handler (fn [{:keys [body-params]}]
                      (if-let [token (auth/authenticate body-params)]
                        {:status 200
                         :body {:token token}}
                        {:status 401
                         :body {:error "Invalid credentials"}}))}}]
   
   ["/protected"
    {:middleware [auth-middleware]
     :openapi {:security [{:bearer-auth []}]}
     :get {:summary "Access protected resource (requires token)"
           :responses {200 {:description "Protected data"
                          :body ::schema/protected-response}
                     401 {:description "Invalid or missing token"
                          :body ::schema/error-response}}
           :handler (fn [request]
                     {:status 200
                      :body {:message "Protected resource"
                            :user (:identity request)}})}}]])

(def app
  (-> (ring/ring-handler
       (ring/router
        (concat
         static-routes
         [["/api"
           {:middleware [[wrap-cors
                         :access-control-allow-origin [#"http://localhost:8280"]
                         :access-control-allow-methods [:get :post :put :delete]
                         :access-control-allow-headers ["Content-Type" "Authorization"]]]}
           api-routes]])
        {:data {:coercion spec-coercion/coercion
                :muuntaja m/instance
                :middleware [swagger/swagger-feature
                           parameters/parameters-middleware
                           muuntaja/format-negotiate-middleware
                           muuntaja/format-response-middleware
                           muuntaja/format-request-middleware
                           coercion/coerce-response-middleware
                           coercion/coerce-request-middleware]}})
       (ring/routes
        (swagger-ui/create-swagger-ui-handler
         {:path "/api-docs"
          :url "/api/swagger.json"})
        (ring/create-default-handler)))
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)))
