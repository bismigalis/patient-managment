(ns patient-managment.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [patient-managment.events]
            [patient-managment.subs]))

(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def Text (r/adapt-react-class (.-Text ReactNative)))
(def TextInput (r/adapt-react-class (.-TextInput ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
;;(def FlatList (r/adapt-react-class (.-FlatList ReactNative)))
(def ScrollView (r/adapt-react-class (.-ScrollView ReactNative)))
(def ListView (r/adapt-react-class (.-ListView ReactNative)))
(def DataSource (.. ReactNative -ListView -DataSource))

(def ds (DataSource. #js {:rowHasChanged #(not= %1 %2)}))

(def Picker (r/adapt-react-class (.-Picker ReactNative)))
(def PickerItem (r/adapt-react-class (.. ReactNative -Picker -Item)))

(def date-picker (.-DatePickerAndroid ReactNative))

;; (def list-view (r/adapt-react-class (.-ListView js/React)))
;; (def list-item (r/adapt-react-class (js/require "react-native-listitem")))
;; (def text-input (r/adapt-react-class (.-TextInput js/React)))

(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def touchable-without-feedback (r/adapt-react-class (.-TouchableWithoutFeedback ReactNative)))

(def logo-img (js/require "./images/cljs.png"))


(def Auth0Lock (js/require "react-native-lock"))
(def lock (Auth0Lock. #js {:clientId "5beus1JLVqz6N4aZ6AxVwZlQMpuL53cf" :domain "bismi.eu.auth0.com"}))



(defn alert [title]
      (.alert (.-Alert ReactNative) title))


(defn loading []
  [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
   [Text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} "Loading..."]])


(defn Patients []
  (let [data (subscribe [:get-patients])]
    (fn []

      [view {:style {:flex 1 :flex-direction "column" :margin 0 :align-items "flex-start"}}

       [view {:style {:flex-direction "row" :margin 10 :align-items "center" :justify-content :space-between}}
        [Text {:style {:font-size 30 :font-weight "100" :margin-right 20 :text-align "center"}} "Patients"]

       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5} :on-press (fn []
                                                                                                        (dispatch-sync [:empty-form])
                                                                                                        (dispatch [:set-route :add-patient])
                                                                                                        )}
        [Text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Add patient"]]
]

       [ListView {:dataSource (.cloneWithRows ds (clj->js (map #(vector (get-in % [:resource :id])
                                                                        (get-in % [:resource :name 0 :given 0]))
                                                               @data)))
                  :enableEmptySections true
                  :render-row (fn [row] (r/as-element
                                         [touchable-highlight {:style {:padding 10} :on-press (fn []
                                                                                                (dispatch-sync [:set-patient-id (first row)])
                                                                                                (dispatch [:set-route :view-patient])
                                                                                                )}
                                          [Text {:style {:text-align "left"}} (second row)]]))}]
       ]
      )))


(defn get-birthday []
  (.then (date-picker.open #js {:mode "spinner"})
         (fn [x]
           (let [action (.-action x)]
             (if-not (= action (.-dismissedAction date-picker))
               (dispatch [:set-form-field :birthday (str (.-year x) "-" (.-month x) "-" (.-day x))])
             ))))
    )

(defn AddPatient []
  (let [data (subscribe [:get-form-data])
        valid? #(every? (complement empty?) (vals %))
        invalid (r/atom nil)]
    (fn []
      [view {:style {:margin 40}}
       [Text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} "Add patient"]

       (if @invalid
         [Text {:style {:font-size 15 :margin-bottom 20 :text-align "center" :color "red"}} "All fields are required"])

       ;;NAME
       [TextInput {:on-change-text #(dispatch [:set-form-field :name %]) :value (:name @data)
                    :placeholder "Name" :style {} }]

       ;;PHONE
       [TextInput {:on-change-text #(dispatch [:set-form-field :phone %]) :value (:phone @data)
                    :keyboard-type :phone-pad
                    :placeholder "Phone" :style {}}]
       ;;GENDER
       [Picker {:on-value-change #(dispatch [:set-form-field :gender %]) :selected-value (:gender @data)}
        [PickerItem {:label "-- Select Gender --" :value ""}]
        [PickerItem {:label "Male" :value "male"}]
        [PickerItem {:label "Female" :value "female"}]
        ]

       ;;BIRTHDAY
       [touchable-highlight {:style {:padding 10
                                     :border-radius 5}
                             :on-press #(get-birthday)}
        [Text {:style {:font-weight "bold"}}
         (or (:birthday @data) "Set birthday")]]

       [view {:style {:flex-direction "row" :margin 10 :align-items "center" :justify-content :center
                      }}
        ;;CANCEL
        [touchable-highlight {:style {:flex 1 :background-color "#999" :padding 10 :border-radius 5}
                              :on-press (fn []
                                          (dispatch-sync [:empty-form])
                                          (dispatch [:set-route :patients]))}
         [Text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Cancel"]]

        ;;ADD
        [touchable-highlight {:style {:flex 1 :background-color "#123" :padding 10 :border-radius 5}
                              :on-press (fn []
                                          (if (valid? @data)
                                            (dispatch [:create-remote-patient @data])
                                            (reset! invalid true)
                                            )
                                          )}
         [Text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Add"]]
        ]
       ])))

(defn ViewPatient []
  (let [patient (subscribe [:get-current-patient])]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "flex-start"}}
       [Text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} "View patient"]

       [Text {:style {:font-size 20 :margin-bottom 20}} (str "Name: "     (get-in @patient [:resource :name 0 :given 0]))]
       [Text {:style {:font-size 20 :margin-bottom 20}} (str "Gender: "   (get-in @patient [:resource :gender]))]
       [Text {:style {:font-size 20 :margin-bottom 20}} (str "Birthday: " (get-in @patient [:resource :birthDate]))]
       [Text {:style {:font-size 20 :margin-bottom 20}} (str "Phone: "    (get-in @patient [:resource :telecom 0 :value]))]

       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(dispatch [:set-route :patients])}
        [Text {:style {:color "white" :text-align "center" :font-weight "bold"}} "Go back"]]])))

(defn app-root []
  (let [route (subscribe [:get-current-route])]
    (fn []
      [(case @route
        :patients Patients
        :add-patient AddPatient
        :view-patient ViewPatient
        loading)]
      )))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "PatientManagment" #(r/reactify-component app-root))
  (.show lock #js {} (fn [err profile token]
                       (dispatch [:get-remote-patients])
                       ))
  )
