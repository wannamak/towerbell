#!/bin/sh

JAR_DIR="./lib"
CLASSPATH="bin/main"

for jar in "$JAR_DIR"/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

clazz=$1
shift

java \
  -Djava.util.logging.config.file=logging.properties \
  -Djava.library.path="$(pwd)/bin" \
  -cp $CLASSPATH \
  $clazz $@
