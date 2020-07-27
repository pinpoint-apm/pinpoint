#!/usr/bin/env bash

this="${BASH_SOURCE-$0}"
while [ -h "$this" ]; do
  ls=`ls -ld "$this"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    this="$link"
  else
    this=`dirname "$this"`/"$link"
  fi
done

# convert relative path to absolute path
bin=`dirname "$this"`
BASE_DIR=`cd "$bin/../../../">/dev/null; pwd`
VERSION=$(mvn -f $BASE_DIR/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)

if [ -f "$BASE_DIR/target/deploy/pinpoint-collector-boot-${VERSION}.jar" ]; then
  # Add below option to override zookeeper address defined at resources/profiles/local/pinpoint-collector.properties
  # -Dpinpoint.zookeeper.address="ZOOKEEPER ADDRESS"
  java -jar -Dspring.profiles.active=local $BASE_DIR/target/deploy/pinpoint-collector-boot-${VERSION}.jar
else
  echo "pinpoint-collector-boot-${VERSION}.jar doesn't exist. Please try again after successfully building pinpoint-collector module."
fi
