(ns pulsar-call-test.actor-utils
  (:require [co.paralleluniverse.pulsar.actors :as actors]
            [co.paralleluniverse.pulsar.core :as pulsar]
            [taoensso.timbre :as log]))

(defn spawn-supervisor []
  (actors/spawn (actors/supervisor :one-for-one (fn [] []))))

(def registered-actors (atom {}))
(def supervisor (atom (spawn-supervisor)))

(defn find-actor [actor-key]
  (@registered-actors actor-key))

(defn create-actor [actor-fn actor-key]
  (try
    (let [spawned-actor (actors/spawn (actor-fn actor-key) :name actor-key)]
      (actors/add-child! @supervisor actor-key :transient 60 1 :sec 60000 spawned-actor)
      (swap! registered-actors assoc actor-key spawned-actor)
      spawned-actor)
    (catch Exception e
      (log/error e "Error spawning actor for key " actor-key))))
