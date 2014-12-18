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
AGENT_DIR=$BASE_DIR/agent
TESTAPP_DIR=$BASE_DIR/testapp

CONF_DIR=$BASE_DIR/conf
CONF_FILE=examples.properties
PINPOINT_CONF_FILE=pinpoint.config

LOGS_DIR=$BASE_DIR/logs
LOG_FILE=examples.testapp.log

PID_DIR=$BASE_DIR/logs/pid
PID_FILE=examples.testapp.pid

TESTAPP_IDENTIFIER=pinpoint-example-testapp
IDENTIFIER=maven.pinpoint.identifier=$TESTAPP_IDENTIFIER

UNIT_TIME=5
CHECK_COUNT=24
CLOSE_WAIT_TIME=`expr $UNIT_TIME \* $CHECK_COUNT`

PROPERTIES=`cat $CONF_DIR/$CONF_FILE 2>/dev/null`
KEY_PORT="example.testapp.port"

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

function func_get_original_path
{
        source_path=""
        source_path_canditate=`find $PINPOINT_BASE_DIR -name "pinpoint-agent"`
        for path in $source_path_canditate;
        do
                if [ -d $path ]; then
                        bootstap_jar_num=`find $path -name pinpoint-bootstrap-*.jar | grep $path/pinpoint-bootstrap | wc -l`
                        lib_num=`find $path -name lib | grep $path/lib | wc -l`

                        if [[ $bootstap_jar_num -eq 1 && $lib_num -eq 1 && -d $path ]]; then
                                source_path=$path
                                break
                        fi
                fi
        done

        echo $source_path
}

function func_init_agent
{
        echo "---initialize $TESTAPP_IDENTIFIER agent.---"

        source_path=$( func_get_original_path )
        if [ -z $source_path ]; then
                echo "illegal pinpoint-agent path($path)."
                exit 1
        fi

        if [ ! -f $CONF_DIR/$PINPOINT_CONF_FILE ]; then
                echo "illegal pinpoint-agent config file($CONF_DIR/$PINPOINT_CONF_FILE)."
                exit 1
        fi

        if [ -d $AGENT_DIR ]; then
                echo "rmdir $AGENT_DIR."
                rm -rf $AGENT_DIR
        fi

        echo "mkdir $AGENT_DIR"
        mkdir $AGENT_DIR

        if [ ! -d $AGENT_DIR ]; then
                echo "mkdir $AGENT_DIR fail."
                exit 1
        fi

        echo "copy pinpoint-agent from $source_path to $AGENT_DIR."
        cp -r $source_path/* $AGENT_DIR

        echo "copy pinpoint.config from $CONF_DIR/$PINPOINT_CONF_FILE to $AGENT_DIR."
        cp -f $CONF_DIR/$PINPOINT_CONF_FILE $AGENT_DIR

}

function func_init_log
{
        echo "---initilize $TESTAPP_IDENTIFIER logs.---"

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
        maven_opt=$MAVEN_OPTS
        pinpoint_agent=`find $AGENT_DIR -name pinpoint-bootstrap-*.jar`
        pinpoint_opt="-javaagent:$pinpoint_agent -Dpinpoint.agentId=test-agent -Dpinpoint.applicationName=TESTAPP"
        export MAVEN_OPTS=$pinpoint_opt

        port=$( func_read_properties "$KEY_PORT" )
        check_url="http://localhost:"$port"/getCurrentTimestamp.pinpoint"

        pid=`nohup mvn -f $TESTAPP_DIR/pom.xml clean package tomcat7:run -D$IDENTIFIER > $LOGS_DIR/$LOG_FILE 2>&1 & echo $!`
        echo $pid > $PID_DIR/$PID_FILE
        export MAVEN_OPTS=$maven_opt

        echo "---$TESTAPP_IDENTIFIER initialization started. pid=$pid.---"

        end_count=0
        process_status=`curl $check_url 2>/dev/null`

        until [[ $process_status =~ ^-?[0-9]+$ ]];
        do
                wait_time=`expr $end_count \* $UNIT_TIME`
                echo "starting $TESTAPP_IDENTIFIER. $wait_time sec/$CLOSE_WAIT_TIME sec(close wait limit)."

                if [ $end_count -ge $CHECK_COUNT ]; then
                        break
                fi

                sleep $UNIT_TIME
                end_count=`expr $end_count + 1`
                process_status=`curl $check_url 2>/dev/null`

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
func_init_agent
func_init_log
func_start_pinpoint_testapp
