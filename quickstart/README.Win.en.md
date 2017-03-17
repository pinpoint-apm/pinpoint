# Running QuickStart on Windows

## Starting
Download Pinpoint with `git clone https://github.com/naver/pinpoint.git` or [download](https://github.com/naver/pinpoint/archive/master.zip) the project as a zip file and unzip.

Install Pinpoint by running `mvnw.cmd install -Dmaven.test.skip=true`

### Install & Start HBase
Download `HBase-1.0.x-bin.tar.gz` from [Apache download site](http://apache.mirror.cdnetworks.com/hbase/)) and unzip it to `quickstart\hbase` directory.

Rename the unzipped directory to `hbase` so that the final HBase directory looks like `quickstart\hbase\hbase`.

**Start HBase** - Run `quickstart\bin\start-hbase.cmd`

**Initialize Tables** - Run `quickstart\bin\init-hbase.cmd`

### Start Pinpoint Daemons

**Collector** - Run `quickstart\bin\start-collector.cmd`

**TestApp** - Run `quickstart\bin\start-testapp.cmd`

**Web UI** - Run `quickstart\bin\start-web.cmd`

### Check Status
Once HBase and the 3 daemons are running, you may visit the following addresses to test out your very own Pinpoint instance.

* Web UI - http://localhost:28080
* TestApp - http://localhost:28081

You can feed trace data to Pinpoint using the TestApp UI, and check them using Pinpoint Web UI. TestApp registers itself as *test-agent* under *TESTAPP*.

## Stopping

**Web UI** - Run `quickstart\bin\stop-web.cmd`

**TestApp** - Run `quickstart\bin\stop-testapp.cmd`

**Collector** - Run `quickstart\bin\stop-collector.cmd`

**HBase** - Run `quickstart\bin\stop-hbase.cmd`
