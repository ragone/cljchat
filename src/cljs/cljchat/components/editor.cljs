(ns cljchat.components.editor
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [reagent.dom.server :refer [render-to-string]]
   [stylefy.core :refer [use-style] :as stylefy]
   ["react-quill" :as quill]
   ["quill-mention"]
   [cljchat.subs :as subs]
   [cljchat.events :as events]
   [cljchat.time :as time]
   [cljchat.components.avatar :as avatar]
   [cljchat.utils :as utils]
   [clojure.string :as str]))

;; Styles

(def list-item-style
  {:display "flex"
   :align-items "center"})

(def name-style {:margin "0 0 0 .5rem"})

(def editor-container-style {:background-color "white"
                             :display "flex"
                             :align-items "center"
                             :padding "1rem"})

(def send-button-icon-style {:height "32px"
                             :width "32px"})

(defn send-button-style [disabled?]
  (cond-> {:flex "0 0 50px"
           :margin-left "1rem"
           :border "0"
           :background-color "unset"
           :cursor "pointer"
           :align-self "flex-end"}
    disabled? (merge {:cursor "not-allowed"
                      :filter "grayscale(100%)"})));

(def editor-style {:border-radius "5px"
                   :border (str (:gray utils/colors) " 1px solid")
                   :flex "1"})

;; Components

(defn send-button [{:keys [on-click disabled?]}]
  [:button (use-style (send-button-style disabled?)
                      {:on-click on-click
                       :disabled disabled?})
   [:img (use-style send-button-icon-style
                    {:src "images/send.svg"
                     :alt "Send"})]])

(defn is-empty? [value]
  (-> value
      (str/replace #"<(.|\n)*?>" "")
      (.trim)
      (empty?)))

(defn can-submit? [value]
  (is-empty? value))

(defn clear-formatting [node delta]
  (set! delta -ops (clj->js (reduce #(conj %1 {"insert" (%2 "insert")})
                                    []
                                    (js->clj (.-ops delta)))))
  delta)

(defn render []
  (let [users-by-id @(re-frame/subscribe [::subs/users-by-id])
        value (reagent/atom "")
        editor (atom nil)
        handle-submit (fn [_range, _context]
                        (when-not (can-submit? @value)
                          (re-frame/dispatch [::events/add-message {:id 50 :author 1 :message @value :timestamp (time/now)}])
                          (reset! value "")
                          false))]
    (reagent/create-class
     {:display-name "editor"
      :component-did-mount #(.focus @editor)
      :reagent-render
      (fn []
        [:div (use-style editor-container-style)
         [:> quill {:ref #(reset! editor %)
                    :theme "bubble"
                    :modules {:mention {:showDenotationChar false
                                        :source (fn [search-term, render-list, _mention-char]
                                                  (let [matches
                                                        (->> (vals users-by-id)
                                                             (reduce #(conj %1 {:id (:id %2) :value (:name %2)})
                                                                     []))]
                                                    (render-list (clj->js matches) search-term)))
                                        :renderItem (fn [item _search-term]
                                                      (let [user (users-by-id ((js->clj item) "id"))]
                                                        (render-to-string
                                                         [:div (use-style list-item-style)
                                                          [avatar/render (:avatar user) (:name user)]
                                                          [:h5 (use-style name-style) (:name user)]])))}
                              :keyboard {:bindings {:shift-enter {:key 13
                                                                  :shiftKey true
                                                                  :handler handle-submit}}}
                              :clipboard {:matchers [[js/Node.ELEMENT_NODE, clear-formatting]]}}
                    :placeholder "And then.."
                    :value @value
                    :onChange (fn [content _delta _source _editor]
                                (reset! value content))
                    :style editor-style}]
         [send-button {:on-click handle-submit
                       :disabled? (can-submit? @value)}]])})))
