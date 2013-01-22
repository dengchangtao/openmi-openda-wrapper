module cta_f90_util_sort

  implicit none

  public

  !  \brief Sort an integer array using the Quicksort algorithm.
  !   An additional interger array is permutated in way as the unsorted array
  ! 
  !   \param id      (input) CHARACTER*1
  !   -        = 'I': sort D in increasing order;
  !   -        = 'D': sort D in decreasing order.
  ! 
  !   \param n       (input) INTEGER
  !           The length of the array D.
  ! 
  !   \param d       (input/output) array, dimension (N)
  !           On entry, the array to be sorted.
  !           On exit, D has been sorted into increasing order
  !  -        (D(1) <= ... <= D(N) ) or into decreasing order
  !  -        (D(1) >= ... >= D(N) ), depending on ID.
  ! 
  !  \param dextra (input/output) array, dimension (N)
  ! 
  !   \param info    (output) INTEGER
  !   -        = 0:  successful exit
  !   -        < 0:  if INFO = -i, the i-th argument had an illegal value
  ! 
  !  \return error status: CTA_OK if successful
  !
  interface CTA_F90_IQSort2
    subroutine CTA_IQSort2( id, n, d, dextra, info )
      character(len=*)              , intent(in   )     ::  id(*)
      integer                       , intent(in   )     ::  n(*)
      integer                       , intent(inout)     ::  d
      integer                       , intent(inout)     ::  dextra
      integer                       , intent(out  )     ::  info
    end subroutine CTA_IQSort2
  end interface

  !  \brief Sort an double array using the Quicksort algorithm.
  !   An additional interger array is permutated in way as the unsorted array
  ! 
  !   \param id      (input) CHARACTER*1
  !   -        = 'I': sort D in increasing order;
  !   -        = 'D': sort D in decreasing order.
  ! 
  !   \param n       (input) INTEGER
  !           The length of the array D.
  ! 
  !   \param d       (input/output) array, dimension (N)
  !           On entry, the array to be sorted.
  !           On exit, D has been sorted into increasing order
  !  -        (D(1) <= ... <= D(N) ) or into decreasing order
  !  -        (D(1) >= ... >= D(N) ), depending on ID.
  ! 
  !  \param dextra (input/output) array, dimension (N)
  ! 
  !   \param info    (output) INTEGER
  !   -        = 0:  successful exit
  !   -        < 0:  if INFO = -i, the i-th argument had an illegal value
  ! 
  !  \return error status: CTA_OK if successful
  !
  interface CTA_F90_DQSort2
    subroutine CTA_DQSort2( id, n, d, dextra, info )
      character(len=*)              , intent(in   )     ::  id(*)
      integer                       , intent(in   )     ::  n(*)
      real(8)                       , intent(inout)     ::  d
      integer                       , intent(inout)     ::  dextra
      integer                       , intent(out  )     ::  info
    end subroutine CTA_DQSort2
  end interface


end module cta_f90_util_sort

