@echo off
setlocal enabledelayedexpansion

REM Runs all tests in the tests directories one by one, and puts the output and 
REM results in a single directory test_results

REM set OPENDA_BINDIR, PATH and CLASSPATH
cd ..\..\bin
set OPENDA_BINDIR=%CD%
set PATH=%CD%;%PATH%

REM ==== check if jre available as distributed with openda ====
set OPENDA_JRE=%OPENDA_BINDIR%..\jre
if not exist "%OPENDA_JRE%\bin\java.exe" goto else
rem openda jre is available
set JAVA_HOME=%OPENDA_JRE%
goto endif
:else
rem no openda jre is available, check if there is a default one
if "%JAVA_HOME%" == "" goto exitwitherror0
:endif

set CLASSPATH=
for /r %OPENDA_BINDIR% %%G in (*.jar) do set CLASSPATH=!CLASSPATH!;"%%G"

cd ..\model_swan\tests
if exist test_results rd /s/q test_results
mkdir test_results

echo.

set CURDIR=s00c2
mkdir test_results\%CURDIR%
call :run_single_test SwanEnKF             SwanEnKFResults.txt    

set CURDIR=swan_l21triad
mkdir test_results\%CURDIR%
call :run_single_test swanSimulation             SimulationResults.txt    
call :run_single_test swanSimplex                SwanSimplexResults.txt    
call :run_single_test swanPowell                 SwanPowelResults.txt    
call :run_single_test swanDud                    SwanDudResults.txt    
call :run_single_test swanGriddedFullSearch      SwanGriddedFullsearchResults.txt    
call :run_single_test swanSimplexWithConstr      SwanSimplexWithConstrResults.txt    
call :run_single_test swanPowellWithConstr       SwanPowellwithConstrResults.txt    
call :run_single_test swanDudWithConstr          SwanDudwithConstrResults.txt    

set CURDIR=swan_l21triad_two_twin
mkdir test_results\%CURDIR%
call :run_single_test Dud1                         Dud1Results.txt
call :run_single_test Dud2                         Dud2Results.txt
call :run_single_test Dud_combined12               Dudcombined12Results.txt
call :run_single_test Simplex_combined12           Simplex_combined12.txt
call :run_single_test Powell_combined12            Powell_combined12.txt
call :run_single_test GriddedFullSearch_combined12 GriddedFullSearch_combined12.txt

call :run_single_test DudWithConstraint_combined12     DudWithConstraint_combined12.txt
call :run_single_test SimplexWithConstraint_combined12 SimplexWithConstraint_combined12.txt
call :run_single_test PowellWithConstraint_combined12  PowellWithConstraint_combined12.txt

echo.
if defined ErrorOccurred goto exitwitherror1
if defined TestDisabled (
   echo WARNING: One or more tests were disabled, the remaining tests finished without error
) else (
   echo All tests were performed and finished without error
)
exit 0

:exitwitherror0
echo No JAVA runtime found - please check this
exit 1 

:exitwitherror1
echo One or more tests finished with an error!
exit 1 

endlocal

:run_single_test

echo Running test: %CURDIR%\%1.oda
set odafile=%CD%\%CURDIR%\%1.oda
"%JAVA_HOME%\bin\java" -Xms128m -Xmx1024m -classpath %CLASSPATH% org.openda.application.OpenDaApplication %odafile% 1>test_results\%CURDIR%\%1.out 2>test_results\%CURDIR%\%1.err
if %errorlevel% gtr 0 goto Error1
if not (%2)==() copy %CURDIR%\%2 test_results\%CURDIR%\%1_%2 >nul
if not (%3)==() copy %CURDIR%\%3 test_results\%CURDIR%\%1_%3 >nul
if not (%4)==() copy %CURDIR%\%4 test_results\%CURDIR%\%1_%4 >nul
if not (%5)==() copy %CURDIR%\%5 test_results\%CURDIR%\%1_%5 >nul
if not (%6)==() copy %CURDIR%\%6 test_results\%CURDIR%\%1_%6 >nul
goto :eof

:Error1
echo ***Error occurred in test %CURDIR%\%1
set ErrorOccurred=1
goto :eof

:donotrun_single_test

echo ***Warning: %CURDIR%\%1.oda test is disabled
set TestDisabled=1
goto :eof
