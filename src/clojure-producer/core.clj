
(ns clojure-producer.core
  (:require [clojure-producer.server :as s]
            [clojure-producer.config :as config]
            [clojure-producer.server :refer [web-app-server]]
            [clojure-producer.kafka-producer :refer [producer-sink]]
            [clojure-producer.kafka-producer :refer [producer-chan]]
            [mount.core :refer [start with-args only]]
            )
  (:gen-class))


(defn web-handler-mode []
  (only #{
          #'producer-chan
          #'producer-sink
          #'web-app-server
          }))


(defn -main
  [& args]
    (start (web-handler-mode) (with-args {:app-port config/config}))
    )



