@ECHO OFF
rem ##########################################################################
rem
rem  Gradle startup script for Windows
rem
rem ##########################################################################

@IF NOT "%OS%"=="Windows_NT" GOTO end
SETLOCAL

rem Attempt to set APP_HOME
SET APP_HOME=
SET DIRNAME=%~dp0
IF "%DIRNAME%"=="." SET DIRNAME=%CD%
SET APP_HOME=%DIRNAME%

SET APP_NAME=gradle
SET APP_BASE_NAME=%~nx0

SET CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

SET DEFAULT_JVM_OPTS=

rem Find java.exe
IF NOT "%JAVA_HOME%"=="" SET JAVACMD=%JAVA_HOME%\bin\java.exe

IF EXIST "%JAVACMD%" GOTO init

SET JAVACMD=java
rem Try to find java.exe from PATH
SET TEMP_JAVA_EXE=
FOR %%i IN (java.exe) DO SET TEMP_JAVA_EXE=%%~dp$PATH:i\java.exe
SET TEMP_JAVA_EXE=%TEMP_JAVA_EXE:~0,-9%
rem If java.exe is found from PATH, set it as JAVACMD
IF NOT "%TEMP_JAVA_EXE%"=="" SET JAVACMD=%TEMP_JAVA_EXE%

:init

IF NOT EXIST "%JAVACMD%" (
    ECHO ERROR: JAVA_HOME is set to an invalid directory: "%JAVA_HOME%"
    ECHO Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
    EXIT /B 1
)

ECHO.
ECHO Setting Java system property: user.home to C:\Gradle\gradle-8.10.2
ECHO.

:run
"%JAVACMD%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
