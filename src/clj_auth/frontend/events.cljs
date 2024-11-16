(ns clj-auth.frontend.events
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]))

(def api-url "/api")

;; Local Storage helpers
(defn set-token! [token]
  (.setItem js/localStorage "auth-token" token))

(defn get-token []
  (.getItem js/localStorage "auth-token"))

(defn remove-token! []
  (.removeItem js/localStorage "auth-token"))

;; Initialize app db
(rf/reg-event-fx
 ::initialize-db
 (fn [_ _]
   (let [token (get-token)]
     {:db {:auth-token token
           :user nil
           :protected-data nil
           :loading (boolean token)}
      :dispatch-n [(when token [::validate-token])]})))

;; Token validation
(rf/reg-event-fx
 ::validate-token
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str api-url "/protected")
                 :headers         {"Authorization" (str "Bearer " (:auth-token db))}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::validate-token-success]
                 :on-failure      [::validate-token-failure]}}))

(rf/reg-event-fx
 ::validate-token-success
 (fn [{:keys [db]} [_ response]]
   {:db (-> db
            (assoc :protected-data response)
            (assoc :loading false))}))

(rf/reg-event-fx
 ::validate-token-failure
 (fn [{:keys [db]} [_ response]]
   (remove-token!)
   {:db (-> db
            (dissoc :auth-token :protected-data)
            (assoc :loading false)
            (assoc :error (if (= 401 (:status response))
                           "Session expired. Please login again."
                           "Failed to validate session.")))}))

;; Subscriptions
(rf/reg-sub
 ::auth-token
 (fn [db]
   (:auth-token db)))

(rf/reg-sub
 ::error
 (fn [db]
   (:error db)))

(rf/reg-sub
 ::protected-data
 (fn [db]
   (:protected-data db)))

(rf/reg-sub
 ::loading
 (fn [db]
   (:loading db)))

;; Login events
(rf/reg-event-fx
 ::login
 (fn [{:keys [db]} [_ credentials]]
   {:db (-> db
            (assoc :loading true)
            (dissoc :error))
    :http-xhrio {:method          :post
                 :uri             (str api-url "/login")
                 :params          credentials
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::login-success]
                 :on-failure      [::login-failure]}}))

(rf/reg-event-fx
 ::login-success
 (fn [{:keys [db]} [_ response]]
   (let [token (:token response)]
     (set-token! token)
     {:db (-> db
              (assoc :auth-token token)
              (dissoc :error)
              (assoc :loading false))
      :dispatch [::fetch-protected-data]})))

(rf/reg-event-fx
 ::login-failure
 (fn [{:keys [db]} [_ response]]
   (remove-token!)
   {:db (-> db
            (assoc :error (or (get-in response [:response :error]) "Login failed"))
            (assoc :loading false))}))

;; Protected data events
(rf/reg-event-fx
 ::fetch-protected-data
 (fn [{:keys [db]} _]
   {:http-xhrio {:method          :get
                 :uri             (str api-url "/protected")
                 :headers         {"Authorization" (str "Bearer " (:auth-token db))}
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::fetch-protected-success]
                 :on-failure      [::fetch-protected-failure]}}))

(rf/reg-event-db
 ::fetch-protected-success
 (fn [db [_ response]]
   (assoc db :protected-data response)))

(rf/reg-event-fx
 ::fetch-protected-failure
 (fn [{:keys [db]} [_ response]]
   (if (= 401 (:status response))
     (do
       (remove-token!)
       {:db (-> db
                (dissoc :auth-token :protected-data)
                (assoc :error "Session expired. Please login again."))})
     {:db (assoc db :error "Failed to fetch protected data")})))

;; Logout event
(rf/reg-event-fx
 ::logout
 (fn [{:keys [db]} _]
   (remove-token!)
   {:db {:auth-token nil
         :user nil
         :protected-data nil}}))
