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

// simple tree test

void test_tree_01(CuTest *tc) {

   CTA_Tree    hroot;
   CTA_Tree    hsub1;
   CTA_Tree    hsub2;
   CTA_Tree    hsub3;
   CTA_String  htxt1;
   CTA_String  htxt2;
   CTA_String  htxt3;
   CTA_String  htxt4;
   CTA_String  htxt5;
   CTA_String  htxt6;
   char str[9];
   CTA_Handle hhandle;
   CTA_Tree hhandlet;
   int retval, count;

   CTA_Core_Initialise();

   CTA_String_Create(&htxt1);
   CTA_String_Set(htxt1, "TesT1TexT");
   CTA_String_Create(&htxt2);
   CTA_String_Set(htxt2, "TesT2TexT");
   CTA_String_Create(&htxt3);
   CTA_String_Set(htxt3, "TesT3TexT");
   CTA_String_Create(&htxt4);
   CTA_String_Set(htxt4, "TesT4TexT");
   CTA_Tree_Create(&hroot);
   CTA_Tree_Create(&hsub1);
   CTA_Tree_Create(&hsub2);
   CTA_Tree_Create(&hsub3);
   
   CTA_Tree_AddHandle(hroot, "01", htxt1);
   CTA_Tree_AddHandle(hroot, "02", hsub1);
   
   CTA_Tree_AddHandle(hsub1, "11", hsub2);
   CTA_Tree_AddHandle(hsub1, "12", htxt2);
   CTA_Tree_AddHandle(hsub1, "13",  hsub3);

   CTA_Tree_AddHandle(hsub2, "21", htxt3);
   
   CTA_Tree_AddHandle(hsub3, "31", htxt4);


   retval = CTA_Tree_CountItems(hroot, &count );
      printf ("6) tree_countitems retval=%d %d  \n",retval,count);

      

   CuAssertIntEquals(tc, CTA_OK, retval);

   retval = CTA_Tree_CountItems(hsub3, &count );

   /*   printf ("7) retval=%d %d  \n",retval,count);*/
   CuAssertIntEquals(tc, 1, count);

   retval = CTA_Tree_GetItem (hroot, 1, &hhandle);
   CuAssertIntEquals(tc, hhandle, htxt1);

   retval = CTA_Tree_GetHandleStr(hroot,"/02/13/31",&hhandle);
      printf ("------------------tree: retval=%d   \n",retval); 
   CuAssertIntEquals(tc, hhandle, htxt4);

   retval = CTA_Tree_GetItem (hroot, 2, &hhandlet);
      printf ("6) retval=%d %d %d \n",retval,hhandlet,hsub1); 
      
   retval = CTA_Tree_CountItems(hhandlet, &count );
      printf ("8) tree_countitems retval=%d %d  \n",retval,count);

   CuAssertIntEquals(tc, hhandlet, hsub1);

   retval = CTA_Tree_GetItemValue ( hroot,1,&htxt5, CTA_STRING);  
   //printf ("6) retval=%d  \n",retval);

   CuAssertIntEquals(tc, CTA_OK, retval);

   retval = CTA_String_Get(htxt5,str);
   CuAssertStrEquals(tc,str,"TesT1TexT");

     CTA_Tree_Print(hroot);

   CTA_Tree_Free(&hroot);
}




CuSuite* TreeSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test_tree_01);
    return suite;
}
