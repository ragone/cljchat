(ns cljchat.time
  (:require
   ["luxon" :as luxon]))

(defn now []
  (-> luxon/DateTime (.local)))

(defn iso->dt [iso]
  (-> luxon/DateTime
      (.fromISO iso)))

(defn dt->str
  ([dt]
   (dt->str dt luxon/DateTime.DATETIME_SHORT))
  ([dt format]
   (-> dt
       (.toLocaleString format))))

(defn interval [dt1 dt2]
  (-> dt2 (.diff dt1)))

(defn in-minutes [dt]
  (-> dt (.as "minutes")))

(def format-same-day "t")

(def format-same-week "ccc t")

(def format-same-year "dd LLL, t")

(def format-default "dd LLL yyyy, t")

(defn dt->rel-str [dt]
  (let [now (now)
        now-start-of-day (.startOf now "day")
        now-start-of-week (.startOf now "week")
        now-start-of-year (.startOf now "year")
        start-of-day (.startOf dt "day")
        start-of-week (.startOf dt "week")
        start-of-year (.startOf dt "year")]
    (cond
      (.equals start-of-day now-start-of-day) (.toFormat dt format-same-day)
      (.equals start-of-week now-start-of-week) (.toFormat dt format-same-week)
      (.equals start-of-year now-start-of-year) (.toFormat dt format-same-year)
      :else (.toFormat dt format-default))))
