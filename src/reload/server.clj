(ns reload.server
  (:require [roll.core :as roll]))


(defn init []
  (roll.core/init "conf/conf.edn"))

