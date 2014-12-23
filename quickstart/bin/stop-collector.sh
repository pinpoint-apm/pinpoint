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

BASE_DIR=`dirname "$bin"`

LOGS_DIR=$BASE_DIR/logs
LOG_FILE=quickstart.collector.log

PID_DIR=$BASE_DIR/logs/pid
PID_FILE=quickstart.collector.pid

COLLECTOR_IDENTIFIER=pinpoint-quickstart-collector
IDENTIFIER=maven.pinpoint.identifier=$COLLECTOR_IDENTIFIER

function func_close_process
{
        echo "---$COLLECTOR_IDENTIFIER destroy started..---"

        PID=`cat $PID_DIR/$PID_FILE 2>/dev/null`
        if [ ! -z $PID ]; then
                echo "shutting down $COLLECTOR_IDENTIFIER. pid=$PID."
                ps aux | grep $PID | grep $IDENTIFIER | grep -v grep | awk '{print $2}' | xargs kill -9
        fi

        process_status=`ps aux | grep $IDENTIFIER | grep -v grep | wc -l`

        if [ ! $process_status -eq 0 ]; then
                echo "shutting down $COLLECTOR_IDENTIFIER. identifier=$COLLECTOR_IDENTIFIER."
                ps aux | grep $IDENTIFIER | grep -v grep | awk '{print $2}' | xargs kill -9
        fi

        process_status=`ps aux | grep $IDENTIFIER | grep -v grep | wc -l`

        if [ $process_status -eq 0 ]; then
                echo "---$COLLECTOR_IDENTIFIER destroy completed.---"
        else
                echo "---$COLLECTOR_IDENTIFIER destroy failed.---"
        fi
}

function func_clear_log
{
        echo "---clear $COLLECTOR_IDENTIFIER logs.---"

        if [ -f  $LOGS_DIR/$LOG_FILE ]; then
                echo "rm $LOGS_DIR/$LOG_FILE."
                rm $LOGS_DIR/$LOG_FILE
        fi

        if [ -f  $PID_DIR/$PID_FILE ]; then
                echo "rm $PID_DIR/$PID_FILE."
                rm $PID_DIR/$PID_FILE
        fi

        # will add validation log file.
}

func_close_process
func_clear_log
