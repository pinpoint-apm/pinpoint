@echo off

setlocal

for %%i in (%0) do (
  if not defined QUICKSTART_BIN_PATH (
    set QUICKSTART_BIN_PATH=%%~dpi
  )
)

set QUICKSTART_BASE=%QUICKSTART_BIN_PATH%\..
set QUICKSTART_HBASE_PATH=%QUICKSTART_BASE%\hbase\hbase
set QUICKSTART_CONF_PATH=%QUICKSTART_BASE%\conf\hbase
set QUICKSTART_HBASE_WIN_SPPORT_PATH=%QUICKSTART_BASE%\support\hbase-win

if not exist %QUICKSTART_HBASE_PATH% (
  echo Please install HBase 0.94.x to %QUICKSTART_HBASE_PATH%
  goto :eof
)

xcopy /S /I %QUICKSTART_CONF_PATH%\hbase-site.xml %QUICKSTART_HBASE_PATH%\conf

xcopy /S /I %QUICKSTART_HBASE_WIN_SPPORT_PATH%\bin %QUICKSTART_HBASE_PATH%\bin
xcopy /S /I %QUICKSTART_HBASE_WIN_SPPORT_PATH%\conf %QUICKSTART_HBASE_PATH%\conf