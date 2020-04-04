(ns cljchat.components.message
  (:require
   [re-frame.core :as re-frame]
   [stylefy.core :refer [use-style]]
   [cljchat.subs :as subs]
   [cljchat.utils :as utils]
   [cljchat.time :as time]
   [cljchat.components.avatar :as avatar]))

;; Constants

(def message-border-radius "20px")

;; Styles

(defn message-style [is-mine?]
  (cond-> {:display "flex"
           :flex-direction "column"}
    is-mine? (merge {:justify-content "flex-end"
                     :display "flex"})))

(defn timestamp-style [expanded?]
  (cond-> {:display "flex"
           :justify-content "center"
           :color "#999"
           :font-weight "600"
           :font-size "12px"
           :text-transform "uppercase"
           :max-height "0"
           :overflow-y "hidden"
           :transition-property "max-height,margin"
           :transition-duration ".5s"
           :transition-timing-function "cubic-bezier(0.250, 0.460, 0.450, 0.940)"}
    expanded? (merge {:max-height "1rem"
                      :margin ".5rem 0"})))

(defn container-style [is-mine?]
  (cond-> {:font-size "14px"
           :display "flex"}
    is-mine? (merge {:justify-content "flex-end"})))

(def content-style {:max-width "75%"
                    :display "flex"
                    :flex-direction "column"})

(def default-bubble-style {:margin ".2rem .1rem"
                           :background (:black utils/colors)
                           :padding "10px 15px"
                           :color "white"
                           :border-top-left-radius "2px"
                           :border-bottom-left-radius "2px"
                           :border-top-right-radius message-border-radius
                           :border-bottom-right-radius message-border-radius
                           :transition-property "box-shadow,margin"
                           :transition-duration ".5s"
                           :transition-timing-function "cubic-bezier(0.250, 0.460, 0.450, 0.940)"
                           :box-shadow "0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.24)"
                           :word-wrap "break-word"})

(def author-style {:margin "0"
                   :font-weight "200"
                   :flex "1"})

(def side-bar-style {:width "1rem"
                     :display "flex"
                     :flex-direction "column"
                     :justify-content "flex-end"})

(defn bubble-style [is-mine? starts-sequence? ends-sequence? expanded?]
  (cond-> default-bubble-style
    is-mine? (cond->
                 :default (merge {:background (:gray utils/colors)
                                  :color "black"
                                  :border-top-left-radius message-border-radius
                                  :border-bottom-left-radius message-border-radius
                                  :border-top-right-radius ".1rem"
                                  :border-bottom-right-radius ".1rem"
                                  :align-self "flex-end"})
                 starts-sequence? (merge {:border-top-right-radius message-border-radius})
                 ends-sequence? (merge {:margin-bottom "1.2rem"
                                        :border-bottom-right-radius message-border-radius}))
    (not is-mine?) (cond->
                       starts-sequence? (merge {:border-top-left-radius message-border-radius})
                       ends-sequence? (merge {:border-bottom-left-radius message-border-radius}))
    expanded? (merge {:box-shadow "0 10px 20px rgba(0, 0, 0, 0.19), 0 6px 6px rgba(0, 0, 0, 0.23)"
                      :margin-top ".2rem"})))

(defn top-bar-style [show-any?]
  (cond-> {:display "flex"
           :width "100%"
           :max-height "1rem"
           :padding "0 1rem"
           :transition-property "max-height"
           :transition-duration ".5s"
           :transition-timing-function "cubic-bezier(0.250, 0.460, 0.450, 0.940)"}
    (not show-any?) (merge {:max-height "0"})))

(defn right-tool-bar [visible?]
  (cond->
        {:transition-property "opacity"
         :transition-duration ".5s"
         :transition-timing-function "cubic-bezier(0.250, 0.460, 0.450, 0.940)"
         :opacity "0"
         :overflow "hidden"
         :align-content "flex-end"
         :display "flex"}
    visible? (merge {:opacity "100%"})))

(defn left-tool-bar [visible?]
  (cond-> {:opacity "0"
           :flex "1"
           :display "flex"}
    visible? (merge {:opacity "100%"})))

(def stat-style {:display "flex"
                 :align-items "center"
                 :margin-left "1rem"})

(def stat-image-style {:margin-right ".2rem"})

(def status-style {:color "brown"
                   :line-height "1rem"})

;; Components

(defn stat [src low & [high]]
  [:div (use-style stat-style)
   [:img (use-style stat-image-style
                    {:src src :alt "Stat"})]
   (str low (when high (str "/" high)))])

(defn status [type]
  (case type
    :paralyzed
    [:div (use-style status-style) "paralyzed"]))

(defn top-bar [{:keys [show-right? show-left? right left visible?]}]
  [:div (use-style (top-bar-style (or visible? show-left? show-right?)))
   (into [:div (use-style (left-tool-bar show-left?))]
         left)
   (into [:div (use-style (right-tool-bar show-right?))]
         right)])

(defn bubble [is-mine? expanded? starts-sequence? ends-sequence? attr]
  [:div (use-style (bubble-style is-mine? starts-sequence? ends-sequence? expanded?)
                   attr)])

(defn render [{:keys [data is-mine? starts-sequence? ends-sequence? show-timestamp? on-click expanded?]}]
  (let [dt (:timestamp data)
        user @(re-frame/subscribe [::subs/user (:author data)])]
    [:div (use-style (message-style is-mine?))
     [:div (use-style (timestamp-style (or show-timestamp? expanded?))
                      {:title (time/dt->str dt)})
      (time/dt->rel-str (:timestamp data))]
     [:div (use-style (container-style is-mine?))
      [:div (use-style side-bar-style)
       (when (and ends-sequence? (not is-mine?))
         [avatar/render (:avatar user) (:name user)])]
      [:div (use-style content-style)
       [top-bar {:visible? starts-sequence?
                 :show-right? expanded?
                 :show-left? (and starts-sequence? (not is-mine?))
                 :right [[status :paralyzed]
                         [stat "images/heart.svg" 10 10]
                         [stat "images/shield.svg" 15]]
                 :left [[:h5 (use-style author-style) (:name user)]]}]
       [bubble is-mine? expanded? starts-sequence? ends-sequence? {:on-click on-click
                                                                   :dangerouslySetInnerHTML {:__html (:message data)}}]]]]))
