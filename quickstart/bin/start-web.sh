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
WEB_DIR=$BASE_DIR/web

CONF_DIR=$BASE_DIR/conf
CONF_FILE=quickstart.properties

LOGS_DIR=$BASE_DIR/logs
LOG_FILE=quickstart.web.log

PID_DIR=$BASE_DIR/logs/pid
PID_FILE=quickstart.web.pid

WEB_IDENTIFIER=pinpoint-quickstart-web
IDENTIFIER=maven.pinpoint.identifier=$WEB_IDENTIFIER

UNIT_TIME=5
CHECK_COUNT=36
CLOSE_WAIT_TIME=`expr $UNIT_TIME \* $CHECK_COUNT`

PROPERTIES=`cat $CONF_DIR/$CONF_FILE 2>/dev/null`
KEY_VERSION="quickstart.version"
KEY_PORT="quickstart.web.port"

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
        echo "---check $WEB_IDENTIFIER process status.---"
        pid=`cat $PID_DIR/$PID_FILE 2>/dev/null`
        process_status=0
        if [ ! -z $pid ]; then
                process_status=`ps aux | grep $pid | grep $IDENTIFIER | grep -v grep | wc -l`

                if [ ! $process_status -eq 0 ]; then
                        echo "already running $WEB_IDENTIFIER process. pid=$pid."
                fi
        fi

        if [ $process_status -eq 0 ]; then
                process_status=`ps aux | grep $IDENTIFIER | grep -v grep | wc -l`

                if [ ! $process_status -eq 0 ]; then
                        echo "already running $WEB_IDENTIFIER process. $IDENTIFIER."
                fi
        fi

        if [ ! $process_status -eq 0 ]; then
                echo "already running $WEB_IDENTIFIER process. bye."
                exit 1
        fi
}

function func_init_log
{
        echo "---initialize $WEB_IDENTIFIER logs.---"

        if [ ! -d $LOGS_DIR ]; then
                echo "mkdir $LOGS_DIR"
                mkdir $LOGS_DIR
        fi

        if [ ! -d $PID_DIR ]; then
                echo "mkdir $PID_DIR"
                mkdir $PID_DIR
        fi

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

function func_start_pinpoint_web
{
	version=$( func_read_properties "$KEY_VERSION" )
	port=$( func_read_properties "$KEY_PORT" ) 
        pid=`nohup ${bin}/../../mvnw -f $WEB_DIR/pom.xml clean package tomcat7:run -D$IDENTIFIER -Dmaven.pinpoint.version=$version > $LOGS_DIR/$LOG_FILE 2>&1 & echo $!`
	check_url="http://localhost:"$port"/serverTime.pinpoint"
        echo $pid > $PID_DIR/$PID_FILE

        echo "---$WEB_IDENTIFIER initialization started. pid=$pid.---"

        process_status=`curl $check_url 2>/dev/null | grep 'currentServerTime'`
        end_count=0

        while [ -z $process_status ]; do
                wait_time=`expr $end_count \* $UNIT_TIME`
                echo "starting $WEB_IDENTIFIER. $wait_time /$CLOSE_WAIT_TIME sec(close wait limit)."

                if [ $end_count -ge $CHECK_COUNT ]; then
                        break
                fi

                sleep $UNIT_TIME
                end_count=`expr $end_count + 1`
                process_status=`curl $check_url 2>/dev/null | grep 'currentServerTime'`
        done

        if [ -z $process_status ]; then
                echo "---$WEB_IDENTIFIER initialization failed. pid=$pid.---"
                kill -9 $pid
        else
                echo "---$WEB_IDENTIFIER initialization completed. pid=$pid.---"
                tail -f  $LOGS_DIR/$LOG_FILE
        fi
}

func_check_process
func_init_log
func_start_pinpoint_web
