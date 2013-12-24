#!/bin/sh
exec hbase shell <<EOF

flush 'AgentInfo'
flush 'AgentStat'

flush 'ApplicationIndex'

flush 'StringMetaData'
flush 'SqlMetaData'
flush 'ApiMetaData'

flush 'ApplicationMapStatisticsCaller'
flush 'ApplicationMapStatisticsCallee'
flush 'ApplicationStatistics'
flush 'HostApplicationMap'

EOF