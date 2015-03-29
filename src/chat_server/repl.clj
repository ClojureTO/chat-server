(ns chat-server.repl
  (:require [clojure.tools.nrepl.server :as nrepl]
            [cider.nrepl]))

(def cider-middleware (map resolve cider.nrepl/cider-middleware))

(defn start-repl
  "Start a repl on a port for debugging."
  [{:keys [port middleware]}]
  (println "Starting REPL on port" port)
  (let [options (concat
                 [:port port]
                 (case middleware
                   :cider (do
                            (println "Installing CIDER REPL middleware")
                            [:handler (apply nrepl/default-handler cider-middleware)])
                   :none (println "No REPL middleware installed")
                   (println (name middleware) "middleware not supported")))]
    (try
      (apply nrepl/start-server options)
      (catch Throwable t
        (println (.getMessage t))))))
