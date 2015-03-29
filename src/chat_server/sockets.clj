(ns chat-server.sockets
  (:import (java.net Socket
                     ServerSocket)))

(defn create-server [port]
  (doto (ServerSocket. port)
    ))
