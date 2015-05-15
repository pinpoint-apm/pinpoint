@echo off

setlocal

set CURRENT_DIR="%~dp0"

for %%i in (%0) do (
  if not defined QUICKSTART_BIN_PATH (
    set QUICKSTART_BIN_PATH=%%~dpi
  )
)

if "%QUICKSTART_BIN_PATH:~-1%" == "\" (
  set QUICKSTART_BIN_PATH=%QUICKSTART_BIN_PATH:~0,-1%
)

set QUICKSTART_BASE=%QUICKSTART_BIN_PATH%\..
set QUICKSTART_HBASE_PATH=%QUICKSTART_BASE%\hbase\hbase
set QUICKSTART_HBASE_WIN_SPPORT_PATH=%QUICKSTART_BASE%\support\hbase-win
set QUICKSTART_HADOOP_HOME=%QUICKSTART_HBASE_WIN_SPPORT_PATH%\hadoop-common

if not defined HADOOP_HOME (
  echo HADOOP_HOME not detected. Using supplied Hadoop binaries in %QUICKSTART_HADOOP_HOME%
  set HADOOP_HOME=%QUICKSTART_HADOOP_HOME%
) else (
  echo HADOOP_HOME detected - %HADOOP_HOME%
)

cd %QUICKSTART_HBASE_PATH%\bin

%QUICKSTART_HBASE_PATH%\bin\stop-hbase.cmd

cd %CURRENT_DIR%