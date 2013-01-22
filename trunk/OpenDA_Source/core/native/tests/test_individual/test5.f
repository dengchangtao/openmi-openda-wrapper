c COSTA: Problem solving environment for data assimilation
c Copyright (C) 2006  Nils van Velzen
c 
c This library is free software; you can redistribute it and/or
c modify it under the terms of the GNU Lesser General Public
c License as published by the Free Software Foundation; either
c version 2.1 of the License, or (at your option) any later version.
c 
c This library is distributed in the hope that it will be useful,
c but WITHOUT ANY WARRANTY; without even the implied warranty of
c MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
c Lesser General Public License for more details.
c 
c You should have received a copy of the GNU Lesser General Public
c License along with this library; if not, write to the Free Software
c Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

      program test5
      implicit none
      include 'cta_f77.inc'

      integer ierr,i,n
      parameter(n=3)
      double precision vals1(n), vals2(n), vals3(n)
      integer userdata
      double precision alpha,dotprod,norm2
      integer iloc
      integer retval
      integer hveccl
      integer hvector1,hvector2,hvector3
      integer nsize

      print *,' '
      print *,'---------------------------------------------'
      print *,'Unit tests for COSTA: Test5'
      print *,'Test vector implementation'
      print *,'Fortran version of Test4 '
      print *,'---------------------------------------------'
c     Initialise default installation of vector object
      call cta_core_initialise(retval)

c     Create a vector
      do i=1,n
         vals1(i)=dble(i-1)
         vals2(i)=dble((i-1)*(i-1))
      enddo
      print *, 'x=[',vals1(1),vals1(2),vals1(3),']'
      print *, 'y=[',(vals2(i),i=1,n),']'
      
      call cta_vector_create(CTA_DEFAULT_VECTOR, n, CTA_DOUBLE,
     +                       userdata,hvector1,retval)
      call cta_vector_setvals(hvector1,vals1,n,CTA_DOUBLE,retval)
      call cta_vector_create(CTA_DEFAULT_VECTOR, n, CTA_DOUBLE,
     +                        userdata,hvector2,retval)
      call cta_vector_setvals(hvector2,vals2,n,CTA_DOUBLE,retval)

c     SCAL
      print *,'SCAL: x=2.0*x'
      alpha=2.0;
      call cta_vector_scal(hvector1,alpha,retval)

      call cta_vector_getvals(hvector1,vals1,n,CTA_DOUBLE, retval)
      print *, 'x=[',(vals1(i),i=1,n),']'

c     Copy
      print *,'COPY: y=x'
      call cta_vector_copy(hvector1,hvector2,retval)

      call cta_vector_getvals(hvector2,vals2,n,CTA_DOUBLE, retval)
      print *, 'y=[',(vals2(i),i=1,n),']'

c     AXPY

      print *,'AXPY: y=x+0.5*y'
      alpha=0.5;
      call cta_vector_axpy(hvector2,alpha,hvector1,retval)
      call cta_vector_getvals(hvector2,vals2,n,CTA_DOUBLE, retval)
      print *, 'y=[',(vals2(i),i=1,n),']'

c     DOT
      print *, 'DOT: (x,y)'
      call cta_vector_dot(hvector1,hvector2,dotprod,retval)
      print *,'(x,y)=',dotprod

c     NORM2
      print *,'NORM2: |x|'
      call CTA_Vector_Nrm2(hvector1,norm2, retval)
      print *, '|x|=',norm2

      print *,'ALOC: x'
      call CTA_Vector_Amax(hvector1,iloc,retval)
      print *,'loc of max x =',iloc


      print *,'DUPLICATE: z from x'
      call cta_vector_duplicate(hvector1,hvector3,retval)

      print *, 'GET SIZE z\n'
      call  cta_vector_getsize(hvector3,nsize,retval)
      print *, 'Size of z is ',nsize
      print *, 'COPY: z=x'
      call CTA_Vector_Copy(hvector1,hvector3,retval)

      call CTA_Vector_GetVals(hvector3,vals3,n,CTA_DOUBLE, retval);
      print *, 'z=[',(vals3(i),i=1,n),']'
      


      print *,'---------------------------------------------'
      print *,'End test';
      print *,'---------------------------------------------'
      print *, ' '
      call exit(0)
      end program

