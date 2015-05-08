#!/bin/bash
set -e
set -x

COLLECTOR_TCP_PORT=${COLLECTOR_TCP_PORT:-}
COLLECTOR_UDP_STAT_LISTEN_PORT=${COLLECTOR_UDP_STAT_LISTEN_PORT:-}
COLLECTOR_UDP_SPAN_LISTEN_PORT=${COLLECTOR_UDP_SPAN_LISTEN_PORT:-}
HBASE_HOST=${HBASE_HOST:-}
HBASE_PORT=${HBASE_PORT:-}

cp /assets/pinpoint-collector.properties /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/pinpoint-collector.properties
cp /assets/hbase.properties /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/hbase.properties

sed 's/COLLECTOR_TCP_PORT/'"${COLLECTOR_TCP_PORT}"'/g' -i /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/pinpoint-collector.properties
sed 's/COLLECTOR_UDP_STAT_LISTEN_PORT/'"${COLLECTOR_UDP_STAT_LISTEN_PORT}"'/g' -i /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/pinpoint-collector.properties
sed 's/COLLECTOR_UDP_SPAN_LISTEN_PORT/'"${COLLECTOR_UDP_SPAN_LISTEN_PORT}"'/g' -i /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/pinpoint-collector.properties
sed 's/HBASE_HOST/'"${HBASE_HOST}"'/g' -i /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/hbase.properties
sed 's/HBASE_PORT/'"${HBASE_PORT}"'/g' -i /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/hbase.properties

/usr/local/tomcat/bin/catalina.sh run