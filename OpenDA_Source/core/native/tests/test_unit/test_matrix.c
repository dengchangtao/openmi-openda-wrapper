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

#include "cta.h"
#include "CuTest.h"
#include <stdlib.h>
#include <math.h>


void test_matrix(CuTest *tc) {

   int retval, i;
   int const n1=5;
   int const n2=3;
   int const n3=2;

   CTA_Handle userdata;
   int rows, columns;
   double const1 = 2.5;
   double vals1[5], vals1b[5], vals2[3], vals3[2] ;
   double valtest[5*3];
   CTA_Matrix A, V;
   CTA_Vector vec_eig;
   double eigs1[3], eigs2[3], valsA[9], valsV1[9], valsV2[9];
   
   CTA_Vector hvec1,hvec2,hvec3;
   CTA_Matrix hmatrix1, hmatrix2;
   CTA_Handle usdata;
   CTA_Handle hand1;
   double val1;
   double const eps=1.0e-6;

  // initialize
   retval=CTA_Core_Initialise();
   //

   printf("BEGIN MATRIX test\n"); 

   /* Create some vectors */
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n1, CTA_DOUBLE,userdata, &hvec1);
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n2, CTA_DOUBLE,userdata, &hvec2);
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n3, CTA_DOUBLE,userdata, &hvec3);
   CuAssertIntEquals(tc, CTA_OK, retval);

   /* fill those  vectors */
   for (i=0;i<n1;i++){vals1[i]=1.0;}
   for (i=0;i<n1;i++){vals1b[i]=i;}
   for (i=0;i<n2;i++){vals2[i]=2.0;}
   for (i=0;i<n3;i++){vals3[i]=3.0;}

   retval=CTA_Vector_SetVals(hvec1,vals1,n1,CTA_DOUBLE);
   retval=CTA_Vector_SetVals(hvec2,vals2,n2,CTA_DOUBLE);
   retval=CTA_Vector_SetVals(hvec3,vals3,n3,CTA_DOUBLE);


   /* create a n1*n2 matrix */
   retval = CTA_Matrix_Create(CTA_DEFAULT_MATRIX, n1,n2, CTA_DOUBLE,
                      userdata, &hmatrix1);
   if (retval != CTA_OK)
     { printf("WRONG! unable to create matrix\n"); }
   //   printf("create matrix: %d\n ",retval);
   CuAssertIntEquals(tc, CTA_OK, retval);

   /* get size of the matrix */
   retval = CTA_Matrix_GetSize (hmatrix1, &rows, &columns);
   //   printf("getsize: length, width %d %d %d \n ",retval, rows, columns);
   CuAssertIntEquals(tc, CTA_OK, retval);

   /* fill the  matrix with a constant value */
   retval = CTA_Matrix_SetConstant (hmatrix1, &const1, CTA_DOUBLE);
   //  printf("setconstant: %d\n ",retval);
   CuAssertIntEquals(tc, CTA_OK, retval);

   /* The getval function. Note that counting begins with 1 instead of 0! */
   retval = CTA_Matrix_GetVal (hmatrix1, &val1, 1, 1, CTA_DOUBLE);
   //   printf("getval: %d %lg \n ",retval, val1);
   CuAssertDblEquals(tc, 2.5, val1,eps);


   /* Use the export function to write the contents of the matrix to screen */
   retval = CTA_Matrix_Export(hmatrix1, CTA_FILE_STDOUT);
   //   printf("export: %d\n ",retval);
   CuAssertIntEquals(tc, CTA_OK, retval);

   /* setcol is used to set an entire vector of the matrix */
   retval = CTA_Matrix_SetCol (hmatrix1, 1, hvec1);
   //   printf("setcol: %d\n ",retval);
   CuAssertIntEquals(tc, CTA_OK, retval);

   /* Fill the first column again,  this time with vec1 where  vec1[i] = i */ 
   retval=CTA_Vector_SetVals(hvec1,vals1b,n1,CTA_DOUBLE);
   retval = CTA_Matrix_SetCol (hmatrix1, 2, hvec1);
   // printf("setcol: %d\n ",retval);

   retval = CTA_Matrix_GetVal (hmatrix1, &val1, 4, 2, CTA_DOUBLE);
   CuAssertDblEquals(tc, 3.0, val1,eps);

   /* duplicate matrix. The contents of matrix2 are now the same as matrix1 */
   retval = CTA_Matrix_Duplicate(hmatrix1, &hmatrix2);
   //  printf("duplicate: %d\n ",retval);
   CuAssertIntEquals(tc, CTA_OK, retval);

   /* use setvals to fill the entire matrix. The filling is column-wise. */
  for (i=0;i<n1*n2;i++){valtest[i]=i*1.0;}
   retval = CTA_Matrix_SetVals(hmatrix2, valtest, n1,n2, CTA_DOUBLE);
   //   printf("setvals: %d\n ",retval);
      retval = CTA_Matrix_Export(hmatrix1, CTA_FILE_STDOUT);
   CuAssertIntEquals(tc, CTA_OK, retval);

   //setval
   //   retval = CTA_Matrix_Export(hmatrix1, CTA_FILE_STDOUT);
   retval = CTA_Matrix_SetVal (hmatrix2, &const1, 2,3, CTA_DOUBLE);
   CuAssertIntEquals(tc, CTA_OK, retval);
   retval = CTA_Matrix_GetVal (hmatrix2, &val1, 2, 3, CTA_DOUBLE);
   CuAssertDblEquals(tc, 2.5, val1,eps);
   // now the matrix is : [0 5 10; 1 6 2.5 ; 2 7 12;3 8 13; 4 9 14]


   retval = CTA_Matrix_Export(hmatrix2, CTA_FILE_STDOUT);
   retval = CTA_Matrix_GetVal (hmatrix1, &val1, 4, 2, CTA_DOUBLE);
   CuAssertDblEquals(tc, 3.0, val1,eps);

   retval = CTA_Matrix_GetDatatype (hmatrix1, &hand1);
   CuAssertIntEquals(tc, CTA_OK, retval);
   CuAssertIntEquals(tc, CTA_DOUBLE, hand1);


   /* test eigenvalues */
   /* A=   [0.56458  0.63596  0.90585
            0.59964  0.08302  0.42814
            0.55744  0.08708  0.80711]

     Then eig=[1.62210 -0.38290 0.21551]'

      V=[ -0.72236  -0.70635  -0.55942
          -0.43171   0.64865  -0.56147
          -0.54021   0.28341   0.60976]

   */

       valsA[0]=0.56458;  valsA[3]=0.63596;  valsA[6]=0.90585;
       valsA[1]=0.59964;  valsA[4]=0.08302;  valsA[7]=0.42814;
       valsA[2]=0.55744;  valsA[5]=0.08708;  valsA[8]=0.80711;
       
       valsV1[0]=-0.72236; valsV1[3]=-0.70635; valsV1[6]=-0.55942;
       valsV1[1]=-0.43171; valsV1[4]= 0.64865; valsV1[7]=-0.56147;
       valsV1[2]=-0.54021; valsV1[5]= 0.28341; valsV1[8]= 0.60976;

       eigs1[0]=1.62210; eigs1[1]=-0.38289; eigs1[2]=0.21551;
       
   retval=CTA_Matrix_Create(CTA_DEFAULT_MATRIX, 3, 3, CTA_DOUBLE, userdata, &A);
   retval=CTA_Matrix_Create(CTA_DEFAULT_MATRIX, 3, 3, CTA_DOUBLE, userdata, &V);
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, 3, CTA_DOUBLE, userdata, &vec_eig);
   retval=CTA_Matrix_SetVals(A,valsA,3,3, CTA_DOUBLE);
   
   retval=CTA_Matrix_EigVals(A, vec_eig, V);
   retval=CTA_Matrix_GetVals(V,valsV2 ,3,3, CTA_DOUBLE);
   retval=CTA_Vector_GetVals(vec_eig,eigs2 ,3,CTA_DOUBLE);

   printf("Eigenvectors\n");
   CTA_Matrix_Export(V,CTA_FILE_STDOUT);
   for (i=0;i<3; i++) {
      CuAssertDblEquals(tc, eigs1[i], eigs2[i],1e-5);
   }
   for (i=0;i<9; i++) {
      CuAssertDblEquals(tc, valsV1[i], valsV2[i],1e-5);
   }

   printf("END MATRIX test\n"); 


}




CuSuite* MatrixSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test_matrix);
    return suite;
}
