#!/bin/sh

JAR_DIR="./lib"
CLASSPATH="build/classes/java/main"
CLASSPATH=$CLASSPATH:"build/classes/kotlin/main"

for jar in "$JAR_DIR"/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

java \
  -Djava.util.logging.config.file=logging.properties \
  -Djava.library.path=$(pwd)/lib \
  -cp $CLASSPATH \
  towerbell.TowerBell config.txt
