(ns patient-managment.subs
  (:require [re-frame.core :refer [reg-sub]]))


(reg-sub
 :get-db
  (fn [db _]
    db))

(reg-sub
  :get-loading
  (fn [db _]
    (:loading db)))

(reg-sub
  :get-current-route
  (fn [db _]
    (:route db)))

(reg-sub
  :get-form-data
  (fn [db _]
    (:form db)))

(reg-sub
  :get-patients
  (fn [db _]
    (:patients db)))

(reg-sub
  :get-current-patient
  (fn [db _]
    (->> (:patients db)
        (filter #(= (:patient-id db)
                    (get-in % [:resource :id])))
        (first))
    ))
