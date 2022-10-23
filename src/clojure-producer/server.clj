(ns clojure-producer.server
  (:require [mount.core :refer [defstate]]
            [org.httpkit.server :refer [run-server]]
            [ring.adapter.jetty :as jetty]
            [clojure.java.jdbc :as sql]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [cheshire.core :as json]
            [clojure-producer.kafka-producer :refer [publish-message]]
            [metrics.counters :refer [inc!]]
            ))

(defn bulk-events-handler [request]
  (let [body-str (slurp (:body request))
        body-decode (json/decode body-str true)
        event (body-decode)]
    (info "Event was received. Event payload:" event)
    (try
      (publish-message event)
      (json/encode {:success true})
      (catch Exception e
        (error "web-handler was unable to publish event" e "event payload:" event)))))

(defroutes all-routes
           (GET "/health-check" [] "success")
           (POST "/endpoint-name/" [] bulk-events-handler))

(defn start-web-app [app-port]
  (info "Starting web app at port" app-port)
  (run-server (-> all-routes
                  (otel-server/request-metrics-handler {:exclude-routes ["/health-check"]}))
              {:port app-port}))

(defn stop-web-app [server]
  (info "Shutting down web app")
  (server :timeout 1000))

(defstate web-app-server
          :start (start-web-app (:app-port config/config))
          :stop (stop-web-app web-app-server))