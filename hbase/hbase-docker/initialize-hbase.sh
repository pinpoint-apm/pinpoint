#!/bin/bash

${HBASE_HOME}/bin/start-hbase.sh

/usr/local/bin/configure-hbase.sh
/usr/local/bin/check-table.sh