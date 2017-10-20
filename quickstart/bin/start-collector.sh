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
COLLECTOR_DIR=$BASE_DIR/collector

CONF_DIR=$BASE_DIR/conf
CONF_FILE=quickstart.properties

LOGS_DIR=$BASE_DIR/logs
LOG_FILE=quickstart.collector.log

PID_DIR=$BASE_DIR/logs/pid
PID_FILE=quickstart.collector.pid

COLLECTOR_IDENTIFIER=pinpoint-quickstart-collector
IDENTIFIER=maven.pinpoint.identifier=$COLLECTOR_IDENTIFIER

UNIT_TIME=5
CHECK_COUNT=36
CLOSE_WAIT_TIME=`expr $UNIT_TIME \* $CHECK_COUNT`

PROPERTIES=`cat $CONF_DIR/$CONF_FILE 2>/dev/null`
KEY_VERSION="quickstart.version"
KEY_PORT="quickstart.collector.port"

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
        echo "---check $COLLECTOR_IDENTIFIER process status.---"

        pid=`cat $PID_DIR/$PID_FILE 2>/dev/null`
        process_status=0
        if [ ! -z $pid ]; then
                process_status=`ps aux | grep $pid | grep $IDENTIFIER | grep -v grep | wc -l`

                if [ ! $process_status -eq 0 ]; then
                        echo "already running $COLLECTOR_IDENTIFIER process. pid=$pid."
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
        echo "---initialize $COLLECTOR_IDENTIFIER logs.---"

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

function func_check_running_pinpoint_collector
{
	port=$( func_read_properties "$KEY_PORT" )

        if [[ "$OS_TYPE" == 'mac' ]]; then
		main_port_num=`lsof -p $pid | grep TCP | grep $port | wc -l `
                process_tcp_port_num=`lsof -p $pid | grep TCP | wc -l `
                process_udp_port_num=`lsof -p $pid |  grep UDP | wc -l `
        else
		main_port_num=`netstat -anp 2>/dev/null | grep $pid/java | grep tcp | grep $port | wc -l `
                process_tcp_port_num=`netstat -anp 2>/dev/null | grep $pid/java | grep tcp | wc -l `
                process_udp_port_num=`netstat -anp 2>/dev/null | grep $pid/java | grep udp | wc -l `
        fi
	
        if [[ $main_port_num -eq 1 && $process_tcp_port_num -ge 2 && $process_udp_port_num -ge 2 ]]; then
                echo "true"
        else
                echo "false"
        fi
}

function func_start_pinpoint_collector
{
		version=$( func_read_properties "$KEY_VERSION" )
        pid=`nohup ${bin}/../../mvnw -f $COLLECTOR_DIR/pom.xml clean package tomcat7:run -D$IDENTIFIER -Dmaven.pinpoint.version=$version > $LOGS_DIR/$LOG_FILE 2>&1 & echo $!`
        echo $pid > $PID_DIR/$PID_FILE

        echo "---$COLLECTOR_IDENTIFIER initialization started. pid=$pid.---"

        end_count=0
	check_running_pinpoint_collector=$( func_check_running_pinpoint_collector )
        while [ "$check_running_pinpoint_collector" == "false" ]; do
                wait_time=`expr $end_count \* $UNIT_TIME`
                echo "starting $COLLECTOR_IDENTIFIER. $wait_time /$CLOSE_WAIT_TIME sec(close wait limit)."
				
                if [ $end_count -ge $CHECK_COUNT ]; then
                        break
                fi

                sleep $UNIT_TIME
                end_count=`expr $end_count + 1`
				
               check_running_pinpoint_collector=$( func_check_running_pinpoint_collector )
        done

        if [[ "$check_running_pinpoint_collector" == "true" ]]; then
                echo "---$COLLECTOR_IDENTIFIER initialization completed. pid=$pid.---"
                tail -f  $LOGS_DIR/$LOG_FILE
        else
                echo "---$COLLECTOR_IDENTIFIER initialization failed. pid=$pid.---"
                kill -9 $pid
        fi
}

func_check_process
func_init_log
func_start_pinpoint_collector
