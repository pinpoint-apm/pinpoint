# Running QuickStart on Windows

## Starting
Download Pinpoint with `git clone https://github.com/pinpoint-apm/pinpoint.git` or [download](https://github.com/pinpoint-apm/pinpoint/archive/master.zip) the project as a zip file and unzip.

Install Pinpoint by running `mvnw.cmd install -DskipTests=true`

### Notice
If you run QuickStart's cmd file on Windows, you must run it at `quickstart\bin` directory.

If you want to run it in a different directory, you need to set the absolute path of the `quickstart\bin` directory in the `QUICKSTART_BIN_PATH` environment variable.

### Install & Start HBase
Download `HBase-1.0.x-bin.tar.gz` from [Apache download site](http://apache.mirror.cdnetworks.com/hbase/)) and unzip it to `quickstart\hbase` directory.

Rename the unzipped directory to `hbase` so that the final HBase directory looks like `quickstart\hbase\hbase`.

**Start HBase** - Run `start-hbase.cmd`

**Initialize Tables** - Run `init-hbase.cmd`

### Start Pinpoint Daemons

**Collector** - Run `start-collector.cmd`

**TestApp** - Run `start-testapp.cmd`

**Web UI** - Run `start-web.cmd`

### Check Status
Once HBase and the 3 daemons are running, you may visit the following addresses to test out your very own Pinpoint instance.

* Web UI - http://localhost:28080
* TestApp - http://localhost:28081

You can feed trace data to Pinpoint using the TestApp UI, and check them using Pinpoint Web UI. TestApp registers itself as *test-agent* under *TESTAPP*.

## Stopping

**Web UI** - Run `stop-web.cmd`

**TestApp** - Run `stop-testapp.cmd`

**Collector** - Run `stop-collector.cmd`

**HBase** - Run `stop-hbase.cmd`
