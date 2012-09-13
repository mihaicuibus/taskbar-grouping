clean:
	@echo Doing clean.
	@rm -rf TaskbarJFrameGrouping/bin

compile:
	@echo Doing compile.
	@mkdir TaskbarJFrameGrouping/bin
	@cd TaskbarJFrameGrouping/src ; javac -d ../../TaskbarJFrameGrouping/bin com/example/taskbar/TaskbarJFrameGrouper.java

run:
	@echo Doing run.
	@java -classpath `pwd`/TaskbarJFrameGrouping/lib/taskbar-lib.jar:`pwd`/TaskbarJFrameGrouping/bin com.example.taskbar.TaskbarJFrameGrouper

pack_dlls:
	@echo Doing pack_dlls.
	@mkdir -p group-setter
	@cp WindowGroupSetter/Debug/WindowGroupSetter.dll group-setter/WindowGroupSetter_x86.dll
	@cp WindowGroupSetter/x64/Debug/WindowGroupSetter.dll group-setter/WindowGroupSetter_x64.dll
	@jar -cf TaskbarJFrameGrouping/lib/taskbar-lib.jar group-setter
	@rm -rf group-setter
