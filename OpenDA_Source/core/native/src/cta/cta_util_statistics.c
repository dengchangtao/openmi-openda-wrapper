/*
$URL: https://repos.deltares.nl/repos/openda/openda_1/public/trunk/core/native/src/cta/cta_util_statistics.c $
$Revision: 1400 $, $Date: 2010-03-18 16:03:08 +0100 (do, 18 mrt 2010) $

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

#include <math.h>
#include "f_cta_utils.h"
#include "cta_datatypes.h"
#include "cta_errors.h"
#include "cta_util_statistics.h"

#define CTA_RAND_U_F77  F77_CALL(cta_rand_u,CTA_RAND_U)
#define CTA_RAND_N_F77  F77_CALL(cta_rand_n,CTA_RAND_N)


int CTA_rand_u(double *x)
// Calculate a realization from a uniform [0 1] distribution
{
   *x=(double) (rand()/(double)(RAND_MAX));
   return CTA_OK;
}


int CTA_rand_n(double *x)
// Calculate a realization from a standard normal distribution
{
//    use the box-muller scheme, which calculates two standard
//    normal random numbers from two standard uniform random
//    numbers.

   // Remember the extra value in variable value2;
   //    remember whether an extra value is available in variable hebnog
   static BOOL hebnog=FALSE;
   static double value2;

   if (hebnog)
   {
   // extra value still available: return it
      hebnog = FALSE;
      *x = value2;
   }
   else
   {
   // no extra value available: calculate 2 normal rando:m numbers
   //   and return only one
      double r1=(double) (rand()+1.0)/(double) (RAND_MAX+1.0);
      double r2=(double) (rand()+1.0)/(double) (RAND_MAX+1.0);
      double hlp = sqrt(-2*log(r1));

      *x     = hlp * cos(2.0*M_PI*r2);
      value2 = hlp * sin(2.0*M_PI*r2);
      hebnog = TRUE;
   }
   return CTA_OK;
}

/* Interfacing with Fortran */

CTAEXPORT void CTA_RAND_U_F77(double *x, int *ierr){
   *ierr=CTA_rand_u(x);
}

CTAEXPORT void CTA_RAND_N_F77(double *x, int *ierr){
   *ierr=CTA_rand_n(x);
}



