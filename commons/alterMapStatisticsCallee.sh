disable 'ApplicationMapStatisticsCallee'
alter 'ApplicationMapStatisticsCallee', {NAME => 'D', TTL => 5184000, VERSION => 1}
enable 'ApplicationMapStatisticsCallee'

disable 'ApplicationMapStatisticsCallee'
alter 'ApplicationMapStatisticsCallee', {NAME => 'D', TTL => 5184000, VERSION => 1, COMPRESSION => 'SNAPPY'}
enable 'ApplicationMapStatisticsCallee'
