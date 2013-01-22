SUBROUTINE SCANSEDZLJ  
!
!  REVISION DATE :  May 24, 2006
!  Craig Jones and Scott James
!***************************************************************
	USE GLOBAL
	IMPLICIT NONE
	INTEGER::IDUMMY,ERROR
!
	WRITE(*,'(A)')'SCANNING INPUT FILE: BED.SDF'  
	OPEN(1,FILE='BED.SDF',STATUS='OLD')  
	READ(1,*,IOSTAT=ERROR) !SKIP THIS LINE
	IF(ERROR==1)THEN
		WRITE(*,'("READ ERROR IN SEDZLJ INPUT FILE")')  
		WRITE(8,'("READ ERROR IN SEDZLJ INPUT FILE")')  
		STOP
	ENDIF
	READ(1,*,IOSTAT=ERROR) IDUMMY,IDUMMY,IDUMMY,KB
	IF(ERROR==1)THEN
		WRITE(*,'("READ ERROR IN SEDZLJ INPUT FILE")')  
		WRITE(8,'("READ ERROR IN SEDZLJ INPUT FILE")')  
		STOP
	ENDIF
	READ(1,*,IOSTAT=ERROR) !SKIP THIS LINE
	IF(ERROR==1)THEN
		WRITE(*,'("READ ERROR IN SEDZLJ INPUT FILE")')  
		WRITE(8,'("READ ERROR IN SEDZLJ INPUT FILE")')  
		STOP
	ENDIF
	READ(1,*,IOSTAT=ERROR) ITBM,NSICM 
	IF(ERROR==1)THEN
		WRITE(*,'("READ ERROR IN SEDZLJ INPUT FILE")')  
		WRITE(8,'("READ ERROR IN SEDZLJ INPUT FILE")')  
		STOP
	ENDIF
	CLOSE(1)  
	RETURN
END SUBROUTINE SCANSEDZLJ

