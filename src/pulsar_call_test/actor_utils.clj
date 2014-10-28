(ns pulsar-call-test.actor-utils
  (:require [co.paralleluniverse.pulsar.actors :as actors]
            [co.paralleluniverse.pulsar.core :as pulsar]
            [taoensso.timbre :as log]))

(defn spawn-supervisor []
  (actors/spawn (actors/supervisor :one-for-one (fn [] []))))

(def actor-registry-lock (Object.))
(def registered-actors (atom {}))
(def supervisor (atom (spawn-supervisor)))

(defn actor [actor-fn actor-key]
  (locking actor-registry-lock
    (or (and (contains? @registered-actors actor-key)
             (@registered-actors actor-key))
        (try
          (let [spawned-actor (actors/spawn (actor-fn actor-key) :name actor-key)]
            (actors/add-child! @supervisor actor-key :transient 60 1 :sec 60000 spawned-actor)
            (actors/register! actor-key spawned-actor)
            (swap! registered-actors assoc actor-key spawned-actor)
            spawned-actor)
          (catch Exception e
            (log/error e "Error spawning actor for key " actor-key))))))

(defn shutdown []
  (try
    (locking actor-registry-lock
      (log/info "Sending actors shutdown message")
      (actors/shutdown! @supervisor)
      (log/info "Joining actors")
      (doseq [[actor-key actor] @registered-actors]
        (pulsar/join actor)
        (actors/unregister! actor)
        (swap! registered-actors dissoc actor-key))
      (reset! supervisor (spawn-supervisor)))
    (catch Exception e
      (log/error e "Exception raised shutting down"))))
