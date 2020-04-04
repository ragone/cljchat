(ns cljchat.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(re-frame/reg-sub
 ::re-pressed-example
 (fn [db _]
   (:re-pressed-example db)))

(re-frame/reg-sub
 ::messages
 (fn [db]
   (:messages db)))

(re-frame/reg-sub
 ::current-user
 (fn [db]
   (:current-user db)))

(re-frame/reg-sub
 ::user
 (fn [db [_ id]]
   ((get-in db [:users :by-id]) id)))

(re-frame/reg-sub
 ::users-by-id
 (fn [db]
   (get-in db [:users :by-id])))
