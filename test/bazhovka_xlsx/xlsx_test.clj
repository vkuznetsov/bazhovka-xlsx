(ns bazhovka-xlsx.xlsx-test
  (:require [clojure.test :refer [deftest testing is]]
            [bazhovka-xlsx.xlsx :as x]
            [dk.ative.docjure.spreadsheet :as d])
  (:import [java.util Calendar]
           [java.io ByteArrayInputStream ByteArrayOutputStream]))

(defn- workbook [] (d/load-workbook-from-resource "test.xlsx"))

(deftest read-members
  (let [members (x/load-members (workbook))
        [m1 m2] members]
    (testing "returns two members"
      (is (= 2 (count members))))
    (testing "first member"
      (is (= 33 (:id m1)))
      (is (= "Member1" (:name m1)))
      (is (= "member1_address" (:address m1)))
      (is (= "member1@email.com" (:email m1)))
      (is (= "+" (get m1 "Payments1")))
      (is (nil? (get m1 "Payments2"))))
    (testing "second member"
      (is (= 34 (:id m2)))
      (is (= "Member2" (:name m2)))
      (is (= "member2_address" (:address m2)))
      (is (= "member2@email.com" (:email m2)))
      (is (= "-" (get m2 "Payments1")))
      (is (= "+" (get m2 "Payments2"))))))

(deftest read-payment-types
  (let [payment-types (x/load-payment-types (workbook))
        [pt1 pt2] payment-types]
    (testing "returns two items"
      (is (= 2 (count payment-types))))
    (testing "first item"
      (is (= "Payments1" (:name pt1)))
      (is (= 1500.0 (:sum pt1))))
    (testing "second item"
      (is (= "Payments2" (:name pt2)))
      (is (= 10000.0 (:sum pt2))))))

(declare assert-payment)
(declare date)

(deftest read-payments
  (testing "read Payments1"
    (let [payments (x/load-payments (workbook) {:name "Payments1"})
          [p1 p2 p3] payments]
      (testing "returns three items"
        (is (= 3 (count payments))))
      (testing "first item"
        (assert-payment p1 {:member-id 33
                            :member-name "Member1"
                            :monthnum 202105
                            :incoming 0.0
                            :accrued 1500.0
                            :paid 0.0
                            :outgoing -1500.0
                            :accrual-date (date 2021 5 10)
                            :payment-date nil}))
      (testing "second item"
        (assert-payment p2 {:member-id 34
                            :member-name "Member2"
                            :monthnum 202105
                            :incoming 0.0
                            :accrued 1500.0
                            :paid 1500.0
                            :outgoing 0.0
                            :accrual-date (date 2021 5 10)
                            :payment-date (date 2021 5 20)}))
      (testing "third item"
        (assert-payment p3 {:member-id 33
                            :member-name "Member1"
                            :monthnum 202106
                            :incoming -1500.0
                            :accrued 1500.0
                            :paid 0.0
                            :outgoing -3000.0
                            :accrual-date (date 2021 6 10)
                            :payment-date nil}))))
  (testing "read Payments2"
    (let [payments (x/load-payments (workbook) {:name "Payments2"})
          [p1] payments]
      (testing "returns one item" (is (= 1 (count payments))))
      (testing "first item"
        (assert-payment p1 {:member-id 34
                            :member-name "Member2"
                            :monthnum 202105
                            :incoming 0.0
                            :accrued 10000.0
                            :paid 0.0
                            :outgoing -10000.0
                            :accrual-date (date 2021 5 10)
                            :payment-date (date 2021 5 20)})))))

(deftest write-payments
  (let [payment {:member-id 33
                 :member-name "Member1"
                 :monthnum 202107
                 :incoming -1500.0
                 :accrued 1500.0
                 :paid 0.0
                 :outgoing -3000.0
                 :accrual-date (date 2021 6 10)
                 :payment-date nil}
        payment-type {:name "Payments1"}
        wb (workbook)
        out (new ByteArrayOutputStream)]
    (x/write-payments wb payment-type [payment])
    (d/save-workbook-into-stream! out wb)
    (let [new-payments (-> (new ByteArrayInputStream (.toByteArray out))
                           (d/load-workbook-from-stream)
                           (x/load-payments payment-type))]
      (is (= 4 (count new-payments)) "there are four payments in workbook")
      (is (= payment (last new-payments)) "the last item is a new payment"))))

(defn- assert-payment [payment {:keys [member-id member-name monthnum incoming accrued paid outgoing accrual-date payment-date]}]
  (is (= member-id (:member-id payment)))
  (is (= member-name (:member-name payment)))
  (is (= monthnum (:monthnum payment)))
  (is (= incoming (:incoming payment)))
  (is (= accrued (:accrued payment)))
  (is (= paid (:paid payment)))
  (is (= outgoing (:outgoing payment)))
  (is (= accrual-date (:accrual-date payment)))
  (is (= payment-date (:payment-date payment))))

(defn date [y m d]
  (let [c (Calendar/getInstance)]
    (.set c y (dec m) d 0 0 0)
    (.set c Calendar/MILLISECOND 0)
    (.getTime c)))