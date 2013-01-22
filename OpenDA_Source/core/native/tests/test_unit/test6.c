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



void test6(CuTest *tc) {
   double const eps=1.0e-6;
   CTA_TreeVector hstate1, hstate2,hstate3,hstates[2],hsubstate, hstate4;
   CTA_TreeVector h_2_state1, h_2_states[2];
   CTA_TreeVector duphstate1,duphsubstate,dup2hstate1;
   CTA_Pack hpack;
   int const n1=5;
   int const n2=3;
   int const n3=2;
   double vals1[5], vals2[3], vals3[2] ,vals1b[5],dotprod;
   CTA_Vector hvec1,hvec2,hvec3;
   double const2[1] , const3=0.0, const5=2.0, const6;
   int i, size,nvecs;  
   CTA_Handle userdata;
   int  retval, rest;
   char tagname[80], orgname[80], orgname2[80];
   CTA_Vector taglist;
   CTA_String str1;
   double one;
   CTA_TreeVector scal;
   

   //   CTA_Handle minfo;
   CTA_Metainfo hdescr_state, minfo, minfo2, minfo3,hdescr_state2;
   CTAI_Gridm *hgrid1, *hgrid2, *hgrid3;

   const2[0] = 2.5;
   

   /* Initialise default installation of vector object */
   retval=CTA_Core_Initialise();

   /* create a new state vector */
   retval=CTA_TreeVector_Create("All numbers of tank model",
                           "tank_model", &hstate1);

   /* make two other states */
   retval=CTA_TreeVector_Create("real state of tank",
                           "tank_determ", &hstate2);
   retval=CTA_TreeVector_Create("real state of tank",
                           "tank_noise", &hstate3);

   /* Now we concatenate them */
   hstates[0]=hstate2;
   hstates[1]=hstate3;
   retval=CTA_TreeVector_Conc(hstate1, hstates, 2); 


   /* --------------------------------- */

   /*  test with directly h_2_state[0] and [1] */



   /* create a new state vector */
   retval=CTA_TreeVector_Create("All numbers of tank model",
                           "tank_model", &h_2_state1);

   /* make two other states */
   retval=CTA_TreeVector_Create("real state of tank",
                           "tank_determ", &h_2_states[0]);
   printf("creeren state als elt van array  %d \n ",retval);
   retval=CTA_TreeVector_Create("real state of tank",
                           "tank_noise",  &h_2_states[1]);

   /* Now we concatenate them */
   retval=CTA_TreeVector_Conc(h_2_state1, h_2_states, 2); 

   printf("Dit was andere manier van substates maken %d \n ",retval);
   /* --------------------------------- */


   retval=CTA_TreeVector_GetSubTreeVec(hstate1, "tank_noise", &hsubstate);
   printf("get substate successful:  %d\n", retval);

   /* Ask the tag of the substate  */
   retval=CTA_TreeVector_GetTag(hsubstate,tagname);
   printf("Name of the tag of substate is '%s'\n",tagname);


   /* First we create three sub-vectors */
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n1, CTA_DOUBLE,userdata, &hvec1);
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n2, CTA_DOUBLE,userdata, &hvec2);
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n3, CTA_DOUBLE,userdata, &hvec3);
   /* fill vectors with constant values: vals_i = i  */
   for (i=0;i<n1;i++){vals1[i]=1.0;}
   for (i=0;i<n2;i++){vals2[i]=2.0;}
   for (i=0;i<n3;i++){vals3[i]=3.0;}
   retval=CTA_Vector_SetVals(hvec1,vals1,n1,CTA_DOUBLE);
   retval=CTA_Vector_SetVals(hvec2,vals2,n2,CTA_DOUBLE);
   retval=CTA_Vector_SetVals(hvec3,vals3,n3,CTA_DOUBLE);

   /* We plug a vector at the root level (Not allowed!) */
   retval=CTA_TreeVector_SetVec(hstate1, hvec1); 
   CuAssertIntEquals(tc,CTA_UNINITIALISED_SUBTREEVECTOR, retval); 

   /* Now we do it correctly at sublevels */
   retval=CTA_TreeVector_SetVec(hstate2, hvec2); 
   CuAssertIntEquals(tc,CTA_OK, retval);
   
   retval=CTA_TreeVector_SetVec(hstate3, hvec3); 
   CuAssertIntEquals(tc,CTA_OK, retval);
   
   /* We extract all vectors in one operation */
   retval=CTA_TreeVector_GetVec(hstate1, hvec1); 
   CuAssertIntEquals(tc,CTA_OK, retval);
   retval=CTA_Vector_GetVals(hvec1,vals1,n1,CTA_DOUBLE);
   CuAssertDblEquals(tc, 2.0, vals1[0],eps);
   CuAssertDblEquals(tc, 2.0, vals1[1],eps);
   CuAssertDblEquals(tc, 2.0, vals1[2],eps);
   CuAssertDblEquals(tc, 3.0, vals1[3],eps);
   CuAssertDblEquals(tc, 3.0, vals1[4],eps);

   /* Plugging at root level is now allowed */
   for (i=0;i<n1;i++){vals1[i]=i;}
   retval=CTA_Vector_SetVals(hvec1,vals1,n1,CTA_DOUBLE);
   CuAssertIntEquals(tc,CTA_OK, retval);
   retval=CTA_TreeVector_SetVec(hstate1, hvec1); 
   CuAssertIntEquals(tc,CTA_OK, retval);

   /* Change contents of vector */
   for (i=0;i<n1;i++){vals1[i]=1.0;}
   retval=CTA_Vector_SetVals(hvec1,vals1,n1,CTA_DOUBLE);
   CuAssertIntEquals(tc,CTA_OK, retval);

   /* Check if vector elements are not changed */
   retval=CTA_TreeVector_SetVec(hstate1, hvec1); 
   CuAssertIntEquals(tc,CTA_OK, retval);
   retval=CTA_Vector_GetVals(hvec1,vals1,n1,CTA_DOUBLE);
   CuAssertIntEquals(tc,CTA_OK, retval);

   CuAssertDblEquals(tc, 1.0, vals1[0],eps);
   CuAssertDblEquals(tc, 1.0, vals1[1],eps);
   CuAssertDblEquals(tc, 1.0, vals1[2],eps);
   CuAssertDblEquals(tc, 1.0, vals1[3],eps);
   CuAssertDblEquals(tc, 1.0, vals1[4],eps);


   /* duplicate state1 */
    retval= CTA_TreeVector_Duplicate(hstate1, &duphstate1);
   //   printf ("11) retval=%d\n",retval);




   retval=CTA_TreeVector_GetVec(duphstate1, hvec1); 
   //printf ("11.1) retval=%d\n",retval);
   retval=CTA_Vector_GetVals(hvec1,vals1,n1,CTA_DOUBLE);
   //printf ("11.2) retval=%d\n",retval);
   //   printf ("v1=[");
   //for (i=0;i<n1;i++) {printf ("%lg ",vals1[i]);};
   //printf ("]\n");


   CuAssertDblEquals(tc, 1.0, vals1[0],eps);
   CuAssertDblEquals(tc, 1.0, vals1[1],eps);
   CuAssertDblEquals(tc, 1.0, vals1[2],eps);
   CuAssertDblEquals(tc, 1.0, vals1[3],eps);
   CuAssertDblEquals(tc, 1.0, vals1[4],eps);

   // check if tags are well copied (not ambiguous?)
   retval=CTA_TreeVector_GetSubTreeVec(hstate1, "tank_noise", &hsubstate);
   // printf ("11.7) retval=%d %d\n",retval,hsubstate);
   retval=CTA_TreeVector_GetSubTreeVec(duphstate1, "tank_noise", &hsubstate);
   //printf ("11.75) retval=%d %d\n",retval,hsubstate);
   retval=CTA_TreeVector_GetSubTreeVec(duphstate1, "tank_model", &hsubstate);
   //printf ("11.75) retval=%d %d\n",retval,hsubstate);

   CuAssertIntEquals(tc, CTA_OK, retval);

   /* getsize */
   retval = CTA_TreeVector_GetSize (hstate1, &size);
   //   printf ("11.3) retval=%d %d\n",retval,size);
   CuAssertIntEquals(tc, 5, size);
   retval = CTA_TreeVector_GetSize (hstate2, &size);
   //   printf ("11.4) retval=%d %d\n",retval,size);
   CuAssertIntEquals(tc, 3, size);


   // Axpy

   for (i=0;i<n1;i++){vals1b[i]=i;}
   retval=CTA_Vector_SetVals(hvec1,vals1b,n1,CTA_DOUBLE);
   retval=CTA_TreeVector_SetVec(duphstate1, hvec1); 
   //   printf ("12.0) retval=%d\n",retval);

   retval = CTA_TreeVector_GetVals (hstate1, vals1, n1, CTA_DOUBLE);
   //printf ("vals1 voor=[");
   //   for (i=0;i<n1;i++) {printf ("%lg ",vals1[i]);};
   //printf ("]\n");

   retval = CTA_TreeVector_Axpy (hstate1, 1.0, duphstate1);
   //printf ("12.1) retval=%d\n",retval);
   retval = CTA_TreeVector_GetVals (hstate1, vals1, n1, CTA_DOUBLE);
   //printf ("12.2) retval=%d\n",retval);
   //printf ("vals1na =[");
   //for (i=0;i<n1;i++) {printf ("%lg ",vals1[i]);};
   //printf ("]\n");

   // axpy of a substate with itself
   retval = CTA_TreeVector_GetVals (hstate2, vals2, n2, CTA_DOUBLE);
   //   printf ("vals2 voor=[");
   //for (i=0;i<n2;i++) {printf ("%lg ",vals2[i]);};
   //printf ("]\n");   // 1 2 3
   retval = CTA_TreeVector_Axpy (hstate2, 1.0, hstate2);
   //printf ("13.1) retval=%d\n",retval);
   retval = CTA_TreeVector_GetVals (hstate2, vals2, n2, CTA_DOUBLE);
   //printf ("13.2) retval=%d\n",retval);
   //printf ("vals2na =[");
   //for (i=0;i<n2;i++) {printf ("%lg ",vals2[i]);};
   //printf ("]\n");

   CuAssertDblEquals(tc, 2.0, vals2[0],eps);
   CuAssertDblEquals(tc, 4.0, vals2[1],eps);
   CuAssertDblEquals(tc, 6.0, vals2[2],eps);


   //   retval=CTA_TreeVector_GetVec(hstate2, hvec2); 
   //retval = CTA_Vector_GetVals (hvec2, vals2, n2, CTA_DOUBLE);
   // printf ("vec2  =[");
   //for (i=0;i<n2;i++) {printf (" %lg ",vals2[i]);};
   //printf ("]\n");   // 2 4 6


   // vector_getval
   retval = CTA_Vector_GetVal(hvec2,1,&const3,CTA_DOUBLE);
   // printf ("vector_getval: retval, waarde %d %lg\n",  retval,const3);
   retval = CTA_Vector_GetVal(hvec2,2,&const3,CTA_DOUBLE);
   //   printf ("vector_getval: retval, waarde %d %lg \n",  retval,const3);
   retval = CTA_Vector_GetVal(hvec2,3,&const3,CTA_DOUBLE);
   //    printf ("vector_getval: retval, waarde %d %lg \n",  retval,const3);
   // dus 2,2,2
   CuAssertDblEquals(tc, 2.0, const3,eps);



   // state_setvals: surpass the vector filling
   vals3[0]=10; vals3[1] = 20; 
   retval = CTA_TreeVector_SetVals (hstate3, &vals3, n3, CTA_DOUBLE);
   //   printf ("state_setval: retval %d  \n",  retval);
   vals3[0]=-1; vals3[1] = -1; 
   retval = CTA_TreeVector_GetVals (hstate3, vals3, n3, CTA_DOUBLE);
   //printf (" state setval: state3 =[");
   //for (i=0;i<n3;i++) {printf ("%lg ",vals3[i]);};
   //printf ("]\n");   //10,20
   CuAssertDblEquals(tc, 10.0, vals3[0],eps);
   CuAssertDblEquals(tc, 20.0, vals3[1],eps);


   /* ask for info of the states */
   retval = CTA_TreeVector_Info(hstate1);
   CuAssertIntEquals(tc, CTA_OK, retval);
   printf("end of state \n \n");

   retval = CTA_TreeVector_Info(duphstate1);
   CuAssertIntEquals(tc, CTA_OK, retval);
   printf("end of state \n \n");

   retval=CTA_TreeVector_GetSubTreeVec(duphstate1, "tank_noise", &hsubstate);
   retval = CTA_TreeVector_Info(hsubstate);
   printf("end of state \n \n");
   CuAssertIntEquals(tc, CTA_OK, retval);


   // CTA_TreeVector_list

   nvecs = CTA_TreeVector_GetVecNumHandles(hstate1);
   printf("state_list: getvecnumhandles %d \n", nvecs);

   retval=CTA_Vector_Create( CTA_DEFAULT_VECTOR, nvecs+1, CTA_STRING,
                           userdata, &taglist);

   retval =  CTA_TreeVector_List(hstate1, taglist);


   retval = CTA_String_Create(&str1);

   for (i=1;i<nvecs+2;i++){

   retval=CTA_Vector_GetVal(taglist, i, &str1, CTA_STRING);
   printf("state_list: dit wordt waarde %d \n", i);
   retval = print_a_string(str1);
   }






   // setconstant and dotproduct


   retval = CTA_TreeVector_SetConstant (hstate2, &const3, CTA_DOUBLE);
   retval = CTA_TreeVector_SetConstant (hstate2, &const5, CTA_DOUBLE);
   //   printf ("13.8)set constant  retval=%d\n",retval);
   CuAssertIntEquals(tc, CTA_OK, retval);

   retval = CTA_TreeVector_Dot (hstate2, hstate1, &dotprod);
   //   printf ("14.2) dotprod retval=%d %lg \n",retval, dotprod);  //retval=18
   CuAssertIntEquals(tc,CTA_TREEVECTORS_NOT_COMPATIBLE, retval);

   // extra check if state2 contains the right values
   //   retval = CTA_TreeVector_GetVals (hstate2, &vals2, n2, CTA_DOUBLE);
   //printf ("%d state2 =[",retval);
   //for (i=0;i<n2;i++) {printf ("%lg ",vals2[i]);};
   //printf ("]\n");

   retval = CTA_TreeVector_Dot (hstate2, hstate2, &dotprod);
   //   printf ("14.2) dotproduct state2*state2 retval=%d %lg \n",retval, dotprod);// 12
   CuAssertDblEquals(tc, 12.0, dotprod, eps);


   // state_copy
   //before:  [2.5 2.5 2.5 10 20] and [0 1 2 3 4]
   retval = CTA_TreeVector_GetVals (hstate1, &vals1, n1, CTA_DOUBLE);
   //printf ("%d __________________state1 voor =[",retval);
   //for (i=0;i<n1;i++) {printf ("%lg ",vals1[i]);};
   //printf ("]\n");
   retval = CTA_TreeVector_GetVals (duphstate1, &vals1, n1, CTA_DOUBLE);
   //printf ("%d dupstate1 voor  =[",retval);
   //for (i=0;i<n1;i++) {printf ("%lg ",vals1[i]);};
   //printf ("]\n");

   // copy substate of state1 to substate of duphstate 
   retval=CTA_TreeVector_GetSubTreeVec(duphstate1, "tank_noise", &duphsubstate);


   retval = CTA_TreeVector_Copy (hstate3, duphsubstate);

 
   //after: [2.5 2.5 2.5 10 20] and [0 1 2 10 20]
   retval = CTA_TreeVector_GetVals (hstate1, &vals1, n1, CTA_DOUBLE);
   //   printf ("%d state1 NA =[",retval);
   //for (i=0;i<n1;i++) {printf ("%lg ",vals1[i]);};
   //printf ("]\n");
   retval = CTA_TreeVector_GetVals (duphstate1, &vals1, n1, CTA_DOUBLE);
   //printf ("%d dupstate1 NA  =[",retval);
   //for (i=0;i<n1;i++) {printf ("%lg ",vals1[i]);};
   //printf ("]\n");


   CuAssertDblEquals(tc, 0.0, vals1[0],eps);
   CuAssertDblEquals(tc, 1.0, vals1[1],eps);
   CuAssertDblEquals(tc, 2.0, vals1[2],eps);
   CuAssertDblEquals(tc, 10.0, vals1[3],eps);
   CuAssertDblEquals(tc, 20.0, vals1[4],eps);

   retval=CTA_Pack_Create(0,&hpack);
   CuAssertIntEquals(tc, CTA_OK, retval);

   //  now add some metainfo
   retval=CTA_Metainfo_Create(&minfo);
        if (retval !=CTA_OK) {printf("error creating metainfo\n");}

         retval = CTA_Metainfo_setRegGrid(minfo, "heatGrid", n1, 1, 0,
                   0.0, 0.0,    0.0,      1.0, 1.0, 0.0);
         if (retval !=CTA_OK)  {printf("error setgrid\n");}

     retval=CTA_TreeVector_SetMetainfo(hstate1, minfo);
     printf("Metainfo added to state: %d \n\n",retval);
   retval=CTA_TreeVector_Export (hstate1, CTA_FILE_STDOUT);

   retval=CTA_TreeVector_Export (hstate1, hpack);
   CuAssertIntEquals(tc, CTA_OK, retval);

   printf("content of packed state:\n");
   CTA_TreeVector_Export(hstate1,CTA_FILE_STDOUT);

   // free hstate1
   retval = CTA_TreeVector_Free (&hstate1, CTA_TRUE);

   retval=CTA_TreeVector_Create("unpack-state",
                           "unpack", &hstate4);

   retval=CTA_TreeVector_Import (hstate4, hpack);
   CuAssertIntEquals(tc, CTA_OK, retval);

   printf("\n content of unpacked state:\n");
   CTA_TreeVector_Export(hstate4,CTA_FILE_STDOUT);
  

   retval=CTA_Pack_Free(&hpack);
   CuAssertIntEquals(tc, CTA_OK, retval);
   
   printf("----------------------------------------------\n");
   //   one=1.0;
   // retval=CTA_TreeVector_Duplicate (hstate1, &scal);
   //CTA_TreeVector_SetConstant(scal, &one, CTA_DOUBLE);

   //CTA_TreeVector_OpOnLeafs(hstate1,scal,CTA_OP_ROOT_RMS,CTA_NULL);
   //CTA_TreeVector_Free(&scal,CTA_TRUE);
   printf("----------------------------------------------\n");
   
   /*
   


CTA_TreeVector_Export (CTA_TreeVector hstate, CTA_Handle *usrdata)

   */





   //   printf ("cta_free retval: %d \n",retval);

   //free vectors
   retval = CTA_Vector_Free(&hvec1);
   retval = CTA_Vector_Free(&hvec2);
   retval = CTA_Vector_Free(&hvec3);
     printf ("cta_vector_free retval: %d \n",retval);


   CuAssertIntEquals(tc, CTA_OK, retval);

   retval = CTA_TreeVector_Free (&duphstate1, CTA_TRUE);
   // printf ("cta_free duphstate retval: %d \n",retval);

   retval = CTA_TreeVector_Free (&duphsubstate, CTA_TRUE);
   // printf ("cta_free duphsubtate retval: %d \n",retval); // gaat fout

   CuAssertIntEquals(tc, CTA_ILLEGAL_HANDLE, retval);

}

/* ----------------------------------------------------------- */

void test7(CuTest *tc) {
   double const eps=1.0e-6;
   CTA_TreeVector hstate1, hstate2,hstate3,hstates[2],hsubstate, hstate4;
   CTA_TreeVector duphstate1,duphsubstate,dup2hstate1;
   CTA_Pack hpack;
   int const n1=5;
   int const n2=3;
   int const n3=2;
   double vals1[5], vals2[3], vals3[2] ,vals1b[5],dotprod;
   CTA_Vector hvec1,hvec2,hvec3;
   double const2[1] , const3=0.0, const5=2.0, const6;
   int i, size,nvecs;  
   CTA_Handle userdata;
   int  retval, rest;
   char tagname[80], orgname[80], orgname2[80];
   CTA_Vector taglist;
   CTA_String str1;
   //   CTA_Handle minfo;
   CTA_Metainfo hdescr_state, minfo, minfo2, minfo3,hdescr_state2, minfo4;
   CTAI_Gridm *hgrid1, *hgrid2, hgrid3;
   const2[0] = 2.5;

   printf ("************ BEGIN TEST7 ************  \n");

   /* Initialise default installation of vector object */
   retval=CTA_Core_Initialise();

   /* create a new state vector */
   retval=CTA_TreeVector_Create("All numbers of tank model",
                           "tank_model", &hstate1);

   /* make two other states */
   retval=CTA_TreeVector_Create("real state of tank",
                           "tank_determ", &hstate2);
   retval=CTA_TreeVector_Create("real state of tank",
                           "tank_noise", &hstate3);

   /* Now we concatenate them */
   hstates[0]=hstate2;
   hstates[1]=hstate3;
   retval=CTA_TreeVector_Conc(hstate1, hstates, 2); 

   retval=CTA_TreeVector_GetSubTreeVec(hstate1, "tank_noise", &hsubstate);
   printf("get substate gelukt:  %d\n", retval);

   /* Ask the tag of the substate  */
   retval=CTA_TreeVector_GetTag(hsubstate,tagname);
   printf("Name of the tag of substate is '%s'\n",tagname);


   /* First we create three sub-vectors */
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n1, CTA_DOUBLE,userdata,
                            &hvec1);
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n2, CTA_DOUBLE,userdata,
                            &hvec2);
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n3, CTA_DOUBLE,userdata,
                            &hvec3);
   /* fill vectors with constant values: vals_i = i  */
   for (i=0;i<n1;i++){vals1[i]=1.0;}
   for (i=0;i<n2;i++){vals2[i]=2.0;}
   for (i=0;i<n3;i++){vals3[i]=3.0;}
   retval=CTA_Vector_SetVals(hvec1,vals1,n1,CTA_DOUBLE);
   retval=CTA_Vector_SetVals(hvec2,vals2,n2,CTA_DOUBLE);
   retval=CTA_Vector_SetVals(hvec3,vals3,n3,CTA_DOUBLE);

   /* We plug a vector at the root level (Not allowed!) */
   retval=CTA_TreeVector_SetVec(hstate1, hvec1); 
   CuAssertIntEquals(tc,CTA_UNINITIALISED_SUBTREEVECTOR, retval); 

   /* Now we do it correctly at sublevels */
   retval=CTA_TreeVector_SetVec(hstate2, hvec2); 
   CuAssertIntEquals(tc,CTA_OK, retval);
   
   retval=CTA_TreeVector_SetVec(hstate3, hvec3); 

   CuAssertIntEquals(tc,CTA_OK, retval);
   
   /* We extract all vectors in one operation */
   retval=CTA_TreeVector_GetVec(hstate1, hvec1); 
   CuAssertIntEquals(tc,CTA_OK, retval);
   retval=CTA_Vector_GetVals(hvec1,vals1,n1,CTA_DOUBLE);
   CuAssertDblEquals(tc, 2.0, vals1[0],eps);
   CuAssertDblEquals(tc, 2.0, vals1[1],eps);
   CuAssertDblEquals(tc, 2.0, vals1[2],eps);
   CuAssertDblEquals(tc, 3.0, vals1[3],eps);
   CuAssertDblEquals(tc, 3.0, vals1[4],eps);

   /* Plugging at root level is now allowed */
   for (i=0;i<n1;i++){vals1[i]=i;}
   retval=CTA_Vector_SetVals(hvec1,vals1,n1,CTA_DOUBLE);
   CuAssertIntEquals(tc,CTA_OK, retval);
   retval=CTA_TreeVector_SetVec(hstate1, hvec1); 
   CuAssertIntEquals(tc,CTA_OK, retval);

   /* Change contents of vector */
   for (i=0;i<n1;i++){vals1[i]=1.0;}
   retval=CTA_Vector_SetVals(hvec1,vals1,n1,CTA_DOUBLE);
   CuAssertIntEquals(tc,CTA_OK, retval);

   /* Check if vector elements are not changed */
   retval=CTA_TreeVector_SetVec(hstate1, hvec1); 
   CuAssertIntEquals(tc,CTA_OK, retval);
   retval=CTA_Vector_GetVals(hvec1,vals1,n1,CTA_DOUBLE);
   CuAssertIntEquals(tc,CTA_OK, retval);

   CuAssertDblEquals(tc, 1.0, vals1[0],eps);
   CuAssertDblEquals(tc, 1.0, vals1[1],eps);
   CuAssertDblEquals(tc, 1.0, vals1[2],eps);
   CuAssertDblEquals(tc, 1.0, vals1[3],eps);
   CuAssertDblEquals(tc, 1.0, vals1[4],eps);


   /* duplicate state1 */
    retval= CTA_TreeVector_Duplicate(hstate1, &duphstate1);
   //   printf ("11) retval=%d\n",retval);




   retval=CTA_TreeVector_GetVec(duphstate1, hvec1); 
   //printf ("11.1) retval=%d\n",retval);
   retval=CTA_Vector_GetVals(hvec1,vals1,n1,CTA_DOUBLE);
   //printf ("11.2) retval=%d\n",retval);
   //   printf ("v1=[");
   //for (i=0;i<n1;i++) {printf ("%lg ",vals1[i]);};
   //printf ("]\n");


   CuAssertDblEquals(tc, 1.0, vals1[0],eps);
   CuAssertDblEquals(tc, 1.0, vals1[1],eps);
   CuAssertDblEquals(tc, 1.0, vals1[2],eps);
   CuAssertDblEquals(tc, 1.0, vals1[3],eps);
   CuAssertDblEquals(tc, 1.0, vals1[4],eps);

   // check if tags are well copied (not ambiguous?)
   retval=CTA_TreeVector_GetSubTreeVec(hstate1, "tank_noise", &hsubstate);
   // printf ("11.7) retval=%d %d\n",retval,hsubstate);
   retval=CTA_TreeVector_GetSubTreeVec(duphstate1, "tank_noise", &hsubstate);
   //printf ("11.75) retval=%d %d\n",retval,hsubstate);
   retval=CTA_TreeVector_GetSubTreeVec(duphstate1, "tank_model", &hsubstate);
   //printf ("11.75) retval=%d %d\n",retval,hsubstate);

   CuAssertIntEquals(tc, CTA_OK, retval);

   /* getsize */
   retval = CTA_TreeVector_GetSize (hstate1, &size);
   //   printf ("11.3) retval=%d %d\n",retval,size);
   CuAssertIntEquals(tc, 5, size);
   retval = CTA_TreeVector_GetSize (hstate2, &size);
   //   printf ("11.4) retval=%d %d\n",retval,size);
   CuAssertIntEquals(tc, 3, size);


   /* Create a "Metainfo"  component */

   retval=CTA_Metainfo_Create(&hdescr_state);
   printf("metainfo_create: retval %d \n", retval);
   CuAssertIntEquals(tc, CTA_OK, retval);


   retval=CTA_Metainfo_Create(&hdescr_state2);


   retval = CTA_Metainfo_GetUnit(hdescr_state,orgname);
   printf("metainfo_getunit before setting: retval %d \n", retval);
   printf("metainfo_getunit: orgname %s \n", orgname);

   retval = CTA_Metainfo_SetUnit(hdescr_state,"m/s");
   printf("metainfo_setunit: retval %d \n", retval);


   retval = CTA_Metainfo_GetUnit(hdescr_state,orgname);
   printf("metainfo_getunit: hdescr_state unit %s \n", orgname);


   hgrid2=malloc(sizeof(CTAI_Gridm));
   retval = CTA_Metainfo_GetGrid(hdescr_state,hgrid2);
   printf("metainfo_getgrid: nx(=3)   %d |%d| \n", retval ,hgrid2->nx);
   /* verander grid */
   hgrid2->nx = 13;
      retval = CTA_Metainfo_SetGrid(hdescr_state,hgrid2);
   printf("metainfo_setgrid: nx(=13)   %d |%d| \n", retval ,hgrid2->nx);


   i=3;
   retval = CTA_Metainfo_SetRest(hdescr_state,&i);
   printf("metainfo_setrest: retval %d \n", retval);

   i=0;
   retval = CTA_Metainfo_GetRest(hdescr_state,&i);
   printf("metainfo_getrest(=3): retval %d %d \n", retval, i);


   // copy
   retval = CTA_Metainfo_Copy(hdescr_state, hdescr_state2);
   printf("metainfo_copy: retval %d \n", retval);
   retval = CTA_Metainfo_GetRest(hdescr_state2,&i);
   printf("metainfo_getrest: rest na copy (=3) %d |%d| \n", retval, i);


   /* koppel nu metainfo aan state */

   retval = CTA_TreeVector_SetMetainfo(hstate1,hdescr_state);
   printf("setmetainfo: retval %d \n", retval);


   // nu een metainfo rechtstreeks uit de state halen

   retval=CTA_Metainfo_Create(&minfo2);
   printf("metainfo_create minfo2: retval %d \n", retval);

   retval = CTA_Metainfo_GetRest(minfo2,&i);
   printf("metainfo_getrest: nieuwe minfo %d |%d| \n", retval, i);

   retval = CTA_TreeVector_GetMetainfo(hstate1,minfo2);
   printf("getmetainfo: retval %d \n", retval);

   retval = CTA_Metainfo_GetRest(minfo2,&i);
   printf("metainfo_getrest: rest uit state (=3) %d |%d| \n", retval, i);

   // haal nu weer grid uit metainfo
   retval = CTA_Metainfo_GetGrid(minfo2,&hgrid3);
   printf("state_metainfo_getgrid: nx(=13)   %d |%d| \n", retval ,hgrid3.nx);



   // kijk of duplicate goed werkt met metainfo
   retval= CTA_TreeVector_Duplicate(hstate1, &dup2hstate1);

     retval=CTA_Metainfo_Create(&minfo3);
   printf("duplicate:metainfo_create minfo3: retval %d \n", retval);

     retval = CTA_TreeVector_GetMetainfo(dup2hstate1,minfo3);
   printf("getmetainfo: retval %d \n", retval);

   retval = CTA_Metainfo_GetRest(minfo3,&i);
   printf("metainfo_getrest: rest uit state (moet 3) %d |%d| \n", retval, i);



   // -------------------------- testgedeelte voor kopieren metainfo
   retval=CTA_TreeVector_GetSubTreeVec(duphstate1, "tank_determ", &duphsubstate);
   printf("GetSubTreeVec: retval %d \n", retval);

   retval=CTA_TreeVector_GetVec(duphsubstate, hvec2);    //  (1 1 1)
   printf("state_getvec: retval %d \n", retval);
   retval=CTA_Vector_GetVals(hvec2,vals2,n2,CTA_DOUBLE);
   printf("duphsubstate orig: %f %f %f \n",vals2[0],vals2[1],vals2[2]);


   for (i=0;i<n2;i++){vals2[i]=2.0;}
   retval=CTA_Vector_SetVals(hvec2,vals2,n2,CTA_DOUBLE);
   retval=CTA_TreeVector_SetVec(hstate2, hvec2);    // hstate1: (2 2 2; 1 1)
   

   // zet ook metainfo aan duphsubstate (rest=10)
   i=10;
   retval = CTA_Metainfo_SetRest(hdescr_state,&i);
   retval = CTA_TreeVector_SetMetainfo(duphsubstate,hdescr_state);

   // koppel metainfo met rest= 20 aan substate hstate2
   i=20;
   retval = CTA_Metainfo_SetRest(hdescr_state,&i);
   retval = CTA_TreeVector_SetMetainfo(hstate2,hdescr_state);



   // kopieer nu hstate2 naar substate van duphstate

   retval = CTA_TreeVector_Copy (hstate2, duphsubstate);
   // printf("state_copy: retval %d \n", retval);
   retval=CTA_TreeVector_GetVec(duphsubstate, hvec2);    //  (2 2 2)
   retval=CTA_Vector_GetVals(hvec2,vals2,n2,CTA_DOUBLE);
   // printf("duphsubstate new: %f %f %f \n",vals2[0],vals2[1],vals2[2]);

   // controleer nu rest van metainfo
     retval=CTA_Metainfo_Create(&minfo4);
   retval = CTA_TreeVector_GetMetainfo(duphsubstate, minfo4);
   printf("1 getmetainfo %d \n", retval);
   retval = CTA_Metainfo_GetRest(minfo4,&i);
   printf("metainfo_getrest: rest of copied state (20): %d |%d| \n", retval, i);

   printf ("************ EIND  TEST7 ************  \n");
}


CuSuite* StateGetSuite() {
    CuSuite* suite = CuSuiteNew();
    SUITE_ADD_TEST(suite, test6);
    SUITE_ADD_TEST(suite, test7);
    return suite;
}
