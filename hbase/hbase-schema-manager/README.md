## HBase Schema Manager
You may use the HBase schema manager to help set up or maintain HBase tables necessary to run Pinpoint.

### Usage
To run, simply build and run the binary *jarfile* in the following form.

```
java -jar <jarfile> [--namespace=<namespace>] [--compression=<algorithm>] [--dry]
                    [--hbase.host=<hostname>] [--hbase.port=<port>] [--hbase.znodeParent=<parent>]
                    <command> [<args>]
```

For a list of commands and their specific usage, run `java -jar <jarfile> --dry help`. 

### How It Works
HBase schema manager reads schema definition files (provided by *hbase-schema-definition* module by default), which keep track of all changes that
are needed to be made to HBase in order to run Pinpoint. These are defined in groups called *change sets*, and this is
the minimum unit of execution for the schema manager.
 
In order to keep track of which change sets have been run, and which change sets are needed to run, the schema manager
creates a table named **SchemaChangeLog** to record all executed change sets in them as change logs. 