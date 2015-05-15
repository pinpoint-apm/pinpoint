@echo off

setlocal

for %%i in (%0) do (
  if not defined QUICKSTART_BIN_PATH (
    set QUICKSTART_BIN_PATH=%%~dpi
  )
)

set QUICKSTART_BASE=%QUICKSTART_BIN_PATH%\..
set QUICKSTART_HBASE_PATH=%QUICKSTART_BASE%\hbase\hbase
set QUICKSTART_HBASE_WIN_SPPORT_PATH=%QUICKSTART_BASE%\support\hbase-win
set QUICKSTART_HADOOP_HOME=%QUICKSTART_HBASE_WIN_SPPORT_PATH%\hadoop-common

if not exist %QUICKSTART_HBASE_PATH% (
  echo HBase not exist in the %QUICKSTART_HBASE_PATH%
  goto :eof
)

if not defined HADOOP_HOME (
  echo HADOOP_HOME not detected. Using supplied Hadoop binaries in %QUICKSTART_HADOOP_HOME%
  set HADOOP_HOME=%QUICKSTART_HADOOP_HOME%
) else (
  echo HADOOP_HOME detected - %HADOOP_HOME%
)

echo create all tables
call %QUICKSTART_HBASE_PATH%\bin\hbase.cmd shell %QUICKSTART_BASE%\conf\hbase\init-hbase.txt