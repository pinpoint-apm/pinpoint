#!/bin/bash
set -e
set -x

#sed -i "/profiler.transport.module=/ s/=.*/=${PROFILER_TRANSPORT_MODULE}/" /pinpoint-agent/pinpoint.config
sed -i "/profiler.transport.module=/ s/=.*/=${PROFILER_TRANSPORT_MODULE}/" /pinpoint-agent/profiles/local/pinpoint.config /pinpoint-agent/profiles/release/pinpoint.config

sed -i "/profiler.collector.ip=/ s/=.*/=${COLLECTOR_IP}/" /pinpoint-agent/profiles/local/pinpoint.config /pinpoint-agent/profiles/release/pinpoint.config
sed -i "/profiler.collector.tcp.port=/ s/=.*/=${COLLECTOR_TCP_PORT}/" /pinpoint-agent/pinpoint-root.config
sed -i "/profiler.collector.stat.port=/ s/=.*/=${COLLECTOR_STAT_PORT}/" /pinpoint-agent/pinpoint-root.config
sed -i "/profiler.collector.span.port=/ s/=.*/=${COLLECTOR_SPAN_PORT}/" /pinpoint-agent/pinpoint-root.config

#sed -i "/profiler.transport.grpc.collector.ip=/ s/=.*/=${COLLECTOR_IP}/" /pinpoint-agent/pinpoint.config
sed -i "/profiler.transport.grpc.collector.ip=/ s/=.*/=${COLLECTOR_IP}/" /pinpoint-agent/profiles/local/pinpoint.config /pinpoint-agent/profiles/release/pinpoint.config
sed -i "/profiler.transport.grpc.agent.collector.port=/ s/=.*/=${PROFILER_TRANSPORT_AGENT_COLLECTOR_PORT}/" /pinpoint-agent/pinpoint-root.config
sed -i "/profiler.transport.grpc.metadata.collector.port=/ s/=.*/=${PROFILER_TRANSPORT_METADATA_COLLECTOR_PORT}/" /pinpoint-agent/pinpoint-root.config
sed -i "/profiler.transport.grpc.stat.collector.port=/ s/=.*/=${PROFILER_TRANSPORT_STAT_COLLECTOR_PORT}/" /pinpoint-agent/pinpoint-root.config
sed -i "/profiler.transport.grpc.span.collector.port=/ s/=.*/=${PROFILER_TRANSPORT_SPAN_COLLECTOR_PORT}/" /pinpoint-agent/pinpoint-root.config
sed -i "/profiler.sampling.type=/ s/=.*/=${PROFILER_SAMPLING_TYPE}/" /pinpoint-agent/profiles/local/pinpoint.config /pinpoint-agent/profiles/release/pinpoint.config
sed -i "/profiler.sampling.counting.sampling-rate=/ s/=.*/=${PROFILER_SAMPLING_COUNTING_SAMPLING_RATE}/" /pinpoint-agent/profiles/local/pinpoint.config /pinpoint-agent/profiles/release/pinpoint.config
sed -i "/profiler.sampling.percent.sampling-rate=/ s/=.*/=${PROFILER_SAMPLING_PERCENT_SAMPLING_RATE}/" /pinpoint-agent/profiles/local/pinpoint.config /pinpoint-agent/profiles/release/pinpoint.config
sed -i "/profiler.sampling.new.throughput=/ s/=.*/=${PROFILER_SAMPLING_NEW_THROUGHPUT}/" /pinpoint-agent/profiles/local/pinpoint.config /pinpoint-agent/profiles/release/pinpoint.config
sed -i "/profiler.sampling.continue.throughput=/ s/=.*/=${PROFILER_SAMPLING_CONTINUE_THROUGHPUT}/" /pinpoint-agent/profiles/local/pinpoint.config /pinpoint-agent/profiles/release/pinpoint.config

exec "$@"
