(ns cljchat.views
  (:require
   [stylefy.core :refer [use-style]]
   [cljchat.components.message-container :as message-container]
   [cljchat.components.editor :as editor]))

(def main-style {:display "flex"
                 :flex-flow "column"
                 :height "100vh"})

(defn main-panel []
  [:div (use-style main-style)
   [message-container/render]
   [editor/render]])
