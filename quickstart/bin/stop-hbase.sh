#!/usr/bin/env bash

this="${BASH_SOURCE-$0}"
while [ -h "$this" ]; do
  ls=`ls -ld "$this"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '.*/.*' > /dev/null; then
    this="$link"
  else
    this=`dirname "$this"`/"$link"
  fi
done

# convert relative path to absolute path
bin=`dirname "$this"`
script=`basename "$this"`
bin=`cd "$bin">/dev/null; pwd`
this="$bin/$script"

CURRENT_DIR=$bin
BASE_DIR=`dirname "$bin"`
HBASE_DIR=$BASE_DIR/hbase

function func_check_hbase_installation
{
    if [[ -d $HBASE_DIR/hbase && -L $HBASE_DIR/hbase ]]; then
        echo "true"
    else
        echo "false"
    fi
}

function func_stop_hbase
{
    hbase_installed=$(func_check_hbase_installation)
    if [[ "$hbase_installed" == "true" ]]; then
        cd $HBASE_DIR/hbase/bin
        ./stop-hbase.sh
        echo "hbase stopped."
        cd $CURRENT_DIR
    else
        echo "Cannot find hbase installation. Exiting."
    fi
}

func_stop_hbase
