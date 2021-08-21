(ns bazhovka-xlsx.month-test
  (:require [clojure.test :refer [deftest testing is]]
            [bazhovka-xlsx.month :as m]))

(deftest to-string-test
  (testing "returns valid string representation of monthnum"
    (is (= "Январь 2021" (m/to-string 202101)))
    (is (= "Декабрь 2020" (m/to-string 202012)))))