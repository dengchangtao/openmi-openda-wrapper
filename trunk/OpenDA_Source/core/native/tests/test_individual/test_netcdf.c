/*
COSTA: Problem solving environment for data assimilation
Copyright (C) 2009  Nils van Velzen

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

// simple test to test the netcdf output of the tree-vectors
int main(int argc, char* argv[]){

int retval;
double dval;
CTA_TreeVector treeVec, treeVec1, treeVec2, treeVec3, treeVec4, treeVec5;
CTA_Vector vec;
CTA_Handle hdum;
CTA_TreeVector subVecs[3];
CTA_Metainfo meta1, meta2, meta3;
CTA_File hFile;
CTA_String path;
int i;

CTA_Core_Initialise();


/* Create and set Meta information */
retval=CTA_TreeVector_Create("sep","sep",&treeVec1);
retval=CTA_Vector_Create(CTA_DEFAULT_VECTOR,12,CTA_DOUBLE,hdum,&vec);
dval=1.0;
retval=CTA_Vector_SetConstant(vec,&dval,CTA_DOUBLE);
for (i=0; i<12; i++){
   dval=sin((double) i);
   retval=CTA_Vector_SetVal(vec,i+1, &dval,CTA_DOUBLE);
}


retval=CTA_TreeVector_SetVec(treeVec1,vec);

retval=CTA_TreeVector_Create("conc","conc",&treeVec2);
retval=CTA_TreeVector_SetVec(treeVec2,vec);

retval=CTA_TreeVector_Create("u","u",&treeVec3);
retval=CTA_TreeVector_SetVec(treeVec3,vec);

retval=CTA_TreeVector_Create("v","v",&treeVec4);
retval=CTA_TreeVector_SetVec(treeVec4,vec);

retval=CTA_TreeVector_Create("vel","vel",&treeVec5);
subVecs[0]=treeVec3;
subVecs[1]=treeVec4;
retval=CTA_TreeVector_Conc(treeVec5,subVecs,2);
printf("retval=%d\n",retval);

retval=CTA_TreeVector_Create("all","all",&treeVec);
printf("retval=%d\n",retval);
subVecs[0]=treeVec5;
subVecs[1]=treeVec1;
subVecs[2]=treeVec2;
retval=CTA_TreeVector_Conc(treeVec,subVecs,3);
printf("retval=%d\n",retval);

/* Create and set Meta information */
CTA_Metainfo_Create( &meta1);
CTA_Metainfo_setRegGrid(meta1, "mygrid", 4, 3, 0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0);
CTA_TreeVector_SetMetainfo(treeVec1, meta1);

CTA_Metainfo_Create( &meta2);
CTA_Metainfo_setRegGrid(meta2, "mygrid", 4, 3, 0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0);
CTA_TreeVector_SetMetainfo(treeVec2, meta2);

CTA_Metainfo_Create( &meta3);
CTA_Metainfo_setRegGrid(meta3, "mygrid", 4, 3, 0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0);
CTA_TreeVector_SetMetainfo(treeVec3, meta3);

// this is an internal function not a users function        
//
CTA_File_Create(&hFile);
CTA_String_Create(&path);
CTA_String_Set(path,"treevec.nc");
CTA_File_Open(hFile, path,CTA_NULL);
CTA_String_Free(&path);

CTA_TreeVector_Export(treeVec,hFile);
CTA_TreeVector_Export(treeVec,hFile);
CTA_File_Free(&hFile);

}
