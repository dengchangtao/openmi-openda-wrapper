/*
COSTA: Problem solving environment for data assimilation
Copyright (C) 2007  Nils van Velzen

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


void test_reltable(CuTest *tc) {

CTA_RelTable reltab1, reltab2, reltab3;
CTA_Vector vselect, vec1, vec2;
int i;
int select[3];
int retval;
double vals[5];

   /* Create three a relation tables */
   retval=CTA_RelTable_Create(&reltab1);
   CuAssertIntEquals(tc, CTA_OK, retval);
   retval=CTA_RelTable_Create(&reltab2);
   CuAssertIntEquals(tc, CTA_OK, retval);
   retval=CTA_RelTable_Create(&reltab3);
   CuAssertIntEquals(tc, CTA_OK, retval);
   
   /* Create vectors */
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, 3, CTA_INTEGER, CTA_NULL,
                            &vselect);
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, 5, CTA_DOUBLE, CTA_NULL,
                            &vec1);
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, 5, CTA_DOUBLE, CTA_NULL,
                            &vec2);
   CuAssertIntEquals(tc, CTA_OK, retval);
   
   /* fill the first en second relation tables */
   /* Table 1:  Select elements 1 4 and 5 */
   /* Table 2:  Select elements 1 2 and 4 */

   select[0]=1; select[1]=4; select[2]=5;
   retval=CTA_Vector_SetVals(vselect,select,3,CTA_INTEGER);   
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_RelTable_SetSelect(reltab1, vselect);  
   CuAssertIntEquals(tc, CTA_OK, retval);
   
   select[0]=1; select[1]=2; select[2]=4;
   retval=CTA_Vector_SetVals(vselect,select,3,CTA_INTEGER);   
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_RelTable_SetSelect(reltab2, vselect);  
   CuAssertIntEquals(tc, CTA_OK, retval);
 
   /* create the fird relation table to be rel1 + inv(rel2) */
   /* this will result in a relation whith the relation
      1->1 4->2 and 5->4 
    */
   retval=CTA_RelTable_SetTableCombine(reltab3, reltab1, CTA_FALSE,
                                                reltab2, CTA_TRUE);
   CuAssertIntEquals(tc, CTA_OK, retval);

   /* apply the combined table to our vectors */
   /* vec1=[0 1 2 3 4];
      vec2=[10 11 12 13 14]
    */
   
    for (i=0;i<5;i++){vals[i]=(double) i;}
   retval=CTA_Vector_SetVals(vec1,vals,5,CTA_DOUBLE);   
   CuAssertIntEquals(tc, CTA_OK, retval);

   for (i=0;i<5;i++){vals[i]=((double) i)+10.0;}
   retval=CTA_Vector_SetVals(vec2,vals,5,CTA_DOUBLE);   
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_RelTable_Apply(reltab3, vec1, vec2); 
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_Vector_GetVals(vec2,vals,5,CTA_DOUBLE);   
   CuAssertIntEquals(tc, CTA_OK, retval);

   CuAssertDblEquals(tc, 0.0,  vals[0],1.0e-12);
   CuAssertDblEquals(tc, 3.0,  vals[1],1.0e-12);
   CuAssertDblEquals(tc, 12.0, vals[2],1.0e-12);
   CuAssertDblEquals(tc, 4.0,  vals[3],1.0e-12);
   CuAssertDblEquals(tc, 14.0, vals[4],1.0e-12);
  
   for (i=0;i<5;i++){vals[i]=((double) i)+10.0;}
   retval=CTA_Vector_SetVals(vec2,vals,5,CTA_DOUBLE);   
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_RelTable_ApplyInv(reltab3, vec2, vec1); 
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_Vector_GetVals(vec1,vals,5,CTA_DOUBLE);   
   CuAssertIntEquals(tc, CTA_OK, retval);

   CuAssertDblEquals(tc, 10.0, vals[0],1.0e-12);
   CuAssertDblEquals(tc, 1.0,  vals[1],1.0e-12);
   CuAssertDblEquals(tc, 2.0,  vals[2],1.0e-12);
   CuAssertDblEquals(tc, 11.0, vals[3],1.0e-12);
   CuAssertDblEquals(tc, 13.0,  vals[4],1.0e-12);

   for (i=0;i<5;i++){vals[i]=((double) i);}
   retval=CTA_Vector_SetVals(vec1,vals,5,CTA_DOUBLE);   
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_RelTable_Apply(reltab1, vec1, vec2); 
   CuAssertIntEquals(tc, CTA_OK, retval);


   retval=CTA_Vector_GetVals(vec2,vals,5,CTA_DOUBLE);   
   CuAssertIntEquals(tc, CTA_OK, retval);
   CuAssertDblEquals(tc, 0.0,  vals[0], 1.0e-12);
   CuAssertDblEquals(tc, 3.0,  vals[1], 1.0e-12);
   CuAssertDblEquals(tc, 4.0,  vals[2], 1.0e-12);
   CuAssertDblEquals(tc, 13.0, vals[3], 1.0e-12);
   CuAssertDblEquals(tc, 14.0, vals[4], 1.0e-12);

   for (i=0;i<5;i++){vals[i]=((double) i)+10.0;}
   retval=CTA_Vector_SetVals(vec2,vals,5,CTA_DOUBLE);   
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_RelTable_ApplyInv(reltab2, vec2, vec1); 
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_Vector_GetVals(vec1,vals,5,CTA_DOUBLE);   
   CuAssertIntEquals(tc, CTA_OK, retval);
   CuAssertDblEquals(tc, 10.0,  vals[0], 1.0e-12);
   CuAssertDblEquals(tc, 11.0,  vals[1], 1.0e-12);
   CuAssertDblEquals(tc, 2.0,  vals[2],  1.0e-12);
   CuAssertDblEquals(tc, 12.0, vals[3],  1.0e-12);
   CuAssertDblEquals(tc, 4.0, vals[4],   1.0e-12);

   /* Free the relation tables */
   retval=CTA_RelTable_Free(&reltab1);
   CuAssertIntEquals(tc, CTA_OK, retval);
   retval=CTA_RelTable_Free(&reltab2);
   CuAssertIntEquals(tc, CTA_OK, retval);
   retval=CTA_RelTable_Free(&reltab3);
   CuAssertIntEquals(tc, CTA_OK, retval);

   printf("END OF RELATION TABLE TEST \n");
}


CuSuite* RelTableSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test_reltable);
    return suite;
}
