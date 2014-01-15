HOME="/Users/netspider/Documents/workspace_pinpoint"
VERSION="1.0.1"

echo "Remove previous"
rm -fr $HOME/pinpoint-testbed/agent-obfuscated
rm -fr $HOME/pinpoint-testbed/agent-org
mkdir $HOME/pinpoint-testbed/agent-obfuscated
mkdir $HOME/pinpoint-testbed/agent-org

echo "Backup original"
cp $HOME/pinpoint-testbed/agent/pinpoint-bootstrap-$VERSION.jar $HOME/pinpoint-testbed/agent-org/
cp $HOME/pinpoint-testbed/agent/lib/pinpoint-profiler-$VERSION.jar $HOME/pinpoint-testbed/agent-org/
cp $HOME/pinpoint-testbed/agent/lib/pinpoint-rpc-$VERSION.jar $HOME/pinpoint-testbed/agent-org/

echo "Obfuscating"
RT_JAR="/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/lib/rt.jar"
proguard.sh -libraryjars $RT_JAR @proguard-$VERSION.conf

echo "Replace original"
cp $HOME/pinpoint-bootstrap/target/pinpoint-bootstrap-$VERSION-jar-with-dependencies-obfuscated.jar $HOME/pinpoint-testbed/agent/pinpoint-bootstrap-$VERSION.jar
cp $HOME/pinpoint-profiler/target/pinpoint-profiler-$VERSION-obfuscated.jar $HOME/pinpoint-testbed/agent/lib/pinpoint-profiler-$VERSION.jar
cp $HOME/pinpoint-rpc/target/pinpoint-rpc-$VERSION-obfuscated.jar $HOME/pinpoint-testbed/agent/lib/pinpoint-rpc-$VERSION.jar

echo "Result is..."
ls -al $HOME/pinpoint-testbed/agent-obfuscated
ls -al $HOME/pinpoint-testbed/agent-org