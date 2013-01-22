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

void myfunc(int a, int b){
   printf("a=%d, b=%d. a*b=%d\n", a, b, a*b);
}

int main(int argc, char* argv[]){
   CTA_Func hfunc1, hfunc2, hfunc3, hfunc4, hfunc5; //function handles
   CTA_Intf hintf; //interface handle
   BOOL err = FALSE;
   int i;
   
   printf("\n");
   printf("---------------------------------------------\n");
   printf("Unit tests for COSTA: Test2\n");
   printf("Create and delete user's functions\n");
   printf("---------------------------------------------\n");
   for (i=0; i<500; i++){
      err && CTA_Func_Create("func1",&myfunc,hintf,&hfunc1);
      err && CTA_Func_Create("func2",&myfunc,hintf,&hfunc2);
      err && CTA_Func_Create("func3",&myfunc,hintf,&hfunc3);
      err && CTA_Func_Create("func4",&myfunc,hintf,&hfunc4);
      err && CTA_Func_Create("func5",&myfunc,hintf,&hfunc5);
      err && CTA_Func_Free(&hfunc1);
      err && CTA_Func_Free(&hfunc2);
      err && CTA_Func_Free(&hfunc3);
      err && CTA_Func_Free(&hfunc4);
      err && CTA_Func_Free(&hfunc5);
   }
   if (err){
      printf("Test Failed\n");
      exit(1);
   }else{
      printf("Test succesfull\n");
      exit(0);
   }
   return CTA_OK;
}



