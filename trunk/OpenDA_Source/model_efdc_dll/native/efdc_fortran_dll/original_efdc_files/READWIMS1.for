      SUBROUTINE READWIMS1
C
C GEOSR 2010.11.02
C
C ** SPECIFY THE TOXIC LOADING POINT
C ** PRODUCE TXSER.INP FOR TOXIC EVENT
C ** READ THE EVENT INFORMATION FROM GUI SYSTEM
C 
C
      USE GLOBAL  
      REAL TXMASS,TXSW,SDAY,EDAY,EVDAY,TLOADTX,TXMASS2,TXVOL
      REAL TXMASS_3D(KC),TXMASS0(KC)
	INTEGER ISYEAR,ISMONTH,ISDATE,ISHR,ISMN,
     &        IEYEAR,IEMONTH,IEDATE,IEHR,IEMN,
     &        IEVYEAR,IEVMONTH,IEVDATE,IEVHR,IEVMN,ITXPRD,
     &        JSDAY,JEDAY,JEVDAY,JYEARDAY
	INTEGER IDTX
	CHARACTER*20 TXNAME
C
C READ TOX EVENT FROM WIMS INFORMATION
C
      OPEN(11,FILE='EVENT_TOX2.INP',STATUS='UNKNOWN')  

      READ(11,*) ISYEAR,ISMONTH,ISDATE,ISHR,ISMN  ! MODEL START TIME
      READ(11,*) IEYEAR,IEMONTH,IEDATE,IEHR,IEMN  ! MODEL END TIME
      READ(11,*) USERDT                           ! TIME INTERVAL
      READ(11,*) TXID0    ! TOXIC ID
      READ(11,*) XTX,YTX  ! ACCIDENT LOCATOPN
      READ(11,*) IEVYEAR,IEVMONTH,IEVDATE,IEVHR,IEVMN  ! ACCIDENT TIME
      READ(11,*) ITXPRD   ! TOXIC RELEASE PERIOD [MIN]
      READ(11,*) TXMASS   ! RELEASED TOXIX AMOUNT [KG]
!      READ(11,*) TXSW     ! SPECIFIC WEIGHT [KG]/[KG] [CWCHO, 101203]
      READ(11,*) TXCHLA   ! CHLA CONC. [UG/L] FOR TOXIC CALCULATION
      READ(11,*) TXTEM    ! WATER TEMPERATURE [DEGREE C] FOR TOXIC CALCULATION
      READ(11,*) TXSSC    ! SSC CONC. [MG/L] FOR TOXIC CALCULATION
      READ(11,*) TXDOC    ! DOC CONC. [MG/L] FOR TOXIC CALCULATION
      READ(11,*) TXI      ! IRADIATION [LY/DAY]
      READ(11,*) TXWSPD   ! WINDSPEED [M/S]

      READ(TXID0(1:2),'(I2)') IDTX
      IF(IDTX.EQ.44)THEN
        READ(TXID0(1:4),'(I4)') IDTOX
      ELSEIF(IDTX.EQ.00)THEN
        IDTOX=0
        NTOX=0  ! 20110127 JGCHO
        NSED=0  ! 20110127 JGCHO
! } 20110127 JGCHO      
      ELSE
        IDTOX=1000
      ENDIF

	
!	OPEN(1,FILE='EFDC.INP',STATUS='UNKNOWN')
!	CALL SEEK('C8')  
!      READ(1,*,IOSTAT=ISO) TCON,TBEGIN
!	CLOSE(1)

      JSDAY=JULIAND(ISYEAR,ISMONTH,ISDATE)
      JEDAY=JULIAND(IEYEAR,IEMONTH,IEDATE)
      JEVDAY=JULIAND(IEVYEAR,IEVMONTH,IEVDATE)

! { GEOSR 2011.6.9 JGCHO END DAY CHECK
      IF (IEYEAR.GT.ISYEAR) THEN
        JYEARDAY=JULIAND(ISYEAR,12,31)
        JEDAY=JEDAY+JYEARDAY
      ENDIF
! } GEOSR 2011.6.9 JGCHO END DAY CHECK

      SDAY=FLOAT(JSDAY)+FLOAT(ISHR)/24.+FLOAT(ISMN)/(24.*60)       ! MODEL START TIME
      EDAY=FLOAT(JEDAY)+FLOAT(IEHR)/24.+FLOAT(IEMN)/(24.*60)       ! MODEL END TIME
      EVDAY=FLOAT(JEVDAY)+FLOAT(IEVHR)/24.+FLOAT(IEVMN)/(24.*60)   ! EVENT TIME

      TLOADTX=EVDAY-SDAY
      NPTXLDS=FLOAT(NINT(TLOADTX*86400.))                ! LOADING START TIME  [SEC]
      NPTXLDE=NPTXLDS+FLOAT(NINT(FLOAT(ITXPRD)*60.))     ! LOADING END TIME    [SEC]
      TXMASS2=TXMASS/(FLOAT(ITXPRD)*60.)          ! RELEASED MASS/TIME   [KG/SEC]
      TXVOL=0.000001                              ! LOADING VOL/SEC     [M3/SEC]
      TXLDC=TXMASS2/TXVOL                         ! CONC. FOR TXSER.INP [MG/L]

	  TBEGIN1=SDAY
      NTC1 = 86400*(JEDAY-JSDAY)+3600*(IEHR-ISHR)+60*(IEMN-ISMN) 
       !NTC1=INT(EDAY-SDAY)
        
	  NPTXLDS=NPTXLDS +TBEGIN1*86400. !*TCON    !CONSIDER TBEGIN 101124
	  NPTXLDE=NPTXLDE +TBEGIN1*86400. !*TCON    !CONSIDER TBEGIN 101124

      CLOSE(11)
C
C PRINT TOXIC TIMESERIES FILE
C
!{ GeoSR, YSSONG, 101125
      IF(IDTOX.GT.0.AND.IDTOX.LT.4440)THEN  ! ONLY FOR TOXIC MODULE
!}
        OPEN(21,FILE='TXSER.INP',STATUS='UNKNOWN')  
        CLOSE(21,STATUS='DELETE')
        OPEN(21,FILE='TXSER.INP',STATUS='UNKNOWN')  

        IF(KC.GT.1)THEN
          TXMASS_3D(1)=TXLDC
        ELSE
          TXMASS_3D(KC)=TXLDC
          TXMASS0(KC)=0.0
          DO K=1,KC-1
            TXMASS_3D(K)=0.0
            TXMASS0(K)=0.0
          ENDDO
        ENDIF

        DO NDUM=1,15
         WRITE(21,8899) 
        ENDDO
        WRITE(21,8898)
        WRITE(21,*) '          0.',(TXMASS0(K),K=1,KC)
        WRITE(21,*) NPTXLDS-1.,(TXMASS0(K),K=1,KC)
        WRITE(21,*) NPTXLDS,(TXMASS_3D(K),K=1,KC)
        WRITE(21,*) NPTXLDE-1.,(TXMASS_3D(K),K=1,KC)
        WRITE(21,*) NPTXLDE,(TXMASS0(K),K=1,KC)
        WRITE(21,*) '   99999999.',(TXMASS0(K),K=1,KC)

 8899   FORMAT('C')
 8898   FORMAT('   0       6       1.       0.       1.       0.')
        CLOSE(21)      
      ENDIF

      IF(IDTOX.GT.0.AND.IDTOX.LT.4440)THEN  ! ONLY FOR TOXIC MODULE (CWCHO)
        OPEN(21,FILE='TOXEVENT.LOG',STATUS='UNKNOWN')  
        WRITE(21,8998) ISYEAR,ISMONTH,ISDATE,ISHR,ISMN
        WRITE(21,8997) IEVYEAR,IEVMONTH,IEVDATE,IEVHR,IEVMN
        WRITE(21,8995) ITXPRD
        WRITE(21,8994) TXMASS
        WRITE(21,8993) TXMASS2
       CLOSE(21)
	ENDIF
 8998 FORMAT('MODEL START TIME     :',2X,I4,'.',I2,'/',I2,'.',I2,':',I2)
 8997 FORMAT('LOADING TIME         :',2X,I4,'.',I2,'/',I2,'.',I2,':',I2)
 8995 FORMAT('LOADING PERIOD [MIN] :',I4)
 8994 FORMAT('LOADING MASS [g]    :',F12.3) ! 2010.12.8
 8993 FORMAT('LOADING RATE [KG/S]  :',F7.3)

      IF(IDTOX.GE.4440)THEN  ! ONLY FOR OIL MODULE(CWCHO 101101) 

	! [CWCHO, 101203]
	  OPEN(1,FILE='TOX2.INFO',STATUS='UNKNOWN')
        
	  DO NDUM=1,27
        READ(1,*)
        ENDDO
 1000   READ(1,*,END=1001) TXID,ISOCTX,TXNAME,TXSG,TXPAR,TXMW,TXHE,TXI0
        IF(TXID.EQ.TXID0) THEN
	  TXSW=TXSG
!	  WRITE(*,*) TXNAME,TXSG
	  ENDIF
	  GOTO 1000
 1001   CONTINUE
	  CLOSE(1)

      TXMASS=TXMASS/1000. ! GEOSR 2011.2.25
	OILVOL=TXMASS/(TXSW*1000.)
	OILSW=TXSW
	OPEN(21,FILE='OILEVENT.LOG',STATUS='UNKNOWN')  
      WRITE(21,8988) ISYEAR,ISMONTH,ISDATE,ISHR,ISMN
      WRITE(21,8987) IEVYEAR,IEVMONTH,IEVDATE,IEVHR,IEVMN
      WRITE(21,8986) ITXPRD
      WRITE(21,8985) TXMASS*1000. ! GEOSR 2011.2.25
      WRITE(21,8984) OILSW
      
 8988 FORMAT('MODEL START TIME     :',2X,I4,'.',I2,'/',I2,'.',I2,':',I2)
 8987 FORMAT('LOADING TIME         :',2X,I4,'.',I2,'/',I2,'.',I2,':',I2)
 8986 FORMAT('LOADING PERIOD [MIN] :',2X,I10)
 8985 FORMAT('LOADING MASS   [G]  :',F12.3)   ! GEOSR 2011.2.25
 8984 FORMAT('LOADING SG  [KG/KG]  :',F12.3)
      CLOSE(21)
	ENDIF


      RETURN
      END

C----------------------------------------------------------------------C
      FUNCTION JULIAND(IYEAR,IMONTH,IDATE)
C----------------------------------------------------------------------C
      IBONUS=0
      IF( MOD( (IYEAR-1900),4 ) .EQ.0 ) IBONUS=1
      DATE1=31+28+IBONUS
      DATE2=DATE1+153
      DATE3=DATE2+153

      IF(IMONTH.GE.3 .AND. IMONTH.LE.7 ) THEN
        ITDATE=DATE2
        IRMONTH=8
       ENDIF

      IF(IMONTH.GE.8 .AND. IMONTH.LE.12 ) THEN
        ITDATE=DATE3
        IRMONTH=13     
      ENDIF

      ITDATE=ITDATE-(IRMONTH-IMONTH)*31 + (IRMONTH-IMONTH)/2 + IDATE-1

      IF(IMONTH.EQ.1) ITDATE=IDATE-1
      IF(IMONTH.EQ.2) ITDATE=31+IDATE-1

      JULIAND=ITDATE+1
      RETURN
      END
