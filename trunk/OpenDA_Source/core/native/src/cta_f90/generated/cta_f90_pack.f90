module cta_f90_pack

  implicit none

  public

  !  \brief Create a pack instance.
  ! 
  !  \param initsize I  the initial size >=0 of the buffer
  !  \param hpack    O  receives handle of new pack object
  ! 
  !  \param status O error status: CTA_OK if successful
  !
  interface CTA_F90_Pack_Create
    subroutine CTA_Pack_Create( initsize, hpack, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer                       , intent(in   )     ::  initsize
      integer(CTA_HANDLE_IKIND)     , intent(out  )     ::  hpack
      integer                       , intent(out  )     ::  status
    end subroutine CTA_Pack_Create
  end interface

  !  \brief Free a pack instance.
  ! 
  !  \param hpack   IO handle of pack object, replaced by CTA_NULL on return
  ! 
  !  \param status O error status: CTA_OK if successful
  !
  interface CTA_F90_Pack_Free
    subroutine CTA_Pack_Free( hpack, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(inout)     ::  hpack
      integer                       , intent(out  )     ::  status
    end subroutine CTA_Pack_Free
  end interface

  !  \brief Add data to pack object.
  ! 
  !  \param hpack    IO handle of pack object
  !  \param data     I  data that must be packed
  !  \param lendat   I  size of the data to be packed (chars)
  ! 
  !  \param status O error status: CTA_OK if successful
  !
  interface CTA_F90_Pack_Add
    module procedure CTA_Pack_Add_integer
    module procedure CTA_Pack_Add_real4
    module procedure CTA_Pack_Add_real8
  end interface

  !  \brief Unpack (get) data from pack object.
  ! 
  !  \param hpack    IO handle of pack object
  !  \param data     O  buffer that receives data that is unpacked from pack-buffer (buffer length must be >= lendat)
  !  \param lendat   I  size of the data to be unpacked (chars)
  ! 
  !  \param status O error status: CTA_OK if successful
  !
  interface CTA_F90_Pack_Get
    module procedure CTA_Pack_Get_integer
    module procedure CTA_Pack_Get_real4
    module procedure CTA_Pack_Get_real8
  end interface

  !  \brief Get length of packed data in pack-buffer.
  ! 
  !  \param hpack    I  handle of pack object
  ! 
  !  \param status O length packed data
  !
  interface CTA_F90_Pack_GetLen
    subroutine CTA_Pack_GetLen( hpack, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(in   )     ::  hpack
      integer                       , intent(out  )     ::  status
    end subroutine CTA_Pack_GetLen
  end interface

  !  \brief Only update administration for added elements
  ! 
  !  This function can be used to update the administration after the
  !  pack-buffer is filled externally (e.g. using an mpi_recv)
  ! 
  !  \param hpack    I  handle of pack object
  !  \param lendat   I  number of added elements (chars)
  ! 
  !  \param status O length packed data
  !
  interface CTA_F90_Pack_AddCnt
    subroutine CTA_Pack_AddCnt( hpack, lendat, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(in   )     ::  hpack
      integer                       , intent(in   )     ::  lendat
      integer                       , intent(out  )     ::  status
    end subroutine CTA_Pack_AddCnt
  end interface

  !  \brief Get the internal pack and unpack pointers
  ! 
  !  This function can be used to save to pointers and
  !  reset the state of the pack component after unpacking or adding
  !  some data
  ! 
  !  \param hpack    I  handle of pack object
  !  \param ip1      O  unpack pointer
  !  \param ip2      O  pack pointer
  ! 
  !  \param status O length packed data
  !
  interface CTA_F90_Pack_GetIndx
    subroutine CTA_Pack_GetIndx( hpack, ip1, ip2, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(in   )     ::  hpack
      integer                       , intent(out  )     ::  ip1
      integer                       , intent(out  )     ::  ip2
      integer                       , intent(out  )     ::  status
    end subroutine CTA_Pack_GetIndx
  end interface

  !  \brief Set the internal pack and unpack pointers
  ! 
  !  This function can be used to restore the pointers and
  !  reset the state of the pack component after unpacking or adding
  !  some data
  ! 
  !  \param hpack    I  handle of pack object
  !  \param ip1      I  unpack pointer. In order to reset all unpackin
  !                     set to CTA_PACK_RESET
  !  \param ip2      I  pack pointer. In order to reset the whole pack object
  !                     set to CTA_PACK_RESET
  ! 
  !  \param status O length packed data
  !
  interface CTA_F90_Pack_SetIndx
    subroutine CTA_Pack_SetIndx( hpack, ip1, ip2, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(in   )     ::  hpack
      integer                       , intent(in   )     ::  ip1
      integer                       , intent(in   )     ::  ip2
      integer                       , intent(out  )     ::  status
    end subroutine CTA_Pack_SetIndx
  end interface


contains

    subroutine CTA_Pack_Add_integer( hpack, data, lendat, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(inout)     ::  hpack
      integer                       , intent(in   )     ::  data(*)
      integer                       , intent(in   )     ::  lendat
      integer                       , intent(out  )     ::  status
      call CTA_Pack_Add( hpack, data, lendat, status )
    end subroutine CTA_Pack_Add_integer

    subroutine CTA_Pack_Add_real4( hpack, data, lendat, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(inout)     ::  hpack
      real(4)                       , intent(in   )     ::  data(*)
      integer                       , intent(in   )     ::  lendat
      integer                       , intent(out  )     ::  status
      call CTA_Pack_Add( hpack, data, lendat, status )
    end subroutine CTA_Pack_Add_real4

    subroutine CTA_Pack_Add_real8( hpack, data, lendat, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(inout)     ::  hpack
      real(8)                       , intent(in   )     ::  data(*)
      integer                       , intent(in   )     ::  lendat
      integer                       , intent(out  )     ::  status
      call CTA_Pack_Add( hpack, data, lendat, status )
    end subroutine CTA_Pack_Add_real8

    subroutine CTA_Pack_Get_integer( hpack, data, lendat, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(inout)     ::  hpack
      integer                       , intent(out  )     ::  data(*)
      integer                       , intent(in   )     ::  lendat
      integer                       , intent(out  )     ::  status
      call CTA_Pack_Get( hpack, data, lendat, status )
    end subroutine CTA_Pack_Get_integer

    subroutine CTA_Pack_Get_real4( hpack, data, lendat, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(inout)     ::  hpack
      real(4)                       , intent(out  )     ::  data(*)
      integer                       , intent(in   )     ::  lendat
      integer                       , intent(out  )     ::  status
      call CTA_Pack_Get( hpack, data, lendat, status )
    end subroutine CTA_Pack_Get_real4

    subroutine CTA_Pack_Get_real8( hpack, data, lendat, status )
      use CTA_F90_Parameters, only : CTA_HANDLE_IKIND
      integer(CTA_HANDLE_IKIND)     , intent(inout)     ::  hpack
      real(8)                       , intent(out  )     ::  data(*)
      integer                       , intent(in   )     ::  lendat
      integer                       , intent(out  )     ::  status
      call CTA_Pack_Get( hpack, data, lendat, status )
    end subroutine CTA_Pack_Get_real8

end module cta_f90_pack

