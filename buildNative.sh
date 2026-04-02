#!/bin/sh

HEADER_OUT_DIR=src/native
CLASS_OUT_DIR=build/main/towerbell/pi/physical
JAVA_SRC_DIR=src/main/java/towerbell/pi/physical
NATIVE_SRC_DIR=src/native
JDK_INCLUDE=/usr/lib/jvm/java-21-openjdk-amd64/include
NATIVE_OUT_DIR=build/native

set -e

javac \
  -h $HEADER_OUT_DIR \
  -d $CLASS_OUT_DIR \
  $JAVA_SRC_DIR/GPIOController.java

javac \
  -h $HEADER_OUT_DIR \
  -d $CLASS_OUT_DIR \
  $JAVA_SRC_DIR/GPIOChipInfoProvider.java

javac \
  -h $HEADER_OUT_DIR \
  -d $CLASS_OUT_DIR \
  $JAVA_SRC_DIR/SystemManagementBus.java

mkdir -p $NATIVE_OUT_DIR

aarch64-linux-gnu-gcc \
  -shared \
  -O3 \
  -Ilibgpiod2 \
  -I/usr/include \
  -I/usr/lib/x86_64-linux-gnu/glib-2.0/include \
  -I$JDK_INCLUDE \
  -I$JDK_INCLUDE/linux \
  -Wl,--no-as-needed \
  libgpiod2/libgpiod.so.3.1.2 \
  $NATIVE_SRC_DIR/towerbell_pi_physical_SystemManagementBus.cpp \
  $NATIVE_SRC_DIR/towerbell_pi_physical_GPIOController.cpp \
  $NATIVE_SRC_DIR/towerbell_pi_physical_GPIOChipInfoProvider.cpp \
  -o $NATIVE_OUT_DIR/libtowerbell.so
