#!/bin/bash

# In flink version < 1.11 we can't use "job cluster" because it's not yet available
if [ "$1" = "standalone-job" ]; then
  if [ -e /build/pinpoint-flink-job.jar ]; then
    cd /build
    zip -d pinpoint-flink-job.jar "profiles/release/hbase.properties"
    zip -d pinpoint-flink-job.jar "profiles/release/pinpoint-flink.properties"
    sed -i -e "s/#PINPOINT_ZOOKEEPER_ADDRESS#/${PINPOINT_ZOOKEEPER_ADDRESS}/g" profiles/release/*
    zip -r pinpoint-flink-job.jar profiles/
    cd -

    mv /build/pinpoint-flink-job.jar pinpoint-flink-job.jar
  fi
  
  shift

  /docker-entrypoint.sh jobmanager &
  sleep 5
  if ! flink list | grep 'RUNNING'; then
    flink run $@
  fi
  wait
else
  exec /docker-entrypoint.sh $@
fi