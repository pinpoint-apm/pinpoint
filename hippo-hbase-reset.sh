exec ./hbase shell <<EOF
disable 'Servers'
disable 'ApplicationsIndex'
disable 'RootTraceIndex'
disable 'TraceIndex'
disable 'Traces'
disable 'SystemInfo'
disable 'RootTraceIndex'

drop 'Servers'
drop 'ApplicationNames'
drop 'RootTraceIndex'
drop 'TraceIndex'
drop 'Traces'
drop 'SystemInfo'
drop 'RootTraceIndex'

create 'Servers', { NAME => 'Agents' }
create 'ApplicationsIndex', { NAME => 'Agents' }
create 'RootTraceIndex', { NAME => 'Trace' }
create 'TraceIndex', { NAME => 'Trace' }
create 'Traces', { NAME => 'Span' }, { NAME => 'Annotation' }
create 'SystemInfo', { NAME => 'JVM' }
create 'RootTraceIndex', { NAME => 'Trace' }

list
EOF