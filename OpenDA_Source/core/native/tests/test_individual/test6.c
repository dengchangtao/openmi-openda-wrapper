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
#include "cta.h"



int main() {
   double const eps=1.0e-6;
   CTA_TreeVector hstate1, hstate2,hstate3,hstates[2],hsubstate, hstate4;
   CTA_TreeVector duphstate1,duphsubstate;
   CTA_Pack hpack;
   int const n1=5;
   int const n2=3;
   int const n3=2;
   double vals1[5], vals2[3], vals3[2] ,vals1b[5],dotprod;
   CTA_Vector hvec1,hvec2,hvec3;
   double const2[1] , const3=0.0, const5=2.0;
   int i, size;  
   CTA_Handle userdata;
   int  retval;
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

   retval=CTA_TreeVector_GetSubTreeVec(hstate1, "tank_noise", &hsubstate);


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
   printf("not allowed to plug vector at root level \n");




   /* Now we do it correctly at sublevels */
   retval=CTA_TreeVector_SetVec(hstate2, hvec2); 
   printf("allowed to plug vector at sublevel \n");
   
   retval=CTA_TreeVector_SetVec(hstate3, hvec3); 
   
   /* We extract all vectors in one operation */
   retval=CTA_TreeVector_GetVec(hstate1, hvec1); 
   retval=CTA_Vector_GetVals(hvec1,vals1,n1,CTA_DOUBLE);


   /* Plugging at root level is now allowed */
   for (i=0;i<n1;i++){vals1[i]=i;}
   retval=CTA_Vector_SetVals(hvec1,vals1,n1,CTA_DOUBLE);

   retval=CTA_TreeVector_SetVec(hstate1, hvec1); 


   /* Change contents of vector */
   for (i=0;i<n1;i++){vals1[i]=1.0;}
   retval=CTA_Vector_SetVals(hvec1,vals1,n1,CTA_DOUBLE);


   /* Check if vector elements are not changed */
   retval=CTA_TreeVector_SetVec(hstate1, hvec1); 

   retval=CTA_Vector_GetVals(hvec1,vals1,n1,CTA_DOUBLE);


   /* duplicate state1 */
   retval= CTA_TreeVector_Duplicate(hstate1, &duphstate1);


   retval=CTA_TreeVector_GetVec(duphstate1, hvec1); 
   //printf ("11.1) retval=%d\n",retval);
   retval=CTA_Vector_GetVals(hvec1,vals1,n1,CTA_DOUBLE);
   //printf ("11.2) retval=%d\n",retval);
    printf ("v1=[");
   for (i=0;i<n1;i++) {printf ("%lg ",vals1[i]);};
   printf ("]\n");


   // check if tags are well copied (not ambiguous?)
   retval=CTA_TreeVector_GetSubTreeVec(hstate1, "tank_noise", &hsubstate);
   // printf ("11.7) retval=%d %d\n",retval,hsubstate);
   retval=CTA_TreeVector_GetSubTreeVec(duphstate1, "tank_noise", &hsubstate);
   //printf ("11.75) retval=%d %d\n",retval,hsubstate);
   retval=CTA_TreeVector_GetSubTreeVec(duphstate1, "tank_model", &hsubstate);
   //printf ("11.75) retval=%d %d\n",retval,hsubstate);

   /* getsize */
   retval = CTA_TreeVector_GetSize (hstate1, &size);
   //   printf ("11.3) retval=%d %d\n",retval,size);

   //   CuAssertIntEquals(tc, 5, size);
   retval = CTA_TreeVector_GetSize (hstate2, &size);
   //   printf ("11.4) retval=%d %d\n",retval,size);
   //CuAssertIntEquals(tc, 3, size);

   // Axpy

   for (i=0;i<n1;i++){vals1b[i]=i;}
   retval=CTA_Vector_SetVals(hvec1,vals1b,n1,CTA_DOUBLE);
   retval=CTA_TreeVector_SetVec(duphstate1, hvec1); 
 
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
   printf ("vals2na =[");
   for (i=0;i<n2;i++) {printf ("%lg ",vals2[i]);};
   printf ("]\n");


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
   //   CuAssertDblEquals(tc, 2.0, const3,eps);



   // state_setvals: surpass the vector filling
   vals3[0]=10; vals3[1] = 20; 
   retval = CTA_TreeVector_SetVals (hstate3, &vals3, n3, CTA_DOUBLE);
   //   printf ("state_setval: retval %d  \n",  retval);
   vals3[0]=-1; vals3[1] = -1; 
   retval = CTA_TreeVector_GetVals (hstate3, vals3, n3, CTA_DOUBLE);
   //printf (" state setval: state3 =[");
   //for (i=0;i<n3;i++) {printf ("%lg ",vals3[i]);};
   //printf ("]\n");   //10,20
  

   // setconstant and dotproduct


   retval = CTA_TreeVector_SetConstant (hstate2, &const3, CTA_DOUBLE);
   retval = CTA_TreeVector_SetConstant (hstate2, &const5, CTA_DOUBLE);
   //   printf ("13.8)set constant  retval=%d\n",retval);
  

   retval = CTA_TreeVector_Dot (hstate2, hstate1, &dotprod);
   //   printf ("14.2) dotprod retval=%d %lg \n",retval, dotprod);  //retval=18
  
   // CuAssertIntEquals(tc,CTA_TreeVectorS_NOT_COMPATIBLE, retval);

   // extra check if state2 contains the right values
   //   retval = CTA_TreeVector_GetVals (hstate2, &vals2, n2, CTA_DOUBLE);
   //printf ("%d state2 =[",retval);
   //for (i=0;i<n2;i++) {printf ("%lg ",vals2[i]);};
   //printf ("]\n");

   retval = CTA_TreeVector_Dot (hstate2, hstate2, &dotprod);
   //   printf ("14.2) dotproduct state2*state2 retval=%d %lg \n",retval, dotprod);// 12
  


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
   printf ("%d dupstate1 NA  =[",retval);
   for (i=0;i<n1;i++) {printf ("%lg ",vals1[i]);};
   printf ("]\n");


   retval=CTA_Pack_Create(0,&hpack);

   retval=CTA_TreeVector_Export (hstate1, hpack);


   retval=CTA_TreeVector_Create("unpack-state",
                           "unpack", &hstate4);

   retval=CTA_TreeVector_Import (hstate4, hpack);

   printf("content of packed state:\n");
   CTA_TreeVector_Export(hstate1,CTA_FILE_STDOUT);
   printf("\n content of unpacked state:\n");
   CTA_TreeVector_Export(hstate4,CTA_FILE_STDOUT);
  


   retval=CTA_Pack_Free(&hpack);
   
   /*


CTA_TreeVector_Export (CTA_TreeVector hstate, CTA_Handle usrdata)

   */



   // free the states

   retval = CTA_TreeVector_Free (&hstate1, CTA_TRUE);
   //   printf ("cta_free retval: %d \n",retval);


   retval = CTA_TreeVector_Free (&duphstate1, CTA_TRUE);
   // printf ("cta_free duphstate retval: %d \n",retval);

//   retval = CTA_TreeVector_Free (&duphsubstate, CTA_TRUE);
   // printf ("cta_free duphsubtate retval: %d \n",retval); // gaat fout

    exit(0);
}



