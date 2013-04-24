exec ./hbase shell <<EOF
disable 'AgentInfo'
disable 'AgentIdApplicationIndex'
disable 'ApplicationIndex'
disable 'SqlMetaData'
disable 'ApiMetaData'
disable 'TraceIndex'
disable 'ApplicationTraceIndex'
disable 'Traces'
disable 'SystemInfo'
disable 'TerminalStatistics'
disable 'BusinessTransactionStatistics'
disable 'ClientStatistics'
disable 'ApplicationMapStatisticsCaller'
disable 'ApplicationMapStatisticsCallee'
disable 'HostApplicationMap'

drop 'AgentInfo'
drop 'AgentIdApplicationIndex'
drop 'ApplicationIndex'
drop 'SqlMetaData'
drop 'ApiMetaData'
drop 'TraceIndex'
drop 'ApplicationTraceIndex'
drop 'Traces'
drop 'SystemInfo'
drop 'TerminalStatistics'
drop 'BusinessTransactionStatistics'
drop 'ClientStatistics'
drop 'ApplicationMapStatisticsCaller'
drop 'ApplicationMapStatisticsCallee'
drop 'HostApplicationMap'

create 'AgentInfo', { NAME => 'Info' }
create 'AgentIdApplicationIndex', { NAME => 'Application' }
create 'ApplicationIndex', { NAME => 'Agents' }
create 'SqlMetaData', { NAME => 'Sql' }
create 'ApiMetaData', { NAME => 'Api' }
create 'TraceIndex', { NAME => 'Trace' , TTL => 259200  }
create 'ApplicationTraceIndex', { NAME => 'Trace' , TTL => 259200  }
create 'Traces', { NAME => 'Span' , TTL => 259200  }, { NAME => 'Annotation' , TTL => 259200  }, { NAME => 'TerminalSpan' , TTL => 259200  }
create 'SystemInfo', { NAME => 'JVM' , TTL => 259200  }
create 'ApplicationMapStatisticsCaller', { NAME => 'Counter' }
create 'ApplicationMapStatisticsCallee', { NAME => 'Counter' }
create 'HostApplicationMap', { NAME => 'Map' }

list
EOF