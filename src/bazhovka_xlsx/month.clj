(ns bazhovka-xlsx.month)

(def MONTH-NAMES
  {1 "Январь"
   2 "Февраль"
   3 "Март"
   4 "Апрель"
   5 "Май"
   6 "Июнь"
   7 "Июль"
   8 "Август"
   9 "Сентябрь"
   10 "Октябрь"
   11 "Ноябрь"
   12 "Декабрь"})

(defn- month [monthnum]
  (rem monthnum 100))

(defn monthname [monthnum]
  (get MONTH-NAMES (month monthnum)))

(defn to-string [monthnum]
  (let [year (int (/ monthnum 100))]
    (str (monthname monthnum) " " year)))