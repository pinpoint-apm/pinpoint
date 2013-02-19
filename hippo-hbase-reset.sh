exec ./hbase shell <<EOF
disable 'AgentInfo'
disable 'AgentIdApplicationIndex'
disable 'ApplicationIndex'
disable 'RootTraceIndex'
disable 'AgentTraceIndex'
disable 'ApplicationTraceIndex'
disable 'Traces'
disable 'SystemInfo'
disable 'TerminalStatistics'
disable 'SqlMetaData'
disable 'ApiMetaData'
disable 'BusinessTransactionStatistics'

drop 'AgentInfo'
drop 'AgentIdApplicationIndex'
drop 'ApplicationIndex'
drop 'RootTraceIndex'
drop 'AgentTraceIndex'
drop 'ApplicationTraceIndex'
drop 'Traces'
drop 'SystemInfo'
drop 'TerminalStatistics'
drop 'SqlMetaData'
drop 'ApiMetaData'
drop 'BusinessTransactionStatistics'

create 'AgentInfo', { NAME => 'Info' , TTL => 259200 }
create 'AgentIdApplicationIndex', { NAME => 'Application', TTL => 259200 }
create 'ApplicationIndex', { NAME => 'Agents' , TTL => 259200 }
create 'RootTraceIndex', { NAME => 'Trace' , TTL => 259200  }
create 'AgentTraceIndex', { NAME => 'Trace' , TTL => 259200  }
create 'ApplicationTraceIndex', { NAME => 'Trace' , TTL => 259200  }
create 'Traces', { NAME => 'Span' , TTL => 259200  }, { NAME => 'Annotation' , TTL => 259200  }, { NAME => 'TerminalSpan' , TTL => 259200  }
create 'SystemInfo', { NAME => 'JVM' , TTL => 259200  }
create 'TerminalStatistics', { NAME => 'Counter' , TTL => 259200  }, { NAME => 'ErrorCount' , TTL => 259200  }
create 'SqlMetaData', { NAME => 'Sql' , TTL => 259200  }
create 'ApiMetaData', { NAME => 'Api' , TTL => 259200  }
create 'BusinessTransactionStatistics', { NAME => 'Normal' , TTL => 259200  }, { NAME => 'Slow' , TTL => 259200  }, { NAME => 'Error' , TTL => 259200  }

list
EOF