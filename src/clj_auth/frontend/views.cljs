(ns clj-auth.frontend.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as rf]
   [clj-auth.frontend.events :as events]))

(defn loading-spinner []
  [:div.flex.justify-center.items-center.py-4
   [:div.animate-spin.rounded-full.h-8.w-8.border-b-2.border-blue-500]])

(defn dashboard []
  (let [protected-data (rf/subscribe [::events/protected-data])
        loading (rf/subscribe [::events/loading])]
    [:div.min-h-screen.bg-gray-100.py-6.flex.flex-col.justify-center.sm:py-12
     [:div.relative.py-3.sm:max-w-xl.sm:mx-auto
      [:div.relative.px-4.py-10.bg-white.shadow-lg.sm:rounded-3xl.sm:p-20
       [:div.max-w-md.mx-auto
        [:div.divide-y.divide-gray-200
         [:div.py-8.text-base.leading-6.space-y-4.text-gray-700.sm:text-lg.sm:leading-7
          [:h2.text-3xl.font-bold.mb-8 "Dashboard"]
          
          (cond
            @loading
            [loading-spinner]
            
            @protected-data
            [:div
             [:div.bg-green-50.p-4.rounded-lg.mb-4
              [:h3.font-semibold.text-green-800 "Authentication Successful!"]
              [:p.mt-2.text-green-700 "You have successfully logged in and accessed protected data."]]
             
             [:div.bg-gray-50.p-4.rounded-lg.mb-4
              [:h3.font-semibold "Protected Data:"]
              [:div.mt-2.text-sm.bg-gray-100.p-4.rounded.overflow-auto
               [:div.mb-2
                [:span.font-semibold "Message: "]
                [:span (get-in @protected-data [:message])]]
               [:div
                [:span.font-semibold "User Info: "]
                [:pre.mt-1 (str (get-in @protected-data [:user]))]]]]
             
             [:div.mt-8
              [:button.w-full.flex.justify-center.py-2.px-4.border.border-transparent.rounded-md.shadow-sm.text-sm.font-medium.text-white.bg-red-600.hover:bg-red-700.focus:outline-none.focus:ring-2.focus:ring-offset-2.focus:ring-red-500
               {:on-click #(rf/dispatch [::events/logout])}
               "Logout"]]]
            
            :else
            [:div.bg-yellow-50.p-4.rounded-lg
             [:h3.font-semibold.text-yellow-800 "Not Authenticated"]
             [:p.mt-2.text-yellow-700 "Please log in to access protected data."]])]]]]]]))

(defn login-form []
  (let [form-data (r/atom {:username "" :password ""})
        error (rf/subscribe [::events/error])
        loading (rf/subscribe [::events/loading])]
    (fn []
      [:div.min-h-screen.bg-gray-100.flex.flex-col.justify-center.py-12.sm:px-6.lg:px-8
       [:div.sm:mx-auto.sm:w-full.sm:max-w-md
        [:h2.mt-6.text-center.text-3xl.font-extrabold.text-gray-900
         "Sign in to your account"]]
       
       [:div.mt-8.sm:mx-auto.sm:w-full.sm:max-w-md
        [:div.bg-white.py-8.px-4.shadow.sm:rounded-lg.sm:px-10
         
         (when @error
           [:div.rounded-md.bg-red-50.p-4.mb-4
            [:div.text-sm.text-red-700
             @error]])
         
         [:form.space-y-6 {:on-submit (fn [e]
                                       (.preventDefault e)
                                       (rf/dispatch [::events/login @form-data]))}
          [:div
           [:label.block.text-sm.font-medium.text-gray-700 {:for "username"}
            "Username"]
           [:div.mt-1
            [:input#username.appearance-none.block.w-full.px-3.py-2.border.border-gray-300.rounded-md.shadow-sm.placeholder-gray-400.focus:outline-none.focus:ring-blue-500.focus:border-blue-500.sm:text-sm
             {:type "text"
              :required true
              :value (:username @form-data)
              :on-change #(swap! form-data assoc :username (.. % -target -value))}]]]
          
          [:div
           [:label.block.text-sm.font-medium.text-gray-700 {:for "password"}
            "Password"]
           [:div.mt-1
            [:input#password.appearance-none.block.w-full.px-3.py-2.border.border-gray-300.rounded-md.shadow-sm.placeholder-gray-400.focus:outline-none.focus:ring-blue-500.focus:border-blue-500.sm:text-sm
             {:type "password"
              :required true
              :value (:password @form-data)
              :on-change #(swap! form-data assoc :password (.. % -target -value))}]]]
          
          [:div
           [:button.w-full.flex.justify-center.py-2.px-4.border.border-transparent.rounded-md.shadow-sm.text-sm.font-medium.text-white.bg-blue-600.hover:bg-blue-700.focus:outline-none.focus:ring-2.focus:ring-offset-2.focus:ring-blue-500
            {:type "submit"
             :disabled @loading}
            (if @loading
              [loading-spinner]
              "Sign in")]]]]]])))

(defn main-panel []
  (let [auth-token (rf/subscribe [::events/auth-token])]
    (if @auth-token
      [dashboard]
      [login-form])))
