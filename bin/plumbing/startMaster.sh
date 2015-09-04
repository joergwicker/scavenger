#!/bin/bash
#
# Starts a scavenger master node.
# 
# This script is a part of the 'scavenger'-script, it should not be used 
# directly.

if [ -z "$LOADED_START_MASTER" ]
then
  export LOADED_START_MASTER='true'
else
  exit 0
fi

function startMaster() {
  completeClassPath="$SCAVENGER_HOME/target/scala-2.10/scavenger_2.10-2.1.jar:$jars"
  if [ $verbose == 'true' ]
  then
    echo "JVM-Options: '$jvmOpts'"
    echo "Classpath: '$completeClassPath'"
    echo "Path to scavenger.conf file: '$configFile'"
    echo "Main class: '$main'"
    echo "Full command: "
    set -x
  fi

  java \
    $jvmOpts \
    -Dakka.remote.netty.tcp.hostname=$host \
    -Dakka.remote.netty.tcp.port=$port \
    -Dconfig.file=$configFile \
    -cp "$completeClassPath" \
    $main
}
