(ns patient-managment.jwt
  (:require [goog.array]
            [goog.crypt.base64]
            [goog.crypt.Hmac]
            [goog.crypt.Sha256]
            ))

(def sha256 (new goog.crypt.Sha256))
(def hmac (goog.crypt.Hmac. sha256
                            (clj->js (map
                                      #(.charCodeAt %)
                                      (goog.array.toArray "K5LzQFZTzAdUmqGyr5yv01E00qICMaq28_nYGBKZvbtf31Ii2trOBnjNYT90qgqE")))))


(defn hmac-sign
  [message]
  (.getHmac hmac message))

(defn convert-to-url-safe [s]
  (-> s
      (clojure.string/replace "=" "")
      (clojure.string/replace "+" "-")
      ))

(defn hmac-verify
  [message signature]
  (= (convert-to-url-safe (goog.crypt.base64/encodeByteArray (hmac-sign message)))
     signature))

(defn valid?
  [token]
  (if-let [[header payload signature] (clojure.string/split token #"\.")]
    (if (every? some?  [header payload signature])
      (hmac-verify (str header "." payload) signature))
    ))
