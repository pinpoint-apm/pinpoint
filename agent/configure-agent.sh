#!/bin/bash
set -e
set -x

COLLECTOR_IP=${COLLECTOR_IP:-127.0.0.1}
COLLECTOR_TCP_PORT=${COLLECTOR_TCP_PORT:-9994}
COLLECTOR_UDP_STAT_LISTEN_PORT=${COLLECTOR_UDP_STAT_LISTEN_PORT:-9995}
COLLECTOR_UDP_SPAN_LISTEN_PORT=${COLLECTOR_UDP_SPAN_LISTEN_PORT:-9996}

DISABLE_DEBUG=${DISABLE_DEBUG:-true}

cp -f /assets/pinpoint.config /assets/pinpoint-agent/pinpoint.config

sed -i "s/profiler.collector.ip=127.0.0.1/profiler.collector.ip=${COLLECTOR_IP}/g" /assets/pinpoint-agent/pinpoint.config

sed -i "s/profiler.collector.tcp.port=9994/profiler.collector.tcp.port=${COLLECTOR_TCP_PORT}/g" /assets/pinpoint-agent/pinpoint.config
sed -i "s/profiler.collector.stat.port=9995/profiler.collector.stat.port=${COLLECTOR_UDP_STAT_LISTEN_PORT}/g" /assets/pinpoint-agent/pinpoint.config
sed -i "s/profiler.collector.span.port=9996/profiler.collector.span.port=${COLLECTOR_UDP_SPAN_LISTEN_PORT}/g" /assets/pinpoint-agent/pinpoint.config

if [ "$DISABLE_DEBUG" == "true" ]; then
    sed -i 's/level value="DEBUG"/level value="INFO"/' /assets/pinpoint-agent/lib/log4j.xml
fi

exec tail -f /dev/null
