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

void callfunc(CTA_Func hfunc, int arg1, int arg2){
   CTA_Function *function;
   CTA_Func_GetFunc(hfunc,&function);
   function(&arg1,&arg2);
};

void call_func_(int *hfunc, int *arg1, int *arg2){
  callfunc((CTA_Func) *hfunc,(int) *arg1,(int) *arg2); 
};




