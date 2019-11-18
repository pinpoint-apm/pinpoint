@echo off

setlocal

set COMPONENT=%1
set COMMAND=%2

set chk_argument=false
for %%i in (collector testapp web) do (
  if "%%i" == "%COMPONENT%" (
    set chk_argument=true
  )
)
for %%i in (start stop) do (
  if "%%i" == "%COMMAND%" (
    set chk_argument=true
  )
)
if chk_argument == false (
  goto print_usage
)

for %%i in (%0) do (
  if not defined QUICKSTART_BIN_PATH (
    set QUICKSTART_BIN_PATH=%%~dpi
  )
)
set BASE_DIR=%QUICKSTART_BIN_PATH%\..

set COMPONENT_DIR=%BASE_DIR%\%COMPONENT%

set CONF_DIR=%BASE_DIR%\conf
set CONF_FILE=quickstart.properties

set LOGS_DIR=%BASE_DIR%\logs
set LOG_FILE=%LOGS_DIR%\quickstart.%COMPONENT%.log
set AGENT_LOG_FILE=%LOGS_DIR%\quickstart.agent.log

set COMPONENT_IDENTIFIER=pinpoint-quickstart-%COMPONENT%
set IDENTIFIER=maven.pinpoint.identifier=%COMPONENT_IDENTIFIER%

set KEY_VERSION="quickstart.version"
set KEY_PORT="quickstart.%COMPONENT%.port"

for /f "tokens=2 delims==" %%i in (
  'type %CONF_DIR%\%CONF_FILE% ^| findstr %KEY_VERSION%'
) do (
  set VERSION=%%i
)
for /f "tokens=2 delims==" %%i in (
  'type %CONF_DIR%\%CONF_FILE% ^| findstr %KEY_PORT%'
) do (
  set PORT=%%i
)

if "%COMMAND%" == "start" (
  goto start_component
) else (
  if "%COMMAND%" == "stop" (
    goto stop_component
  )
  goto print_usage
)

:start_component
  set MAVEN=..\..\mvnw.cmd
  call %MAVEN% --version 1> nul 2>&1
  if not "%errorlevel%" == "0" (
    echo "Apache Maven (mvn) required."
    goto :eof
  )

  if not exist %LOGS_DIR% (
    mkdir %LOGS_DIR%
  )

  goto :start_process
)

:start_process
  set UNIT_TIME=5
  set END_COUNT=36
  set WAIT_TIME=0
  set /a CLOSE_WAIT_TIME=UNIT_TIME*END_COUNT

  start "%COMPONENT_IDENTIFIER%" cmd /c %MAVEN% -f %COMPONENT_DIR%/pom.xml clean package cargo:run -D%IDENTIFIER% -Dmaven.pinpoint.version=%VERSION% ^> %LOG_FILE%
  timeout /NOBREAK 1

  for /f "tokens=3 delims=," %%i in ('wmic process where Name^="java.exe" get ProcessId^,CommandLine /Format:csv ^| findstr "%IDENTIFIER%"') do set PID=%%i

  if not "%PID%" == "" (
    echo "---%COMPONENT_IDENTIFIER% initialization started---"
  ) else (
    echo "---%COMPONENT_IDENTIFIER% initialization start failed---"
    goto run_fail
  )
  set CHECK_COUNT=0

:check_running_pinpoint_process
  for /f "tokens=5" %%i IN (
    'netstat -ano ^| findstr %PORT%'
  ) do  (
    if "%%i" == "%PID%" goto run_success
  )

  if %CHECK_COUNT% GTR %END_COUNT% (
    goto run_fail
  ) else (
    set /a CHECK_COUNT=CHECK_COUNT+1
    set /a WAIT_TIME=CHECK_COUNT*UNIT_TIME
    echo "starting %COMPONENT_IDENTIFIER%. %WAIT_TIME% / %CLOSE_WAIT_TIME% sec(close wait limit)."
    timeout /NOBREAK %UNIT_TIME%
    goto check_running_pinpoint_process
  )

:run_fail
  echo "---%COMPONENT_IDENTIFIER% initialization failed. pid=%PID%.---"
  if not "%PID%" == "" (
    taskkill /f /pid %PID%
  )
  goto :eof

:run_success
  echo "---%COMPONENT_IDENTIFIER% initialization completed. pid=%PID%.---"
  goto :eof

:stop_component
  for /f "tokens=3 delims=," %%i in ('wmic process where Name^="java.exe" get ProcessId^,CommandLine /Format:csv ^| findstr "%IDENTIFIER%"') do set PID=%%i
  if not "%PID%" == "" (
    echo "shutting down %COMPONENT_IDENTIFIER%. pid=%PID%."
    taskkill /f /pid %PID%
  ) ELSE (
    echo "not running %COMPONENT_IDENTIFIER%"
  )

  for /f "tokens=3 delims=," %%i in ('wmic process where Name^="java.exe" get ProcessId^,CommandLine /Format:csv ^| findstr "%IDENTIFIER%"') do set PID=%%i
  if not "%PID%" == "" (
  echo "shutting down %COMPONENT_IDENTIFIER%. pid=%PID%."
    taskkill /f /pid %PID%
  ) ELSE (
    echo "not running %COMPONENT_IDENTIFIER%"
  )

  goto :eof

:print_usage
  echo Usage: quickstart ^<component^> ^<command^>
  echo.
  echo Component
  echo   collector       Pinpoint Collector
  echo   testapp         Pinpoint TestApp
  echo   web             Pinpoint Web UI
  echo.
  echo Commands:
  echo   start           Start the component
  echo   stop            Stop the component
  goto :eof