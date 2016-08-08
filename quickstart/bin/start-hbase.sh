#!/usr/bin/env bash

HBASE_VERSION=hbase-1.0.3
HBASE_FILE=$HBASE_VERSION-bin.tar.gz
HBASE_DL_URL=http://apache.mirror.cdnetworks.com/hbase/$HBASE_VERSION/$HBASE_FILE
HBASE_ARCHIVE_DL_URL=http://archive.apache.org/dist/hbase/$HBASE_VERSION/$HBASE_FILE

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

CONF_DIR=$BASE_DIR/conf
HBASE_DIR=$BASE_DIR/hbase
DATA_DIR=$BASE_DIR/data

function func_check_hbase_installation
{
    if [ -d $HBASE_DIR/$HBASE_VERSION ]; then
        echo "true"
    else
        echo "false"
    fi
}

function func_download_hbase
{
    if type curl > /dev/null 2>&1; then
        if [[ `curl -s --head $HBASE_DL_URL | head -n 1 2>&1 | grep "HTTP/1.[01] [23].."` ]]; then
            curl -O $HBASE_DL_URL
        else
            curl -O $HBASE_ARCHIVE_DL_URL
        fi
        echo "true"
    elif type wget > /dev/null 2>&1; then
        if [[ `wget -S --spider $FAIL_URL 2>&1 | grep "HTTP/1.[01] [23].."` ]]; then 
            wget $HBASE_DL_URL
        else
            wget $HBASE_ARCHIVE_DL_URL
        fi
        echo "true"
    else
        echo "false"
    fi
}

function delete_data_directory
{
    if [ -d $DATA_DIR ]; then
        rm -r $DATA_DIR
    fi
}

function func_install_hbase
{
    if [ ! -d $HBASE_DIR ]; then
        mkdir $HBASE_DIR
    fi
    cd $HBASE_DIR
    echo "Downloading hbase..."
    download_successful=$(func_download_hbase)
    if [[ "$download_successful" == "false" ]]; then
        echo "hbase download failed - wget or curl required."
        echo "Exiting"
        exit 0
    fi
    delete_data_directory
    tar xzf $HBASE_FILE
    rm $HBASE_FILE
    if [ -h hbase ]; then
        unlink hbase
    fi
    ln -s $HBASE_VERSION hbase
    cp $CONF_DIR/hbase/hbase-site.xml $HBASE_DIR/$HBASE_VERSION/conf/
    chmod +x $HBASE_DIR/$HBASE_VERSION/bin/start-hbase.sh
}

function func_start_hbase
{
    hbase_already_installed=$(func_check_hbase_installation)
    if [[ "$hbase_already_installed" == "true" ]]; then
        echo "HBase already installed. Starting hbase..."
    else
        echo "Hbase not detected."
        func_install_hbase
    fi
    cd $HBASE_DIR/$HBASE_VERSION/bin
    ./start-hbase.sh
    cd $CURRENT_DIR
}

func_start_hbase
