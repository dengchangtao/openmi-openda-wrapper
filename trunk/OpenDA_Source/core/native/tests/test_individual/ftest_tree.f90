! COSTA: Problem solving environment for data assimilation
! Copyright (C) 2007  Nils van Velzen
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
program ftest_tree
implicit none
include 'cta_f90.inc'
integer retval, ierr
integer hfnam, hmap
integer hinput, hstations, hstation, item
integer nStations, i, nItems, j
integer datatype

character(len=256) ::mapnam
character(len=*), parameter :: IFILE = 'stations.xml'
!
!     initialise COSTA environment and read input file
!
call cta_core_initialise(retval)
call cta_string_create(hfnam,retval)
call cta_string_set(hfnam,IFILE,retval)
call cta_xml_read(hfnam,hinput,retval)
call cta_string_free(hfnam,retval)

! Get section stations from tree (is root)
CALL CTA_Tree_GetHandleStr(hinput, 'stations', hstations, ierr)
IF (ierr/=CTA_OK) stop 'Error 1'

! Count number of stations in the given hstations
  CALL CTA_Tree_CountItems(hstations, nStations, ierr)
  if (ierr/=CTA_OK) stop 'Error 2'
  print *,'nStations=',nStations

  DO i=1,nStations
    print *, '-------- Handling item',i,'----------'
    ! Get the -th item from stations (<station>)
    CALL CTA_Tree_GetItem(hstations, i, hstation, ierr)
    if (ierr/=CTA_OK) stop 'Error 3'
    CALL CTA_Tree_Print(hstation, ierr)
    if (ierr/=CTA_OK)  stop 'Error 4'

    ! Get name of mapping
    CALL CTA_Tree_GetHandleStr(hstation, 'mappingname', hmap, ierr)
    if (ierr/=CTA_OK)  stop 'Error 5'
    call cta_string_get(hmap,mapnam,ierr)
    if (ierr/=CTA_OK)  stop 'Error 6'
    print *,'mapname=',trim(mapnam)
    
    ! Count number of elements in this level of station tree
    CALL CTA_Tree_CountItems(hstation, nItems, ierr)
    if (ierr/=CTA_OK)  stop 'Error 7'
    print *,'this stations contains',nItems,' items'
   
    DO j=1,nItems
      CALL CTA_Tree_GetItem(hstation, j, item, ierr)
      if (ierr/=CTA_OK)  stop 'Error 8'
      print *,'Handle of item=',item
      print *,'=========================='
      CALL CTA_Tree_Print(item, ierr)
      print *,'=========================='
      if (j==1) then
         CALL CTA_Tree_GetHandleStr(item, '/', hmap, ierr)
         call cta_string_get(hmap,mapnam,ierr)
         if (ierr/=CTA_OK)  stop 'Error 6'
         print *,'mapname 2=',trim(mapnam)
      endif
      
      CALL CTA_Handle_GetDatatype(item, datatype, ierr)
      if (ierr/=CTA_OK)  stop 'Error 9'
      IF (datatype==CTA_STRING)THEN
        CALL CTA_String_Get(item, mapnam, ierr);
        if (ierr/=CTA_OK)  stop 'Error 10'
        WRITE (*,*) mapnam
      ELSE 
         print *,'Item',j,'Is not a string'
      ENDIF
    ENDDO
  ENDDO
! 

end program ftest_tree
