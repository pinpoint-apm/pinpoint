# multi-table.sh
This script adds schemas, real-time tables, and offline tables to a Pinot cluster.

### Steps
1. **Edit Template Files**  
   If necessary, edit the template files. 
   Template files should be located in the execution directory.
2. **Run Script**  
   Run `multi-table.sh` to create configuration files and add tables to the Pinot cluster.  
   The script requires `START_INDEX`, `END_INDEX`, and `CONTROLLER_ADDRESS` as arguments.

**Note**:
- If Kafka broker or topic is not available for a real-time table, the table will not be added to the Pinot cluster.
- Make sure to check Kafka broker list in `streamConfigs`:`stream.kafka.broker.list` within `template_REALTIME.json`.
- The script generates configuration files by substituting `${NUM}` from template files.


### Arguments
- `START_INDEX`: The start index for table creation. Must be non-negative integers up to 2 digits.
- `END_INDEX`: The end index for table creation. Must be non-negative integers up to 2 digits.
- `CONTROLLER_ADDRESS`: The address of the Pinot cluster controller.

**Note**:
- must be `START_INDEX` <= `END_INDEX`.


## Example Commands
Command for adding 1 table (inspectorStatAgent00)
~~~
$ multi-table.sh 0 0 http://localhost:9000
~~~

Command for adding 3 table (inspectorStatAgent01 to inspectorStatAgent03) 
~~~
$ multi-table.sh 1 3 http://localhost:9000
~~~

