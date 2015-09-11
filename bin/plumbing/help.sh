#!/bin/bash
#
# Contains help for the 'scavenger'-script. Should not be used directly.

if [ -z "$LOADED_HELP" ]
then
  export LOADED_HELP='true'
else
  exit
fi

function printHelp() {
  echo "Scavenger 2.x"
  echo "This script starts Scavenger 2.x nodes."
  echo ""
  echo "Usage:  "
  echo "    $ scavenger <cmd> <options>"
  echo ""
  echo "Commands:"
  echo "    startSeed"
  echo "    startWorker"
  echo "    startMaster"
  echo ""
  echo "Options:"
  echo "  --jvm-options '-DmyOpt1=val1 -DmyOpt2=val2 ...'"
  echo "  --scavenger-conf <pathToScavengerConfFile>"
  echo "  --host <hostNameOrIp>"
  echo "  --port <portNumber>"
  echo "  --jars <applicationSpecificJars>"
  echo "  --main <full.name.of.ClientApp> "
  echo "  --verbose | -v"
  echo ""
  echo "Examples:"
  echo "1) Assuming 'scavenger.conf' is in 'pathXYZ/scavenger.conf',"
  echo "   all dependency JARs are in the directory 'pathZYX/target/pack/lib/',"
  echo "   you can start a seed node as follows: "
  echo "   "
  echo "    $ scavenger startSeed \\"
  echo "      --jars 'pathZYX/target/pack/lib/*' \\"
  echo "      --scavenger-conf 'pathXYZ/scavenger.conf'"
  echo "     "
  echo "   Notice: there are single quotes around the paths, "
  echo "   and it's just '.../lib/*', not '.../lib/*.jar'."
  echo "2) Starting worker nodes is similar:"
  echo "    "
  echo "    $ scavenger startWorker \\"
  echo "      --jars 'path/to/allTheJars/*' \\"
  echo "      --scavenger-conf 'path/to/scavenger.conf' \\"
  echo "      --port 54321 "
  echo "    "
  echo "   Notice that the hostname is determined automatically, "
  echo "   so you can leave out the option --host most of the times."
  echo "   However, it might be necessary to pick some other port if "
  echo "   Akka's default ports are already occupied."
  echo "3) A master node makes sense only as a part of the client application."
  echo "   Therefore, to start a master node, we have to specify the full"
  echo "   name of the class that contains the 'main'-method and starts "
  echo "   the master node:"
  echo "    $ scavenger startMaster \\"
  echo "      --jars 'path/to/all/the/jars/*' \\"
  echo "      --scavenger-conf 'path/to/scavenger.conf' \\"
  echo "      --main org.myOrg.myApp.MyMain"
}
