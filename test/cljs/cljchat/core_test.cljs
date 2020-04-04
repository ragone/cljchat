(ns cljchat.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [cljchat.core :as core]))

(deftest fake-test
  (testing "fake description"
    (is (= 1 2))))
