exec ./hbase shell <<EOF
disable 'AgentIdApplicationIndex'
disable 'ApplicationIndex'
disable 'RootTraceIndex'
disable 'TraceIndex'
disable 'ApplicationTraceIndex'
disable 'Traces'
disable 'SystemInfo'

drop 'AgentIdApplicationIndex'
drop 'ApplicationIndex'
drop 'RootTraceIndex'
drop 'TraceIndex'
drop 'ApplicationTraceIndex'
drop 'Traces'
drop 'SystemInfo'

create 'AgentIdApplicationIndex', { NAME => 'Application' }
create 'ApplicationIndex', { NAME => 'Agents' }
create 'RootTraceIndex', { NAME => 'Trace' }
create 'TraceIndex', { NAME => 'Trace' }
create 'ApplicationTraceIndex', { NAME => 'Trace' }
create 'Traces', { NAME => 'Span' }, { NAME => 'Annotation' }
create 'SystemInfo', { NAME => 'JVM' }

list
EOF