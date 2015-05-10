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