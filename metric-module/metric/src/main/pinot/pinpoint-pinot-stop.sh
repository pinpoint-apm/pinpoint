#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------------------
# Pinot Cluster with Kafka stop script.
# -----------------------------------------------------------------------------
#

kafka_2.13-2.6.0/bin/kafka-server-stop.sh
process_id=$!
wait $process_id
echo "stopped kafka-server"

pid=$(pgrep -f PinotAdministrator\ StartController)
kill $pid
echo "stopped pinot-controller"
pid=$(pgrep -f PinotAdministrator\ StartBroker)
kill $pid
echo "stopped pinot-broker"
pid=$(pgrep -f PinotAdministrator\ StartServer)
kill $pid
echo "stopped pinot-server"
pid=$(pgrep -f PinotAdministrator\ StartZookeeper)
kill $pid
echo "stopped pinot-zookeeper"
#apache-pinot-incubating-0.6.0-bin/bin/pinot-admin.sh StopProcess -controller -server -broker