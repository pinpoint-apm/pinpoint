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

create 'AgentInfo', { NAME => 'Info' }
create 'AgentIdApplicationIndex', { NAME => 'Application' }
create 'ApplicationIndex', { NAME => 'Agents' }
create 'SqlMetaData', { NAME => 'Sql' }
create 'ApiMetaData', { NAME => 'Api' }
create 'TraceIndex', { NAME => 'Trace' , TTL => 259200  }
create 'ApplicationTraceIndex', { NAME => 'Trace' , TTL => 259200  }
create 'Traces', { NAME => 'Span' , TTL => 259200  }, { NAME => 'Annotation' , TTL => 259200  }, { NAME => 'TerminalSpan' , TTL => 259200  }
create 'SystemInfo', { NAME => 'JVM' , TTL => 259200  }
create 'TerminalStatistics', { NAME => 'Counter' , TTL => 259200  }
create 'BusinessTransactionStatistics', { NAME => 'Normal' , TTL => 259200  }, { NAME => 'Slow' , TTL => 259200  }, { NAME => 'Error' , TTL => 259200  }
create 'ClientStatistics', { NAME => 'Counter' , TTL => 259200  }

list
EOF