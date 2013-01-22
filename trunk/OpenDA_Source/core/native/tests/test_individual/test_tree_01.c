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

int main(int argc, char **argv)
{
   CTA_Tree    hroot;
   CTA_Tree    hsub1;
   CTA_Tree    hsub2;
   CTA_Tree    hsub3;
   CTA_String  htxt1;
   CTA_String  htxt2;
   CTA_String  htxt3;
   CTA_String  htxt4;
   CTA_String  htxt5;
   CTA_String  hitem;
   char str[9];
   CTA_Handle hhandle;
   int retval, count ;

   CTA_Core_Initialise();

   CTA_String_Create(&htxt1);
   CTA_String_Set(htxt1, "TesT1TexT");
   CTA_String_Create(&htxt2);
   CTA_String_Set(htxt2, "TesT2TexT");
   CTA_String_Create(&htxt3);
   CTA_String_Set(htxt3, "TesT3TexT");
   CTA_String_Create(&htxt4);
   CTA_String_Set(htxt4, "TesT4TexT");
   CTA_String_Create(&htxt5);
   CTA_String_Set(htxt5, "TesT5TexT");
   
   CTA_Tree_Create(&hroot);
   CTA_Tree_Create(&hsub1);
   CTA_Tree_Create(&hsub2);
   CTA_Tree_Create(&hsub3);
   
   CTA_Tree_AddHandle(hroot, "01", htxt1);
   CTA_Tree_AddHandle(hroot, "02", hsub1);
   
   CTA_Tree_AddHandle(hsub1, "11", hsub2);
   CTA_Tree_AddHandle(hsub1, "12", htxt2);
   CTA_Tree_AddHandle(hsub1, "13",  hsub3);

   CTA_Tree_AddHandle(hsub2, "21", htxt3);
   
   CTA_Tree_AddHandle(hsub3, "31", htxt4);
   // Note same name !
   CTA_Tree_AddHandle(hsub3, "31", htxt5);


   /* retval=CTA_Tree_GetHandle(hroot,htxt1,&hitem); */
   /* printf ("1) retval=%d %d %d  \n",retval,htxt1,hitem); */


   retval = CTA_Tree_CountItems(hroot, &count );
   printf ("6) retval=%d %d  \n",retval,count);

   retval = CTA_Tree_CountItems(hsub3, &count );
   printf ("7) retval=%d %d  \n",retval,count);

   retval = CTA_Tree_GetItem (hroot, 1, &hhandle);
   printf ("6) retval; linkertak; hsub1= %d %d %d \n",retval,hhandle,htxt1);

   retval = CTA_Tree_GetItem (hroot, 2, &hhandle);
   printf ("6) retval=%d %d %d \n",retval,hhandle,hsub1);

   retval = CTA_Tree_GetItemValue ( hroot,1,&htxt5, CTA_STRING);  
   printf ("6) retval=%d  \n",retval);
   retval = CTA_String_Get(htxt5,str);
   printf ("6) retval=%d  %s\n ",retval,str);

   CTA_Tree_Print(hroot);

   hitem=0;
   retval=CTA_Tree_GetHandleStr(hroot, "02/11/21", &hitem);
   printf ("8) retval=%d %d\n ",retval,hitem);
   retval = CTA_String_Get(hitem,str);
   printf ("8) retval=%d %s\n ",retval,str);

   retval=CTA_Tree_GetHandleStr(hroot, "02/13/31<2>", &hitem);
   retval = CTA_String_Get(hitem,str);
   printf ("8) retval=%d %s\n ",retval,str);

   CTA_String_Create(&hitem);
   CTA_String_Set(hitem, "02/13/31");
   retval=CTA_Tree_CountHandles(hroot, hitem, &count);
   printf ("8) retval=%d %d\n ",retval,count);
   
   retval=CTA_Tree_CountHandlesStr(hroot, "02/13/31", &count);
   printf ("8) retval=%d %d\n ",retval,count);


   CTA_Tree_Free(&hroot);
   return CTA_OK;   
}
