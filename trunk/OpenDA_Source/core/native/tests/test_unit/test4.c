/*
COSTA: Problem solving environment for data assimilation
Copyright (C) 2005  Nils van Velzen

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

#include <stdlib.h>
#include <math.h>
#include "CuTest.h"
#include "cta.h"

// General use of vectors and vector functions


void test4(CuTest *tc) {
   double const eps=1.0e-6;
   int i;  
   int const n=3;
   double vals1[3], vals2[3], vals3[3],vals4[4];
   CTA_Handle userdata;
   double alpha,dotprod,norm2;
   int iloc;
   int  retval;
   int nsize, maxlen;
   CTA_String str1;
 
   CTA_Datatype datatype;
   CTA_Vector hvector1,hvector2,hvector3,hvector5;
   CTA_Vector hformat;
   CTA_Vector tabel[3];

   double sqrt20;


   /* Initialise default installation of vector object */
   retval=CTA_Core_Initialise();

   /* Create a vector */
   for (i=0;i<n;i++){
      vals1[i]=(double) i;
      vals2[i]=(double) i*i;
   }
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n, CTA_DOUBLE,userdata,&hvector1);
   retval=CTA_Vector_SetVals(hvector1,vals1,n,CTA_DOUBLE);
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n, CTA_DOUBLE,userdata,&hvector2);
   retval=CTA_Vector_SetVals(hvector2,vals2,n,CTA_DOUBLE);


   /* ce Fill table with three vectors */

   tabel[0] = hvector1;
   tabel[1] = hvector2;
   tabel[2] = hvector2;


   /*  hformat : a string */
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, 3, CTA_STRING, userdata, &hformat);
   retval = CTA_String_Create(&str1);
   retval = CTA_String_Set(str1,"|%12.3g");
   retval = CTA_Vector_SetVal(hformat,1,&str1,CTA_STRING);
   retval = CTA_String_Set(str1,"%12.4g    ");
   retval = CTA_Vector_SetVal(hformat,2,&str1,CTA_STRING);
   retval = CTA_String_Set(str1,"%12.3g |");
   retval = CTA_Vector_SetVal(hformat,3,&str1,CTA_STRING);

   CuAssertIntEquals(tc, CTA_OK, retval);

   /* SCAL: scale a vector */
   alpha=2.0;
   retval=CTA_Vector_Scal(hvector1,alpha);

   retval=CTA_Vector_GetVals(hvector1,vals1,n,CTA_DOUBLE);

   CuAssertDblEquals(tc, 0.0, vals1[0],eps);
   CuAssertDblEquals(tc, 2.0, vals1[1],eps);
   CuAssertDblEquals(tc, 4.0, vals1[2],eps);

   /* Copy */
   retval=CTA_Vector_Copy(hvector1,hvector2);

   retval=CTA_Vector_GetVals(hvector2,vals2,n,CTA_DOUBLE);

   CuAssertDblEquals(tc, 0.0, vals2[0],eps);
   CuAssertDblEquals(tc, 2.0, vals2[1],eps);
   CuAssertDblEquals(tc, 4.0, vals2[2],eps);

   /* AxPy */
   alpha=0.5;
   retval=CTA_Vector_Axpy(hvector2,alpha,hvector1);
   retval=CTA_Vector_GetVals(hvector2,vals2,n,CTA_DOUBLE);

   CuAssertDblEquals(tc, 0.0, vals2[0],eps);
   CuAssertDblEquals(tc, 3.0, vals2[1],eps);
   CuAssertDblEquals(tc, 6.0, vals2[2],eps);

   /* ElmDiv y=[1 2 4] and x=[2 2 2]*/
   vals1[0]=1.0;
   vals1[1]=2.0;
   vals1[2]=4.0;
   vals2[0]=2.0;
   vals2[1]=2.0;
   vals2[2]=2.0;
   retval=CTA_Vector_SetVals(hvector1,vals1,n,CTA_DOUBLE);
   retval=CTA_Vector_SetVals(hvector2,vals2,n,CTA_DOUBLE);
   retval=CTA_Vector_ElmDiv(hvector1,hvector2);
   retval=CTA_Vector_GetVals(hvector1,vals1,n,CTA_DOUBLE);

   CuAssertDblEquals(tc, 2.0, vals1[0],eps);
   CuAssertDblEquals(tc, 1.0, vals1[1],eps);
   CuAssertDblEquals(tc, 0.5, vals1[2],eps);
   
   
   /* Dot product of two vectors [0 2 4] and [0 3 6]  */
   vals1[0]=0.0;
   vals1[1]=2.0;
   vals1[2]=4.0;
   vals2[0]=0.0;
   vals2[1]=3.0;
   vals2[2]=6.0;
   retval=CTA_Vector_SetVals(hvector1,vals1,n,CTA_DOUBLE);
   retval=CTA_Vector_SetVals(hvector2,vals2,n,CTA_DOUBLE);
   
   retval=CTA_Vector_Dot(hvector1,hvector2,&dotprod);

   CuAssertDblEquals(tc, 30.0, dotprod,eps);


   /* Length of vecotr 1 = [0 2 4] */
   retval=CTA_Vector_Nrm2(hvector1,&norm2);
   
   sqrt20=sqrt(20.0);
   CuAssertDblEquals(tc, sqrt20, norm2,eps);

   /* position of largest element  */
   retval=CTA_Vector_Amax(hvector1,&iloc);

   CuAssertIntEquals(tc, 3, iloc);

   retval=CTA_Vector_Duplicate(hvector1,&hvector3);

   retval=CTA_Vector_GetSize(hvector3,&nsize);
   CuAssertIntEquals(tc, 3, nsize);

   retval=CTA_Vector_Copy(hvector1,hvector3);

   retval=CTA_Vector_GetVals(hvector3,vals3,n,CTA_DOUBLE);

   CuAssertDblEquals(tc, 0.0, vals3[0],eps);
   CuAssertDblEquals(tc, 2.0, vals3[1],eps);
   CuAssertDblEquals(tc, 4.0, vals3[2],eps);


   /*  append vector */ 
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n+1,CTA_DOUBLE,userdata,&hvector5);
   retval =CTA_Vector_AppendVal(hvector3,&vals2[1] ,CTA_DOUBLE);
   retval=CTA_Vector_GetSize(hvector3,&nsize);
   /*   printf("Size of zp is %d\n",nsize);*/

   CuAssertIntEquals(tc, n+1, nsize);

   /* appended vector [0 2 4] with 3 */ 
   retval=CTA_Vector_GetVals(hvector3,vals4,n+1,CTA_DOUBLE);

   CuAssertDblEquals(tc, 0.0, vals4[0],eps);
   CuAssertDblEquals(tc, 2.0, vals4[1],eps);
   CuAssertDblEquals(tc, 4.0, vals4[2],eps);
   CuAssertDblEquals(tc, 3.0, vals4[3],eps);

   /*  printf("retval=%d\n",retval);*/



   //   printf ("zp=[");
   //for (i=0;i<n+1;i++) {printf ("%lg ",vals4[i]);};
   //printf ("]\n");


   /* maxlen */
   retval=CTA_Vector_GetMaxLen (hformat, &maxlen);
   /*   printf("maxlen of zp is %d\n",maxlen); */
   CuAssertIntEquals(tc, 10, maxlen);

   /* print tabel */

   retval=CTA_Vector_Print_Table (tabel,3,hformat);
  
 /*   printf("print tabel %d\n",retval); */


   /* get datatype */
   retval = CTA_Vector_GetDatatype(hvector1,&datatype);
   CuAssertIntEquals(tc, CTA_OK, retval);

   /* export vector */
   retval=CTA_Vector_Export(hvector1, CTA_FILE_STDOUT) ;
   CuAssertIntEquals(tc, CTA_OK, retval);
   /* printf("retval ctavector_export %d\n",retval); */

}

CuSuite* VectorGetSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test4);
    return suite;
}
