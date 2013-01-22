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


int main(int argc, char **argv)
{
   CTA_Tree     htree;
   CTA_String   hIFILE;
   int          retval;
   
   CTA_Core_Initialise();
   
   retval=CTA_String_Create(&hIFILE);
   printf ("1) retval=%d  \n",retval);

   retval=CTA_String_Set(hIFILE, IFILE);
   printf ("2) retval=%d  \n",retval);

   retval=CTA_XML_Read(hIFILE, &htree);
   printf ("3) retval=%d  \n",retval);

   printf("TREE\n====\n");
   CTA_Tree_Print(htree);
   printf("\n");
   
}





