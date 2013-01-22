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


void test_packing(CuTest *tc) {
   double const eps=1.0e-6;
   int i;  
   int const n=3;
   double vals1[3], vals2[3];
   CTA_Handle userdata;
   int  retval;

   CTA_Vector hvector1,hvector2;
   CTA_String hstring1,hstring2;
   CTA_Pack   hpack;

   /* Initialise default installation of vector object */
   retval=CTA_Core_Initialise();

   for (i=0;i<n;i++){
      vals1[i]=(double) i;
      vals2[i]=(double) i*i;
   }
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n, CTA_DOUBLE,userdata, &hvector1);

   CuAssertIntEquals(tc, CTA_OK, retval);
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n, CTA_DOUBLE,userdata, &hvector2);
   CuAssertIntEquals(tc, CTA_OK, retval);
   retval=CTA_Vector_SetVals(hvector1,vals1,n,CTA_DOUBLE);
   
   retval=CTA_String_Create(&hstring1);
   retval=CTA_String_Create(&hstring2);
   
   retval=CTA_String_Set(hstring1, "A string we are going to pack and unpack");

   // Pack vector and string 
   retval=CTA_Pack_Create(0,&hpack);
   CuAssertIntEquals(tc, CTA_OK, retval);
   retval=CTA_Vector_Export(hvector1,hpack);
   CuAssertIntEquals(tc, CTA_OK, retval);
   retval=CTA_String_Export(hstring1,hpack);
   CuAssertIntEquals(tc, CTA_OK, retval);

   // unpack a vector and string  
   retval=CTA_Vector_Import(hvector2,hpack);
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval=CTA_Vector_GetVals(hvector2,vals2,n,CTA_DOUBLE);
   CuAssertIntEquals(tc, CTA_OK, retval);

   CuAssertDblEquals(tc, 0.0, vals2[0],eps);
   CuAssertDblEquals(tc, 1.0, vals2[1],eps);
   CuAssertDblEquals(tc, 2.0, vals2[2],eps);

   retval=CTA_String_Import(hstring2,hpack);
   CuAssertStrEquals(tc,"A string we are going to pack and unpack",
   CTAI_String_GetPtr(hstring2));

   CTA_String_Free(&hstring1);
   CTA_String_Free(&hstring2);
   CTA_Vector_Free(&hvector1);
   CTA_Vector_Free(&hvector2);
   CTA_Pack_Free(&hpack);
}

CuSuite* PackSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test_packing);
    return suite;
}
