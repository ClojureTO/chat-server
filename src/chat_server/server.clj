(ns chat-server.server
  (:require [clj-sockets.core :as socket]
            [clojure.string :as string]))

(def clients (ref []))

(defn write-line
  "Write a single message to a client, returning the client."
  [client message]
  (socket/write-line (:socket client) message)
  client)

(defn error
  "Write an error to a socket, returning nil."
  [socket message]
  (socket/write-line socket (str "ERROR: " message)))

(defn nick-exists?
  "Return truthy if a nick is already in use, otherwise nil."
  [nick]
  (let [nicks (map #(-> % deref :nick) @clients)]
    (boolean (some #{nick} nicks))))

(defn set-nick
  "Set the nick of an existing client. This function is transactional."
  [client nick]
  (dosync
   (if (nick-exists? nick)
     (error (:socket @client) "nick already exists")
     (send-off client assoc :nick nick))))

(defn send-message
  "Send a message to all clients but the originating one."
  [client message]
  (doseq [d-client @clients]
    (when-not (= client d-client)
      (send-off d-client write-line (str (:nick @client) ": " message)))))

(defn terminate-client!
  "Close the socket and remove the client from the list."
  [client]
  (try
    (dosync
     (alter clients (partial remove #{client})))
    (socket/close-socket (:socket @client))
    (catch Throwable e
      (println (.getMessage e)))))

(defn listen-client
  "Listen for and dispatch incoming messages from a client."
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

(defn handle-client-error
  "Handle a client error."
  [the-agent exception]
  (let [s (:socket @the-agent)
        msg (.getMessage exception)]
    (when-not (.isClosed s)
      (error s msg))
    (println msg)
    (terminate-client! the-agent)))

(defn new-client
  "Takes a freshly opened socket connection, creates a new client and
  calls the dispatcher."
  [s]
  (loop [line (socket/read-line s)]
    (if-let [[_ nick] (re-matches #"USER (.*)" line)]
      (if-let [client (dosync
                       (when-not (nick-exists? nick)
                         (let [client (agent {:socket s :nick nick :channels []}
                                             :error-mode :continue
                                             :error-handler handle-client-error)]
                           (alter clients conj client)
                           client)))]
        (listen-client client)
        (do (error s "nick is already taken, try another")
            (recur (socket/read-line s))))
      (do (error s "first set a nick with USER")
          (recur (socket/read-line s))))))
