@echo off

rem Setup bin. dir for openda.
rem Script has to be run from the dir. containing the script.

set OPENDA_BINDIR=%~dp0
set PATH=%~dp0;%PATH%
@echo OPENDA_BINDIR set to %OPENDA_BINDIR%
