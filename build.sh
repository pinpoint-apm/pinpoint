VERSION="0.0.4-SNAPSHOT"
DEPLOY_DIR="../pinpoint-testbed/agent"

# profiler
pushd .
cd ../pinpoint-profiler
mvn clean install eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip -Dthrift.executable.property=/Users/netspider/DEV-TOOLS/thrift-0.9.0/bin/thrift
popd

# bootstrap
mvn clean install eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip

rm -fr $DEPLOY_DIR
mkdir -p $DEPLOY_DIR/lib

cp ./target/pinpoint-bootstrap-$VERSION-jar-with-dependencies.jar $DEPLOY_DIR/pinpoint-bootstrap-$VERSION.jar

cp ../pinpoint-rpc/target/pinpoint-rpc-$VERSION.jar $DEPLOY_DIR/lib
cp ../pinpoint-rpc/target/dependency/netty-3.6.6.Final.jar $DEPLOY_DIR/lib

cp ../pinpoint-profiler/target/pinpoint-profiler-$VERSION.jar $DEPLOY_DIR/lib
cp ../pinpoint-profiler/target/dependency/javassist-3.16.1-GA.jar $DEPLOY_DIR/lib
cp ../pinpoint-profiler/target/dependency/libthrift-0.9.0.jar $DEPLOY_DIR/lib
cp ../pinpoint-profiler/target/dependency/log4j-1.2.16.jar $DEPLOY_DIR/lib
cp ../pinpoint-profiler/target/dependency/slf4j-log4j12-1.6.6.jar $DEPLOY_DIR/lib
cp ../pinpoint-profiler/target/dependency/slf4j-api-1.6.6.jar $DEPLOY_DIR/lib

cp ../pinpoint-profiler/runscript/help.txt $DEPLOY_DIR
cp ../pinpoint-profiler/target/classes/pinpoint.config $DEPLOY_DIR
cp ../pinpoint-profiler/target/classes/log4j.xml $DEPLOY_DIR