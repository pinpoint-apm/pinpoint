@rem/*
@rem * Licensed to the Apache Software Foundation (ASF) under one
@rem * or more contributor license agreements.  See the NOTICE file
@rem * distributed with this work for additional information
@rem * regarding copyright ownership.  The ASF licenses this file
@rem * to you under the Apache License, Version 2.0 (the
@rem * "License"); you may not use this file except in compliance
@rem * with the License.  You may obtain a copy of the License at
@rem *
@rem *     http://www.apache.org/licenses/LICENSE-2.0
@rem *
@rem * Unless required by applicable law or agreed to in writing, software
@rem * distributed under the License is distributed on an "AS IS" BASIS,
@rem * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem * See the License for the specific language governing permissions and
@rem * limitations under the License.
@rem */

@rem included in all the hbase scripts with source command
@rem should not be executable directly
@rem also should not be passed any arguments, since we need original $*
@rem Modelled after $HADOOP_HOME/bin/hadoop-env.sh.

@rem Make sure java environment is set
@rem

if "%HBASE_BIN_PATH:~-1%" == "\" (
  set HBASE_BIN_PATH=%HBASE_BIN_PATH:~0,-1%
)

if "%1" == "--config" (
  set HBASE_CONF_DIR=%2
  shift
  shift
)

@rem the root of the hbase installation
if not defined HBASE_HOME (
   set HBASE_HOME=%HBASE_BIN_PATH%\..
)

@rem Allow alternate hbase conf dir location.
if not defined HBASE_CONF_DIR (
  set HBASE_CONF_DIR=%HBASE_HOME%\conf
)

@rem List of hbase regions servers.
if not defined HBASE_REGIONSERVERS (
  set HBASE_REGIONSERVERS=%HBASE_CONF_DIR%\regionservers
)

@rem List of hbase secondary masters.
if not defined HBASE_BACKUP_MASTERS (
  set HBASE_BACKUP_MASTERS=%HBASE_CONF_DIR%\backup-masters
)

@rem Source the hbase-env.sh.  Will have JAVA_HOME defined.
if exist "%HBASE_CONF_DIR%\hbase-env.cmd" (
  call "%HBASE_CONF_DIR%\hbase-env.cmd"
)

if not defined JAVA_HOME (
  echo Warning: JAVA_HOME environment variable is not set. Defaulting to c:\apps\java
  set JAVA_HOME=c:\apps\java
)

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo Error: JAVA_HOME is incorrectly set or could not find java at the location %JAVA_HOME%\bin\
  exit /B 2
)

set JAVA="%JAVA_HOME%\bin\java"

for %%i in (%0) do (
  if not defined HBASE_BIN_PATH (
    set HBASE_BIN_PATH=%%~dpi
  )
)