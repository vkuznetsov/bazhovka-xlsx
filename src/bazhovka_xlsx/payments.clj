(ns bazhovka-xlsx.payments
  (:import [java.util Calendar]))

(defn- last-payments-by-member [payments]
  (reduce (fn [acc payment] (assoc acc (:member-id payment) payment)) {} payments))

(defn- today []
  (let [calendar (Calendar/getInstance)]
    (.set calendar Calendar/HOUR_OF_DAY, 0)
    (.set calendar Calendar/MINUTE, 0)
    (.set calendar Calendar/SECOND, 0)
    (.set calendar Calendar/MILLISECOND, 0)
    (.getTime calendar)))

(defn- dummy-payment [member]
  {:member-id (:id member)
   :name (str (:name member) ": " (:address member))
   :outgoing 0.0})

(defn- new-payment [payment-sum monthnum member last-payment]
  {:member-id (:id member)
   :member-name (:name last-payment)
   :monthnum monthnum
   :incoming (:outgoing last-payment)
   :accrued payment-sum
   :paid 0
   :outgoing (- (:outgoing last-payment) payment-sum)
   :accrual-date (today)})

(defn new-payments [monthnum payment-type payments all-members]
  (let [payment-type-name (:name payment-type)
        payment-sum (:sum payment-type)
        members-for-payment (filter #(= "+" (get % payment-type-name)) all-members)
        last-payments (last-payments-by-member payments)]
    (println {:members all-members, :members-for-payment members-for-payment})
    (map (fn [member]
           (let [last-payment (get last-payments (:id member) (dummy-payment member))]
             (new-payment payment-sum monthnum member last-payment))) members-for-payment)))