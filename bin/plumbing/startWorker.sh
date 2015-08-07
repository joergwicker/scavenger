#!/bin/bash
#
# Starts a scavenger worker node.
# 
# This script is a part of the 'scavenger'-script, it should not be used 
# directly.

if [ -z "$LOADED_START_WORKER" ]
then
  export LOADED_START_WORKER='true'
else
  exit 0
fi

function startWorker() {
  java \
    $jvmOpts \
    -Dakka.remote.netty.tcp.hostname=$host \
    -Dakka.remote.netty.tcp.port=$port \
    -Dconfig.file=$configFile \
    -cp "$SCAVENGER_HOME/target/scala-2.10/scavenger_2.10-2.1.jar:$jars" \
    scavenger.app.WorkerMain
}
