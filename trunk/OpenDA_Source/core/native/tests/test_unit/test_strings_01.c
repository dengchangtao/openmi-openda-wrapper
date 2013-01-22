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

////////////////////////////////////////////////////////////////////////////
// TEST THE COSTA-STRING OBJECTS


/* we also use the following auxiliary function */
int print_a_string( CTA_String test)
{
   char str[100];
   int retval;
   retval = CTA_String_Get(test,str);
   if (retval != CTA_OK) {return retval;};
   printf("Contents of the string are '%s'\n",str);
   return CTA_OK;
}


void test_strings(CuTest *tc) {

  //int StringTest()
  //{
   CTA_String test, testkeep;
   CTA_String test1, test2, testlijst[2];
   int retval;
   char str1ch[100];
   int i;
   int len;
   CTA_String str1;
   CTA_Vector vec1,vec2;
   int n = 4;
   CTA_Handle userdata;

   // we generate a lot of handles, but use only one of them
   retval = CTA_String_Create(&test);


   for (i=0; i<6; i++)
   {
      retval = CTA_String_Create(&test);
      //      printf(" handle %d\n", test); 
      if (i == 4) {testkeep = test;}
   }

   CuAssertIntEquals(tc, CTA_OK, retval);

   test = testkeep;

   //   printf("We made a cOSTA-string.\n");

   // Fill the string
   retval = CTA_String_Set(test,"This is a test");

   CuAssertIntEquals(tc, CTA_OK, retval);

   // Print the string using the function defined above
   retval = print_a_string(test);
   //   if (retval != CTA_OK)
   //  { printf("Wrong! Cannot print the string!\n"); return -1; }

   // Ask for the length of the string
   retval = CTA_String_GetLength(test,&len);
   //  if (retval != CTA_OK)
   //   { printf("WRONG! Unable to compute length of string!\n"); return -1; }
   //   printf("Length of string is %d characters\n",len);

   CuAssertIntEquals(tc, 14, len);

   // Increase length of string and repeat
   retval = CTA_String_Set(test,"This is a new test");
//     if (retval != CTA_OK)
//     { printf("Wrong! Unable to fill string \n"); return -1; }

   //   retval = print_a_string(test);

   retval = CTA_String_GetLength(test,&len);

   CuAssertIntEquals(tc, 18, len);

   // Destroy the string, create it again and repeat

   test1 = test;
   retval = CTA_String_Free(&test);
   test = test1;
   //   if (retval != CTA_OK)
   //   { printf("Wrong! unable to destroy string\n"); return -1; }

   retval = CTA_String_Set(test,"This is the third test");
   //  if (retval == CTA_OK)
   //    { printf("Wrong! We seem able to  use a destroyed string\n"); return -1; }
   //printf("A destroyed string cannot be used\n");

   retval = CTA_String_Free(&test);
   // if (retval == CTA_OK)
   //   { printf("Wrong! We seem able to destroy string twice \n"); return -1; }

   retval = CTA_String_Create(&test);
   if (retval != CTA_OK)
     //    { printf("Wrong! Unable to create string\n"); return -1; }
     // printf("Handle after re-creating =%d\n",test);

   retval = CTA_String_Set(test,"This is already the third test");
   // if (retval != CTA_OK)
   //   { printf("WRONG! unable to fill string \n"); return -1; }

   //   retval = print_a_string(test);


   retval = CTA_String_GetLength(test,&len);
   //  if (retval != CTA_OK)
   // { printf("Wrong! Unable to compute length of string!\n"); return -1; }
   //   printf("Length of string is %d characters\n",len);


   /* copy string */

   /*  Without first creating the new one; no problem */

   retval = CTA_String_Create(&test1);

   retval = CTA_String_Duplicate (test, &test2);

   CuAssertIntEquals(tc, CTA_OK, retval);

   /* Copying to a created string  */
   retval = CTA_String_Duplicate (test, &test1);
   //   printf("retval string_copy %d\n",retval);

   CuAssertIntEquals(tc, CTA_OK, retval);

   retval = CTA_String_Set(test,"-- test ---");
   retval = CTA_String_Set(test1,"+++test1++++");
   retval = CTA_String_Set(test2,"---Dit is alweer de derde test");
   /* concatenation */

   testlijst[0] = test1;
   testlijst[1] = test2;
   retval = CTA_String_Conc (test, testlijst[0]);
   retval = CTA_String_Conc (test, testlijst[1]);

   //   printf("retval string_concatenation %d\n ",retval);
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval = print_a_string(test);

   /* getvalue */
   retval = CTA_String_GetValue (test1, &str1ch, CTA_STRING);
   //printf("retval string_getvalue %d\n ",retval);
   //printf("%s \n", str1ch);
      CuAssertIntEquals(tc, CTA_OK, retval); 
   CuAssertStrEquals(tc,str1ch,"+++test1++++");


////////////////////////////////////////////////////////////////////////////
// TEST THE COSTA-STRING-VECTOR OBJECTS
//int StrVecTest()
//{

//  int retval;

  // Make 2 vectors and a string
  retval = CTA_String_Create(&str1);
   retval = CTA_String_Set(str1,"Test string");
 
  // printf("A string with contents 'Test string' has been made\n\n");


  retval=CTA_Vector_Create( CTA_DEFAULT_VECTOR, n, CTA_STRING, userdata, &vec1);
  // if (retval != CTA_OK)
  //    { printf("WRONG! Unable to make vector! %d\n",retval); return -1; }
  retval=CTA_Vector_Create( CTA_DEFAULT_VECTOR, n, CTA_STRING, userdata, &vec2);
  // if (retval != CTA_OK)
  //    { printf("WRONG! Unable to make vector! %d\n",retval); return -1; }

  // Fill a  string-vector and print
  retval=CTA_Vector_SetConstant( vec1, &str1, CTA_STRING);
  //  if (retval != CTA_OK)
  //    { printf("WRONG! Unable to fill vector!\n"); return -1; }

   CuAssertIntEquals(tc, CTA_OK, retval);

   //printf("A string vector has been made \n");

   //   printf("The contents of the vector is:\n");
   //retval = CTA_Vector_Export(vec1,CTA_FILE_STD_OUT);
   //if (retval != CTA_OK)
   //   { printf("WRONG! Unable to print vector!\n"); return -1; }



  // Change the contents of the vector and print
  retval=CTA_String_Set(str1,"Alternative text");
  // if (retval != CTA_OK)
  //    { printf("WRONG! Unable to fill string!\n"); return -1; }
  retval=CTA_Vector_SetVal( vec1, 2, &str1, CTA_STRING);
  //if (retval != CTA_OK)
  //   { printf("WRONG! Unable to adjust vector!\n"); return -1; }
  //  printf("The string vector has been adjusted\n\n");

   CuAssertIntEquals(tc, CTA_OK, retval);

   // printf("The contents of the vector is:\n");
   //  retval = CTA_Vector_Export(vec1,CTA_FILE_STD_OUT);
  //if (retval != CTA_OK)
  //   { printf("WRONG! Unable to print vector!\n"); return -1; }


  // Copy the vector, change it and print both
  retval=CTA_Vector_Copy( vec1, vec2);
  //  if (retval != CTA_OK)
  //  { printf("WRONG! Unable to copy vector!\n"); return -1; }

  retval=CTA_String_Set(str1,"Alt 2");
  //if (retval != CTA_OK)
    //  { printf("WRONG! Unable to fill string!\n"); return -1; }
  retval=CTA_Vector_SetVal( vec2, 1, &str1, CTA_STRING);
  //if (retval != CTA_OK)
  //  { printf("WRONG! Unable to change vector!\n"); return -1; }

   CuAssertIntEquals(tc, CTA_OK, retval);

   // printf("The vector has been copied; the copy is has been changed:\n\n");
   //printf("Contents of original vector is:\n");
   //  retval = CTA_Vector_Export(vec1,CTA_FILE_STD_OUT);
  //if (retval != CTA_OK)
    //  { printf("WRONG! Unable to print vector!\n"); return -1; }
  //printf("Contents of copy is:\n");
  //retval = CTA_Vector_Export(vec2,CTA_FILE_STD_OUT);
  //if (retval != CTA_OK)
  //  { printf("WRONG! Unable to print vector!\n"); return -1; }


  // Destroy all used COSTA objects
  retval = CTA_Vector_Free (&vec1);
  //if (retval != CTA_OK)
  //  { printf("WRONG! Unable to destroy Vector! \n"); return -1; }
  retval = CTA_Vector_Free (&vec2);
  //if (retval != CTA_OK)
  //  { printf("WRONG! Unable to destroy Vector! \n"); return -1; }

  retval = CTA_String_Free (&str1);
  //if (retval != CTA_OK)
  //  { printf("WRONG! Unable to destroy String! \n"); return -1; }
  //printf("String agina destroyed\n");
   CuAssertIntEquals(tc, CTA_OK, retval);


}

CuSuite* StringsSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test_strings);
    return suite;
}
