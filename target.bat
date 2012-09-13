@echo off

set argCount=0
for %%x in (%*) do set /A argCount+=1

if not %argCount% == 1 (
	echo Must provide exactly one argument.
	exit /B;
)

if "%1" == "clean" (
	goto clean	
)

if "%1" == "compile" (
	goto compile
)

if "%1" == "run" (
	goto run
)

if "%1" == "pack_dlls" (
	goto pack_dlls
)

echo Bad argument. Only 'clean', 'compile', 'run' and 'pack_dlls' are accepted.
exit /B;

:clean
echo Doing clean.
rmdir /s /q TaskbarJFrameGrouping\bin
exit /B;

:compile
echo Doing compile.
mkdir TaskbarJFrameGrouping\bin
cd TaskbarJFrameGrouping\src
javac -d ..\..\TaskbarJFrameGrouping\bin com\example\taskbar\TaskbarJFrameGrouper.java
cd ..\..
exit /B;

:run
echo Doing run.
java -classpath "%cd%\TaskbarJFrameGrouping\lib\taskbar-lib.jar";"%cd%\TaskbarJFrameGrouping\bin" com.example.taskbar.TaskbarJFrameGrouper
exit /B;

:pack_dlls
echo Doing pack_dlls.
mkdir group-setter
copy WindowGroupSetter\Debug\WindowGroupSetter.dll group-setter\WindowGroupSetter_x86.dll
copy WindowGroupSetter\x64\Debug\WindowGroupSetter.dll group-setter\WindowGroupSetter_x64.dll
jar -cf TaskbarJFrameGrouping\lib\taskbar-lib.jar group-setter
rmdir /s /q group-setter
exit /B;


