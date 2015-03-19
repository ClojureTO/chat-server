(ns chat-server.core
  (:require [clj-sockets.core :as socket])
  (:gen-class))

(def clients (ref {}))


(defn serve-client
  [nick client]
  (doseq [line (socket/read-lines client)]
    (let [other-clients (vals (dissoc @clients nick))]
      (case (first (clojure.string/split line #" "))
        ;; Instructor Note: explain why a doall is needed here
        "MSG" (doall (map #(socket/write-line % (str nick ": " (subs line 4))) other-clients))

        ;;TODO: Process other kinds of command here 
        
        ;; else
        (socket/write-line client "ERROR: I don't understand")))))

(defn new-client
  [client]
  (let [command (socket/read-line client)]
    (if-let [[_ nick] (re-matches #"USER (.*)" command)]
      ;; Instructor note: explain why we have to use a transaction here to make sure checking if user exists and adding them happens atomically
      (if (dosync
           (when-not (get @clients nick)
             (alter clients assoc nick client)))
        
        (serve-client nick client)
        
        (do
          (socket/write-line client "ERROR: Nick already taken")
          (socket/close-socket client))))))

(defn -main
  "The hello world of chat servers"
  [& args]
  (let [port 1234
        server-socket (socket/create-server port)]
    (println "Listening on port" port)
    (loop [client (socket/listen server-socket)]
      ;; TODO: improve debugging inside this future
      (future (new-client client))
      (recur (socket/listen server-socket)))))
