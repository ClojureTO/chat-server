# ClojureTO Workshop Chat Server

This is a skeleton project for a chat server we'll use to build a chat client and server. Hopefully, we'll work together to build a full featured chat web app. Then we can finally have a backchannel to chat about the ClojureTO Workshop while we learn clojure!

## Week 1
(Which is week 3 of the workshop if you're paying attention)

To start we'll work in groups on implementing a very basic server that listens on a socket and routes messages, and a client that can talk to it.

Start by implementing the protocol below. We are using a single `ref` to handle shared state. In the future, we will port this code over to use `core.async`

### The Protocol
- It's a line based protocol. Newline signifies end of message
- nicknames can contain only `[a-z|A-Z|0-9|-+]`,
- usernames can begin with any character other then #
- After connecting, each user sends `USER nickname` as their first command
- Messaging is done with `MSG message`


### Goals for Week 1
The skeleton code implements a simple chat server. This week you should:
- Understand the server code (we'll go over it together)
- Talk to the server using [netcat](http://en.wikipedia.org/wiki/Netcat)
- Start by writing a separate program to function as a client. It should allow the user to specify a server/port and nickname as command line args (Run `lein new app chat-client` to create a new app skeleton)
- Implement private messaging in the server
- Bonus points if the client can connect to multiple servers at once


## Goals for the future
- Add support for channels, which start with #
- Automatically place users in #general and allow users to join and leave channels with `JOIN #channel` and `LEAVE #channel`
- Update messaging syntax to be `MSG #channel message`
- Devise a mechanism for the client to be on multiple channels at once
- Add support for changing nicknames
- Migrate to core.aync
- Replace sockets with websockets and write a web UI in cljs
