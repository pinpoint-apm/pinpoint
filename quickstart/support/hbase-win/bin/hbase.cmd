@echo off
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
@rem 
@rem The hbase command script.  Based on the hadoop command script putting
@rem in hbase classes, libs and configurations ahead of hadoop's.
@rem
@rem TODO: Narrow the amount of duplicated code.
@rem
@rem Environment Variables:
@rem
@rem   JAVA_HOME        The java implementation to use.  Overrides JAVA_HOME.
@rem
@rem   HBASE_CLASSPATH  Extra Java CLASSPATH entries.
@rem
@rem   HBASE_HEAPSIZE   The maximum amount of heap to use.
@rem                    Default is unset and uses the JVMs default setting
@rem                    (usually 1/4th of the available memory).
@rem
@rem   HBASE_OPTS       Extra Java runtime options.
@rem
@rem   HBASE_CONF_DIR   Alternate conf dir. Default is ${HBASE_HOME}/conf.
@rem
@rem   HBASE_ROOT_LOGGER The root appender. Default is INFO,console
@rem
@rem   JRUBY_HOME       JRuby path: $JRUBY_HOME\lib\jruby.jar should exist.
@rem                    Defaults to the jar packaged with HBase.
@rem
@rem   JRUBY_OPTS       Extra options (eg '--1.9') passed to the hbase shell.
@rem                    Empty by default.
@rem   HBASE_SHELL_OPTS Extra options passed to the hbase shell.
@rem                    Empty by default.


setlocal enabledelayedexpansion

for %%i in (%0) do (
  if not defined HBASE_BIN_PATH (
    set HBASE_BIN_PATH=%%~dpi
  )
)

if "%HBASE_BIN_PATH:~-1%" == "\" (
  set HBASE_BIN_PATH=%HBASE_BIN_PATH:~0,-1%
)

rem This will set HBASE_HOME, etc.
set hbase-config-script=%HBASE_BIN_PATH%\hbase-config.cmd
call "%hbase-config-script%" %*
if "%1" == "--config" (
  shift
  shift
)

rem Detect if we are in hbase sources dir
set in_dev_env=false

if exist "%HBASE_HOME%\target" set in_dev_env=true

rem --service is an internal option. used by MSI setup to install HBase as a windows service
if "%1" == "--service" (
   set service_entry=true
   shift
)

set hbase-command=%1
shift

@rem if no args specified, show usage
if "%hbase-command%"=="" (
  goto :print_usage
  endlocal
  goto :eof
)

set JAVA_HEAP_MAX=
set JAVA_OFFHEAP_MAX=

rem check envvars which might override default args
if defined HBASE_HEAPSIZE (
  set JAVA_HEAP_MAX=-Xmx%HBASE_HEAPSIZE%m
)

if defined HBASE_OFFHEAPSIZE (
  set JAVA_OFFHEAP_MAX=-XX:MaxDirectMemory=%HBASE_OFFHEAPSIZE%m
)

set CLASSPATH=%HBASE_CONF_DIR%;%JAVA_HOME%\lib\tools.jar

rem Add maven target directory
set cached_classpath_filename=%HBASE_HOME%\target\cached_classpath.txt
if "%in_dev_env%"=="true" (

  rem adding maven main classes to classpath
  for /f %%i in ('dir /b "%HBASE_HOME%\hbase-*"') do (
    if exist %%i\target\classes set CLASSPATH=!CLASSPATH!;%%i\target\classes
  )

  rem adding maven test classes to classpath
  rem For developers, add hbase classes to CLASSPATH
  for /f %%i in ('dir /b "%HBASE_HOME%\hbase-*"') do (
    if exist %%i\target\test-classes set CLASSPATH=!CLASSPATH!;%%i\target\test-classes
  )

  if not exist "%cached_classpath_filename%" (
    echo "As this is a development environment, we need %cached_classpath_filename% to be generated from maven (command: mvn install -DskipTests)"
    goto :eof
  )

  for /f "delims=" %%i in ('type "%cached_classpath_filename%"') do set CLASSPATH=%CLASSPATH%;%%i
)

@rem For releases add hbase webapps to CLASSPATH
@rem Webapps must come first else it messes up Jetty
if exist "%HBASE_HOME%\hbase-webapps" (
  set CLASSPATH=%CLASSPATH%;%HBASE_HOME%
)

if exist "%HBASE_HOME%\target\hbase-webapps" (
  set CLASSPATH=%CLASSPATH%;%HBASE_HOME%\target
)

for /F %%f in ('dir /b "%HBASE_HOME%\hbase*.jar" 2^>nul') do (
  if not "%%f:~-11"=="sources.jar" (
    set CLASSPATH=!CLASSPATH!;%HBASE_HOME%\%%f
  )
)

@rem Add libs to CLASSPATH
if exist "%HBASE_HOME%\lib" (
  set CLASSPATH=!CLASSPATH!;%HBASE_HOME%\lib\*
)

@rem Add user-specified CLASSPATH last
if defined HBASE_CLASSPATH (
  set CLASSPATH=%CLASSPATH%;%HBASE_CLASSPATH%
)

@rem Default log directory and file
if not defined HBASE_LOG_DIR (
  set HBASE_LOG_DIR=%HBASE_HOME%\logs
)

if not defined HBASE_LOGFILE (
  set HBASE_LOGFILE=hbase.log
)

set JAVA_PLATFORM=

rem If avail, add Hadoop to the CLASSPATH and to the JAVA_LIBRARY_PATH
set PATH=%PATH%;"%HADOOP_HOME%\bin"
set HADOOP_IN_PATH=hadoop.cmd

if exist "%HADOOP_HOME%\bin\%HADOOP_IN_PATH%" (
  set hadoopCpCommand=call %HADOOP_IN_PATH% classpath 2^>nul
  for /f "eol= delims=" %%i in ('!hadoopCpCommand!') do set CLASSPATH_FROM_HADOOP=%%i
  if defined CLASSPATH_FROM_HADOOP (
    set CLASSPATH=%CLASSPATH%;!CLASSPATH_FROM_HADOOP!
  )
  set HADOOP_CLASSPATH=%CLASSPATH%

  set hadoopJLPCommand=call %HADOOP_IN_PATH% org.apache.hadoop.hbase.util.GetJavaProperty java.library.path 2^>nul
  for /f "eol= delims=" %%i in ('!hadoopJLPCommand!') do set HADOOP_JAVA_LIBRARY_PATH=%%i
  if not defined JAVA_LIBRARY_PATH (
     set JAVA_LIBRARY_PATH=!HADOOP_JAVA_LIBRARY_PATH!
  ) else (
     set JAVA_LIBRARY_PATH=%JAVA_LIBRARY_PATH%;!HADOOP_JAVA_LIBRARY_PATH!
  )
)

if exist "%HBASE_HOME%\build\native" (
  set platformCommand=call %JAVA% -classpath "%CLASSPATH%" org.apache.hadoop.util.PlatformName
  for /f %%i in ('!platformCommand!') do set JAVA_PLATFORM=%%i
  set _PATH_TO_APPEND=%HBASE_HOME%\build\native\!JAVA_PLATFORM!;%HBASE_HOME%\build\native\!JAVA_PLATFORM!\lib
  if not defined JAVA_LIBRARY_PATH (
    set JAVA_LIBRARY_PATH=!_PATH_TO_APPEND!
  ) else (
    set JAVA_LIBRARY_PATH=%JAVA_LIBRARY_PATH%;!_PATH_TO_APPEND!
  )
)

rem This loop would set %hbase-command-arguments%
set _hbasearguments=
:MakeCmdArgsLoop
  if [%1]==[] goto :EndLoop 

  if not defined _hbasearguments (
    set _hbasearguments=%1
  ) else (
    set _hbasearguments=!_hbasearguments! %1
  )
  shift
goto :MakeCmdArgsLoop 
:EndLoop 

set hbase-command-arguments=%_hbasearguments%

@rem figure out which class to run
set corecommands=shell master regionserver thrift thrift2 rest avro hlog wal hbck hfile zookeeper zkcli upgrade mapredcp
for %%i in ( %corecommands% ) do (
  if "%hbase-command%"=="%%i" set corecommand=true
)

if defined corecommand (
  call :%hbase-command% %hbase-command-arguments%
) else (
  if "%hbase-command%" == "classpath" (
    echo %CLASSPATH%
    goto :eof
  )
  if "%hbase-command%" == "version" (
    set CLASS=org.apache.hadoop.hbase.util.VersionInfo
  ) else (
    set CLASS=%hbase-command%
  )
)

if not defined HBASE_IDENT_STRING (
  set HBASE_IDENT_STRING=%USERNAME%
)

@rem Set the right GC options based on the what we are running
set servercommands=master regionserver thrift thrift2 rest avro zookeeper
for %%i in ( %servercommands% ) do (
  if "%hbase-command%"=="%%i" set servercommand=true
)

if "%servercommand%" == "true" (
  set HBASE_OPTS=%HBASE_OPTS% %SERVER_GC_OPTS%
) else (
  set HBASE_OPTS=%HBASE_OPTS% %CLIENT_GC_OPTS%
)

@rem If HBase is run as a windows service, configure logging
if defined service_entry (
  set HBASE_LOG_PREFIX=hbase-%hbase-command%-%COMPUTERNAME%
  set HBASE_LOGFILE=!HBASE_LOG_PREFIX!.log
  if not defined HBASE_ROOT_LOGGER (
    set HBASE_ROOT_LOGGER=INFO,DRFA
  )
  set HBASE_SECURITY_LOGGER=INFO,DRFAS
  set loggc=!HBASE_LOG_DIR!\!HBASE_LOG_PREFIX!.gc
  set loglog=!HBASE_LOG_DIR!\!HBASE_LOGFILE!

  if "%HBASE_USE_GC_LOGFILE%" == "true" (
    set HBASE_OPTS=%HBASE_OPTS% -Xloggc:"!loggc!"
  )
)


@rem Have JVM dump heap if we run out of memory.  Files will be 'launch directory'
@rem and are named like the following: java_pid21612.hprof. Apparently it does not
@rem 'cost' to have this flag enabled. Its a 1.6 flag only. See:
@rem http://blogs.sun.com/alanb/entry/outofmemoryerror_looks_a_bit_better
set HBASE_OPTS=%HBASE_OPTS% -Dhbase.log.dir="%HBASE_LOG_DIR%"
set HBASE_OPTS=%HBASE_OPTS% -Dhbase.log.file="%HBASE_LOGFILE%"
set HBASE_OPTS=%HBASE_OPTS% -Dhbase.home.dir="%HBASE_HOME%"
set HBASE_OPTS=%HBASE_OPTS% -Dhbase.id.str="%HBASE_IDENT_STRING%"
set HBASE_OPTS=%HBASE_OPTS% -XX:OnOutOfMemoryError="taskkill /F /PID %p"

if not defined HBASE_ROOT_LOGGER (
  set HBASE_ROOT_LOGGER=INFO,console
)
set HBASE_OPTS=%HBASE_OPTS% -Dhbase.root.logger="%HBASE_ROOT_LOGGER%"

if defined JAVA_LIBRARY_PATH (
  set HBASE_OPTS=%HBASE_OPTS% -Djava.library.path="%JAVA_LIBRARY_PATH%"
)

rem Enable security logging on the master and regionserver only
if not defined HBASE_SECURITY_LOGGER (
  set HBASE_SECURITY_LOGGER=INFO,NullAppender
  if "%hbase-command%"=="master" (
    set HBASE_SECURITY_LOGGER=INFO,DRFAS
  )
  if "%hbase-command%"=="regionserver" (
    set HBASE_SECURITY_LOGGER=INFO,DRFAS
  )
)
set HBASE_OPTS=%HBASE_OPTS% -Dhbase.security.logger="%HBASE_SECURITY_LOGGER%"

set HEAP_SETTINGS=%JAVA_HEAP_MAX% %JAVA_OFFHEAP_MAX%
set java_arguments=%HEAP_SETTINGS% %HBASE_OPTS% -classpath "%CLASSPATH%" %CLASS% %hbase-command-arguments%

if defined service_entry (
  call :makeServiceXml %java_arguments%
) else (
  call %JAVA% %java_arguments%
)

endlocal
goto :eof

:shell
  rem eg export JRUBY_HOME=/usr/local/share/jruby
  if defined JRUBY_HOME (
    set CLASSPATH=%CLASSPATH%;%JRUBY_HOME%\lib\jruby.jar
    set HBASE_OPTS=%HBASE_OPTS% -Djruby.home="%JRUBY_HOME%" -Djruby.lib="%JRUBY_HOME%\lib"
  )
  rem find the hbase ruby sources
  if exist "%HBASE_HOME%\lib\ruby" (
    set HBASE_OPTS=%HBASE_OPTS% -Dhbase.ruby.sources="%HBASE_HOME%\lib\ruby"
  ) else (
    set HBASE_OPTS=%HBASE_OPTS% -Dhbase.ruby.sources="%HBASE_HOME%\hbase-shell\src\main\ruby"
  )
  set HBASE_OPTS=%HBASE_OPTS% %HBASE_SHELL_OPTS%

  set CLASS=org.jruby.Main -X+O %JRUBY_OPTS% "%HBASE_HOME%\bin\hirb.rb"
  goto :eof

:master
  set CLASS=org.apache.hadoop.hbase.master.HMaster
  if NOT "%1"=="stop" (
    set HBASE_OPTS=%HBASE_OPTS% %HBASE_MASTER_OPTS%
  )
  goto :eof

:regionserver
  set CLASS=org.apache.hadoop.hbase.regionserver.HRegionServer
  if NOT "%1"=="stop" (
    set HBASE_OPTS=%HBASE_OPTS% %HBASE_REGIONSERVER_OPTS%
  )
  goto :eof

:thrift
  set CLASS=org.apache.hadoop.hbase.thrift.ThriftServer
  if NOT "%1" == "stop" (
    set HBASE_OPTS=%HBASE_OPTS% %HBASE_THRIFT_OPTS%
  )
  goto :eof

:thrift2
  set CLASS=org.apache.hadoop.hbase.thrift2.ThriftServer
  if NOT "%1" == "stop" (
    set HBASE_OPTS=%HBASE_OPTS% %HBASE_THRIFT_OPTS%
  )
  goto :eof

:rest
  set CLASS=org.apache.hadoop.hbase.rest.RESTServer
  if NOT "%1"=="stop" (
    set HBASE_OPTS=%HBASE_OPTS% %HBASE_REST_OPTS%
  )
  goto :eof

:avro
  set CLASS=org.apache.hadoop.hbase.avro.AvroServer
  if NOT "%1"== "stop" (
    set HBASE_OPTS=%HBASE_OPTS% %HBASE_AVRO_OPTS%
  )
  goto :eof

:zookeeper
  set CLASS=org.apache.hadoop.hbase.zookeeper.HQuorumPeer
  if NOT "%1"=="stop" (
    set HBASE_OPTS=%HBASE_OPTS% %HBASE_ZOOKEEPER_OPTS%
  )
  goto :eof

:hbck
  set CLASS=org.apache.hadoop.hbase.util.HBaseFsck
  goto :eof

@rem TODO remove older 'hlog' command
:hlog
  set CLASS=org.apache.hadoop.hbase.wal.WALPrettyPrinter
  goto :eof

:wal
  set CLASS=org.apache.hadoop.hbase.wal.WALPrettyPrinter
  goto :eof

:hfile
  set CLASS=org.apache.hadoop.hbase.io.hfile.HFile
  goto :eof

:zkcli
  set CLASS=org.apache.hadoop.hbase.zookeeper.ZooKeeperMainServer
  goto :eof

:upgrade
  set CLASS=org.apache.hadoop.hbase.migration.UpgradeTo96
  goto :eof

:mapredcp
  set CLASS=org.apache.hadoop.hbase.util.MapreduceDependencyClasspathTool
  goto :eof

:makeServiceXml
  set arguments=%*
  @echo ^<service^>
  @echo   ^<id^>%hbase-command%^</id^>
  @echo   ^<name^>%hbase-command%^</name^>
  @echo   ^<description^>This service runs Isotope %hbase-command%^</description^>
  @echo   ^<executable^>%JAVA%^</executable^>
  @echo   ^<arguments^>%arguments%^</arguments^>
  @echo ^</service^>
  goto :eof

:print_usage
  echo Usage: hbase [^<options^>] ^<command^> [^<args^>]
  echo where ^<command^> an option from one of these categories::
  echo Options:
  echo   --config DIR    Configuration direction to use. Default: ./conf
  echo.
  echo Commands:
  echo Some commands take arguments. Pass no args or -h for usage."
  echo   shell           Run the HBase shell
  echo   hbck            Run the hbase 'fsck' tool
  echo   wal             Write-ahead-log analyzer
  echo   hfile           Store file analyzer
  echo   zkcli           Run the ZooKeeper shell
  echo   upgrade         Upgrade hbase
  echo   master          Run an HBase HMaster node
  echo   regionserver    Run an HBase HRegionServer node
  echo   zookeeper       Run a Zookeeper server
  echo   rest            Run an HBase REST server
  echo   thrift          Run the HBase Thrift server
  echo   thrift2         Run the HBase Thrift2 server
  echo   classpath       Dump hbase CLASSPATH
  echo   mapredcp        Dump CLASSPATH entries required by mapreduce
  echo   version         Print the version
  echo   CLASSNAME       Run the class named CLASSNAME
  goto :eof
