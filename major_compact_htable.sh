#!/bin/sh
exec hbase shell <<EOF

major_compact 'AgentInfo'
major_compact 'AgentStat'
major_compact 'ApplicationIndex'

major_compact 'StringMetaData'
major_compact 'SqlMetaData'
major_compact 'ApiMetaData'

major_compact 'ApplicationTraceIndex'
major_compact 'Traces'

major_compact 'ApplicationMapStatisticsCaller'
major_compact 'ApplicationMapStatisticsCallee'
major_compact 'ApplicationStatistics'
major_compact 'HostApplicationMap'


major_compact 'AgentInfo'
major_compact 'AgentStat'
major_compact 'ApplicationIndex'

major_compact 'StringMetaData'
major_compact 'SqlMetaData'
major_compact 'ApiMetaData'

major_compact 'ApplicationTraceIndex'
major_compact 'Traces'

major_compact 'ApplicationMapStatisticsCaller'
major_compact 'ApplicationMapStatisticsCallee'
major_compact 'ApplicationMapStatisticsSelf'

major_compact 'ApplicationStatistics'
major_compact 'HostApplicationMap'
major_compact 'HostApplicationMap_Ver2'

EOF