(defproject pulsar-call-test "1.0.0"
  :description "A kafka -> kafka bridge"
  :url "http://www.github.braintreeps.com/braintree/pulsar-call-test"
  :jvm-opts ["-XX:+UseG1GC"]
  :java-agents [[co.paralleluniverse/quasar-core "0.6.1"]]
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main pulsar-call-test.core
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [com.taoensso/timbre "3.2.1"]
                 [co.paralleluniverse/pulsar "0.6.1"]])
