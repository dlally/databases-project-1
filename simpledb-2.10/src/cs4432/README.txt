Douglas Lally - dlally@wpi.edu
Nathaniel Miller - nwmiller@wpi.edu
CS4432 - Project 1
-----------------------------------
Installation / Execution Instructions:
1) Import ExtendedSimpleDB.zip as an existing project into your Eclipse Workspace
2) Make sure you are using a Java 1.7 JRE. (1.8 should work, but we did not test it)
3) Select the desired replacement policy by modifying Line 24 in /src/simpledb/buffer/BasicBufferMgr.java
   replacementPolicy should equal either BufferReplacementPolicy.LRU or BufferReplacementPolicy.CLOCK
4) Delete any previous database(s) created by other groups in the default location
5) Launch the server by running the main function of /src/simpledb/server/Startup.java
6) Execute test by running /src/cs4432/TestDBProgram.java

To switch replacement policy:
1) Stop the server if it is running
2) Select the desired replacement policy by modifying Line 24 in /src/simpledb/buffer/BasicBufferMgr.java
   replacementPolicy should equal either BufferReplacementPolicy.LRU or BufferReplacementPolicy.CLOCK
3) Launch the server by running the main function of /src/simpledb/server/Startup.java
4) Execute test by running /src/cs4432/TestDBProgram.java