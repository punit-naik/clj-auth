(ns clj-auth.frontend.core
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [clj-auth.frontend.events :as events]
            [clj-auth.frontend.views :as views]
            [day8.re-frame.http-fx]))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))

(defn ^:export init []
  (rf/dispatch-sync [::events/initialize-db])
  (mount-root))
