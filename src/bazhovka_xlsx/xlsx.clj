(ns bazhovka-xlsx.xlsx
  (:require [dk.ative.docjure.spreadsheet :as x]))

(defn default [v] #(if (nil? %) v %))

(def MEMBERS-SHEET-NAME "Список жителей")
(def MEMBERS-SHEET-META {:column-map {"ID" :id, "ФИО", :name, "адрес" :address, "email" :email}
                         :column-coercers {:id int}})

(def PAYMENTS-SHEET-META {:column-map {"ID" :member-id
                                       "ФИО" :member-name
                                       "расчётный месяц" :monthnum
                                       "вход. остаток" :incoming
                                       "начислено" :accrued
                                       "оплачено" :paid
                                       "исх. остаток" :outgoing
                                       "дата начисления" :accrual-date
                                       "дата оплаты" :payment-date}
                          :column-coercers {:member-id int, :monthnum int}})

(def PAYMENT-TYPES-SHEET-NAME "Список взносов")
(def PAYMENT-TYPES-SHEET-META {:column-map {"Название" :name, "Сумма" :sum}
                               :column-coercers {:sum (default 0.0)}})

(defn- translate-column-names [column-names column-map]
  (map (fn [column-name] (get column-map column-name column-name)) column-names))

(defn- coerce-column-value [column-name value column-coercers]
  (if-let [coerce-fn (get column-coercers column-name)]
    (coerce-fn value)
    value))

(defn- coerce-row [row-map column-coercers]
  (reduce (fn [acc [column-name value]]
            (assoc acc column-name (coerce-column-value column-name value column-coercers)))
          {} row-map))

(defn- pad-col [coll n val]
  (take n (concat coll (repeat val))))

(defn- row-to-map-fn [translated-column-names column-coercers]
  (fn [row] (let [padded-row (pad-col row (count translated-column-names) nil)]
              (-> (zipmap translated-column-names padded-row) (coerce-row column-coercers)))))

(defn- rows-to-map [rows column-names column-map column-coercers]
  (let [translated-column-names (translate-column-names column-names column-map)]
    (map (row-to-map-fn translated-column-names column-coercers) rows)))

(defn- read-rows [sheet-meta [column-names-row & rest-rows]]
  (rows-to-map rest-rows column-names-row (:column-map sheet-meta) (:column-coercers sheet-meta)))

(defn- load-table [wb sheet-name sheet-meta]
  (->> wb
       (x/select-sheet sheet-name)
       x/row-seq
       (map x/cell-seq)
       (map #(map x/read-cell %))
       (read-rows sheet-meta)))

(defn- ordered-row-values [row ordered-keys]
  (map #(get row %) ordered-keys))

(defn- add-rows [wb sheet-name sheet-meta rows]
  (let [column-names (->> wb (x/select-sheet sheet-name) x/row-seq first (map #(x/read-cell %)))
        column-map (:column-map sheet-meta)
        ordered-row-keys (map (fn [column-name] (get column-map column-name column-name)) column-names)
        ordered-values-of-rows (map (fn [row] (ordered-row-values row ordered-row-keys)) rows)]
    (x/add-rows! (x/select-sheet sheet-name wb) ordered-values-of-rows)))

(defn open-workbook [filename] (x/load-workbook filename))
(defn save-workbook [wb filename] (x/save-workbook! filename wb))

(defn load-members [wb] (load-table wb MEMBERS-SHEET-NAME MEMBERS-SHEET-META))
(defn load-payments [wb payment-type] (load-table wb (:name payment-type) PAYMENTS-SHEET-META))
(defn load-payment-types [wb] (load-table wb PAYMENT-TYPES-SHEET-NAME PAYMENT-TYPES-SHEET-META))

(defn write-payments [wb payment-type payments]
  (add-rows wb (:name payment-type) PAYMENTS-SHEET-META payments))
