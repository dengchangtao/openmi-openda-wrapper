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
#include "CuTest.h"
#include "cta.h"

void myplus(int a, int b, int *c){
   *c=a+b;
}
void mymult(int a, int b, int *c){
       *c=a*b;
}
void test1a(CuTest *tc) {
  /* Make a user function myplus */
   CTA_Func hfunc; //function handle
   CTA_Intf hintf; //interface handle
   CTA_Function *function;
   int      result;

#if HAVE_LIBNETCDF
   printf("netcdf IS USED!\n");

#else
   printf("netcdf IS NOT USED!\n");

#endif

   
   hintf=CTA_NULL;
   CTA_Func_Create("myfunc",&myplus,hintf,&hfunc);
   CTA_Func_GetFunc(hfunc,&function);
   function(10,11,&result);
   CTA_Func_Free(&hfunc);

    CuAssertIntEquals(tc, 21, result);
}
void test1b(CuTest *tc) {
  /* Make a user function mymult */

   CTA_Func hfunc; //function handle
   CTA_Intf hintf; //interface handle
   CTA_Function *function;
   int      result;
   
   hintf=CTA_NULL;
   CTA_Func_Create("myfunc",&mymult,hintf,&hfunc);
   CTA_Func_GetFunc(hfunc,&function);
   function(10,11,&result);
   CTA_Func_Free(&hfunc);

   CuAssertIntEquals(tc, 110, result);
}


void test1c(CuTest *tc) {
   int i,j;
   int const MAXLOOP=5;
   int const MAXFUNC=200;
   CTA_Func hfunc1,hfunc2,hfunc3,hfunc4,hfunc5,hfunc[200];
   
   CTA_Intf hintf; 
   CTA_String name1,name2,name3,name4,name5;
   CTA_Datatype datatype1, datatype2, datatype3, datatype4, datatype5;

   CTA_String_Create(&name1);
   CTA_String_Create(&name2);
   CTA_String_Create(&name3);
   CTA_String_Create(&name4);
   CTA_String_Create(&name5);

   // create and delete a large number of functions in order to 
   // trigger reallocation and use of parameter administration
   for (i=0; i<MAXLOOP; i++){
      for (j=0;j<MAXFUNC;j++){
		 hintf=CTA_NULL;
         CTA_Func_Create("Name not OK",&mymult,hintf,&hfunc[j]);
      };
      for (j=MAXFUNC-1;j>=0;j--){
         CTA_Func_Free(&hfunc[j]);
      }
   };             
   CTA_Func_Create("func1",&mymult,hintf,&hfunc1);
   CTA_Func_Create("func2",&myplus,hintf,&hfunc2);
   CTA_Func_Create("func3",&myplus,hintf,&hfunc3);
   CTA_Func_Create("func4",&myplus,hintf,&hfunc4);
   CTA_Func_Create("func5",&mymult,hintf,&hfunc5);

   // check handles:
   // NOTE: Implementation dependent tests!
/*   CuAssertIntEquals(tc, (int) hfunc1, 0);
   CuAssertIntEquals(tc, (int) hfunc2, 1);
   CuAssertIntEquals(tc, (int) hfunc3, 2);
   CuAssertIntEquals(tc, (int) hfunc4, 3);
   CuAssertIntEquals(tc, (int) hfunc5, 4);
*/

   CTA_Handle_GetDatatype(hfunc1, &datatype1);
   CTA_Handle_GetDatatype(hfunc2, &datatype2);
   CTA_Handle_GetDatatype(hfunc3, &datatype3);
   CTA_Handle_GetDatatype(hfunc4, &datatype4);
   CTA_Handle_GetDatatype(hfunc5, &datatype5);

   // check datatypes
   CuAssertIntEquals(tc, (int) datatype1, (int) CTA_FUNCTION );
   CuAssertIntEquals(tc, (int) datatype2, (int) CTA_FUNCTION );
   CuAssertIntEquals(tc, (int) datatype3, (int) CTA_FUNCTION );
   CuAssertIntEquals(tc, (int) datatype4, (int) CTA_FUNCTION );
   CuAssertIntEquals(tc, (int) datatype5, (int) CTA_FUNCTION );

   CTA_Handle_GetName(hfunc1,  name1);
   CTA_Handle_GetName(hfunc2,  name2);
   CTA_Handle_GetName(hfunc3,  name3);
   CTA_Handle_GetName(hfunc4,  name4);
   CTA_Handle_GetName(hfunc5,  name5);

   // check names
   CuAssertStrEquals(tc, name1, "func1");
   CuAssertStrEquals(tc, name2, "func2");
   CuAssertStrEquals(tc, name3, "func3");
   CuAssertStrEquals(tc, name4, "func4");
   CuAssertStrEquals(tc, name5, "func5");

   CTA_Func_Free(&hfunc5);
   CTA_Func_Free(&hfunc4);
   CTA_Func_Free(&hfunc3);
   CTA_Func_Free(&hfunc2);
   CTA_Func_Free(&hfunc1);

   CTA_String_Free(&name1);
   CTA_String_Free(&name2);
   CTA_String_Free(&name3);
   CTA_String_Free(&name4);
   CTA_String_Free(&name5);
};    

void test1d(CuTest *tc) {
  // test: check arguments of the function
   CTA_Func hfunc; //function handle
   CTA_Intf hintf; //interface handle
   CTA_Intf hintf_check; //interface handle
   CTA_Function *function;
   int      result;
   CTA_Datatype argtyp[3];
   int      ierr;
   BOOL     ok;

   // Create interface
   argtyp[0]=CTA_INTEGER;
   argtyp[1]=CTA_INTEGER;
   argtyp[2]=CTA_INTEGER;
   ierr=CTA_Intf_Create("myplus", argtyp, 3, &hintf);
   CuAssertIntEquals(tc, 0, ierr);

   argtyp[1]=CTA_VOID;
   argtyp[2]=CTA_VOID;
   ierr=CTA_Intf_Create("check", argtyp, 3, &hintf_check);
   CuAssertIntEquals(tc, 0, ierr);

   // Create function
   ierr=CTA_Func_Create("myfunc",&myplus,hintf,&hfunc);
   CuAssertIntEquals(tc, 0, ierr);

   // Get interface
   ierr=CTA_Func_GetFunc(hfunc,&function);
   CuAssertIntEquals(tc, 0, ierr);

   // Check interface
   ierr=CTA_Intf_Match_ha(hintf,argtyp,3,&ok);
   CuAssertIntEquals(tc, 0, ierr);
   CuAssertIntEquals(tc, (int) TRUE, (int) ok);

   ierr=CTA_Intf_Match_hh(hintf_check,hintf,&ok);
   CuAssertIntEquals(tc, 0, ierr);
   CuAssertIntEquals(tc, (int) TRUE, (int) ok);

   function(10,11,&result);
   CTA_Func_Free(&hfunc);

   CuAssertIntEquals(tc, 21, result);
}







CuSuite* StrUtilGetSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test1a);
    SUITE_ADD_TEST(suite, test1b);
    SUITE_ADD_TEST(suite, test1c);
    SUITE_ADD_TEST(suite, test1d);
    return suite;
}
                      
