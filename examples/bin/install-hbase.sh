#!/usr/bin/env bash

HBASE_VERSION=hbase-0.94.24
HBASE_FILE=$HBASE_VERSION.tar.gz
HBASE_DL_URL=http://apache.mirror.cdnetworks.com/hbase/$HBASE_VERSION/$HBASE_FILE

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

BASE_DIR=`dirname "$bin"`

CONF_DIR=$BASE_DIR/conf
HBASE_DIR=$BASE_DIR/hbase

# check if HBase exists
if [ -d $HBASE_DIR/$HBASE_VERSION ]; then
    echo "HBase already installed. Exiting."
    exit 0
fi

# initialize and start HBase standalone
mkdir $HBASE_DIR
cd $HBASE_DIR
wget $HBASE_DL_URL 2>/dev/null || curl -O $HBASE_DL_URL
tar xzf $HBASE_FILE
rm $HBASE_FILE
cp $CONF_DIR/hbase/hbase-site.xml $HBASE_DIR/$HBASE_VERSION/conf/
cd $HBASE_DIR/$HBASE_VERSION/bin

chmod +x start-hbase.sh
./start-hbase.sh

