! COSTA: Problem solving environment for data assimilation
! Copyright (C) 2006  Nils van Velzen
! 
! This library is free software; you can redistribute it and/or
! modify it under the terms of the GNU Lesser General Public
! License as published by the Free Software Foundation; either
! version 2.1 of the License, or (at your option) any later version.
! 
! This library is distributed in the hope that it will be useful,
! but WITHOUT ANY WARRANTY; without even the implied warranty of
! MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
! Lesser General Public License for more details.
! 
! You should have received a copy of the GNU Lesser General Public
! License along with this library; if not, write to the Free Software
! Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


program test_performance
implicit none
include 'cta_f77.inc'
integer, parameter ::NSUBS=50
integer, parameter ::NTIMES=1
integer, parameter ::NUMSTATES=100
integer, parameter ::nvec=2000
integer retval, istate, itime, isub
integer hvec
real(kind=8) ::tstart, tstop
integer, dimension(NUMSTATES) ::hstates
integer, dimension(NSUBS) ::hsub


call cta_core_initialise(retval)

do itime=1,NTIMES
   call cpu_time(tstart)
   do istate=1,NUMSTATES
      call cta_treevector_create('some name','my_tag',hstates(istate),retval)
      if (retval/=CTA_OK) stop 'ERROR'
      do isub=1,NSUBS
         call cta_treevector_create('some name','my_tag',hsub(isub),retval)
         if (retval/=CTA_OK) stop 'ERROR'
         call cta_vector_create(CTA_DEFAULT_VECTOR,nvec,CTA_DOUBLE,CTA_NULL,hvec,retval)
         if (retval/=CTA_OK) stop 'ERROR'
         call cta_treevector_setvec(hsub(isub),hvec,retval)
         if (retval/=CTA_OK) stop 'ERROR'
      enddo 
      call cta_treevector_conc(hstates(istate),hsub,NSUBS,retval)
      !call cta_treevector_setconstant(hstates(istate),1.0d0, CTA_DOUBLE, retval)
   enddo
   call cpu_time(tstop)
   print *,'Total time creating states is', (tstop-tstart)/dble(NUMSTATES)

   call cpu_time(tstart)
   do istate=1,NUMSTATES-1
      call cta_treevector_axpy(hstates(istate),2.0d0, hstates(istate+1), retval)
      if (retval/=CTA_OK) stop 'ERROR'
   enddo
   call cpu_time(tstop)
   print *,'Total time axpy is', (tstop-tstart)/dble(NUMSTATES-1)

   do istate=1,NUMSTATES
      call cta_treevector_free(hstates(istate),CTA_TRUE,retval)
   enddo
   call cpu_time(tstop)
   print *,'Total time free is', (tstop-tstart)/dble(NUMSTATES)
enddo


   call cta_core_finalise(retval)

end program test_performance


