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


void test_obsdescr(CuTest *tc) {

   CTA_String sfilename;
   CTA_StochObs hsobs;
   CTA_ObsDescr hobsdescr, hobsdescr2, hobsdescr_sub;
   CTA_ObsDescr hdescr_tab;
   int nmeasr1, nmeasr2, nkeys1, nkeys2;
   CTA_Vector vKeys1, vKeys2, vCol1, vCol2, vec1, vec2;
   CTA_Pack hpack;
   CTA_String selection;
   CTA_Time timespan;
   int ierr, dum,i;
   int ival;
   int usrdata[1];
   double vals1[6];
   CTA_RelTable reltable;
   
   
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

   /* Greate sub-selection of observation description */
   /* Ask a number of obs */
   ierr=CTA_ObsDescr_Observation_Count(hobsdescr, &nmeasr1);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   CuAssertIntEquals(tc, 6, nmeasr1);

   /* Do a timeselect on the observation description */
   ierr=CTA_Time_Create(&timespan);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_Time_SetSpan(timespan,0.0, 3.0);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_ObsDescr_CreateTimSel(hobsdescr, timespan, CTA_NULL, &hobsdescr2);

   
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_ObsDescr_Observation_Count(hobsdescr2, &nmeasr2);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   CuAssertIntEquals(tc, 3, nmeasr2);

   /* Ask a number of keys */
   ierr=CTA_ObsDescr_Property_Count(hobsdescr, &nkeys1);
   ierr=CTA_ObsDescr_Property_Count(hobsdescr2, &nkeys2);
   CuAssertIntEquals(tc, nkeys1, nkeys2);


   /* test the creation of a selection */
   ierr=CTA_RelTable_Create(&reltable);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   ierr=CTA_String_Create(&selection);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   ierr=CTA_String_Set(selection,"TIME > 3");
   CuAssertIntEquals(tc, CTA_OK, ierr);
   
   ierr=CTA_ObsDescr_CreateSel(hobsdescr, selection, 
                                reltable, &hobsdescr_sub);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   ierr=CTA_Vector_Create(CTA_DEFAULT_VECTOR,6,CTA_INTEGER,CTA_NULL,&vec1);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_Vector_Create(CTA_DEFAULT_VECTOR,2,CTA_INTEGER,CTA_NULL,&vec2);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   for (i=1;i<=6;i++){
      ierr=CTA_Vector_SetVal(vec1,i, &i,CTA_INTEGER);
      CuAssertIntEquals(tc, CTA_OK, ierr);
   }
   
  ierr=CTA_RelTable_Apply(reltable, vec1, vec2);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   for (i=1;i<=2;i++){
      ierr=CTA_Vector_GetVal(vec2,i, &ival,CTA_INTEGER);
      CuAssertIntEquals(tc, CTA_OK, ierr);
      CuAssertIntEquals(tc, i+4, ival);
   }

   ierr=CTA_String_Free(&selection);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   ierr=CTA_RelTable_Free(&reltable);
   CuAssertIntEquals(tc, CTA_OK, ierr);

   ierr=CTA_ObsDescr_Free(&hobsdescr);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_ObsDescr_Free(&hobsdescr2);
   CuAssertIntEquals(tc, CTA_OK, ierr);
   ierr=CTA_ObsDescr_Free(&hobsdescr_sub);
   CuAssertIntEquals(tc, CTA_OK, ierr);





}


CuSuite* ObsdescrSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test_obsdescr);
    return suite;
}
