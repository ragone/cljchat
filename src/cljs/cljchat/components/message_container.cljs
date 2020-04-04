(ns cljchat.components.message-container
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [stylefy.core :refer [use-style]]
   ["element-resize-detector" :as elementResizeDetectorMaker]
   [cljchat.components.message :as message]
   [cljchat.time :as time]
   [cljchat.subs :as subs]
   [cljchat.utils :as utils]))

;; Styles

(def message-container-style {:padding "2rem 2rem 0 2rem"
                              :flex "1 1 auto"
                              :overflow-y "auto"})

;; Components

(defn scroll!
  ([el time]
   (scroll! el [0 (.-scrollTop el)] [0 (.-scrollHeight el)] time))
  ([el start end time]
   (.play (goog.fx.dom.Scroll. el (clj->js start) (clj->js end) time))))

(defn scrolled-to-end? [el tolerance]
  ;; at-end?: element.scrollHeight - element.scrollTop === element.clientHeight
  (> tolerance (- (.-scrollHeight el) (.-scrollTop el) (.-clientHeight el))))

(defn autoscroll []
  (let [resize-observer (atom nil)
        scroll-maybe (fn [this]
                       (let [node (reagent/dom-node this)]
                         (when (scrolled-to-end? node 100)
                           (scroll! node 600))))]
    (reagent/create-class
     {:display-name  "autoscroll"
      :component-did-mount (fn [this]
                             (reset! resize-observer (elementResizeDetectorMaker))
                             (.listenTo @resize-observer (reagent/dom-node this) (fn [el]
                                                                                   (scroll-maybe this)))
                             (scroll! (reagent/dom-node this) 0))
      :component-will-unmount (fn [_this]
                                (when @resize-observer
                                  (.uninstall @resize-observer)))
      :component-did-update scroll-maybe
      :reagent-render
      (fn [{:keys [children class]}]
        (into [:div {:class class}] children))})))

(defn render-messages [messages user on-click expanded]
  (let [indicies (range (count messages))
        gap 60]
    (for [index indicies
          :let [previous (when (>= (- index 1) 0) (nth messages (- index 1)))
                current (nth messages index)
                next (when (<= (+ index 1) (last indicies)) (nth messages (+ index 1)))
                time-since-previous (when previous (time/interval (:timestamp previous) (:timestamp current)))
                time-to-next (when next (time/interval (:timestamp current) (:timestamp next)))]]
      [message/render {:key (:id current)
                       :data current
                       :is-mine? (= (:author current) user)
                       :starts-sequence? (not (and previous (= (:author previous) (:author current))
                                                   time-since-previous (> gap (time/in-minutes time-since-previous))))
                       :ends-sequence? (not (and next
                                                 (= (:author next) (:author current))
                                                 time-to-next (> gap (time/in-minutes time-to-next))))
                       :show-timestamp? (not (and time-since-previous (> gap (time/in-minutes time-since-previous))))
                       :on-click #(on-click (:id current))
                       :expanded? (= (:id current) expanded)}])))

(defn render []
  (let [expanded (reagent/atom nil)
        handle-click #(reset! expanded (if (= @expanded %) nil %))
        messages (re-frame/subscribe [::subs/messages])
        me (re-frame/subscribe [::subs/current-user])]
    (fn []
      [autoscroll (use-style message-container-style
                             {:children (render-messages @messages @me handle-click @expanded)})])))
