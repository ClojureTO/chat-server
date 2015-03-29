(ns chat-server.core
  (:require [chat-server.server :as server]
            [clj-sockets.core :as socket]
            [chat-server.repl :as repl]
            [clojure.string :as string]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port to listen for client connections on"
    :default 1234
    :parse-fn #(Integer/parseInt %)]
   ["-r" "--repl-port PORT" "Port to start a REPL on"
    :default 7001
    :parse-fn #(Integer/parseInt %)]
   ["-m" "--repl-middleware MIDDLEWARE" "REPL Middleware to load (currently only 'cider' supported)"
    :default :none
    :parse-fn #(keyword %)]
   ["-h" "--help" "Print this helpful text"]])

(defn usage [options-summary]
  (->> ["This is the simplest chat server in the world."
        ""
        "Usage: lein run -- [-p port] [-r repl-port] [-m middleware]"
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn error-msg [errors]
  (str "Error parsing command line:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main
  "The hello world of chat servers"
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 0) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors))

      :else
      (let [server-socket (socket/create-server (:port options))
            repl-options {:port (:repl-port options)
                          :middleware (:repl-middleware options)}]
        (repl/start-repl repl-options)
        (println "Listening on port" (:port options))
        (loop [client (socket/listen server-socket)]
          ;; TODO: improve debugging inside this future
          (future (server/new-client client))
          (recur (socket/listen server-socket)))))))
