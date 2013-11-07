HOME="/Users/netspider/Documents/workspace_pinpoint"
VERSION="0.0.4"

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
proguard.sh @proguard-$VERSION.conf

echo "Replace original"
cp $HOME/pinpoint-testbed/agent-obfuscated/pinpoint-bootstrap-$VERSION.jar $HOME/pinpoint-testbed/agent/
cp $HOME/pinpoint-testbed/agent-obfuscated/pinpoint-profiler-$VERSION.jar $HOME/pinpoint-testbed/agent/lib
cp $HOME/pinpoint-testbed/agent-obfuscated/pinpoint-rpc-$VERSION.jar $HOME/pinpoint-testbed/agent/lib

echo "Result is..."
ls -al $HOME/pinpoint-testbed/agent-obfuscated
ls -al $HOME/pinpoint-testbed/agent-org