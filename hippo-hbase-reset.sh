exec ./hbase shell <<EOF
disable 'Servers'
disable 'ApplicationIndex'
disable 'RootTraceIndex'
disable 'TraceIndex'
disable 'Traces'
disable 'SystemInfo'
disable 'RootTraceIndex'

drop 'Servers'
drop 'ApplicationIndex'
drop 'RootTraceIndex'
drop 'TraceIndex'
drop 'Traces'
drop 'SystemInfo'
drop 'RootTraceIndex'

create 'Servers', { NAME => 'Agents' }
create 'ApplicationIndex', { NAME => 'Agents' }
create 'RootTraceIndex', { NAME => 'Trace' }
create 'TraceIndex', { NAME => 'Trace' }
create 'Traces', { NAME => 'Span' }, { NAME => 'Annotation' }
create 'SystemInfo', { NAME => 'JVM' }
create 'RootTraceIndex', { NAME => 'Trace' }

list
EOF