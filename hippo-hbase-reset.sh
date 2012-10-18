exec ./hbase shell <<EOF
disable 'Servers'
disable 'TraceIndex'
disable 'Traces'
disable 'SystemInfo'

drop 'Servers'
drop 'TraceIndex'
drop 'Traces'
drop 'SystemInfo'

create 'Servers', { NAME => 'Agents' }
create 'TraceIndex', { NAME => 'Trace' }
create 'Traces', { NAME => 'Span' }, { NAME => 'Annotation' }
create 'SystemInfo', { NAME => 'JVM' }

list
EOF