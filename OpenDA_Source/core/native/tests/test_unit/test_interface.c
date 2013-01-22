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

// Test Suite for the C-interface functions

int CTA_Intf_Create(const char *name, const CTA_Datatype *argtyp,
                    const int narg,CTA_Intf *hintf);
int CTA_Intf_Free(CTA_Intf *hintf);
int CTA_Intf_Match_aa(const CTA_Datatype *argtyp1, const int narg1, 
                      const CTA_Datatype *argtyp2, const int narg2,
                      BOOL *flag);
int CTA_Intf_Match_ha(const CTA_Intf hintf1,  
                      const CTA_Datatype *argtyp2, const int narg2, BOOL *flag);
int CTA_Intf_Match_hh(const CTA_Intf hintf1, const CTA_Intf hintf2, BOOL *flag);



void test1(CuTest *tc) {
// Create test implementation of CTA_Intf_Match_hh
  int retval;
  CTA_Datatype argtyp[3];
  CTA_Intf     hintf1, hintf2, hintf3, hintf4, hintf5;
  BOOL flag;

  retval=CTA_Core_Initialise();

  // create first interface
  argtyp[0]=CTA_REAL; 
  argtyp[1]=CTA_FSTRING; 
  argtyp[2]=CTA_INTEGER; 
  retval=CTA_Intf_Create("intf1", argtyp, 3, &hintf1);
  CuAssertIntEquals(tc, CTA_OK, retval);

  // create second interface
  argtyp[0]=CTA_REAL; 
  argtyp[1]=CTA_FSTRING; 
  argtyp[2]=CTA_VOID; 
  retval=CTA_Intf_Create("intf2", argtyp, 3, &hintf2);
  CuAssertIntEquals(tc, CTA_OK, retval);

  // create third interface
  argtyp[0]=CTA_REAL; 
  argtyp[1]=CTA_CSTRING; 
  argtyp[2]=CTA_VOID; 
  retval=CTA_Intf_Create("intf3", argtyp, 3, &hintf3);
  CuAssertIntEquals(tc, CTA_OK, retval);

  // create fourth interface
  argtyp[0]=CTA_REAL; 
  argtyp[1]=CTA_CSTRING; 
  retval=CTA_Intf_Create("intf4", argtyp, 2, &hintf4);
  CuAssertIntEquals(tc, CTA_OK, retval);

  // create fifth interface
  argtyp[0]=CTA_VOID; 
  argtyp[1]=CTA_CSTRING; 
  retval=CTA_Intf_Create("intf5", argtyp, 2, &hintf5);
  CuAssertIntEquals(tc, CTA_OK, retval);

  //Match the different interfaces
  retval=CTA_Intf_Match_hh(hintf1, hintf1, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) TRUE, (int) flag);

  retval=CTA_Intf_Match_hh(hintf1, hintf2, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) TRUE, (int) flag);

  retval=CTA_Intf_Match_hh(hintf1, hintf3, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) FALSE, (int) flag);

  retval=CTA_Intf_Match_hh(hintf3, hintf3, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) TRUE, (int) flag);

  retval=CTA_Intf_Match_hh(hintf3, hintf4, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) FALSE, (int) flag);

  retval=CTA_Intf_Match_hh(hintf4, hintf5, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) TRUE, (int) flag);

  //Free interfaces
  retval=CTA_Intf_Free(&hintf1);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf1);

  retval=CTA_Intf_Free(&hintf2);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf2);

  retval=CTA_Intf_Free(&hintf3);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf3);

  retval=CTA_Intf_Free(&hintf4);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf4);

  retval=CTA_Intf_Free(&hintf5);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf5);
}


void test2(CuTest *tc) {
// Create test implementation of CTA_Intf_Match_ha
  int retval;
  CTA_Datatype argtyp1[3], argtyp2[3], argtyp3[3],
               argtyp4[2], argtyp5[2];
  CTA_Intf     hintf1, hintf2, hintf3, hintf4, hintf5;
  int          nargs1, nargs2, nargs3, nargs4, nargs5;
  BOOL flag;

  // create first interface
  nargs1=3;
  argtyp1[0]=CTA_REAL; 
  argtyp1[1]=CTA_FSTRING; 
  argtyp1[2]=CTA_INTEGER; 
  retval=CTA_Intf_Create("intf1", argtyp1, nargs1, &hintf1);
  CuAssertIntEquals(tc, CTA_OK, retval);

  // create second interface
  nargs2=3;
  argtyp2[0]=CTA_REAL; 
  argtyp2[1]=CTA_FSTRING; 
  argtyp2[2]=CTA_VOID; 
  retval=CTA_Intf_Create("intf2", argtyp2, nargs2, &hintf2);
  CuAssertIntEquals(tc, CTA_OK, retval);

  // create third interface
  nargs3=3;
  argtyp3[0]=CTA_REAL; 
  argtyp3[1]=CTA_CSTRING; 
  argtyp3[2]=CTA_VOID; 
  retval=CTA_Intf_Create("intf3", argtyp3, nargs3, &hintf3);
  CuAssertIntEquals(tc, CTA_OK, retval);

  // create fourth interface
  nargs4=2;
  argtyp4[0]=CTA_REAL; 
  argtyp4[1]=CTA_CSTRING; 
  retval=CTA_Intf_Create("intf4", argtyp4, nargs4, &hintf4);
  CuAssertIntEquals(tc, CTA_OK, retval);

  // create fifth interface
  nargs5=2;
  argtyp5[0]=CTA_VOID; 
  argtyp5[1]=CTA_CSTRING; 
  retval=CTA_Intf_Create("intf5", argtyp5, nargs5, &hintf5);
  CuAssertIntEquals(tc, CTA_OK, retval);

  //Match the different interfaces
  retval=CTA_Intf_Match_ha(hintf1, argtyp1, nargs1, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) TRUE, (int) flag);

  retval=CTA_Intf_Match_ha(hintf1, argtyp2, nargs2, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) TRUE, (int) flag);

  retval=CTA_Intf_Match_ha(hintf1, argtyp3, nargs3, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) FALSE, (int) flag);

  retval=CTA_Intf_Match_ha(hintf3, argtyp3, nargs3, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) TRUE, (int) flag);

  retval=CTA_Intf_Match_ha(hintf3, argtyp4, nargs4, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) FALSE, (int) flag);

  retval=CTA_Intf_Match_ha(hintf4, argtyp5, nargs5, &flag);
  retval=CTA_Intf_Match_hh(hintf4, hintf5, &flag);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, (int) TRUE, (int) flag);

  //Free interfaces
  retval=CTA_Intf_Free(&hintf1);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf1);

  retval=CTA_Intf_Free(&hintf2);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf2);

  retval=CTA_Intf_Free(&hintf3);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf3);

  retval=CTA_Intf_Free(&hintf4);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf4);

  retval=CTA_Intf_Free(&hintf5);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf5);
}


void test3(CuTest *tc) {
// Test implementation of interfaces; create errors
  int retval;
  CTA_Datatype argtyp1[3], argtyp2[3];
  CTA_Intf     hintf1, hintf2, hintf3;
  int          nargs1, nargs2;
  BOOL flag;
  CTA_Handle   handle;

  // create first interface
  nargs1=3;
  argtyp1[0]=CTA_REAL; 
  argtyp1[1]=CTA_FSTRING; 
  argtyp1[2]=CTA_INTEGER; 
  retval=CTA_Intf_Create("intf1", argtyp1, nargs1, &hintf1);
  CuAssertIntEquals(tc, CTA_OK, retval);

  // create second interface
  nargs2=3;
  argtyp2[0]=CTA_REAL; 
  argtyp2[1]=CTA_FSTRING; 
  argtyp2[2]=CTA_VOID; 
  retval=CTA_Intf_Create("intf2", argtyp2, nargs2, &hintf2);
  CuAssertIntEquals(tc, CTA_OK, retval);

  // create an non-interface handle (do not try this at home!)
  retval=CTA_Handle_Create("func", CTA_HANDLE,NULL, &handle);
  CuAssertIntEquals(tc, CTA_OK, retval);


  //Match the different interfaces
  retval=CTA_Intf_Match_hh(hintf1, CTA_NULL, &flag);
  CuAssertIntEquals(tc, CTA_ILLEGAL_HANDLE,retval);

  retval=CTA_Intf_Match_hh(handle, hintf2, &flag);
  CuAssertIntEquals(tc, CTA_INCOMPATIBLE_HANDLE, retval);

  //Free interfaces

  retval=CTA_Intf_Free(&handle);
  CuAssertIntEquals(tc, CTA_INCOMPATIBLE_HANDLE, retval);

  //Free illegal handle
  hintf3=-999;
  retval=CTA_Intf_Free(&hintf3);
  CuAssertIntEquals(tc, CTA_ILLEGAL_HANDLE, retval);


  hintf3=CTA_NULL;
  retval=CTA_Intf_Free(&hintf1);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf3);


  retval=CTA_Intf_Free(&hintf1);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf1);

  retval=CTA_Intf_Free(&hintf2);
  CuAssertIntEquals(tc, CTA_OK, retval);
  CuAssertIntEquals(tc, CTA_NULL, hintf2);

}


CuSuite* InterfaceSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test1);
    SUITE_ADD_TEST(suite, test2);
    SUITE_ADD_TEST(suite, test3);
    return suite;
}
                     
