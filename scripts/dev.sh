#!/bin/sh

KOTLIN_STDLIB=$(ls /opt/idea-*/plugins/Kotlin/kotlinc/lib/kotlin-stdlib.jar)
JAR_DIR="./lib"
CLASSPATH="build/classes/java/main"
CLASSPATH=$CLASSPATH:"build/classes/kotlin/main"
CLASSPATH=$CLASSPATH:"$KOTLIN_STDLIB"

for jar in "$JAR_DIR"/*.jar; do
    CLASSPATH="$CLASSPATH:$jar"
done

java \
  -Djava.util.logging.config.file=logging.properties \
  -Djava.library.path=$(pwd)/lib \
  -cp $CLASSPATH \
  towerbell.TowerBell config.txt
