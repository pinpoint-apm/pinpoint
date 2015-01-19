@echo off
@rem/**
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

@rem Modelled after $HADOOP_HOME/bin/stop-hbase.sh.

@rem Stop hadoop hbase daemons.  Run this on master node.

setlocal

for %%i in (%0) do (
  if not defined HBASE_BIN_PATH (
    set HBASE_BIN_PATH=%%~dpi
  )
)

if "%HBASE_BIN_PATH:~-1%" == "\" (
  set HBASE_BIN_PATH=%HBASE_BIN_PATH:~0,-1%
)
set hbase-config-script=%HBASE_BIN_PATH%\hbase-config.cmd
call %hbase-config-script%

set distModeCommand=call %HBASE_BIN_PATH%\hbase.cmd org.apache.hadoop.hbase.util.HBaseConfTool hbase.cluster.distributed
for /f %%i in ('%distModeCommand%') do set distMode=%%i

if "%distMode%"=="false" (
  call %HBASE_BIN_PATH%\hbase.cmd master stop

) else (
  if "%distMode%"=="true" (
     @echo This is not implemented yet. Stay tuned.
  ) else (
    echo ERROR: Could not determine the startup mode.
  )
)

@rem -------------- End of main script --------------
endlocal
goto :eof