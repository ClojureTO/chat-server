(defproject chat-server "0.1.0-SNAPSHOT"
  :description "Simple chat server"
  :url "https://github.com/ClojureTO/chat-server"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-sockets "0.1.0"]

                 ;; Command line argument processing
                 [org.clojure/tools.cli "0.3.1"]

                 ;; REPL support
                 [org.clojure/tools.nrepl "0.2.8"]
                 [cider/cider-nrepl "0.9.0-SNAPSHOT"]]

  :main ^:skip-aot chat-server.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
