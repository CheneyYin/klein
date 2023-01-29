(ns jepsen.kelin
  (:import [com.ofcoder.klein.jepsen.server JepsenClient])
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:require [clojure.tools.logging :refer :all]
            [clojure.string :as cstr]
            [jepsen.checker.timeline :as timeline]
            [jepsen
             [nemesis :as nemesis]
             [checker :as checker]
             [independent :as independent]
             [cli :as cli]
             [client :as client]
             [control :as c]
             [db :as db]
             [generator :as gen]
             [tests :as tests]]
            [knossos.model :as model]
            [jepsen.control.util :as cu]
            [jepsen.os :as os]))

(def fn-opts [[nil "--testfn TEST" "Test function name."]])

(defn- parse-long [s] (Long/parseLong s))
(defn- parse-boolean [s] (Boolean/parseBoolean s))

(def cli-opts
  "Additional command line options."
  [["-s"
    "--slots SLOTS"
    "Number of klein server ranges."
    :default  1
    :parse-fn parse-long
    :validate [pos? "Must be a positive number"]]
   ["-q"
    "--quorum BOOL"
    "Whether to read from quorum."
    :default  false
    :parse-fn parse-boolean
    :validate [boolean? "Must be a boolean value."]]
   ["-r"
    "--rate HZ"
    "Approximate number of requests per second, per thread."
    :default  10
    :parse-fn read-string
    :validate [#(and (number? %) (pos? %)) "Must be a positive number"]]
   [nil
    "--ops-per-key NUM"
    "Maximum number of operations on any given key."
    :default  100
    :parse-fn parse-long
    :validate [pos? "Must be a positive integer."]]])

;;DB
(defn db
  "klein DB for a particular version."
  [version]
  (reify
   db/DB
   (setup! [_ test node]
           (info node "installing klein" version)
           (c/cd "~" (c/exec :sh "java -jar klein-server.jar"))
           (Thread/sleep 10000)
           (info node "installed klein" version))
   (teardown! [_ test node]
              (info node "tearing down klein"))))

;client
(defn r [_ _] {:type :invoke, :f :read, :value nil})
(defn w [_ _] {:type :invoke, :f :write, :value (rand-int 150)})

(defn- create-client0 [test]
  (doto
   (JepsenClient. test)))

(def create-client (memoize create-client0))

(defn- write
  "write a key/value to klein server"
  [client value]
  (info "write result: " (-> client :conn (.put value))))

(defn- read
  "read value by key from klein server"
  [client]
  (doto (-> client :conn (.get))))

(defrecord Client [conn]
  client/Client
  (open! [this test node]
    (-> this
        (assoc :conn (create-client node))))
  (setup! [this test])
  (invoke! [this test op]
    (try
      (case (:f op)
        :read  (assoc op :type :ok, :value (read this))
        :write (do
                 (write this (:value op))
                 (assoc op :type :ok)))
      (catch Exception e
        (let [^String msg (.getMessage e)]
          (cond
            (and msg (.contains msg "TIMEOUT")) (assoc op :type :fail, :error :timeout)
            :else
            (assoc op :type :fail :error (.getMessage e)))))))

  (teardown! [this test])

  (close! [_ test]))

(defn klein-test
  "Given an options map from the command line runner (e.g. :nodes, :ssh, :concurrency, ...), constructs a test map."
  [opts]
  (info "opts: " opts)
  (merge tests/noop-test
         {:pure-generators true
          :name            "klein"
          ;          :os              centos/os
          ;          :db              (db "0.0.1")
          :client          (Client. nil)
          :model (model/register 0)
          :checker (checker/compose
                    {:perf     (checker/perf)
                     :timeline (timeline/html)
                     :linear   (checker/linearizable)
                     })
          :generator       (->> (gen/mix [r w])
                                (gen/stagger 1)
                                (gen/nemesis nil)
                                (gen/time-limit 15))}
         opts))

(defn -main
  "Handles command line arguments. Can either run a test, or a web server for
  browsing results."
  [& args]
  (info "hello klein: " args)
  (cli/run!
   (merge
    (cli/single-test-cmd
     {:test-fn klein-test})
    (cli/serve-cmd))
   args))

;d:/lein.bat run test --time-limit 40 --concurrency 10 --test-count 10 --nodes 1:172.22.0.79:1218,2:172.22.0.80:1218,3:172.22.0.90:1218,4:172.22.0.91:1218,5:172.22.0.96:1218 --username root --password 123456
;d:/lein.bat run test --time-limit 40 --concurrency 10 --test-count 10 --username root --password 123456
