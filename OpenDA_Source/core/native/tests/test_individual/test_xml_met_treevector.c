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

#include <stdio.h>
#include "cta.h"

/*  #define IFILE ("V:\\E05q_SIMONA_KALMINA\\c59206-costa-xml\\xml\\test001.xml") */


#define IFILE ("mod_input.xml")

#define OFILE ("test001.xml")
#define OFILEb ("bewerkt_test001.xml")


#define DA_FILE1 ("example_1_very_simple.xml")
#define DA_FILE1b ("example1b.xml")

#define DA_FILE2 ("example_2_fields_without_metadata.xml")

#define DA_FILE3 ("example_3_fields_with_metadata.xml")

#define DA_FILE4 ("example_4_timeseries.xml")

#define DA_FILE5 ("example_1_rewritten.xml")





void make_state(CTA_TreeVector *hstate1) {
   double const eps=1.0e-6;
   CTA_TreeVector hstate2,hstate3,hstates[2],hsubstate, hstate4;
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
                           "tank_model", hstate1);

   /* make two other states */
   retval=CTA_TreeVector_Create("real state of tank",
                           "tank_determ", &hstate2);
   retval=CTA_TreeVector_Create("real state of tank",
                           "tank_noise", &hstate3);

   /* Now we concatenate them */
   hstates[0]=hstate2;
   hstates[1]=hstate3;
   retval=CTA_TreeVector_Conc(*hstate1, hstates, 2); 

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

   /* Now we do it correctly at sublevels */
   retval=CTA_TreeVector_SetVec(hstate2, hvec2); 
   
   retval=CTA_TreeVector_SetVec(hstate3, hvec3); 
}






int main(int argc, char **argv)
{
   CTA_Tree     htree;
   CTA_TreeVector    hstate, hstate2;
   CTA_String   hIFILE;
   CTA_String   hOFILE, hO2FILE, tFILE;
   CTA_String   hstr;
   char         sz[4096];
   CTA_Datatype dt;
   int          n;
   int          i, retval;
   char uitfile[100];
   char infile[100];
   
   CTA_Core_Initialise();
   
   retval=CTA_String_Create(&hIFILE);
   printf ("1) retval=%d  \n",retval);

   retval=CTA_String_Set(hIFILE, IFILE);
   printf ("2) retval=%d  \n",retval);

   //   retval=CTA_XML_Read(hIFILE, &htree);

    
  CTA_Tree_Create(&htree);
   make_state(&hstate);
  CTA_Tree_AddHandle(htree, "my_state", hstate);
  CTA_TreeVector_Export(hstate,CTA_FILE_STDOUT);
  printf("now writing state without metainfo \n");

  //   sprintf(uitfile,"WBC_NEW__test");
  // printf("uitfile: %s\n",uitfile);
  // CTA_String_Create(&tFILE);
  // CTA_String_Set(tFILE, uitfile);

  //retval=CTA_XML_Write(tFILE, htree);

   
   
   printf("READ NEW FORMAT XML_FILE\n=====\n");
   CTA_String_Create(&hOFILE);

   //  strcpy(infile,DA_FILE1);
   //  strcpy(infile,DA_FILE2);
          strcpy(infile,DA_FILE3);
   // strcpy(infile,DA_FILE4);




   CTA_String_Set(hOFILE, infile);



   /* make export file */
   sprintf(uitfile,"WBC_NEW__%s",infile);
   printf("uitfile: %s\n",uitfile);


   CTA_Tree_Free(&htree);

   retval=CTA_XML_Read(hOFILE, &htree);
   printf ("5) CTA_XML_Read: retval=%d  \n",retval);
   CTA_Tree_Print(htree);

   retval=CTA_Tree_GetHandleStr(htree, "treeVectorFile/tree-vector", &hstate2);
   printf ("5a) CTA_Tree_GetHandleStr retval=%d  hstate2=%d \n",retval,hstate2);


   printf("export van state:---> \n");
   CTA_TreeVector_Export(hstate2,CTA_FILE_STDOUT);
   printf("<----export van state. \n");

   CTA_String_Create(&hO2FILE);
   CTA_String_Set(hO2FILE, uitfile);
   retval=CTA_XML_Write(hO2FILE, htree);


   printf ("6) CTA_XML_Write: retval=%d  \n",retval);
   
   CTA_Tree_Free(&htree);



   retval=CTA_XML_Read(hO2FILE, &htree);
   printf ("7) CTA_XML_Read 2e keer: retval=%d  \n",retval);
   CTA_Tree_Print(htree);


   retval=CTA_Tree_GetHandleStr(htree, "treeVectorFile/tree-vector", &hstate2);
   printf ("7a) CTA_Tree_GetHandleStr retval=%d  hstate2=%d \n",retval,hstate2);

   CTA_TreeVector_Export(hstate2,CTA_FILE_STDOUT);

   CTA_String_Free(&hOFILE);
   return CTA_OK;   
   
}





