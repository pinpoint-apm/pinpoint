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

COLLECTOR_DIR=$BASE_DIR/collector

LOGS_DIR=$BASE_DIR/logs
LOG_FILE=examples.collector.log

PID_DIR=$BASE_DIR/logs/pid
PID_FILE=examples.collector.pid

COLLECTOR_IDENTIFIER=pinpoint-example-collector
IDENTIFIER=maven.pinpoint.identifier=$COLLECTOR_IDENTIFIER

function func_check_process
{
        echo "---check $COLLECTOR_IDENTIFIER process status.---"

        PID=`cat $PID_DIR/$PID_FILE 2>/dev/null`
        process_status=0
        if [ ! -z $PID ]; then
                process_status=`ps aux | grep $PID | grep $IDENTIFIER | grep -v grep | wc -l`

                if [ ! $process_status -eq 0 ]; then
                        echo "already running $COLLECTOR_IDENTIFIER process. pid=$PID."
                fi
        fi

        if [ $process_status -eq 0 ]; then
                process_status=`ps aux | grep $IDENTIFIER | grep -v grep | wc -l`

                if [ ! $process_status -eq 0 ]; then
                        echo "already running $COLLECTOR_IDENTIFIER process. $IDENTIFIER."
                fi
        fi

        if [ ! $process_status -eq 0 ]; then
                echo "already running $COLLECTOR_IDENTIFIER process. bye."
                exit 1
        fi
}

function func_init_log
{
        echo "---initilize $COLLECTOR_IDENTIFIER logs.---"

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

function func_start_pinpoint_collector
{
        PID=`nohup mvn -f $COLLECTOR_DIR/pom.xml clean package tomcat7:run -D$IDENTIFIER > $LOGS_DIR/$LOG_FILE 2>&1 & echo $!`
        echo $PID > $PID_DIR/$PID_FILE

        echo "---$COLLECTOR_IDENTIFIER initialization started. pid=$PID.---"

        process_tcp_port_num=`netstat -anp 2>/dev/null | grep $PID | grep tcp | wc -l `
        process_udp_port_num=`netstat -anp 2>/dev/null | grep $PID | grep udp | wc -l `
        end_count=0

        while [[ $process_tcp_port_num -ne 2 || $process_udp_port_num -ne 2 ]]
        do
                echo "starting $COLLECTOR_IDENTIFIER. wait($end_count/40)."
                if [ $end_count -ge 40 ]; then
                        break
                fi

                sleep 3
                end_count=`expr $end_count + 1`

                process_tcp_port_num=`netstat -anp 2>/dev/null | grep $PID | grep tcp | wc -l `
                process_udp_port_num=`netstat -anp 2>/dev/null | grep $PID | grep udp | wc -l `
        done

        if [[ $process_tcp_port_num -ne 2 || $process_udp_port_num -ne 2 ]]; then
                echo "---$WEB_IDENTIFIER initialization failed. pid=$PID.---"
                kill -9 $PID
        else
                echo "---$WEB_IDENTIFIER initialization completed. pid=$PID.---"
                tail -f  $LOGS_DIR/$LOG_FILE
        fi
}

func_check_process
func_init_log
func_start_pinpoint_collector
