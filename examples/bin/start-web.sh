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

LOGS_DIR=$BASE_DIR/logs
LOG_FILE=examples.web.log

PID_DIR=$BASE_DIR/logs/pid
PID_FILE=examples.web.pid

WEB_IDENTIFIER=pinpoint-example-web
IDENTIFIER=maven.pinpoint.identifier=$WEB_IDENTIFIER

function func_check_process
{
        echo "---check $WEB_IDENTIFIER process status.---"
        PID=`cat $PID_DIR/$PID_FILE 2>/dev/null`
        process_status=0
        if [ ! -z $PID ]; then
                process_status=`ps aux | grep $PID | grep $IDENTIFIER | grep -v grep | wc -l`

                if [ ! $process_status -eq 0 ]; then
                        echo "already running $WEB_IDENTIFIER process. pid=$PID."
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
        echo "---initilize $WEB_IDENTIFIER logs.---"

        if [ ! -d $LOGS_DIR ]; then
                echo "mkdir $LOGS_DIR"
                mkdir $LOGS_DIR
        fi

        if [ ! -d $PID_DIR ]; then
                echo "mkdir $PID_DIR"
                mkdir $PID_DIR
        fi

        if [ -f  $LOGS_DIR/$LOG_FILE ]; then
                echo "clear log=$LOGS_DIR/$LOG_FILE."
                rm $LOGS_DIR/$LOG_FILE
        fi

        if [ -f  $PID_DIR/$PID_FILE ]; then
                echo "clear pid=$PID_DIR/$PID_FILE."
                rm $PID_DIR/$PID_FILE
        fi

        # will add validation log file.
}

function func_start_pinpoint_web
{
        PID=`nohup mvn -f $WEB_DIR/pom.xml clean package tomcat7:run -D$IDENTIFIER > $LOGS_DIR/$LOG_FILE 2>&1 & echo $!`
        echo $PID > $PID_DIR/$PID_FILE

        echo "---$WEB_IDENTIFIER initialization started. pid=$PID.---"

        process_status=`curl 'http://localhost:28080/serverTime.pinpoint' 2>/dev/null | grep 'currentServerTime'`
        end_count=0

        while [ -z $process_status ]
        do
                echo "starting $WEB_IDENTIFIER. wait($end_count/40)."

                if [ $end_count -ge 40 ]; then
                        break
                fi

                sleep 3
                end_count=`expr $end_count + 1`
                process_status=`curl 'http://localhost:28080/serverTime.pinpoint' 2>/dev/null | grep 'currentServerTime'`
        done

        if [ -z $process_status ]; then
                echo "---$WEB_IDENTIFIER initialization failed. pid=$PID.---"
                kill -9 $PID
        else
                echo "---$WEB_IDENTIFIER initialization completed. pid=$PID.---"
                tail -f  $LOGS_DIR/$LOG_FILE
        fi
}

func_check_process
func_init_log
func_start_pinpoint_web
