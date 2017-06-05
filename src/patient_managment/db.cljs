(ns patient-managment.db
  (:require [clojure.spec.alpha :as s]))

;; spec of app-db
(s/def ::greeting string?)
(s/def ::app-db
  (s/keys :req-un [::route]))

(def empty-form {:name "" :phone "" :gender "" :birthday nil})

;; initial state of app-db
(def app-db
  {:loading false
   :route :index
   :patient-id nil ;;"bd2c15a6-fc57-4c5b-9abe-5cfb426368e2"
   :token nil
   :auth {;;:grant_type     "authorization_code"
          :client-id      "5beus1JLVqz6N4aZ6AxVwZlQMpuL53cf"
          :code-verifier  "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE"
          :code-challenge "huVe5Sif9SgWtYkAgGw7CvEPQ6NI0AdBuSVp1DNWPLI"
          :redirect-uri   "http://bismi.ru/callback"

          }
   :form empty-form
   :patients []
   })
