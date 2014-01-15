VERSION="1.0.1"
DEPLOY_DIR="../pinpoint-testbed/agent"

#profile
PROFILE=""
if [ "$1" != "" ] ; then
	PROFILE="-P $1"
	echo "*********************"
	echo "USING PROFILE $1"
	echo "*********************"
else
	echo "*********************"
	echo "USING DEFAULT PROFILE"
	echo "*********************"
fi

# bootstrap
mvn clean install eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip $PROFILE
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

# profiler
pushd .
cd ../pinpoint-profiler
mvn clean install eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip $PROFILE
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi
popd

rm -fr $DEPLOY_DIR
mkdir -p $DEPLOY_DIR/lib

cp ./target/pinpoint-bootstrap-$VERSION-jar-with-dependencies.jar $DEPLOY_DIR/pinpoint-bootstrap-$VERSION.jar
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-rpc/target/pinpoint-rpc-$VERSION.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-thrift/target/pinpoint-thrift-$VERSION.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-rpc/target/dependency/netty-3.6.6.Final.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-profiler/target/pinpoint-profiler-$VERSION.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-profiler/target/dependency/javassist-3.18.0-GA.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-thrift/target/dependency/libthrift-0.9.1.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-profiler/target/dependency/log4j-1.2.16.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-profiler/target/dependency/slf4j-log4j12-1.7.5.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-profiler/target/dependency/slf4j-api-1.7.5.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-profiler/target/dependency/guava-14.0.1.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-profiler/target/dependency/metrics-*-3.0.1.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-profiler/runscript/help.txt $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-profiler/target/classes/pinpoint.config $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ../pinpoint-profiler/target/classes/log4j.xml $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi