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


void test_obsdescr_table(CuTest *tc) {

   CTA_String sfilename;
   CTA_StochObs hsobs;
   CTA_ObsDescr hobsdescr;
   CTA_ObsDescr hdescr_tab;
   int nmeasr1, nmeasr2, nkeys1, nkeys2;
   CTA_Vector vKeys1, vKeys2, vCol1, vCol2;
   CTA_Pack hpack;
   int ierr;
   int dum;
   
   /* First create a DEFAULT_SQL_Stochastic observer */
   ierr=CTA_String_Create(&sfilename);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_String_Set(sfilename,"obs.sql");
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_SObs_Create(CTA_DEFAULT_SOBS,sfilename,&hsobs);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   /* Get the obsdescr-component */
   ierr=CTA_SObs_GetDescription(hsobs, &hobsdescr);
   CuAssertIntEquals(tc, CTA_OK, ierr);


   /* Create a "Table" observation description component */
   ierr=CTA_ObsDescr_Create(CTA_OBSDESCR_TABLE,hobsdescr, &hdescr_tab);
   CuAssertIntEquals(tc, CTA_OK, ierr);


   ierr=CTA_ObsDescr_Create(CTA_OBSDESCR_TABLE,hobsdescr, &hdescr_tab);
   CuAssertIntEquals(tc, CTA_OK, ierr);


   ierr=CTA_ObsDescr_Create(CTA_OBSDESCR_TABLE,hobsdescr, &hdescr_tab);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   /* Ask a number of keys */
   ierr=CTA_ObsDescr_Property_Count(hobsdescr, &nkeys1);
   printf("nkeys1 %d \n",nkeys1);

   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_ObsDescr_Property_Count(hdescr_tab, &nkeys2);
   printf("nkeys2 %d \n",nkeys2);

   CuAssertIntEquals(tc, CTA_OK, ierr);
   
   /* Ask a number of obs */
   ierr=CTA_ObsDescr_Observation_Count(hobsdescr, &nmeasr1);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_ObsDescr_Observation_Count(hdescr_tab, &nmeasr2);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   printf("nmeasr1 nmeasr2 %d %d  \n",nmeasr1, nmeasr2);

   /* create vectors for holding keys and columns */
   ierr=CTA_Vector_Create(CTA_DEFAULT_VECTOR, nkeys1,  CTA_STRING, dum, &vKeys1);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_Vector_Create(CTA_DEFAULT_VECTOR, nkeys2,  CTA_STRING, dum, &vKeys2);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   CuAssertIntEquals(tc, nkeys1, nkeys2);

   ierr=CTA_Vector_Create(CTA_DEFAULT_VECTOR, nmeasr1, CTA_DOUBLE, dum, &vCol1);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_Vector_Create(CTA_DEFAULT_VECTOR, nmeasr2, CTA_DOUBLE, dum, &vCol2);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   CuAssertIntEquals(tc, nmeasr1, nmeasr2);

   
   /* get all keys  */
   ierr=CTA_ObsDescr_Get_PropertyKeys(hobsdescr, vKeys1);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_ObsDescr_Get_PropertyKeys(hdescr_tab, vKeys2);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   ierr=CTA_ObsDescr_Get_ValueProperties(hobsdescr, "EAST",vCol1,
                                    CTA_DOUBLE);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_ObsDescr_Get_ValueProperties(hdescr_tab, "EAST",vCol2,
                                    CTA_DOUBLE);

   
   ierr=CTA_Vector_Export(vCol1,CTA_FILE_STDOUT);

   CuAssertIntEquals(tc, CTA_OK, ierr);


   /* Pack the table-obsdescr */
   ierr=CTA_Pack_Create(0,&hpack);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_ObsDescr_Export(hdescr_tab, hpack);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   /* unpack (create) the table-obsdescr */
   ierr=CTA_ObsDescr_Create(CTA_OBSDESCR_TABLE,hpack, &hdescr_tab);
   CuAssertIntEquals(tc, CTA_OK, ierr);



   /* Free the obs-descr */
   ierr=CTA_ObsDescr_Free(&hobsdescr);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_ObsDescr_Free(&hdescr_tab);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   /* Free all our workstuff */
   ierr=CTA_Pack_Free(&hpack);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_SObs_Free(&hsobs);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_Vector_Free(&vKeys1);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_Vector_Free(&vKeys2);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_Vector_Free(&vCol1);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_Vector_Free(&vCol2);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   

//   CuAssertDblEquals(tc, 0.0, vals2[0],eps);
//   retval=CTA_String_Import(hstring2,hpack);
//   CuAssertStrEquals(tc,"A string we are going to pack and unpack",
//   CTAI_String_GetPtr(hstring2));

}

CuSuite* ObsdescrTableSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test_obsdescr_table);
    return suite;
}
