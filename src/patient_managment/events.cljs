(ns patient-managment.events
  (:require
   [re-frame.core :refer [reg-event-db after dispatch]]
   [clojure.spec.alpha :as s]
   [patient-managment.db :as db :refer [app-db empty-form]]
   [patient-managment.api :as api]
   ))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-db
 validate-spec
 (fn [_ _]
   app-db))

(reg-event-db
 :set-route
 validate-spec
 (fn [db [_ value]]
   (assoc db :route value)))

(reg-event-db
 :set-form-field
 validate-spec
 (fn [db [_ key value]]
   (assoc-in db [:form key] value)))

(reg-event-db
 :empty-form
 (fn [db _]
   (assoc db :form empty-form)))

(reg-event-db
 :get-remote-patients
 (fn [db _]
   (-> (js/fetch "https://bismi.aidbox.io/fhir/Patient")
       (.then #(.json %))
       (.then (fn [res]
                (let [patients (:entry (js->clj res :keywordize-keys true))]
                  (dispatch [:set-patients patients])
                  )))
       )
   db))


(reg-event-db
 :set-patients
 (fn [db [_ patients]]
   (assoc db
          :patients patients
          :route :patients
          )
   ))

(reg-event-db
 :set-patient-id
 (fn [db [_ patient-id]]
   (assoc db :patient-id patient-id)
   ))

(reg-event-db
 :create-remote-patient
 (fn [db [_ data]]
   (def DATA data)
   (def PATIENT (api/create-patient data))
   (-> (js/fetch "https://bismi.aidbox.io/fhir/Patient"
                 (clj->js {:method "POST"
                           :headers {"Content-Type" "application/json"}
                           :body (js/JSON.stringify (clj->js (api/create-patient data)))}))
     (.then #(.json %))
     (.then (fn [res]
              (dispatch [:add-patient (js->clj res :keywordize-keys true)])
              )
            ))
   db))

(reg-event-db
 :add-patient
 (fn [db [_ patient]]
   (-> db
       (update :patients conj {:resource patient})
       (assoc :route :patients))
   ))
