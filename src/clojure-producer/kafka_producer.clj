(ns clojure-producer.kafka-producer
  (:require [mount.core :refer [defstate]]
            [clojure-producer.config :as config]
            [af.kafka.producer-sink :as af-sink]
            [clojure.core.async :as async]
            [cheshire.core :as json])
  )

(defstate producer-chan
          :start (async/chan 1000))


(defn run-kafka-producer []
  (info "Starting kafka producer")
  (let [producer-config config/kafka-producer-config]
    (println producer-config)
    (af-sink/producer-sink producer-config producer-chan)))


(defstate producer-sink
                :start (run-kafka-producer))

(defn publish-message [message]
  (let [message-str (json/encode message {:escape-non-ascii true})]
    (info "Event was sent to Kafka topic" message)
    (async/>!! producer-chan message-str)))

