#!/bin/bash
set -e
set -x
cp ${BASE_DIR}/hbase-create.hbase ${BASE_DIR}/hbase-update-ttl.hbase

sed -i "/AgentInfo/s/TTL => .[[:digit:]]*/TTL => ${AGENTINFO_TTL:-31536000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/AgentStatV2/s/TTL => .[[:digit:]]*/TTL => ${AGENTSTATV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/ApplicationStatAggre/s/TTL => .[[:digit:]]*/TTL => ${APPSTATAGGRE_TTL:-5184000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/ApplicationIndex/s/TTL => .[[:digit:]]*/TTL => ${APPINDEX_TTL:-31536000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/AgentLifeCycle/s/TTL => .[[:digit:]]*/TTL => ${AGENTLIFECYCLE_TTL:-5184000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/AgentEvent/s/TTL => .[[:digit:]]*/TTL => ${AGENTEVENT_TTL:-5184000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/StringMetaData/s/TTL => .[[:digit:]]*/TTL => ${STRINGMETADATA_TTL:-15552000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/ApiMetaData/s/TTL => .[[:digit:]]*/TTL => ${APIMETADATA_TTL:-31536000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/SqlMetaData_Ver2/s/TTL => .[[:digit:]]*/TTL => ${SQLMETADATA_TTL:-15552000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/TraceV2/s/TTL => .[[:digit:]]*/TTL => ${TRACEV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/ApplicationTraceIndex/s/TTL => .[[:digit:]]*/TTL => ${APPTRACEINDEX_TTL:-5184000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/ApplicationMapStatisticsCaller_Ver2/s/TTL => .[[:digit:]]*/TTL => ${APPMAPSTATCALLERV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/ApplicationMapStatisticsCallee_Ver2/s/TTL => .[[:digit:]]*/TTL => ${APPMAPSTATCALLEV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/ApplicationMapStatisticsSelf_Ver2/s/TTL => .[[:digit:]]*/TTL => ${APPMAPSTATSELFV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-create.hbase
sed -i "/HostApplicationMap_Ver2/s/TTL => .[[:digit:]]*/TTL => ${HOSTAPPMAPV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-create.hbase

sed -i "s/create/alter/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/AgentInfo/s/TTL => .[[:digit:]]*/TTL => ${AGENTINFO_TTL:-31536000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/AgentStatV2/s/TTL => .[[:digit:]]*/TTL => ${AGENTSTATV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/ApplicationStatAggre/s/TTL => .[[:digit:]]*/TTL => ${APPSTATAGGRE_TTL:-5184000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/ApplicationIndex/s/TTL => .[[:digit:]]*/TTL => ${APPINDEX_TTL:-31536000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/AgentLifeCycle/s/TTL => .[[:digit:]]*/TTL => ${AGENTLIFECYCLE_TTL:-5184000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/AgentEvent/s/TTL => .[[:digit:]]*/TTL => ${AGENTEVENT_TTL:-5184000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/StringMetaData/s/TTL => .[[:digit:]]*/TTL => ${STRINGMETADATA_TTL:-15552000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/ApiMetaData/s/TTL => .[[:digit:]]*/TTL => ${APIMETADATA_TTL:-31536000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/SqlMetaData_Ver2/s/TTL => .[[:digit:]]*/TTL => ${SQLMETADATA_TTL:-15552000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/TraceV2/s/TTL => .[[:digit:]]*/TTL => ${TRACEV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/ApplicationTraceIndex/s/TTL => .[[:digit:]]*/TTL => ${APPTRACEINDEX_TTL:-5184000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/ApplicationMapStatisticsCaller_Ver2/s/TTL => .[[:digit:]]*/TTL => ${APPMAPSTATCALLERV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/ApplicationMapStatisticsCallee_Ver2/s/TTL => .[[:digit:]]*/TTL => ${APPMAPSTATCALLEV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/ApplicationMapStatisticsSelf_Ver2/s/TTL => .[[:digit:]]*/TTL => ${APPMAPSTATSELFV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-update-ttl.hbase
sed -i "/HostApplicationMap_Ver2/s/TTL => .[[:digit:]]*/TTL => ${HOSTAPPMAPV2_TTL:-5184000}/g" ${BASE_DIR}/hbase-update-ttl.hbase

exec "$@"
