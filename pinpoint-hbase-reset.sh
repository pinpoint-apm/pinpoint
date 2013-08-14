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
disable 'ApplicationMapStatisticsCaller'
disable 'ApplicationMapStatisticsCallee'
disable 'ApplicationStatistics'
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
drop 'ApplicationMapStatisticsCaller'
drop 'ApplicationMapStatisticsCallee'
drop 'ApplicationStatistics'
drop 'HostApplicationMap'

create 'AgentInfo', { NAME => 'Info' }
create 'AgentIdApplicationIndex', { NAME => 'Application' }
create 'ApplicationIndex', { NAME => 'Agents' }
create 'SqlMetaData', { NAME => 'Sql' }
create 'ApiMetaData', { NAME => 'Api' }
create 'TraceIndex', { NAME => 'I', TTL => 604800  }
create 'ApplicationTraceIndex', { NAME => 'I', TTL => 604800  }
create 'Traces', { NAME => 'S', TTL => 604800  }, { NAME => 'A', TTL => 604800  }, { NAME => 'T', TTL => 604800  }
create 'SystemInfo', { NAME => 'JVM', TTL => 604800  }
create 'ApplicationMapStatisticsCaller', { NAME => 'C', TTL => 604800, VERSION => 1 }
create 'ApplicationMapStatisticsCallee', { NAME => 'C', TTL => 604800, VERSION => 1 }
create 'ApplicationStatistics', { NAME => 'C', TTL => 604800, VERSION => 1 }
create 'HostApplicationMap', { NAME => 'M', TTL => 604800, VERSION => 1 }

list
EOF