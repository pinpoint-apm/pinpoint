exec ./hbase shell <<EOF
disable 'AgentIdApplicationIndex'
disable 'ApplicationIndex'
disable 'RootTraceIndex'
disable 'TraceIndex'
disable 'ApplicationTraceIndex'
disable 'Traces'
disable 'SystemInfo'
disable 'TerminalStatistics'
disable 'SqlMetaData'

drop 'AgentIdApplicationIndex'
drop 'ApplicationIndex'
drop 'RootTraceIndex'
drop 'TraceIndex'
drop 'ApplicationTraceIndex'
drop 'Traces'
drop 'SystemInfo'
drop 'TerminalStatistics'
drop 'SqlMetaData'

create 'AgentIdApplicationIndex', { NAME => 'Application' }
create 'ApplicationIndex', { NAME => 'Agents' }
create 'RootTraceIndex', { NAME => 'Trace' }
create 'TraceIndex', { NAME => 'Trace' }
create 'ApplicationTraceIndex', { NAME => 'Trace' }
create 'Traces', { NAME => 'Span' }, { NAME => 'Annotation' }, { NAME => 'TerminalSpan' }
create 'SystemInfo', { NAME => 'JVM' }
create 'TerminalStatistics', { NAME => 'Counter' }
create 'SqlMetaData', { NAME => 'Sql' }

list
EOF