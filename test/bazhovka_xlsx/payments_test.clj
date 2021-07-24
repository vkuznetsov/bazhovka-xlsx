(ns bazhovka-xlsx.payments-test
  (:require [clojure.test :refer [deftest testing is]]
            [bazhovka-xlsx.payments :as p]))

(def payment-type1 {:name "pt1", :sum 5})
(def payment-type2 {:name "pt2", :sum 10})

(def members [{:id "m1", :name "member1", (:name payment-type1) "+", (:name payment-type2) ""}
              {:id "m2", :name "member2", (:name payment-type1) "+", (:name payment-type2) "+"}
              {:id "m3", :name "member3", (:name payment-type1) nil, (:name payment-type2) "+"}
              {:id "m4", :name "member4", (:name payment-type1) nil, (:name payment-type2) "-"}])

(def payments [{:member-id "m1", :name "member1" :monthnum 202104, :incoming 0, :accrued 5, :paid 5, :outgoing 0}
               {:member-id "m2", :name "member2" :monthnum 202104, :incoming 0, :accrued 5, :paid 5, :outgoing 0}
               {:member-id "m3", :name "member3" :monthnum 202104, :incoming 0, :accrued 5, :paid 5, :outgoing 0}
               {:member-id "m1", :name "member1" :monthnum 202105, :incoming 0, :accrued 5, :paid 10, :outgoing 5}
               {:member-id "m2", :name "member2" :monthnum 202105, :incoming 0, :accrued 5, :paid 5, :outgoing 0}
               {:member-id "m3", :name "member3" :monthnum 202105, :incoming 0, :accrued 5, :paid 0, :outgoing -5}])

(defn- assert-payment [payment {:keys [member-id member-name monthnum incoming accrued paid outgoing]}]
  (is (= member-id (:member-id payment)))
  (is (= member-name (:name payment)))
  (is (= monthnum (:monthnum payment)))
  (is (= incoming (:incoming payment)))
  (is (= accrued (:accrued payment)))
  (is (= paid (:paid payment)))
  (is (= outgoing (:outgoing payment))))

(deftest new-payments-for-payment-type1
  (let [new-payments (p/new-payments 202106 payment-type1 payments members)
        [p1, p2] new-payments]
    (testing "returns two payments" (is (= 2 (count new-payments))))
    (testing "payment for member1"
      (assert-payment p1 {:member-id "m1", :member-name "member1", :monthnum 202106, :incoming 5, :accrued 5, :paid 0, :outgoing 0}))
    (testing "payment for member2"
      (assert-payment p2 {:member-id "m2", :member-name "member2", :monthnum 202106, :incoming 0, :accrued 5, :paid 0, :outgoing -5}))))

(deftest new-payments-for-payment-type2
  (let [new-payments (p/new-payments 202106 payment-type2 payments members)
        [p1, p2] new-payments]
    (testing "returns two payments" (is (= 2 (count new-payments))))
    (testing "payment for member2"
      (assert-payment p1 {:member-id "m2", :member-name "member2", :monthnum 202106, :incoming 0, :accrued 10, :paid 0, :outgoing -10}))
    (testing "payment for member3"
      (assert-payment p2 {:member-id "m3", :member-name "member3", :monthnum 202106, :incoming -5, :accrued 10, :paid 0, :outgoing -15}))))


