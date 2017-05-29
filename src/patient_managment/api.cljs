(ns patient-managment.api
  ;;(:require [])
  )

(defn create-patient [data]
  {:name [{:use "official",
           :given [(:name data)]}]

   :birthDate (:birthday data)
   :resourceType "Patient"
   :active true
   :telecom [{:use "mobile",
              :rank 1,
              :value (:phone data)
              :system "phone"}
             ]
   :gender (:gender data)
   }
  )
