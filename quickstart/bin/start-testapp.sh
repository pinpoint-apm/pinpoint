#!/usr/bin/env bash

UNAME=`uname`
OS_TYPE="linux";
if [[ "$UNAME" == "Darwin" ]]; then
        OS_TYPE="mac"
fi

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
PINPOINT_BASE_DIR=`dirname "$BASE_DIR"`
TESTAPP_DIR=$BASE_DIR/testapp

CONF_DIR=$BASE_DIR/conf
CONF_FILE=quickstart.properties
PINPOINT_CONF_FILE=pinpoint.config

LOGS_DIR=$BASE_DIR/logs
LOG_FILE=quickstart.testapp.log

PID_DIR=$BASE_DIR/logs/pid
PID_FILE=quickstart.testapp.pid

TESTAPP_IDENTIFIER=pinpoint-quickstart-testapp
IDENTIFIER=maven.pinpoint.identifier=$TESTAPP_IDENTIFIER

UNIT_TIME=5
CHECK_COUNT=36
CLOSE_WAIT_TIME=`expr $UNIT_TIME \* $CHECK_COUNT`

PROPERTIES=`cat $CONF_DIR/$CONF_FILE 2>/dev/null`
KEY_VERSION="quickstart.version"
KEY_CONTEXT_PATH="quickstart.testapp.context.path"
KEY_PORT="quickstart.testapp.port"

function func_read_properties
{
        key="^"$1"="

        for entry in $PROPERTIES;
        do
                value=`echo $entry | grep $key`

                if [ ! -z $value ]; then
                        echo $entry | cut -d '=' -f2
                        break
                fi
        done
}

function func_check_process
{
        echo "---check $TESTAPP_IDENTIFIER process status.---"

        pid=`cat $PID_DIR/$PID_FILE 2>/dev/null`
        process_status=0
        if [ ! -z $pid ]; then
                process_status=`ps aux | grep $pid | grep $IDENTIFIER | grep -v grep | wc -l`

                if [ ! $process_status -eq 0 ]; then
                        echo "already running $TESTAPP_IDENTIFIER process. pid=$pid."
                fi
        fi

        if [ $process_status -eq 0 ]; then
                process_status=`ps aux | grep $IDENTIFIER | grep -v grep | wc -l`

                if [ ! $process_status -eq 0 ]; then
                        echo "already running $TESTAPP_IDENTIFIER process. $IDENTIFIER."
                fi
        fi

        if [ ! $process_status -eq 0 ]; then
                echo "already running $TESTAPP_IDENTIFIER process. bye."
                exit 1
        fi
}

function func_init_log
{
        echo "---initialize $TESTAPP_IDENTIFIER logs.---"

        if [ ! -d $LOGS_DIR ]; then
                echo "mkdir $LOGS_DIR"
                mkdir $LOGS_DIR
        fi

        if [ ! -d $PID_DIR ]; then
                echo "mkdir $PID_DIR"
                mkdir $PID_DIR
        fi

        if [ -f  $LOGS_DIR/$LOG_FILE ]; then
                echo "rm LOGS_DIR/$LOG_FILE."
                rm $LOGS_DIR/$LOG_FILE
        fi

        if [ -f  $PID_DIR/$PID_FILE ]; then
                echo "rm $PID_DIR/$PID_FILE."
                rm $PID_DIR/$PID_FILE
        fi

        # will add validation log file.
}

function func_start_pinpoint_testapp
{
        version=$( func_read_properties "$KEY_VERSION" )

        context_path=$( func_read_properties "$KEY_CONTEXT_PATH" )
        if [ "$context_path" == "/" ]; then
                context_path=""
        fi
        port=$( func_read_properties "$KEY_PORT" )
        check_url="http://localhost:$port$context_path/getCurrentTimestamp.pinpoint"

        pid=`nohup ${bin}/../../mvnw -f $TESTAPP_DIR/pom.xml clean package cargo:run -D$IDENTIFIER -Dmaven.pinpoint.version=$version >> $LOGS_DIR/$LOG_FILE 2>&1 & echo $!`
        echo $pid > $PID_DIR/$PID_FILE

        echo "---$TESTAPP_IDENTIFIER initialization started. pid=$pid.---"

        end_count=0
        process_status=`curl $check_url 2>/dev/null | grep 'getCurrentTimestamp'`

        while [ -z $process_status ]; do
                wait_time=`expr $end_count \* $UNIT_TIME`
                echo "starting $TESTAPP_IDENTIFIER. $wait_time /$CLOSE_WAIT_TIME sec(close wait limit)."

                if [ $end_count -ge $CHECK_COUNT ]; then
                        break
                fi

                sleep $UNIT_TIME
                end_count=`expr $end_count + 1`
                process_status=`curl $check_url 2>/dev/null | grep 'getCurrentTimestamp'`

        done

        if [ -z $process_status ]; then
                echo "---$TESTAPP_IDENTIFIER initialization failed. pid=$pid.---"
                kill -9 $pid
        else
                echo "---$TESTAPP_IDENTIFIER initialization completed. pid=$pid.---"
                tail -f  $LOGS_DIR/$LOG_FILE
        fi

}


func_check_process
func_init_log
func_start_pinpoint_testapp
