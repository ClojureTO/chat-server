(ns chat-server.core
  (:require [clj-sockets.core :as socket]
            [chat-server.repl :as repl]
            [clojure.string :as string])
  (:gen-class))

(def clients (ref []))

(defn write-line [client message]
  (socket/write-line (:socket client) message)
  client)

(defn error
  [socket message]
  (socket/write-line socket (str "ERROR: " message)))

(defn nick-exists?
  [nick]
  (let [nicks (map #(-> % deref :nick) @clients)]
    (some #{nick} nicks)))

(defn set-nick [client nick]
  (dosync
   (if (nick-exists? nick)
     (error (:socket @client) "nick already exists")
     (send-off client assoc :nick nick))))

(defn send-message [client message]
  (doseq [d-client @clients]
    (when-not (= client d-client)
      (send-off d-client write-line (str (:nick @client) ": " message)))))

(defn terminate-client!
  [client]
  (socket/close-socket (:socket @client))
  (dosync
   (alter clients (partial remove #{client}))))

(defn listen-client
  [client]
  (let [{:keys [socket nick channels] :or {channels []}} @client]
    (loop [line (socket/read-line socket)]
      (let [[command & words] (string/split line #" ")]
        (case command
          "USER" (set-nick client (string/join "-" words))
          "MSG" (send-message client (string/join " " words))
          "QUIT" (terminate-client! client)
          (error socket "I don't understand")))
      (when-not (.isClosed socket)
        (recur (socket/read-line socket))))))

(defn new-client
  [s]
  (loop [line (socket/read-line s)]
    (if-let [[_ nick] (re-matches #"USER (.*)" line)]
      (if-let [client (dosync
                       (when-not (nick-exists? nick)
                         (let [client (agent {:socket s :nick nick :channels []})]
                           (alter clients conj client)
                           client)))]
        (listen-client client)
        (do (error s "nick is already taken, try another")
            (recur (socket/read-line s))))
      (do (error s "first set a nick with USER")
          (recur (socket/read-line s))))))

(defn -main
  "The hello world of chat servers"
  [& args]
  (let [port 1234
        server-socket (socket/create-server port)]
    (println "Listening on port" port)
    (repl/start-repl 7001)
    (loop [client (socket/listen server-socket)]
      ;; TODO: improve debugging inside this future
      (future (new-client client))
      (recur (socket/listen server-socket)))))
