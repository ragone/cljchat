(ns cljchat.components.avatar
  (:require
   [stylefy.core :refer [use-style]]))

;; Styles

(def avatar-style {:height "1rem"
                   :width "1rem"
                   :border-radius "50%"})

;; Components

(defn render [src author]
  [:img (use-style avatar-style
                   {:src src
                    :alt author})])
