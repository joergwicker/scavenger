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
  echo "1) Assuming 'scavenger.conf' is in '/pathXYZ/scavenger.conf',"
  echo "   all dependency JARs are in the directory '/pathZYX/target/pack/lib/',"
  echo "   you can start a seed node as follows: "
  echo "    $ scavenger startSeed --jars '/pathZYX/target/pack/lib/*' --scavenger-conf '/pathXYZ/scavenger.conf'"
  echo "   Notice: there are single quotes around the paths, and it's just '.../lib/*', not '.../lib/*.jar' or "
  echo "   something like this. The glob-syntax for java-classpath is very fragile, adding '*.jar' in the end "
  echo "   breaks it, and you will probably get ClassNotFoundExceptions"
  echo "   ========== TODO: the stuff below is not verified =========="
  echo "2) Starting worker nodes is similar, but one has to keep in mind "
  echo "   that every worker needs the jars with all classes that are"
  echo "   used for the actual computations:"
  echo "    $ scavenger startWorker --jars /myApp/target/myStuff.jar"
  echo "   Notice that the hostname should be determined automatically, "
  echo "   so that you should leave the option --host out most of the times."
  echo "4) A master node makes sense only as a part of the client application."
  echo "   Therefore, to start a master node, we have to specify the full"
  echo "   name of the class that contains the 'main'-method and starts "
  echo "   the master node:"
  echo "    $ scavenger startMaster \\"
  echo "      --jars /myApp/myJar.jar \\"
  echo "      --main org.myOrg.myApp.MyMain"
}
