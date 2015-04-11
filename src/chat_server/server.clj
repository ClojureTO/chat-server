(ns chat-server.server
  (:require [clojure.string :as string]
            [clojure.java.io :as io])
  (:import (java.net Socket SocketException)))

(def clients (ref []))

(defn write-line
  "Write a single message to a client, returning the client."
  [{:keys [writer] :as client} message]
  (.write writer (str message "\n"))
  (.flush writer)
  client)

(defn error
  "Write an error to a socket, returning nil."
  [writer message]
  (.write writer (str "ERROR: " message "\n"))
  (.flush writer))

(defn send-message
  "Send a message to all clients but the originating one."
  [client message]
  (doseq [d-client @clients]
    (when-not (= client d-client)
      (send-off d-client write-line (str (:nick @client) ": " message)))))

(defn reply [client message]
  (send-off client write-line message))

(defn terminate-client!
  "Close the connections and remove the client from the list."
  [client]
  (try
    (dosync
     (alter clients (partial remove #{client})))
    (.close (:writer @client))
    (.close (:reader @client))
    (catch Throwable e
      (println (.getMessage e)))))

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
     (error (:writer @client) "nick already exists")
     (send client assoc :nick nick))))

(defn listen-client
  "Listen for and dispatch incoming messages from a client."
  [client]
  (let [{:keys [reader writer nick channels] :or {channels []}} @client]
    (loop [line (.readLine reader)]
      (if-let [[command & words] (and line (string/split line #" "))]
        (do (case command
              "LIST" (reply client (clojure.string/join " " (map #(-> % deref :nick) @clients)))
              "USER" (set-nick client (string/join "-" words))
              "MSG" (send-message client (string/join " " words))
              "QUIT" (terminate-client! client)
              (error writer "I don't understand"))
            (recur (.readLine reader)))
        (terminate-client! client)))))

(defn handle-client-error
  "Handle a client error."
  [the-agent exception]
  (let [w (:writer @the-agent)
        msg (.getMessage exception)]
    (try
      (error w msg)
      (catch SocketException e
        (println (.getMessage e) "terminating" (:nick @the-agent))
        (terminate-client! the-agent))
      (finally
        (println msg)))))

(defn new-client
  "Takes a freshly opened socket connection, creates a new client and
  calls the dispatcher."
  [s]
  (let [r (io/reader s)
        w (io/writer s)]
    (loop [line (.readLine r)]
      (if-let [[_ nick] (and line (re-matches #"USER (.*)" line))]
        (if-let [client (dosync
                         (when-not (nick-exists? nick)
                           (let [client (agent {:reader r :writer w :nick nick :channels []}
                                               :error-mode :continue
                                               :error-handler handle-client-error)]
                             (alter clients conj client)
                             client)))]
          (listen-client client)
          (do (error w "nick is already taken, try another")
              (recur (.readLine r))))
        (do (error w "first set a nick with USER")
            (recur (.readLine r)))))))
