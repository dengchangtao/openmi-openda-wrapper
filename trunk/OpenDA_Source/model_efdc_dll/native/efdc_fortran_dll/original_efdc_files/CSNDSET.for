      FUNCTION CSNDSET(SND,SDEN,IOPT)  
C  
C CHANGE RECORD  
C **  CALCULATES HINDERED SETTLING CORRECTION FOR CLASS NS NONCOHESIVE  
C **  SEDIMENT  
C  
      ROPT=FLOAT(IOPT)  
      CSNDSET=(1.-SDEN*SND)**ROPT  
      RETURN  
      END  
