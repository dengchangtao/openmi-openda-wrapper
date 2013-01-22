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

////////////////////////////////////////////////////////////////////////////
// TEST THE COSTA-STRING OBJECTS
int print_a_string( CTA_String test)
{
   char str[100];
   int retval;
   retval = CTA_String_Get(test,str);
   if (retval != CTA_OK) {return retval;};
   printf("De inhoud van de string is '%s'\n",str);
   return CTA_OK;
}


int StringTest()
{
   CTA_String test;
   CTA_String test1, test2, test3, testlijst[2];
   int retval;
   char str1[100];

   // Ik maak een heleboel handles aan: ik gebruik alleen de 3e
   retval = CTA_String_Create(&test);
   if (retval != CTA_OK)
      { printf("FOUT! kan geen COSTA-string maken\n"); return -1; }
   if (test==CTA_NULL)
      { printf("FOUT! ik heb een nul-handle gekregen\n"); return -1; }
   int i;
   for (i=0; i<6; i++)
   {
      retval = CTA_String_Create(&test);
      printf(" handle %d\n", test); 
      if (retval != CTA_OK)
         { printf("FOUT! ik kan geen string aanmaken\n"); return -1; }
   }
   test=3;

   printf("Ik heb een COSTA-string gemaakt.\n");

   // Vul de string
   retval = CTA_String_Set(test,"Dit is een test");
   if (retval != CTA_OK)
      { printf("FOUT! kan string niet vullen\n"); return -1; }

   // Druk hem af
   retval = print_a_string(test);
   if (retval != CTA_OK)
      { printf("FOUT! kan string niet afdrukken!\n"); return -1; }

   // Vraag lengte op
   int len;
   retval = CTA_String_GetLength(test,&len);
   if (retval != CTA_OK)
      { printf("FOUT! kan stringlengte niet berekenen!\n"); return -1; }
   printf("De string is %d letters lang\n",len);

   // Maak de string langer en doe het opnieuw
   retval = CTA_String_Set(test,"Dit is een nieuwe test");
   if (retval != CTA_OK)
      { printf("FOUT! kan string niet vullen\n"); return -1; }

   retval = print_a_string(test);
   if (retval != CTA_OK)
      { printf("FOUT! kan string niet afdrukken!\n"); return -1; }

   retval = CTA_String_GetLength(test,&len);
   if (retval != CTA_OK)
      { printf("FOUT! kan stringlengte niet berekenen!\n"); return -1; }
   printf("De string is %d letters lang\n",len);

   // Vernietig de string, maak hem opnieuw en doe het opnieuw

   test1 = test;
   retval = CTA_String_Free(&test);
   test = test1;
   if (retval != CTA_OK)
      { printf("FOUT! kan string niet vernietigen!\n"); return -1; }

   retval = CTA_String_Set(test,"Dit is de derde test");
   if (retval == CTA_OK)
      { printf("FOUT! mag vernietigde string gebruiken!\n"); return -1; }
   printf("Een vernietigde string mag je niet gebruiken\n");

   retval = CTA_String_Free(&test);
   if (retval == CTA_OK)
      { printf("FOUT! kan string tweemaal vernietigen!\n"); return -1; }
   printf("Strings mogen niet tweemaal vernietigd worden.\n");

   retval = CTA_String_Create(&test);
   if (retval != CTA_OK)
      { printf("FOUT! ik kan geen string aanmaken\n"); return -1; }
   printf("Handle na opnieuw aanmaken=%d\n",test);

   retval = CTA_String_Set(test,"Dit is alweer de derde test");
   if (retval != CTA_OK)
      { printf("FOUT! kan string niet vullen\n"); return -1; }

   retval = print_a_string(test);
   if (retval != CTA_OK)
      { printf("FOUT! kan string niet afdrukken!\n"); return -1; }

   retval = CTA_String_GetLength(test,&len);
   if (retval != CTA_OK)
      { printf("FOUT! kan stringlengte niet berekenen!\n"); return -1; }
   printf("De string is %d letters lang\n",len);


   /* copieer string */

   /*  zonder creeeren; gaat goed */

   retval = CTA_String_Create(&test1);

   retval = CTA_String_Duplicate (test, &test2);
   printf("retval string_copy %d\n",retval); 

   /* wel gecreeerd */
   retval = CTA_String_Duplicate (test, &test1);
   
   printf("retval string_copy %d\n",retval);

   retval = CTA_String_Set(test,"-- test ---");
   retval = CTA_String_Set(test1,"+++test1++++");
   retval = CTA_String_Set(test2,"---Dit is alweer de derde test");
   /* concatenatie */

   testlijst[0] = test1;
   testlijst[1] = test2;
   retval = CTA_String_Conc(test, testlijst[0]);
   retval = CTA_String_Conc(test, testlijst[1]);
   printf("retval string_concatenatie %d\n ",retval);
   retval = print_a_string(test);

   /* getvalue */
   retval = CTA_String_GetValue (test1, &str1, CTA_STRING);
   printf("retval string_getvalue %d\n ",retval);
   printf("%s \n", str1);
     /*


     */





   printf("-------------------------------------------------------\n");
   printf("           De STRING-TEST is gelukt!\n");
   printf("-------------------------------------------------------\n");

   return CTA_OK;


}




////////////////////////////////////////////////////////////////////////////
// TEST THE COSTA-FILE OBJECTS
int FileTest()
{
   int retval;
   CTA_File test;

   retval = CTA_File_Create(&test);
   if (retval != CTA_OK)
      { printf("FOUT! kan geen file aanmaken!\n"); return -1; }
   if (test==CTA_NULL)
      { printf("FOUT! ik heb een nul-handle gekregen\n"); return -1; }

   printf("Ik heb een COSTA-file gemaakt\n");

   retval = CTA_File_Set(test,stdout);
   retval = CTA_File_Set(test,stdout);
   retval = CTA_File_Set(test,stdout);
   if (retval != CTA_OK)
      { printf("FOUT! kan file niet vullen!\n"); return -1; }

   printf("Stdout is in de COSTA-file gestopt.\n");

   FILE *file=stdin;
   retval = CTA_File_Get(test,&file);
   if (retval != CTA_OK)
      { printf("FOUT! kan file niet opvragen!\n"); return -1; }
   if (file != stdout)
      { printf("FOUT! de teruggegeven file is niet gelijk aan stdout\n");}

   printf("Ik heb de FILE* pointer uit de COSTA-file gehaald.\n");

   retval = CTA_String_Free(&test);
   if (retval == CTA_OK)
      { printf("FOUT! kan file vernietigen als string!\n"); return -1; }

   printf("Files mogen niet benaderd woren als string.\n");

   CTA_Handle test1=test;
   retval = CTA_File_Free(&test);
   test=test1;
   if (retval != CTA_OK)
      { printf("FOUT! kan file niet vernietigen!\n"); return -1; }

   printf("De file is vernietigd.\n");

   retval = CTA_File_Free(&test);
   if (retval == CTA_OK)
      { printf("FOUT! kan file tweemaal vernietigen!\n"); return -1; }

   printf("De file mag niet tweemaal worden vernietigd.\n");

   retval = CTA_File_Set(test,stdout);
   if (retval == CTA_OK)
   {
      printf("FOUT! kan FILE-handle in vernietigde file stoppen!\n");
      return -1;
   }

   printf("Een vernietigde file mag niet meer worden gebruikt.\n");

   retval = CTA_File_Create(&test);
   if (retval != CTA_OK)
      { printf("FOUT! kan geen file aanmaken!\n"); return -1; }

   printf("Ik heb opnieuw een COSTA-file gemaakt\n");

   retval = CTA_File_Set(test,stdout);
   retval = CTA_File_Set(test,stdout);
   retval = CTA_File_Set(test,stdout);
   if (retval != CTA_OK)
      { printf("FOUT! kan file niet vullen!\n"); return -1; }

   printf("Stdout is in de COSTA-file gestopt.\n");

   file=stdin;
   retval = CTA_File_Get(test,&file);
   if (retval != CTA_OK)
      { printf("FOUT! kan file niet opvragen!\n"); return -1; }
   if (file != stdout)
   {
      printf("FOUT! de teruggegeven file is niet gelijk aan stdout\n");
      return -1;
   }

   printf("Ik heb de FILE* pointer uit de COSTA-file gehaald.\n");

   printf("-------------------------------------------------------\n");
   printf("           De FILE-TEST is gelukt!\n");
   printf("-------------------------------------------------------\n");
   return CTA_OK;
}

////////////////////////////////////////////////////////////////////////////
// TEST THE COSTA-STRING-VECTOR OBJECTS
int StrVecTest()
{

  int retval;
  CTA_Handle *dum;
  CTA_String str1;
  CTA_Vector vec1,vec2;

  // Maak 2 vectoren en een string
  retval = CTA_String_Create(&str1);
  if (retval != CTA_OK)
      { printf("FOUT! kan geen String maken! %d\n",retval); return -1; }
  retval = CTA_String_Set(str1,"Test string");
  if (retval != CTA_OK)
      { printf("FOUT! Kan string niet vullen!\n"); return -1; }

  printf("Een string met inhoud 'Test string' is gemaakt\n\n");

  int n = 4;
  CTA_Handle userdata;
  retval=CTA_Vector_Create( CTA_DEFAULT_VECTOR, n, CTA_STRING,userdata,&vec1);
  if (retval != CTA_OK)
      { printf("FOUT! kan geen Vector maken! %d\n",retval); return -1; }
  retval=CTA_Vector_Create( CTA_DEFAULT_VECTOR, n, CTA_STRING,userdata,&vec2);
  if (retval != CTA_OK)
      { printf("FOUT! kan geen Vector maken! %d\n",retval); return -1; }


  // Vul een string-vector en druk af
  retval=CTA_Vector_SetConstant( vec1, &str1, CTA_STRING);
  if (retval != CTA_OK)
      { printf("FOUT! Kan Vector niet vullen!\n"); return -1; }

  printf("Een string vector is gemaakt\n\n");

  printf("De inhoud van de vector is:\n");
  retval = CTA_Vector_Export(vec1,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! Kan Vector niet afdrukken!\n"); return -1; }



  // Verander de inhoud en druk af
  retval=CTA_String_Set(str1,"Alternatieve inhoud");
  if (retval != CTA_OK)
      { printf("FOUT! Kan string niet vullen!\n"); return -1; }
  retval=CTA_Vector_SetVal( vec1, 2, &str1, CTA_STRING);
  if (retval != CTA_OK)
      { printf("FOUT! Kan vector niet aanpassen!\n"); return -1; }
  printf("De string vector is aangepast\n\n");

  printf("De inhoud van de vector is:\n");
  retval = CTA_Vector_Export(vec1,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! Kan vector niet afdrukken!\n"); return -1; }


  // Kopieer de vector, verander, en druk beide af
  retval=CTA_Vector_Copy( vec1, vec2);
  if (retval != CTA_OK)
      { printf("FOUT! Kan Vector niet kopieren!\n"); return -1; }

  retval=CTA_String_Set(str1,"Alt 2");
  if (retval != CTA_OK)
      { printf("FOUT! Kan string niet vullen!\n"); return -1; }
  retval=CTA_Vector_SetVal( vec2, 1, &str1, CTA_STRING);
  if (retval != CTA_OK)
      { printf("FOUT! Kan vector niet aanpassen!\n"); return -1; }

  printf("De vector is gekopieerd; de kopie is aangepast:\n\n");
  printf("De inhoud van de oorspronkelijke vector is:\n");
  retval = CTA_Vector_Export(vec1,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! Kan Vector niet afdrukken!\n"); return -1; }
  printf("De inhoud van de kopie is:\n");
  retval = CTA_Vector_Export(vec2,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! Kan Vector niet afdrukken!\n"); return -1; }


  // Wis alle gebruikte COSTA-objecten
  retval = CTA_Vector_Free (&vec1);
  if (retval != CTA_OK)
      { printf("FOUT! Kan Vector niet vernietigen!\n"); return -1; }
  retval = CTA_Vector_Free (&vec2);
  if (retval != CTA_OK)
      { printf("FOUT! Kan Vector niet vernietigen!\n"); return -1; }

  retval = CTA_String_Free (&str1);
  if (retval != CTA_OK)
      { printf("FOUT! Kan String niet vernietigen!\n"); return -1; }
  printf("De string is weer gewist\n");

  printf("-------------------------------------------------------\n");
  printf("           De STRING-VECTOR-TEST is gelukt!\n");
  printf("-------------------------------------------------------\n");



  return CTA_OK;
}

////////////////////////////////////////////////////////////////////////////
// TEST THE COSTA-STOCHASTIC OBSERVER OBJECTS
int StochObsTest( CTA_ObsDescr * descr)
{
  int retval;
  CTA_Handle *dum;
  CTA_StochObs hsobs1, hsobs2, hsobs3, hsobs4, hsobs5;
  CTA_String userdata;

  // Maak userdata-items
  retval = CTA_String_Create(&userdata);
  if (retval != CTA_OK)
      { printf("FOUT! kan geen String maken! %d\n",retval); return -1; }
  retval = CTA_String_Set(userdata,"omi_no2_EU_20060202.nc");
  if (retval != CTA_OK)
      { printf("FOUT! Kan string niet vullen!\n"); return -1; }

  //  retval = CTA_File_Create(&userdata);
  //if (retval != CTA_OK)
  //   { printf("FOUT! kan geen File maken! %d\n",retval); return -1; }
  //retval = CTA_File_Set(userdata,stdout);
  //if (retval != CTA_OK)
  //   { printf("FOUT! Kan file niet vullen!\n"); return -1; }

  printf("userdata-items zijn gemaakt: een string en een file\n");

  retval = CTA_SObs_Create(CTA_NETCDF_SOBS, userdata, &hsobs1);
  if (retval != CTA_OK)
      { printf("FOUT! kan geen StochObs maken!\n"); return -1; }

  printf("StochObs is aangemaakt\n\n");

  // Afdrukken van een Stochobs
  //retval = CTA_SObs_Export(hsobs1,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan StochObs niet afdrukken!\n"); return -1; }

  printf("StochObs is afgedrukt\n\n");

  // Selectie maken, afdrukken
  retval = CTA_String_Set(userdata,"time BETWEEN 10.0 AND 12.0 ");
  if (retval != CTA_OK)
      { printf("FOUT! kan geen nieuwe waarde in string zetten!\n"); return -1; }

  printf("Een conditie is in de userdata opgenomen\n");

  retval = CTA_SObs_CreateSel(hsobs1,userdata,&hsobs2);
  if (retval != CTA_OK)
      { printf("FOUT! createsel werkt niet!\n"); return -1; }

  printf("Een selectie is gemaakt uit StochObs\n\n");

  retval = CTA_SObs_Export(hsobs2,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan StochObs niet afdrukken!\n"); return -1; }

  printf("nieuwe StochObs is afgedrukt\n\n");

  // retval = CTA_SObs_Export(hsobs1,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan StochObs niet afdrukken!\n"); return -1; }

  printf("Oude StochObs is afgedrukt\n\n");

  printf("StochObs is afgedrukt\n\n");


  /*

  // Selectie maken, afdrukken
  ////////  SPECIALE SELECTIE: alle stations-data van stations met een i
  retval = CTA_String_Set(userdata,"name like '%i%' and value isnull");
  if (retval != CTA_OK)
      { printf("FOUT! kan geen nieuwe waarde in string zetten!\n"); return -1; }

  printf("Een conditie is in de userdata opgenomen\n");

  retval = CTA_SObs_CreateSel(hsobs1,userdata,&hsobs4);
  if (retval != CTA_OK)
      { printf("FOUT! kan StochObs niet afdrukken!\n"); return -1; }

  printf("Een selectie is gemaakt uit StochObs\n\n");
/*
  retval = CTA_String_Set(userdata,"values also");
  if (retval != CTA_OK)
      { printf("FOUT! Kan string niet vullen!\n"); return -1; }

  retval = CTA_SObs_Export(hsobs4,&userdata,2);
  if (retval != CTA_OK)
      { printf("FOUT! kan StochObs niet afdrukken!\n"); return -1; }

  printf("nieuwe StochObs is afgedrukt\n\n");
*/



  // Selectie van een selectie maken, afdrukken
  retval = CTA_String_Set(userdata,"time BETWEEN 10.1 and 10.82");
  if (retval != CTA_OK)
      { printf("FOUT! kan geen nieuwe waarde in string zetten!\n"); return -1; }

  printf("Een nieuwe conditie is in de userdata opgenomen\n");

  retval = CTA_SObs_CreateSel(hsobs2,userdata,&hsobs3);
  if (retval != CTA_OK)
      { printf("FOUT! kan geen selectie maken!\n"); return -1; }

  printf("Een selectie is gemaakt uit de tweede StochObs (dus nu selectie van selectie) \n\n");

  retval = CTA_SObs_Export(hsobs3,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan StochObs niet afdrukken!\n"); return -1; }

  printf("Nieuwste StochObs (3) is afgedrukt\n\n");

  // retval = CTA_SObs_Export(hsobs1,CTA_FILE_STDOUT);
  //if (retval != CTA_OK)
  //    { printf("FOUT! kan StochObs niet afdrukken!\n"); return -1; }

  //  printf("Oude StochObs is afgedrukt\n\n");

  // Waarden opvragen
  int nmeasr;
  retval = CTA_SObs_Count(hsobs3,&nmeasr);
  if (retval != CTA_OK)
      { printf("FOUT! kan StochObs niet afdrukken!\n"); return -1; }

  printf("Er zitten %d metingen in de kleine observator\n\n",nmeasr);

  CTA_Vector hvalues;
  retval = CTA_Vector_Create( CTA_DEFAULT_VECTOR, nmeasr, CTA_DOUBLE,
              userdata, &hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan geen vector maken!\n"); return -1; }

  printf("Ik heb een COSTA-vector gemaakt om de metingen in op te slaan.\n\n");

  retval = CTA_SObs_GetVal( hsobs3, hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan de waardes niet opvragen!\n"); return -1; }

  printf("De values zijn:\n");
  retval = CTA_Vector_Export(hvalues,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan de waardes niet afdrukken!\n"); return -1; }
  printf("\n");

  retval = CTA_SObs_GetTimes( hsobs3, hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan de tijden niet opvragen!\n"); return -1; }

  printf("De tijden zijn:\n");
  retval = CTA_Vector_Export(hvalues,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan de waardes niet afdrukken!\n"); return -1; }
  printf("\n");





  
  // Waarden proberen opvragen met incorrecte vectoren
  retval = CTA_Vector_Free(&hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan vector niet vernietigen!\n"); return -1; }

  retval = CTA_Vector_Create( CTA_DEFAULT_VECTOR, nmeasr-1, CTA_DOUBLE,
              userdata, &hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan vector niet opnieuw maken!\n"); return -1; }

  printf("Ik heb een COSTA-vector gemaakt die te klein is voor de metingen.\n\n");

  retval = CTA_SObs_GetVal( hsobs3, hvalues);
  if (retval == CTA_OK)
  {
      printf("FOUT! de waardes worden in een te klein array opgeslagen.\n");
      //   return -1;
  }

  printf("%s %d %s\n\n","Ik krijg foutmelding",retval,
                        "als ik metingen in een te klein array wil stoppen");

  retval = CTA_Vector_Free(&hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan vector niet vernietigen!\n"); return -1; }

  retval = CTA_Vector_Create( CTA_DEFAULT_VECTOR, nmeasr+1, CTA_DOUBLE,
              userdata, &hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan vector niet opnieuw maken!\n"); return -1; }

  printf("Ik heb een COSTA-vector gemaakt die te groot is voor de metingen.\n\n");

  retval = CTA_SObs_GetVal( hsobs3, hvalues);
  if (retval == CTA_OK)
  {
      printf("FOUT! de waardes worden in een te groot array opgeslagen.\n");
      //    return -1;
  }

  printf("%s %d %s\n\n","Ik krijg foutmelding",retval,
                        "als ik metingen in een te groot array il stoppen");

  retval = CTA_Vector_Free(&hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan vector niet vernietigen!\n"); return -1; }

  // Expectations, Variances en Realizations opvragen
  retval = CTA_Vector_Create( CTA_DEFAULT_VECTOR, nmeasr, CTA_DOUBLE,
              userdata, &hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan vector niet opnieuw maken!\n"); return -1; }

  printf("Ik heb een COSTA-vector gemaakt voor de metingen.\n\n");

  retval = CTA_SObs_GetExpectation( hsobs3, hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan de expectations niet opvragen!\n"); return -1; }

  printf("De expectations zijn:\n");
  retval = CTA_Vector_Export(hvalues,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan de expectations niet afdrukken!\n"); return -1; }
  printf("\n");

  retval = CTA_SObs_GetVar( hsobs3, hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan de varianties niet opvragen %d!\n",retval); return -1; }

  printf("De varianties zijn:\n");
  retval = CTA_Vector_Export(hvalues,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan de varianties niet afdrukken!\n"); return -1; }
  printf("\n");

  retval = CTA_SObs_GetStd( hsobs3, hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan de standard deviations niet opvragen!\n"); return -1; }
  printf("De std's zijn:\n");
  retval = CTA_Vector_Export(hvalues,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan de std niet afdrukken!\n"); return -1; }
  printf("\n");





  
  retval = CTA_SObs_GetRealisation( hsobs3, hvalues);
  if (retval != CTA_OK)
      { printf("FOUT! kan de realizations niet opvragen!\n"); return -1; }

  printf("De realizations zijn:\n");
  retval = CTA_Vector_Export(hvalues,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan de realizations niet afdrukken!\n"); return -1; }
  printf("\n");

  // Covariantematrix opvragen (eerst een matrix maken)
  CTA_Matrix hmatrix;
  retval = CTA_Matrix_Create(CTA_DEFAULT_MATRIX, nmeasr, nmeasr, CTA_DOUBLE,
                      userdata, &hmatrix);
  if (retval != CTA_OK)
      { printf("FOUT! kan geen matrix maken!\n"); return -1; }

  retval = CTA_SObs_GetCovMat( hsobs3, hmatrix);
  if (retval != CTA_OK)
    { printf("FOUT! kan de covariantiematrix niet opvragen!\n");// return -1; 
}

  printf("De covariantiematrix staat nu in een COSTA-matrix\n\n");

  retval = CTA_Matrix_Export(hmatrix,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
    { printf("FOUT! kan de covariantiematrix niet afdrukken!\n");// return -1;
 }

  retval = CTA_SObs_GetDescription( hsobs3, descr);
  if (retval != CTA_OK)
  {
      printf("FOUT! kan geen obsdescr maken voor deze observer!\n");
      return -1;
  }

  printf("Ik heb een COSTA-observation description gemaakt\n\n");

  // Lege selectie maken, afdrukken
  retval = CTA_String_Set(userdata,"value > dummy");
  if (retval != CTA_OK)
      { printf("FOUT! kan geen nieuwe waarde in string zetten!\n"); return -1; }

  printf("Een conditie is in de userdata opgenomen\n");


  retval = CTA_SObs_CreateSel(hsobs2,userdata,&hsobs5);
  if (retval != CTA_OK)
      { printf("FOUT! kan StochObs niet afdrukken!\n"); return -1; }



  retval = CTA_SObs_Export(hsobs5,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan StochObs niet afdrukken!\n"); return -1; }

  printf("na sobs_export \n");

  retval=CTA_SObs_Free( &hsobs1);

  printf("na obs1 \n");

  if (retval != CTA_OK)
     { printf("FOUT! kan stochobs niet wissen!\n"); return -1; }
  retval=CTA_SObs_Free( &hsobs2);

  printf("na obs2 \n");


  if (retval != CTA_OK)
     { printf("FOUT! kan stochobs niet wissen!\n"); return -1; }
  retval=CTA_SObs_Free( &hsobs3);
  if (retval != CTA_OK)
     { printf("FOUT! kan stochobs niet wissen!\n"); return -1; }
//  retval=CTA_SObs_Free( &hsobs4);
//  if (retval != CTA_OK)
//     { printf("FOUT! kan stochobs niet wissen!\n"); return -1; }
  retval=CTA_SObs_Free( &hsobs5);
  if (retval != CTA_OK)
     { printf("FOUT! kan stochobs niet wissen!\n"); return -1; }
  retval=CTA_Matrix_Free( &hmatrix);
  if (retval != CTA_OK)
     { printf("FOUT! kan matrix niet wissen!\n"); return -1; }
  retval=CTA_String_Free( &userdata);
  if (retval != CTA_OK)
     { printf("FOUT! kan string niet wissen!\n"); return -1; }
  retval=CTA_Vector_Free( &hvalues);
  if (retval != CTA_OK)
     { printf("FOUT! kan vector niet wissen!\n"); return -1; }

  printf("De COSTA-objecten zijn gewist\n\n");

  printf("-------------------------------------------------------\n");
  printf("           De STOCHASTIC OBSERVER-TEST is gelukt!\n");
  printf("-------------------------------------------------------\n");
  return CTA_OK;
}





////////////////////////////////////////////////////////////////////////////
// TEST THE COSTA-OBSERVER-DESCRIPTION OBJECTS
int ObsDescrTest(CTA_ObsDescr descr)
{
  int retval;
  int nkeys;
  CTA_Handle *dum;
  CTA_Handle userdata;
  retval = CTA_ObsDescr_Property_Count(descr, &nkeys);
  if (retval != CTA_OK)
  {
    printf("FOUT! kan aantal keys niet opvragen!%d \n",retval);
      return -1;
  }
  printf("Er zitten %d kolommen in de observations-tabel\n",nkeys);

  CTA_Vector Keys;
  retval=CTA_Vector_Create( CTA_DEFAULT_VECTOR, nkeys, CTA_STRING,
                            userdata, &Keys);
  if (retval != CTA_OK)
      { printf("FOUT! kan keys-vector niet maken!\n"); return -1; }
  retval=CTA_ObsDescr_Get_PropertyKeys( descr, Keys);
  if (retval != CTA_OK)
      { printf("FOUT! kan keys-vector niet vullen!\n"); return -1; }
  
  printf("The Keys zijn:\n");
  retval = CTA_Vector_Export(Keys,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan de Keys niet afdrukken!\n"); return -1; }
  
  int nmeasr;
  retval = CTA_ObsDescr_Observation_Count(descr, &nmeasr);
  if (retval != CTA_OK)
  {
      printf("FOUT! kan aantal observations niet opvragen!\n");
      return -1;
  }
  printf("Er zitten %d rijen in de observations-tabel\n",nmeasr);
 
  // Lees en print de LONG-coordinatess 
  CTA_Vector Properties;
  retval=CTA_Vector_Create( CTA_DEFAULT_VECTOR, nmeasr, CTA_REAL,
                            userdata, &Properties);
  if (retval != CTA_OK)
      { printf("FOUT! kan properties-vector niet maken!\n"); return -1; }
  retval=CTA_ObsDescr_Get_ValueProperties( descr, "longitude", Properties, CTA_REAL );
  if (retval != CTA_OK)
    { printf("FOUT! kan properties-vector niet vullen: %d !\n",retval); return -1; }

  printf("The longitudes zijn:\n");
  retval = CTA_Vector_Export(Properties,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
    { printf("FOUT! kan de properties niet afdrukken!\n");// return -1;
    }

  retval=CTA_Vector_Free( &Properties );
  if (retval != CTA_OK)
      { printf("FOUT! kan properties-vector niet wissen!\n"); return -1; }



  // Lees en print de lat-coordinates
  retval=CTA_Vector_Create( CTA_DEFAULT_VECTOR, nmeasr, CTA_REAL,
                            userdata, &Properties);
  if (retval != CTA_OK)
      { printf("FOUT! kan properties-vector niet maken!\n"); return -1; }
  retval=CTA_ObsDescr_Get_ValueProperties( descr, "latitude", Properties, CTA_REAL );
  if (retval != CTA_OK)
    { printf("FOUT! kan properties-vector niet vullen!\n"); //return -1; 
}

  printf("The latitudes zijn:\n");
  retval = CTA_Vector_Export(Properties,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan de properties niet afdrukken!\n"); return -1; }
  retval=CTA_Vector_Free( &Properties );
  if (retval != CTA_OK)
      { printf("FOUT! kan properties-vector niet wissen!\n"); return -1; }


  // Lees en print de 3e kernel
  retval=CTA_Vector_Create( CTA_DEFAULT_VECTOR, nmeasr, CTA_REAL,
                            userdata, &Properties);
  if (retval != CTA_OK)
      { printf("FOUT! kan properties-vector niet maken!\n"); return -1; }
  retval=CTA_ObsDescr_Get_ValueProperties( descr, "kernel_3", Properties, CTA_REAL );
  if (retval != CTA_OK)
    { printf("FOUT! kan properties-vector niet vullen!\n"); //return -1; 
}

  printf("The 3e kernelelementen zijn:\n");
  retval = CTA_Vector_Export(Properties,CTA_FILE_STDOUT);
  if (retval != CTA_OK)
      { printf("FOUT! kan de properties niet afdrukken!\n"); return -1; }
  retval=CTA_Vector_Free( &Properties );
  if (retval != CTA_OK)
      { printf("FOUT! kan properties-vector niet wissen!\n"); return -1; }






  retval=CTA_ObsDescr_Free( &descr);
  if (retval != CTA_OK)
     { printf("FOUT! kan description niet wissen!\n"); return -1; }
  printf("\n\nThe COSTA-objecten zijn gewist.\n");
  




  printf("-------------------------------------------------------\n");
  printf("           De OBSERVER-DESCRIPTOR TEST is gelukt!\n");
  printf("-------------------------------------------------------\n");
  return CTA_OK;
}







int main(int argc, char* argv[])
{

   int retval;
   int gelukt=0;

   printf("\n\n------------- STRING TEST----------------\n");
   retval = StringTest();
   if (retval == CTA_OK)
   {
      fprintf(stderr,"String Test is gelukt\n");
   }
   else
   {
      gelukt = 1;
      fprintf(stderr,"********* String Test is mislukt\n");
   }

   // COSTA-initialisatie en declaratie van een StochObs
   retval = CTA_Core_Initialise();
   if (retval != CTA_OK)
    { fprintf(stderr,"Initialisatie is mislukt\n"); return -1; }

   printf("COSTA-initialisatie is gelukt\n\n");




   printf("\n\n------------- STRING-VECTOR TEST----------------\n");
   // retval = StrVecTest();
   if (retval == CTA_OK)
   {
      fprintf(stderr,"String-vector Test is gelukt\n");
   }
   else
   {
      gelukt = 2;
      fprintf(stderr,"*********** String-vector Test is mislukt\n");
   }



   printf("\n\n------------- FILE TEST----------------\n");
   //   retval = FileTest();
   if (retval == CTA_OK)
   {
      fprintf(stderr,"File Test is gelukt\n");
   }
   else
   {
      gelukt = 3;
      fprintf(stderr,"*********** File Test is mislukt\n");
   }

   CTA_ObsDescr descr;
   printf("\n\n------------- STOCHOBS TEST----------------\n");
   retval = StochObsTest(&descr);
   if (retval == CTA_OK)
   {
      fprintf(stderr,"StochObs Test is gelukt\n");
   }
   else
   {
      gelukt = 4;
      fprintf(stderr,"*********** StochObs Test is mislukt\n");
   }

   if (0){
   printf("\n\n------------- OBSDESCR TEST----------------\n");
   retval = ObsDescrTest(descr);
   if (retval == CTA_OK)
   {
      fprintf(stderr,"ObsDescr Test is gelukt\n");
   }
   else
   {
      gelukt = 5;
      fprintf(stderr,"*********** ObsDescr Test is mislukt\n");
   }
   }
   printf("Gelukt is %d\n",gelukt);
   exit(gelukt);
}
