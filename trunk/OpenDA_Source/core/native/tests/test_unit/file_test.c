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



void file_test(CuTest *tc) {


////////////////////////////////////////////////////////////////////////////
// TEST THE COSTA-FILE OBJECTS
//int FileTest()

   int retval;
   CTA_File test;
   FILE *file;
   CTA_Handle test1;

   retval = CTA_File_Create(&test);
   if (retval != CTA_OK)
      { printf("WRONG! unable to create file!\n"); }
   if (test==CTA_NULL)
      { printf("WRONG! We got a null-handle\n"); }

   CuAssertIntEquals(tc, CTA_OK, retval);
   //   printf("We made a COSTA-file \n");

   retval = CTA_File_Set(test,stdout);
   retval = CTA_File_Set(test,stdout);
   retval = CTA_File_Set(test,stdout);
   if (retval != CTA_OK)
      { printf("WRONG! Unable to fill file!\n"); }

   CuAssertIntEquals(tc, CTA_OK, retval);
   //   printf("Stdout is in de COSTA-file gestopt.\n");

   file=stdin;
   retval = CTA_File_Get(test,&file);
   if (retval != CTA_OK)
      { printf("WRONG! Unable to get the file!\n"); }
   if (file != stdout)
      { printf("WRONG! the returned file is not equal to stdout\n");}

   CuAssertIntEquals(tc, CTA_OK, retval);
   //   printf("I took the FILE* pointer out of the COSTA-file.\n");

   retval = CTA_String_Free(&test);
   if (retval == CTA_OK)
      { printf("WRONG! Cannot destroy the file as if it is a string!\n"); }

   //   printf("Files must not be apporached as string.\n");

   test1=test;
   retval = CTA_File_Free(&test);
   test=test1;
   if (retval != CTA_OK)
      { printf("WRONG! Unable to destroy file!\n"); }

   CuAssertIntEquals(tc, CTA_OK, retval);
   //   printf("De file is vernietigd.\n");

   retval = CTA_File_Free(&test);
   if (retval == CTA_OK)
      { printf("WRONG! We seem able to destroy the file twice!\n"); }


   retval = CTA_File_Set(test,stdout);
   if (retval == CTA_OK)
   {
      printf("WRONG! We can put the FILE-handle in a destroyed file!\n");
      ;
   }

   retval = CTA_File_Create(&test);
   if (retval != CTA_OK)
      { printf("WRONG! unable to create file\n"); }

   //   printf("We created a file \n");

   retval = CTA_File_Set(test,stdout);
   retval = CTA_File_Set(test,stdout);
   retval = CTA_File_Set(test,stdout);
   if (retval != CTA_OK)
      { printf("WRONG! unable to fill file!\n"); }

   CuAssertIntEquals(tc, CTA_OK, retval);

   //   printf("Stdout is in de COSTA-file gestopt.\n");

   file=stdin;
   retval = CTA_File_Get(test,&file);
   if (retval != CTA_OK)
      { printf("WRONG! unable to get file!\n"); }
   if (file != stdout)
   {
      printf("WRONG! the returned file is not equal to  stdout\n");
      ;
   }

   CuAssertIntEquals(tc, CTA_OK, retval);

   printf("-------------------------------------------------------\n");
   printf("           The FILE-TEST has ended!\n");
   printf("-------------------------------------------------------\n");

}






CuSuite* FileSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, file_test);
    return suite;
}

