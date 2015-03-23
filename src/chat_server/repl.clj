(ns chat-server.repl
  (:require [clojure.tools.nrepl.server :as nrepl]))

(defn start-repl
  "Start a repl on a port for debugging."
  [port]
  (try
    (nrepl/start-server :port port)
    (println "REPL started on port" port)
    (catch Throwable t
      (println (.getMessage t)))))
