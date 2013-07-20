jsync
=====

A Java implementation of the rsync algorithm (not the protocol) and corresponding command 
line client and daemon.

Usage:

1. Start daemon:
java -cp build/libs/jsync.jar de.tntinteractive.jsync.JsyncDaemon 13579

2. Synchronize directory tree

2.1 Directory from local to remote:
java -cp build/libs/jsync.jar de.tntinteractive.jsync.JsyncClient localDirectory targethost:13579 targetParentDirectory

2.2 Directory contents from local to remote (small slash, big difference ;) ):
java -cp build/libs/jsync.jar de.tntinteractive.jsync.JsyncClient localDirectory/ targethost:13579 targetDir
Target directory will be created if non-existant.

2.3 Syncing from a program:
Have a look at de.tntinteractive.jsync.JsyncClient.syncDirectory()


Further info can be found in the wiki:
https://github.com/tobiasbaum/jsync/wiki

If you're looking for binaries, look into
https://github.com/tobiasbaum/jsync/binaries
(yeah, I know distributing binaries over github is not ideal)

