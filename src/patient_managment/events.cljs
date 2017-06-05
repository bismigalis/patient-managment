(ns patient-managment.events
  (:require
   [re-frame.core :refer [reg-event-db after dispatch dispatch-sync]]
   [clojure.spec.alpha :as s]
   [patient-managment.db :as db :refer [app-db empty-form]]
   [patient-managment.api :as api]
   [patient-managment.jwt :as jwt]
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
 (fn [_ _]
   app-db))

(reg-event-db
 :set-loading
 (fn [db [_ value]]
   (assoc db :loading value)))

(reg-event-db
 :set-route
 (fn [db [_ value]]
   (assoc db :route value)))

(reg-event-db
 :set-form-field
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
       (.catch #(print "AJAX ERROR:" %))
       )
   db))


(reg-event-db
 :get-access-token
 (fn [db [_ code]]
   (-> (js/fetch "https://bismi.eu.auth0.com/oauth/token"
                   (clj->js {:method "POST"
                             :headers {"Content-Type" "application/json"}
                             :body (js/JSON.stringify (clj->js
                                                       {:grant_type "authorization_code"
                                                        :client_id (get-in db [:auth :client-id])
                                                        :code code
                                                        :code_verifier (get-in db [:auth :code-verifier])
                                                        :redirect_uri (get-in db [:auth :redirect-uri])
                                                        }))}))
         (.then #(.json %))
         (.then (fn [res]
                  (let [token (js->clj res :keywordize-keys true)]
                    ;;(pr "TOKEN:" token)
                    (if (jwt/valid? (:id_token token))
                      (do
                        (dispatch-sync [:set-token token])
                        (dispatch [:get-remote-patients])
                        )
                      (dispatch [:set-route :unauthorized])))
                  ))
         (.catch #(print "ERROR GET ACCES TOKEN: " %))
         )
   (assoc db :loading true)
   )
 )

(reg-event-db
 :set-token
 (fn [db [_ token-data]]
   (assoc db
          :token token-data
          )
   ))

(reg-event-db
 :set-patients
 (fn [db [_ patients]]
   (assoc db
          :patients patients
          :route :patients
          :loading false
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
   (assoc db :loading true)))

(reg-event-db
 :add-patient
 (fn [db [_ patient]]
   (-> db
       (update :patients conj {:resource patient})
       (assoc :route :patients
              :loading false))
   ))
