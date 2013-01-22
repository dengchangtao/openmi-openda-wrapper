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

int print_a_string( CTA_String test) {
   char str[100];
   int retval;
   retval = CTA_String_Get(test,str);
   if (retval != CTA_OK) {return retval;};
   printf("De inhoud van de string is '%s'\n",str);
   return CTA_OK;
}



int main(int argc, char* argv[]){
   int i;  
   int const n=3;
   double vals1[n], vals2[n], vals3[n], vals4[n+1];
   CTA_Handle userdata;
   double alpha,dotprod,norm2;
   int iloc;
   int retval;
   int nsize, maxlen;
   CTA_String str1;
 
   CTA_Datatype datatype;
   CTA_Vector hvector1,hvector2,hvector3,hvector4,hvector5;
   CTA_Vector hformaat;
   CTA_Vector tabel[3];


   printf("\n");
   printf("---------------------------------------------\n");
   printf("Unit tests for COSTA: Test4\n");
   printf("Test vector implementation \n");
   printf("---------------------------------------------\n");

   /* Initialise default installation of vector object */
   retval=CTA_Core_Initialise();


   /* Create a vector */
   for (i=0;i<n;i++){
      vals1[i]=(double) i;
      vals2[i]=(double) i*i;
   }
   printf ("vals1=[");
   for (i=0;i<n;i++) {printf ("%lg ",vals1[i]);};
   printf ("]\n");
   printf ("vals2=[");
   for (i=0;i<n;i++) {printf ("%lg ",vals2[i]);};
   printf ("]\n");
   
   // Create the COSTA vector hvector1: 
   retval=CTA_Vector_Create(
              CTA_DEFAULT_VECTOR,  // Vector Class
              n,                   // vector dimensions
              CTA_DOUBLE,          // data type: double precision
              userdata,            // ???
              &hvector1);          // vector (handle)

   // Fill the COSTA vector 
   retval=CTA_Vector_SetVals(
              hvector1,            // vector (handle)
              vals1,               // values
              n,                   // vector dimensions
              CTA_DOUBLE);         // data type: double precision

   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n, CTA_DOUBLE,userdata,&hvector2);


   /* ce vul tabel met drie vectoren */

   tabel[0] = hvector1;
   tabel[1] = hvector2;
   tabel[2] = hvector2;


   /* declareer hformaat : een string */
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, 3, CTA_STRING, userdata, &hformaat);
   retval = CTA_String_Create(&str1);
   printf("string vector formaat retval=%d\n",retval);
   retval = CTA_String_Set(str1,"%12.3g  ");
   printf("string vector formaat retval=%d\n",retval);

   retval = CTA_Vector_SetVal(hformaat,1,&str1,CTA_STRING);
   printf("string vector formaat retval=%d\n",retval);
   retval = CTA_Vector_SetVal(hformaat,2,&str1,CTA_STRING);
   printf("string vector formaat retval=%d\n",retval);
   retval = CTA_Vector_SetVal(hformaat,3,&str1,CTA_STRING);
   printf("string vector formaat retval=%d\n",retval);
   /* ce */
   printf("retval=%d\n",retval);

   retval=CTA_Vector_GetVal(hformaat,2,&str1,CTA_STRING);
   printf("retval getval =%d\n",retval);
   printf("str1 =");
   retval=print_a_string(str1);



   /* SCAL */
   printf("SCAL: vals1=2.0*vals1\n");
   alpha=2.0;
   retval=CTA_Vector_Scal(hvector1,alpha);

   // Obtain the current values
   retval=CTA_Vector_GetVals(hvector1,vals1,n,CTA_DOUBLE);
   printf ("x=[");
   for (i=0;i<n;i++) {printf ("%lg ",vals1[i]);};
   printf ("]\n");

   /* Copy */
   printf("COPY: y=x\n");
   retval=CTA_Vector_Copy(hvector1,hvector2);

   retval=CTA_Vector_GetVals(hvector2,vals2,n,CTA_DOUBLE);
   printf ("vals2=[");
   for (i=0;i<n;i++) {printf ("%lg ",vals2[i]);};
   printf ("]\n");

   printf("AXPY: y=x+0.5*y\n");
   alpha=0.5;
   retval=CTA_Vector_Axpy(hvector2,alpha,hvector1);
   printf("retval=%d\n",retval);

   retval=CTA_Vector_GetVals(hvector2,vals2,n,CTA_DOUBLE);
   printf ("y=[");
   for (i=0;i<n;i++) {printf ("%lg ",vals2[i]);};
   printf ("]\n");

   printf("DOT: (x,y)\n");
   retval=CTA_Vector_Dot(hvector1,hvector2,&dotprod);
   printf ("(x,y)=%lg\n",dotprod);


   printf("NORM2: |x|\n");
   retval=CTA_Vector_Nrm2(hvector1,&norm2);
   printf ("|x|=%lg\n",norm2);

   printf("ALOC: x\n");
   retval=CTA_Vector_Amax(hvector1,&iloc);
   printf ("loc of max x =%d\n",iloc);

   printf("DUPLICATE: z from x\n");
   retval=CTA_Vector_Duplicate(hvector1,&hvector3);

   printf("z=[");
   retval=CTA_Vector_GetVals(hvector3,vals3,n,CTA_DOUBLE);
   for (i=0;i<n;i++) {printf ("%lg ",vals3[i]);};
   printf ("]\n");

   printf("GET SIZE z\n");
   retval=CTA_Vector_GetSize(hvector3,&nsize);
   printf("Size of z is %d\n",nsize);

   printf("COPY: z=x\n");
   retval=CTA_Vector_Copy(hvector1,hvector3);

   retval=CTA_Vector_GetVals(hvector3,vals3,n,CTA_DOUBLE);
   printf ("z=[");
   for (i=0;i<n;i++) {printf ("%lg ",vals3[i]);};
   printf ("]\n");

   retval=CTA_Vector_Duplicate(hvector3, &hvector4);
   printf("retval=%d\n",retval);
   retval=CTA_Vector_GetVals(hvector4,vals3,n,CTA_DOUBLE);
   printf("retval=%d\n",retval);
   printf ("z_dupl=[");
   for (i=0;i<n;i++) {printf ("%lg ",vals3[i]);};
   printf ("]\n");

   /*  append vector */ 
   retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR, n+1, CTA_DOUBLE,userdata,&hvector5);
   printf("retval=%d\n",retval);
   retval =CTA_Vector_AppendVal(hvector3,&vals2[1] ,CTA_DOUBLE);
   printf("appendval. retval=%d\n",retval);
   retval=CTA_Vector_GetSize(hvector3,&nsize);
   printf("Size of zp is %d\n",nsize);
   retval=CTA_Vector_GetVals(hvector3,vals4,n+1,CTA_DOUBLE);
   printf("retval=%d\n",retval);
   printf ("zp=[");
   for (i=0;i<n+1;i++) {printf ("%lg ",vals4[i]);};
   printf ("]\n");


   /* maxlen */
   retval=CTA_Vector_GetMaxLen (hformaat, &maxlen);
   printf("maxlen of zp is %d\n",maxlen);

  
   /* print tabel */
   retval=CTA_Vector_Print_Table (tabel,3,hformaat);

   printf("print tabel %d\n",retval);


   /* get datatype */
   retval = CTA_Vector_GetDatatype(hvector1,&datatype);
   printf("print datatype %d\n",retval);
   printf("datatype %d\n",datatype);

   /* export vector */
   retval=CTA_Vector_Export(hvector1, CTA_FILE_STDOUT) ;
   printf("retval ctavector_export %d\n",retval);


   printf("---------------------------------------------\n");
   printf("End test\n");
   printf("---------------------------------------------\n");
   printf("\n");
   exit(0);
}



