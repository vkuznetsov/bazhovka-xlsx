(ns bazhovka-xlsx.core
  (:require [bazhovka-xlsx.xlsx :as x]
            [bazhovka-xlsx.payments :as p])
  (:gen-class))

(defn -main
  "I don't do a whole lot."
  [& _args]
  (println "Hello, World!"))

(defn create-new-payments [wb monthnum payment-type members]
  (let [payments (x/load-payments wb payment-type)
        new-payments (p/new-payments monthnum payment-type payments members)]
    (x/write-payments wb payment-type new-payments)))

(defn create-new-payments-for-all-types [wb monthnum]
  (let [payment-types (x/load-payment-types wb)
        actual-payment-types (filter #(> (:sum %) 0) payment-types)
        members (x/load-members wb)]
    (doseq [payment-type actual-payment-types] (create-new-payments wb monthnum payment-type members))))

