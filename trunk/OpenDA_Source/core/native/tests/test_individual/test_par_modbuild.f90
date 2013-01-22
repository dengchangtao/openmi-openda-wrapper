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

program main
implicit none
include 'cta_f77.inc'

integer, parameter           ::MAXMODEL=10
integer, dimension(MAXMODEL) ::hmodel
integer                      ::hfnam
integer                      ::imodel
integer                      ::hmodinp
integer                      ::htree
integer                      ::time
integer                      ::hpack
integer                      ::state
integer                      ::ierr

character(len=*), parameter :: IFILE = 'test_par_modbuild.xml'
!
!     initialise COSTA environment
!
call cta_core_initialise(ierr)

call oscill_model_createfunc()
call cta_modbuild_par_createclass(CTA_MODBUILD_PAR)

! Read XML input file
call cta_string_create(hfnam,ierr)
call cta_string_set(hfnam,IFILE,ierr)
call cta_xml_read(hfnam,htree,ierr)
call cta_string_free(hfnam,ierr)

! Get model input
call cta_tree_gethandlestr(htree,'/costa/model',hmodinp, ierr)
if (ierr.ne.CTA_OK) stop 'cannot get model input from xml'

!Create MAXMODEL model instances
do imodel=1,MAXMODEL
   call cta_model_create(CTA_MODBUILD_PAR,hmodinp,hmodel(imodel),ierr)
   if (ierr/=CTA_OK) stop 'error creating model'
enddo

!Propagate all modes forward in time
print *,'compute model 1 t=0..10'
call cta_time_create(time,ierr)
call cta_time_setspan(time, 0.0d0, 10.0d0, ierr)
call cta_model_compute(hmodel(1), time, ierr)



call cta_pack_create(0, hpack, ierr)
do imodel=2,MAXMODEL
   call cta_model_export(hmodel(1), hpack, ierr)
   call cta_model_import(hmodel(imodel),hpack, ierr)
   if (ierr/=CTA_OK) stop 'error creating model'
   
enddo
call cta_pack_free(hpack, ierr)

print *,'compute all models t=10..20'
call cta_time_setspan(time, 10.0d0, 20.0d0, ierr)
do imodel=2,MAXMODEL
   call cta_model_compute(hmodel(imodel), time, ierr)
   if (ierr/=CTA_OK) stop 'error model compute'
enddo

print *,'model axpy for all models'
do imodel=2,MAXMODEL
   call cta_model_axpystate(hmodel(imodel), -1.0d0, hmodel(1), ierr)
   if (ierr/=CTA_OK) stop 'error model compute'
enddo

print *,'State of model '
print *,'Note first state should be non-zero others must be zero'
state=CTA_NULL
do imodel=1,MAXMODEL
   call cta_model_getstate(hmodel(imodel), state, ierr)
   if (ierr/=CTA_OK) stop 'error model compute'
   call cta_treevector_export(state, CTA_FILE_STDOUT, ierr)
enddo



call cta_treevector_free(state, CTA_TRUE, ierr)
call cta_tree_free(htree,ierr)

call cta_modbuild_par_finalize
end program main
