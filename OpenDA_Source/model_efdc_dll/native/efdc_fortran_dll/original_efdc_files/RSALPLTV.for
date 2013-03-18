      SUBROUTINE RSALPLTV(ITMP)  
C  
C CHANGE RECORD  
C **  SUBROUTINE RSALPLTV WRITES A FILE FOR VERTICAL PLANE CONTOURING  
C **  OF RESIDUAL SALINITY AND VERTICAL DIFFUSIVITY ALONG AN ARBITARY  
C **  SEQUENCE OF (I,J) POINTS  
C  
C *** PMC  THIS ROUTINE USES HMP, THE STATIC IC DEPTH.  SHOULDN'T IT USE HP?

      USE GLOBAL  
      CHARACTER*80 TITLE1,TITLE2,TITLE3,TITLE5  
      REAL,SAVE,ALLOCATABLE,DIMENSION(:)::ABTMP  
      IF(.NOT.ALLOCATED(ABTMP))THEN
		ALLOCATE(ABTMP(KCM))
	    ABTMP=0.0 
	ENDIF
C
      IF(ITMP.EQ.2) RETURN  
      IF(ITMP.EQ.3) RETURN  
      IF(ITMP.EQ.4) RETURN  
      IF(ITMP.GE.5) GOTO 1000  
      IF(JSRSPV(ITMP).NE.1) GOTO 300  
      TITLE1='RESIDUAL SALINITY CONTOURS'  
      TITLE2='RESIDUAL VERTICAL DIFFUSIVITY CONTOURS'  
      TITLE3='FLUX AVG RESID VERT DIFF CONTOURS'  
      IF(ISECSPV.GE.1)THEN  
        OPEN(11,FILE='RSALCV1.OUT',STATUS='UNKNOWN')  
        OPEN(21,FILE='RVISCV1.OUT',STATUS='UNKNOWN')  
        OPEN(31,FILE='RVEFCV1.OUT',STATUS='UNKNOWN')  
        CLOSE(11,STATUS='DELETE')  
        CLOSE(21,STATUS='DELETE')  
        CLOSE(31,STATUS='DELETE')  
        OPEN(11,FILE='RSALCV1.OUT',STATUS='UNKNOWN')  
        OPEN(21,FILE='RVISCV1.OUT',STATUS='UNKNOWN')  
        OPEN(31,FILE='RVEFCV1.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.2)THEN  
        OPEN(12,FILE='RSALCV2.OUT',STATUS='UNKNOWN')  
        OPEN(22,FILE='RVISCV2.OUT',STATUS='UNKNOWN')  
        OPEN(32,FILE='RVEFCV2.OUT',STATUS='UNKNOWN')  
        CLOSE(12,STATUS='DELETE')  
        CLOSE(22,STATUS='DELETE')  
        CLOSE(32,STATUS='DELETE')  
        OPEN(12,FILE='RSALCV2.OUT',STATUS='UNKNOWN')  
        OPEN(22,FILE='RVISCV2.OUT',STATUS='UNKNOWN')  
        OPEN(32,FILE='RVEFCV2.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.3)THEN  
        OPEN(13,FILE='RSALCV3.OUT',STATUS='UNKNOWN')  
        OPEN(23,FILE='RVISCV3.OUT',STATUS='UNKNOWN')  
        OPEN(33,FILE='RVEFCV3.OUT',STATUS='UNKNOWN')  
        CLOSE(13,STATUS='DELETE')  
        CLOSE(23,STATUS='DELETE')  
        CLOSE(33,STATUS='DELETE')  
        OPEN(13,FILE='RSALCV3.OUT',STATUS='UNKNOWN')  
        OPEN(23,FILE='RVISCV3.OUT',STATUS='UNKNOWN')  
        OPEN(33,FILE='RVEFCV3.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.4)THEN  
        OPEN(14,FILE='RSALCV4.OUT',STATUS='UNKNOWN')  
        OPEN(24,FILE='RVISCV4.OUT',STATUS='UNKNOWN')  
        OPEN(34,FILE='RVEFCV4.OUT',STATUS='UNKNOWN')  
        CLOSE(14,STATUS='DELETE')  
        CLOSE(24,STATUS='DELETE')  
        CLOSE(34,STATUS='DELETE')  
        OPEN(14,FILE='RSALCV4.OUT',STATUS='UNKNOWN')  
        OPEN(24,FILE='RVISCV4.OUT',STATUS='UNKNOWN')  
        OPEN(34,FILE='RVEFCV4.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.5)THEN  
        OPEN(15,FILE='RSALCV5.OUT',STATUS='UNKNOWN')  
        OPEN(25,FILE='RVISCV5.OUT',STATUS='UNKNOWN')  
        OPEN(35,FILE='RVEFCV5.OUT',STATUS='UNKNOWN')  
        CLOSE(15,STATUS='DELETE')  
        CLOSE(25,STATUS='DELETE')  
        CLOSE(35,STATUS='DELETE')  
        OPEN(15,FILE='RSALCV5.OUT',STATUS='UNKNOWN')  
        OPEN(25,FILE='RVISCV5.OUT',STATUS='UNKNOWN')  
        OPEN(35,FILE='RVEFCV5.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.6)THEN  
        OPEN(16,FILE='RSALCV6.OUT',STATUS='UNKNOWN')  
        OPEN(26,FILE='RVISCV6.OUT',STATUS='UNKNOWN')  
        OPEN(36,FILE='RVEFCV6.OUT',STATUS='UNKNOWN')  
        CLOSE(16,STATUS='DELETE')  
        CLOSE(26,STATUS='DELETE')  
        CLOSE(36,STATUS='DELETE')  
        OPEN(16,FILE='RSALCV6.OUT',STATUS='UNKNOWN')  
        OPEN(26,FILE='RVISCV6.OUT',STATUS='UNKNOWN')  
        OPEN(36,FILE='RVEFCV6.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.7)THEN  
        OPEN(17,FILE='RSALCV7.OUT',STATUS='UNKNOWN')  
        OPEN(27,FILE='RVISCV7.OUT',STATUS='UNKNOWN')  
        OPEN(37,FILE='RVEFCV7.OUT',STATUS='UNKNOWN')  
        CLOSE(17,STATUS='DELETE')  
        CLOSE(27,STATUS='DELETE')  
        CLOSE(37,STATUS='DELETE')  
        OPEN(17,FILE='RSALCV7.OUT',STATUS='UNKNOWN')  
        OPEN(27,FILE='RVISCV7.OUT',STATUS='UNKNOWN')  
        OPEN(37,FILE='RVEFCV7.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.8)THEN  
        OPEN(18,FILE='RSALCV8.OUT',STATUS='UNKNOWN')  
        OPEN(28,FILE='RVISCV8.OUT',STATUS='UNKNOWN')  
        OPEN(38,FILE='RVEFCV8.OUT',STATUS='UNKNOWN')  
        CLOSE(18,STATUS='DELETE')  
        CLOSE(28,STATUS='DELETE')  
        CLOSE(38,STATUS='DELETE')  
        OPEN(18,FILE='RSALCV8.OUT',STATUS='UNKNOWN')  
        OPEN(28,FILE='RVISCV8.OUT',STATUS='UNKNOWN')  
        OPEN(38,FILE='RVEFCV8.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.9)THEN  
        OPEN(19,FILE='RSALCV9.OUT',STATUS='UNKNOWN')  
        OPEN(29,FILE='RVISCV9.OUT',STATUS='UNKNOWN')  
        OPEN(39,FILE='RVEFCV9.OUT',STATUS='UNKNOWN')  
        CLOSE(19,STATUS='DELETE')  
        CLOSE(29,STATUS='DELETE')  
        CLOSE(39,STATUS='DELETE')  
        OPEN(19,FILE='RSALCV9.OUT',STATUS='UNKNOWN')  
        OPEN(29,FILE='RVISCV9.OUT',STATUS='UNKNOWN')  
        OPEN(39,FILE='RVEFCV9.OUT',STATUS='UNKNOWN')  
      ENDIF  
      DO IS=1,ISECSPV  
        LUN1=10+IS  
        LUN2=20+IS  
        LUN3=30+IS  
        LINES=NIJSPV(IS)  
        LEVELS=KC  
        WRITE (LUN1,99) TITLE1,CCTITLE(LUN1)  
        WRITE (LUN1,101)LINES,LEVELS  
        WRITE (LUN1,250)(ZZ(K),K=1,KC)  
        WRITE (LUN2,99) TITLE2,CCTITLE(LUN2)  
        WRITE (LUN2,101)LINES,LEVELS  
        WRITE (LUN2,250)(ZZ(K),K=1,KC)  
        WRITE (LUN3,99) TITLE3,CCTITLE(LUN2)  
        WRITE (LUN3,101)LINES,LEVELS  
        WRITE (LUN3,250)(ZZ(K),K=1,KC)  
        CLOSE(LUN1)  
        CLOSE(LUN2)  
        CLOSE(LUN3)  
      ENDDO  
      JSRSPV(ITMP)=0  
  300 CONTINUE  
      IF(ISDYNSTP.EQ.0)THEN  
        TIME=DT*FLOAT(N)+TCON*TBEGIN  
        TIME=TIME/TCON  
      ELSE  
        TIME=TIMESEC/TCON  
      ENDIF  
      IF(ISECSPV.GE.1)THEN  
        OPEN(11,FILE='RSALCV1.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(21,FILE='RVISCV1.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(31,FILE='RVEFCV1.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.2)THEN  
        OPEN(12,FILE='RSALCV2.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(22,FILE='RVISCV2.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(32,FILE='RVEFCV2.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.3)THEN  
        OPEN(13,FILE='RSALCV3.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(23,FILE='RVISCV3.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(33,FILE='RVEFCV3.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.4)THEN  
        OPEN(14,FILE='RSALCV4.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(24,FILE='RVISCV4.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(34,FILE='RVEFCV4.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.5)THEN  
        OPEN(15,FILE='RSALCV5.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(25,FILE='RVISCV5.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(35,FILE='RVEFCV5.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.6)THEN  
        OPEN(16,FILE='RSALCV6.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(26,FILE='RVISCV6.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(36,FILE='RVEFCV6.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.7)THEN  
        OPEN(17,FILE='RSALCV7.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(27,FILE='RVISCV7.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(37,FILE='RVEFCV7.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.8)THEN  
        OPEN(18,FILE='RSALCV8.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(28,FILE='RVISCV8.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(38,FILE='RVEFCV8.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.9)THEN  
        OPEN(19,FILE='RSALCV9.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(29,FILE='RVISCV9.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        OPEN(39,FILE='RVEFCV9.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
      ENDIF  
      DO IS=1,ISECSPV  
        LUN1=10+IS  
        LUN2=20+IS  
        LUN3=30+IS  
        WRITE (LUN1,100)N,TIME  
        WRITE (LUN2,100)N,TIME  
        WRITE (LUN3,100)N,TIME  
        DO NN=1,NIJSPV(IS)  
          I=ISPV(NN,IS)  
          J=JSPV(NN,IS)  
          L=LIJ(I,J)  
          ZETA=HLPF(L)-HMP(L)  
          HBTMP=HMP(L)  
          WRITE(LUN1,200)IL(L),JL(L),DLON(L),DLAT(L),ZETA,HBTMP  
          WRITE(LUN1,250)(SALLPF(L,K),K=1,KC)  
          WRITE(LUN2,200)IL(L),JL(L),DLON(L),DLAT(L),ZETA,HBTMP  
          WRITE(LUN3,200)IL(L),JL(L),DLON(L),DLAT(L),ZETA,HBTMP  
          ABTMP(1)=5000.*ABLPF(L,1)*HLPF(L)  
          ABTMP(KC)=5000.*ABLPF(L,KS)*HLPF(L)  
          DO K=2,KS  
            ABTMP(K)=5000.*(ABLPF(L,K-1)+ABLPF(L,K))*HLPF(L)  
          ENDDO  
          WRITE(LUN2,250)(ABTMP(K),K=1,KC)  
          ABTMP(1)=-50.*ABEFF(L,1)  
          ABTMP(KC)=50.*ABEFF(L,KS)  
          DO K=2,KS  
            ABTMP(K)=-50.*(ABEFF(L,K-1)+ABEFF(L,K))  
          ENDDO  
          WRITE(LUN3,250)(ABTMP(K),K=1,KC)  
        ENDDO  
        CLOSE(LUN1)  
        CLOSE(LUN2)  
        CLOSE(LUN3)  
      ENDDO  
      GOTO 2000  
 1000 CONTINUE  
      IF(JSRSPV(ITMP).NE.1) GOTO 1300  
      TITLE1='RESIDUAL TOXIC CONTAMIANT CONTOURS'  
      TITLE2='RESIDUAL COHESIVE SED CONTOURS'  
      TITLE2='RESIDUAL NONCOHESIVE SED CONTOURS'  
      IF(ISECSPV.GE.1)THEN  
        IF(ITMP.EQ.5) OPEN(11,FILE='RTOXCV1.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(21,FILE='RSEDCV1.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(31,FILE='RSNDCV1.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.5) CLOSE(11,STATUS='DELETE')  
        IF(ITMP.EQ.6) CLOSE(21,STATUS='DELETE')  
        IF(ITMP.EQ.8) CLOSE(31,STATUS='DELETE')  
        IF(ITMP.EQ.5) OPEN(11,FILE='RTOXCV1.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(21,FILE='RSEDCV1.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.8) OPEN(31,FILE='RSNDCV1.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.2)THEN  
        IF(ITMP.EQ.5) OPEN(12,FILE='RTOXCV2.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(22,FILE='RSEDCV2.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(32,FILE='RSNDCV2.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.5) CLOSE(12,STATUS='DELETE')  
        IF(ITMP.EQ.6) CLOSE(22,STATUS='DELETE')  
        IF(ITMP.EQ.7) CLOSE(32,STATUS='DELETE')  
        IF(ITMP.EQ.5) OPEN(12,FILE='RTOXCV2.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(22,FILE='RSEDCV2.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(32,FILE='RSNDCV2.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.3)THEN  
        IF(ITMP.EQ.5) OPEN(13,FILE='RTOXCV3.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(23,FILE='RSEDCV3.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(33,FILE='RSNDCV3.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.5)  CLOSE(13,STATUS='DELETE')  
        IF(ITMP.EQ.6) CLOSE(23,STATUS='DELETE')  
        IF(ITMP.EQ.7) CLOSE(33,STATUS='DELETE')  
        IF(ITMP.EQ.5) OPEN(13,FILE='RTOXCV3.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(23,FILE='RSEDCV3.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(33,FILE='RSNDCV3.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.4)THEN  
        IF(ITMP.EQ.5) OPEN(14,FILE='RTOXCV4.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(24,FILE='RSEDCV4.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(34,FILE='RSNDCV4.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.5) CLOSE(14,STATUS='DELETE')  
        IF(ITMP.EQ.6) CLOSE(24,STATUS='DELETE')  
        IF(ITMP.EQ.7) CLOSE(34,STATUS='DELETE')  
        IF(ITMP.EQ.5) OPEN(14,FILE='RTOXCV4.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(24,FILE='RSEDCV4.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(34,FILE='RSNDCV4.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.5)THEN  
        IF(ITMP.EQ.5) OPEN(15,FILE='RTOXCV5.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(25,FILE='RSEDCV5.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(35,FILE='RSNDCV5.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.5) CLOSE(15,STATUS='DELETE')  
        IF(ITMP.EQ.6) CLOSE(25,STATUS='DELETE')  
        IF(ITMP.EQ.7) CLOSE(35,STATUS='DELETE')  
        IF(ITMP.EQ.5) OPEN(15,FILE='RTOXCV5.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(25,FILE='RSEDCV5.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(35,FILE='RSNDCV5.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.6)THEN  
        IF(ITMP.EQ.5) OPEN(16,FILE='RTOXCV6.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(26,FILE='RSEDCV6.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(36,FILE='RSNDCV6.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.5) CLOSE(16,STATUS='DELETE')  
        IF(ITMP.EQ.6) CLOSE(26,STATUS='DELETE')  
        IF(ITMP.EQ.7) CLOSE(36,STATUS='DELETE')  
        IF(ITMP.EQ.5) OPEN(16,FILE='RTOXCV6.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(26,FILE='RSEDCV6.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(36,FILE='RSNDCV6.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.7)THEN  
        IF(ITMP.EQ.5) OPEN(17,FILE='RTOXCV7.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(27,FILE='RSEDCV7.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(37,FILE='RSNDCV7.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.5) CLOSE(17,STATUS='DELETE')  
        IF(ITMP.EQ.6) CLOSE(27,STATUS='DELETE')  
        IF(ITMP.EQ.7) CLOSE(37,STATUS='DELETE')  
        IF(ITMP.EQ.5) OPEN(17,FILE='RTOXCV7.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(27,FILE='RSEDCV7.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(37,FILE='RSNDCV7.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.8)THEN  
        IF(ITMP.EQ.5) OPEN(18,FILE='RTOXCV8.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(28,FILE='RSEDCV8.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(38,FILE='RSNDCV8.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.5) CLOSE(18,STATUS='DELETE')  
        IF(ITMP.EQ.6) CLOSE(28,STATUS='DELETE')  
        IF(ITMP.EQ.7) CLOSE(38,STATUS='DELETE')  
        IF(ITMP.EQ.5) OPEN(18,FILE='RTOXCV8.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(28,FILE='RSEDCV8.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(38,FILE='RSNDCV8.OUT',STATUS='UNKNOWN')  
      ENDIF  
      IF(ISECSPV.GE.9)THEN  
        IF(ITMP.EQ.5) OPEN(19,FILE='RTOXCV9.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(29,FILE='RSEDCV9.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(39,FILE='RSNDCV9.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.5) CLOSE(19,STATUS='DELETE')  
        IF(ITMP.EQ.6) CLOSE(29,STATUS='DELETE')  
        IF(ITMP.EQ.7) CLOSE(39,STATUS='DELETE')  
        IF(ITMP.EQ.5) OPEN(19,FILE='RTOXCV9.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.6) OPEN(29,FILE='RSEDCV9.OUT',STATUS='UNKNOWN')  
        IF(ITMP.EQ.7) OPEN(39,FILE='RSNDCV9.OUT',STATUS='UNKNOWN')  
      ENDIF  
      DO IS=1,ISECSPV  
        LUN1=10+IS  
        LUN2=20+IS  
        LUN3=30+IS  
        LINES=NIJSPV(IS)  
        LEVELS=KC  
        IF(ITMP.EQ.5) WRITE (LUN1,99) TITLE1,CCTITLE(LUN1)  
        IF(ITMP.EQ.5) WRITE (LUN1,101)LINES,LEVELS  
        IF(ITMP.EQ.5) WRITE (LUN1,250)(ZZ(K),K=1,KC)  
        IF(ITMP.EQ.6) WRITE (LUN2,99) TITLE2,CCTITLE(LUN2)  
        IF(ITMP.EQ.6) WRITE (LUN2,101)LINES,LEVELS  
        IF(ITMP.EQ.6) WRITE (LUN2,250)(ZZ(K),K=1,KC)  
        IF(ITMP.EQ.7) WRITE (LUN3,99) TITLE3,CCTITLE(LUN2)  
        IF(ITMP.EQ.7) WRITE (LUN3,101)LINES,LEVELS  
        IF(ITMP.EQ.7) WRITE (LUN3,250)(ZZ(K),K=1,KC)  
        IF(ITMP.EQ.5) CLOSE(LUN1)  
        IF(ITMP.EQ.6) CLOSE(LUN2)  
        IF(ITMP.EQ.7) CLOSE(LUN3)  
      ENDDO  
      JSRSPV(ITMP)=0  
 1300 CONTINUE  
      IF(ISDYNSTP.EQ.0)THEN  
        TIME=DT*FLOAT(N)+TCON*TBEGIN  
        TIME=TIME/TCON  
      ELSE  
        TIME=TIMESEC/TCON  
      ENDIF  
C  
C ** TOXICS  
C  
      IF(ITMP.EQ.5)THEN  
        IF(ISECSPV.GE.1)THEN  
          OPEN(11,FILE='RTOXCV1.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.2)THEN  
          OPEN(12,FILE='RTOXCV2.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.3)THEN  
          OPEN(13,FILE='RTOXCV3.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.4)THEN  
          OPEN(14,FILE='RTOXCV4.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.5)THEN  
          OPEN(15,FILE='RTOXCV5.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.6)THEN  
          OPEN(16,FILE='RTOXCV6.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.7)THEN  
          OPEN(17,FILE='RTOXCV7.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.8)THEN  
          OPEN(18,FILE='RTOXCV8.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.9)THEN  
          OPEN(19,FILE='RTOXCV9.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        DO IS=1,ISECSPV  
          LUN1=10+IS  
          WRITE (LUN1,100)N,TIME  
          DO NN=1,NIJSPV(IS)  
            I=ISPV(NN,IS)  
            J=JSPV(NN,IS)  
            L=LIJ(I,J)  
            ZETA=HLPF(L)-HMP(L)  
            HBTMP=HMP(L)  
            WRITE(LUN1,200)IL(L),JL(L),DLON(L),DLAT(L),ZETA,HBTMP  
            WRITE(LUN1,250)(TOXLPF(L,K,1),K=1,KC)  
          ENDDO  
          CLOSE(LUN1)  
        ENDDO  
      ENDIF  
C  
C **  COHESIVE SEDIMENT  
C  
      IF(ITMP.EQ.6)THEN  
        IF(ISECSPV.GE.1)THEN  
          OPEN(21,FILE='RSEDCV1.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.2)THEN  
          OPEN(22,FILE='RSEDCV2.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.3)THEN  
          OPEN(23,FILE='RSEDCV3.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.4)THEN  
          OPEN(24,FILE='RSEDCV4.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.5)THEN  
          OPEN(25,FILE='RSEDCV5.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.6)THEN  
          OPEN(26,FILE='RSEDCV6.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.7)THEN  
          OPEN(27,FILE='RSEDCV7.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.8)THEN  
          OPEN(28,FILE='RSEDCV8.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.9)THEN  
          OPEN(29,FILE='RSEDCV9.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        DO IS=1,ISECSPV  
          LUN2=20+IS  
          WRITE (LUN2,100)N,TIME  
          DO NN=1,NIJSPV(IS)  
            I=ISPV(NN,IS)  
            J=JSPV(NN,IS)  
            L=LIJ(I,J)  
            ZETA=HLPF(L)-HMP(L)  
            HBTMP=HMP(L)  
            WRITE(LUN2,200)IL(L),JL(L),DLON(L),DLAT(L),ZETA,HBTMP  
            WRITE(LUN2,250)(SEDTLPF(L,K),K=1,KC)  
          ENDDO  
          CLOSE(LUN2)  
        ENDDO  
      ENDIF  
C  
C ** NONCHOESIVE SEDIMENT  
C  
      IF(ITMP.EQ.7)THEN  
        IF(ISECSPV.GE.1)THEN  
          OPEN(31,FILE='RSNDCV1.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.2)THEN  
          OPEN(32,FILE='RSNDCV2.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.3)THEN  
          OPEN(33,FILE='RSNDCV3.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.4)THEN  
          OPEN(34,FILE='RSNDCV4.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.5)THEN  
          OPEN(35,FILE='RSNDCV5.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.6)THEN  
          OPEN(36,FILE='RSNDCV6.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.7)THEN  
          OPEN(37,FILE='RSNDCV7.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.8)THEN  
          OPEN(38,FILE='RSNDCV8.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        IF(ISECSPV.GE.9)THEN  
          OPEN(39,FILE='RSNDCV9.OUT',POSITION='APPEND',STATUS='UNKNOWN')  
        ENDIF  
        DO IS=1,ISECSPV  
          LUN3=30+IS  
          WRITE (LUN3,100)N,TIME  
          DO NN=1,NIJSPV(IS)  
            I=ISPV(NN,IS)  
            J=JSPV(NN,IS)  
            L=LIJ(I,J)  
            ZETA=HLPF(L)-HMP(L)  
            HBTMP=HMP(L)  
            WRITE(LUN3,200)IL(L),JL(L),DLON(L),DLAT(L),ZETA,HBTMP  
            WRITE(LUN3,250)(SNDTLPF(L,K),K=1,KC)  
          ENDDO  
          CLOSE(LUN3)  
        ENDDO  
      ENDIF  
 2000 CONTINUE  
   99 FORMAT(A40,2X,A20)  
  100 FORMAT(I10,F12.4)  
  101 FORMAT(2I10)  
  200 FORMAT(2I5,1X,6E14.6)  
  250 FORMAT(12E12.4)  
      RETURN  
      END  
