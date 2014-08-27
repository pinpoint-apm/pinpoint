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
flush 'ApplicationMapStatisticsSelf'
flush 'ApplicationStatistics'
flush 'HostApplicationMap'
flush 'HostApplicationMap_Ver2'

EOF