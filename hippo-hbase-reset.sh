exec ./hbase shell <<EOF
disable 'Servers'
disable 'RootTraceIndex'
disable 'TraceIndex'
disable 'Traces'
disable 'SystemInfo'

drop 'Servers'
drop 'RootTraceIndex'
drop 'TraceIndex'
drop 'Traces'
drop 'SystemInfo'

create 'Servers', { NAME => 'Agents' }
create 'RootTraceIndex', { NAME => 'Trace' }
create 'TraceIndex', { NAME => 'Trace' }
create 'Traces', { NAME => 'Span' }, { NAME => 'Annotation' }
create 'SystemInfo', { NAME => 'JVM' }

list
EOF