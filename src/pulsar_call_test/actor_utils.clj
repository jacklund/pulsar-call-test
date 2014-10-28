(ns pulsar-call-test.actor-utils
  (:require [co.paralleluniverse.pulsar.actors :as actors]
            [co.paralleluniverse.pulsar.core :as pulsar]
            [taoensso.timbre :as log]))

(def registered-actors (atom {}))

(defn find-actor [actor-key]
  (@registered-actors actor-key))

(defn create-actor [actor-fn actor-key]
  (try
    (let [spawned-actor (actors/spawn (actor-fn actor-key) :name actor-key)]
      (swap! registered-actors assoc actor-key spawned-actor)
      spawned-actor)
    (catch Exception e
      (log/error e "Error spawning actor for key " actor-key))))
