(ns reload.client
  (:require [taoensso.sente :as sente]
            [taoensso.sente.packers.transit :as sente-transit]
            [ajax.core :refer [GET]]
            [reload.css :as rcss]))



(defn reload-page []
  ;; try getting page first and after reload it
  (GET window.location.href
       {:handler #(js/window.location.reload true)}))



(defmulti event-msg-handler :id)

(defmethod event-msg-handler
  :default         ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id]}])


(defmethod event-msg-handler
  :chsk/recv
  [{:as ev-msg :keys [?data]}]
  (let [[id paths] ?data]
    (condp = id
      :reload/css  (rcss/reload-css-files-remote paths)
      :reload/page (reload-page)
      nil)))


(def ?csrf-token
  (when-let [el (.getElementById js/document "sente-csrf-token")]
    (.getAttribute el "data-csrf-token")))


(defn init-sente []
  (let [{:keys [chsk ch-recv send-fn state]}
        (sente/make-channel-socket-client!
         "/chsk"
         ?csrf-token
         {:packer (sente-transit/get-transit-packer)})]
    {:chsk       chsk
     :ch-chsk    ch-recv
     :chsk-send! send-fn
     :chsk-state state}))


(defn start-sente! []
  (let [{:keys [ch-chsk]} (init-sente)]
    (->> event-msg-handler
         (sente/start-client-chsk-router! ch-chsk))))




(defn ^:export main [& args]
  (start-sente!))

(main)
