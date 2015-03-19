# ClojureTO Workshop Chat Server

This is a skeleton project for a chat server we'll use to build a chat client and server. Hopefully, we'll work together to build a full featured chat web app. Then we can finally have a backchannel to chat about the ClojureTO Workshop while we learn clojure!

## Week 1
(Which is week 3 of the workshop if you're paying attention)

To start we'll work in groups on implementing a very basic server that listens on a socket and routes messages, and a client that can talk to it.

Start by implementing the protocol below. Don't worry about concurrency yet! The library included in this skeleton project `clj-sockets` is synchronous, so no need to read up on Clojure concurrency primitives or core.async channels. Start with a synchronous server, and we'll make it async next week.

### The Protocol
- It's a line based protocol. Newline signifies end of message
- channel and nicknames can contain only `[a-z|A-Z|0-9|-+]`,
- channel names must begin with #,
- usernames can begin with any character other then #
- After connecting, each user sends `USER nickname` as their first command
- At first there's only one channel, `#general`, we'll add more later!
- Messaging a user is done with `MSG nickanme message`
- Messaging a channel is done with `MSG #channel message`

### Goals for Week 1
- Get a feel for working with a project larger then one function
- Write a couple of tests (either with [clojure.test](https://clojure.github.io/clojure/clojure.test-api.html) or [midje](https://github.com/marick/Midje)).
- Try to practice REPL-driven development
- Think about how to organize code into namespaces
- Have fun!!!
