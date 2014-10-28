(ns pulsar-call-test.core
  (:require [co.paralleluniverse.pulsar.actors :as actors]
            [co.paralleluniverse.pulsar.core :as pulsar]
            [taoensso.timbre :as log]
            [pulsar-call-test.actor-utils :as actor-utils]))

(defn actor [actor-id]
  (actors/gen-server
    (reify actors/Server

      (terminate [_ reason]
        (if reason
          (log/info "Actor" actor-id "shutting down due to" reason)
          (log/info "Actor" actor-id "shutting down")))

      (init [_])

      (handle-call [_ from id message]
          :success))))

(defn find-actor [id]
  (actor-utils/find-actor id))

(defn handle-message [thread-name message]
  (let [id (:id message)
        message-id (get-in message [:value :message-id])
        actor (find-actor id)]
    (try
      (when (zero? (mod message-id 1000))
        (log/info "Thread" thread-name "sending" message-id))
      (actors/call! actor [:message (:value message)])
      (catch Exception e
        (log/error e "Error calling actor, message" message)))))

(def ids (range 10))

(defn create-actors []
  (doseq [id ids]
    (actor-utils/create-actor actor (str id))))

(defn generate-message [id message-id]
  {:id (str id) :value {:message-id message-id :data "something"}})

(defn generate-messages [message-id]
  (let [id (rand-nth ids)]
    (cons (generate-message id (swap! message-id inc)) (lazy-seq (generate-messages message-id)))))

(defn create-message-thread [name]
  (pulsar/spawn-thread :name name
    (fn []
      (let [message-id (atom 0)]
        (doseq [message (generate-messages message-id)]
          (handle-message name message))))))

(defn start-processing []
  (create-message-thread "A")
  (create-message-thread "B"))

(defn -main [& args]
  (create-actors)
  (start-processing))
