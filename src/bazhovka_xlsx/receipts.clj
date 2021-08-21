(ns bazhovka-xlsx.receipts
  (:require [clj-pdf.core :as p]
            [bazhovka-xlsx.month :as m]))

(defn- amount-to-paid [incoming accrued]
  (let [sum (- incoming accrued)]
    (if (neg? sum) (- sum) 0)))

(defn- accrued-str [{:keys [accrued]}] (str "Начислено: " accrued "р."))
(defn- total-str [{:keys [incoming accrued]}] (str "К оплате: " (amount-to-paid incoming accrued) "р."))
(defn- payer-str [{:keys [member-name]}] (str "Ф.И.О.: " member-name))
(defn- payment-type-str [payment-type] (str "Назначение платежа: " (:name payment-type)))
(defn- month-str [{:keys [monthnum]}] (str "Расчётный месяц: " (m/to-string monthnum)))
(defn- balance-str [{:keys [incoming]}] (if (< incoming 0)
                                          (str "Задолженность на начало месяца: " (- incoming) "р.")
                                          (str "Остаток на начало месяца: " incoming "р.")))

(defn- recipient-str [settings] (:recipient settings))
(defn- inn-str [settings] (str "ИНН: " (:inn settings)))
(defn- account-no-str [settings] (str "№ счёта: " (:account-no settings)))
(defn- bank-name-str [settings] (str "Банк: " (:bank-name settings)))
(defn- bik-str [settings] (str "БИК: " (:bik settings)))
(defn- corr-no-str [settings] (str "кор./сч. банка: " (:corr-no settings)))

(defn- note1 [settings] (:note1 settings))
(defn- note2 [settings] (:note2 settings))
(defn- note3 [settings] (:note3 settings))

(defn- right-part [payment-type payment settings]
  [:paragraph {:leading 12}
   [:chunk {:style :bold} (recipient-str settings)] [:line {:dotted false}] [:spacer]
   (inn-str settings) "; " (account-no-str settings) [:spacer]
   (bank-name-str settings) "; " (bik-str settings) "; " (corr-no-str settings) [:spacer]
   (payment-type-str payment-type) "; " (month-str payment) [:spacer]
   (payer-str payment) [:spacer]
   (balance-str payment) "; " (accrued-str payment) "; " [:spacer]
   [:chunk {:style :bold} (total-str payment)] [:spacer]])

(defn- left-part [part-title settings]
  [:paragraph
   part-title [:spacer]
   (note1 settings) [:spacer]
   (note2 settings) [:spacer]
   (note3 settings)])

(defn pdf-file-name [{:keys [member-name member-id monthnum]}]
  (str member-id " " member-name " " (m/to-string monthnum) ".pdf"))

(defn create-pdf [settings payment-type payment]
  (let [right-part (right-part payment-type payment settings)
        filename (pdf-file-name payment)]
    (p/pdf
     [{:size :a4
       :font {:encoding :unicode :ttf-name "fonts/arialuni.ttf"}}
      [:pdf-table {:bounding-box [50 100]} [15 35]
       [(left-part "Извещение" settings) right-part]
       [(left-part "Квитанция" settings) right-part]]] filename)
    filename))



;; (defn create-receipt [payment])

