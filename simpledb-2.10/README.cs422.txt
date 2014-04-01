This is SimpleDB 2.10 repackaged for CS422. The original package can be
downloaded from http://cs.bc.edu/~sciore/simpledb/intro.html.

The following changes are made:
. Removed all .DS_Store files.
. Moved /simpledb to /src/simpledb
. Removed the client code for Derby.
. Moved the SimpleDB client code to the simpledb.client package.
. Modified the class simpledb.server.Startup to use a default database name
  "StudentDB" if one is not given in the command line arguments.
. Removed /.settings and replaced .project and .classpath so the project uses
  default Eclipse settings.

csun@calstatela.edu, 11/5/2013
