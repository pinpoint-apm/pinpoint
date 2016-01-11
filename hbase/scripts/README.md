# Scripts
Helper scripts are available to help set up or maintain Pinpoint.

## HBase
To run the HBase scripts, feed in the `.hbase` file into the HBase shell.

* *hbase-create.hbase* - create tables necessary for Pinpoint
* *hbase-create-snappy.hbase* - create tables necessary for Pinpoint using snappy compression (*requires [snappy](http://code.google.com/p/snappy)*)
* *hbase-drop.hbase* - disables and drops all tables created for Pinpoint
* *hbase-flush-table.hbase* - flushes all tables
* *hbase-major-compact-htable.hbase* - major compacts all tables

For example, you can create Pinpoint tables by running the following line from shell:

`$HBASE_HOME/bin/hbase shell $PATH_TO_SCRIPTS/hbase-create.hbase`

*hbase-flush-table.hbase*, and *hbase-major-compact-htable.hbase* are there purely for maintenance.

## HBase Table Description

* ApplicationIndex, HostApplicationMap  : Tables for applicationIds and agentIds registered under them
* AgentInfo : Table for basic agent information ex) ip, hostname agentversion, start time, etc
* AgentStat : Table for agent’s statistical data ex) cpuload, gc, heap etc
* AgentLifeCycle : Table for agent’s life cycle data.
* AgentEvent : Table for various agent events ex) request for thread dump, etc
* ApiMetaData : Meta-table for method information
* SqlMetaData : Meta-table for sql statements
* StringMetaData : Meta-table for string values  ex) method arguments, exception names, etc
* ApplicationTraceIndex : Index table for trace data
* Traces : Table for traced transactions
* ApplicationMapStatisticsCaller, ApplicationMapStatisticsCallee, ApplicationMapStatisticsSelf : Table for storing rpc statistics between various agents

## About TTL config
- You do not have to use the TTL value set in the HBase script files. You may set it to any desired period to hold data that fits your specific needs/environment.
- The ratio of TTL values do not need to be strictly followed as well. The reason that some table's TTL value is higher is because they would contain data that would affect data in other tables if deleted.
ex) We set a high TTL value for ApiMetaData table, because if the TTL value is shorter than an agent’s lifespan and the api metadata is deleted, trace information cannot be reconstructed properly.
