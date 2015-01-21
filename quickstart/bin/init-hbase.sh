#!/usr/bin/env bash

quickstart_bin=`dirname "${BASH_SOURCE-$0}"`
quickstart_bin=`cd "$quickstart_bin">/dev/null; pwd`
quickstart_base=$quickstart_bin/..
quickstart_base=`cd "$quickstart_base">/dev/null; pwd`

"$quickstart_bin"/../hbase/hbase/bin/hbase shell $quickstart_base/conf/hbase/init-hbase.txt