#!/bin/bash
#
# Starts a scavenger seed node.
# 
# This script is a part of the 'scavenger'-script, it should not be used 
# directly.

if [ -z 'SOURCED_START_SEED' ]
then
  export SOURCED_START_SEED='true'
else
  return
fi

function startSeed() {
  java \
    $jvmOpts \
    -Dconfig.file=$configFile \
    -cp $SCAVENGER_HOME/target/scala-2.10/scavenger_2.10-2.1.jar \
    scavenger.app.SeedMain
}