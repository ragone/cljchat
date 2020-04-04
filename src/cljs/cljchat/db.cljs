(ns cljchat.db
  (:require
   [talltale.core :as t]
   [cljchat.time :as time]))

(def date (time/now))

(def default-db
  {:name "re-frame"
   :current-user 1
   :users {:by-id {1 {:id 1 :name "Alex" :avatar "http://i.pravatar.cc/600?img=17"}
                   2 {:id 2 :name "John" :avatar "http://i.pravatar.cc/600?img=17"}}}
   :messages [{:id 1 :author 1 :message (t/lorem-ipsum) :timestamp (-> date (.minus (clj->js {"days" 110})))}
              {:id 2 :author 1 :message (t/lorem-ipsum) :timestamp (-> date (.minus (clj->js {"days" 105})))}
              {:id 3 :author 1 :message (t/lorem-ipsum) :timestamp (-> date (.minus (clj->js {"days" 2})))}
              {:id 4 :author 2 :message (t/lorem-ipsum) :timestamp (-> date (.minus (clj->js {"days" 1})))}
              {:id 5 :author 1 :message (t/lorem-ipsum) :timestamp (-> date (.minus (clj->js {"days" 1})))}
              {:id 6 :author 2 :message (t/lorem-ipsum) :timestamp (-> date (.minus (clj->js {"days" 0})))}
              {:id 7 :author 2 :message (t/lorem-ipsum) :timestamp (-> date (.minus (clj->js {"days" 0})))}]})
