(require '[clojure.java.shell :as shell])
(require '[cljs.build.api :as b])
(require '[cljs.repl :as repl])
(require '[cljs.repl.nashorn :as nashorn])

(defmulti task first)

(defmethod task :default
  [args]
  (let [all-tasks  (-> task methods (dissoc :default) keys sort)
        interposed (->> all-tasks (interpose ", ") (apply str))]
    (println "Unknown or missing task. Choose one of:" interposed)
    (System/exit 1)))

(def foreign-libs
  [{:file "https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/moment.js"
    :file-min "https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.24.0/moment.min.js"
    :provides ["vendor.moment"]}])

(def build-opts
  {:output-to "out/leapyears.js"
   :source-map true
   :output-dir "out/leapyears"
   :main 'leapyears.core
   :verbose true
   :optimizations :none
   :foreign-libs foreign-libs})

(defmethod task "repl"
  [args]
  (repl/repl (nashorn/repl-env)
             :foreign-libs foreign-libs
             :output-dir "out/nashorn"
             :cache-analysis true))

(defmethod task "build"
  [args]
  (b/build (b/inputs "src" "vendor") build-opts))

(defmethod task "watch"
  [args]
  (b/watch (b/inputs "src" "vendor") build-opts))


(defmethod task "build:tests"
  [args]
  (b/build (b/inputs "src" "vendor" "test")
           (assoc build-opts
                  :main 'leapyears.test.main
                  :output-to "out/tests.js"
                  :output-dir "out/tests"
                  :target :nodejs)))

(defmethod task "watch:tests"
  [args]
  (letfn [(run-tests []
            (let [{:keys [out err]} (shell/sh "node" "out/tests.js")]
              (println out err)))]
    (println "Start watch loop...")
    (try
      (b/watch (b/inputs "src", "test")
               (assoc build-opts
                      :main 'leapyears.test.main
                      :watch-fn run-tests
                      :output-to "out/tests.js"
                      :output-dir "out/tests"
                      :target :nodejs))

      (catch Exception e
        (println "Error on running tests:" e)
        ;; (Thread/sleep 2000)
        (task args)))))

(task *command-line-args*)
