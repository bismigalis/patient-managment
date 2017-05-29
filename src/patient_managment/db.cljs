(ns patient-managment.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::greeting string?)
(s/def ::app-db
  (s/keys :req-un [::route]))

(def empty-form {:name "" :phone "" :gender "" :birthday nil})

;; initial state of app-db
(def app-db
  {:route :index
   :patient-id nil ;;"bd2c15a6-fc57-4c5b-9abe-5cfb426368e2"

   :form empty-form

   :patients [#_{:resource {:id "bd2c15a6-fc57-4c5b-9abe-5cfb426368e2",
                          :meta  {:extension  [{:url "fhir-request-method", :valueString "POST"}
                                               {:url "fhir-request-uri", :valueUri "Patient"}],
                                  :versionId "0743a8b2-b14c-4bf4-ab16-c3d608dad81c",
                                  :lastUpdated "2017-05-26T15:19:56.526Z"},
                          :name  [{:use "official", :given  ["Ruslan"]}],
                          :active true,
                          :gender "male",
                          :telecom  [{:use "mobile", :rank 1, :value "+79787859975", :system "phone"}],
                          :birthDate "1979-04-23",
                          :resourceType "Patient"}}]
   })
