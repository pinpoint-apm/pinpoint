#!/bin/bash

if echo -e "exists 'HostApplicationMap_Ver2'" | ${HBASE_HOME}/bin/hbase shell 2>&1 | grep -q "does exist" 2>/dev/null
then
    echo "Tables already exist"
    #sleep 15
    #${HBASE_HOME}/bin/hbase shell ${BASE_DIR}/hbase-update-ttl.hbase
else
    sleep 15
    echo "create tables"
    ${HBASE_HOME}/bin/hbase shell ${BASE_DIR}/hbase-create.hbase
fi