!! MOD_V2.0
!! Copyright (c) 2012 OpenDA Association
!! All rights reserved.
!!
!! This file is part of OpenDA.
!!
!! OpenDA is free software: you can redistribute it and/or modify
!! it under the terms of the GNU Lesser General Public License as
!! published by the Free Software Foundation, either version 3 of
!! the License, or (at your option) any later version.
!!
!! OpenDA is distributed in the hope that it will be useful,
!! but WITHOUT ANY WARRANTY; without even the implied warranty of
!! MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
!! GNU Lesser General Public License for more details.
!!
!! You should have received a copy of the GNU Lesser General Public License
!! along with OpenDA.  If not, see <http://www.gnu.org/licenses/>.
 
!! @author Werner Kramer       VORtech BV  
!!                             P.O. Box 260
!!                             2600 AG Delft
!!                             The Netherlands
!!                             www.vortech.nl


! ----------------------------------------------------------------------------
! This module provides the functions which are called by EfdcDLL.java.
! 
! It contains the methods that are required for the IModelInstance and 
! IExchangeItem interfaces. For more information see  
! org.openda.model_efdc_dll.IEfdcFortranNativeDLL
! 
! Each model instance has it own memory storing the state and time series.
! If an instance becomes active this state and time series is copied to EFDC
! memory. When a model integration is finished the calculated state is again
! stored in model instance memory. 
! 
! Exchange Items communicate with the model instance memory not directly with 
! EFDC memory. 
! ----------------------------------------------------------------------------
module m_openda_wrapper
 
  use model_state
  use model_aser_time_series
  use model_pser_time_series
  use model_qser_time_series
  use model_cser_time_series

  implicit none

  !private

  ! model directories and files
  character(len=256)  :: dm_model_parent_dir   ! parent directory for template model and all instances
  character(len=256)  :: dm_template_model_dir ! template model that will be cloned for each instances
  character(len=256), dimension(:), pointer :: model_instance_dirs => NULL() ! a directory for each instances
  integer, dimension(:), pointer :: dm_outfile_handle
  integer, parameter :: dm_general_log_handle = 100
  
  integer, parameter :: instances_realloc_size = 6    ! #instances to be added when the max #instances has been exceed

  ! actual model instances identification
  integer :: dm_max_dm_model_instance_count = 0  ! max #instances
  integer :: dm_model_instance_count = 0  ! actual #instance
  integer :: dm_model_instance_in_memory = 0 ! index of the instance currenty in memory

  logical, parameter :: debug = .false.
  integer, private, parameter :: debug_file_handle = 666

contains


  ! --------------------------------------------------------------------------
  ! Initialize the dll, change to model directory
  ! Initialize EFDC model
  ! Set active exchange items for CSER time series 
  ! --------------------------------------------------------------------------
  subroutine init(parent_directory, template_directory, pd_string_length, td_string_length)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_init_' :: init
#endif

    USE GLOBAL, only: TBEGIN, TCON, TIDALP, NTC, TIMEDAY, & 
         NDASER, NASERM, NDPSER, NPSERM, NDQSER, NQSERM,NDCSER, NCSERM, &
         NTOX, NSED, NSND

    ! arguments
    integer, intent(in) :: pd_string_length ! length of parent dir string
    integer, intent(in) :: td_string_length ! length of template dir string
    character(len=pd_string_length), intent(in)  :: parent_directory ! parent directory for model instances (full path)
    character(len=td_string_length), intent(in)  :: template_directory ! directory name (under model_parent_dir) containing the model that will be cloned for each instance
        
    !locals
    character(len=td_string_length + 20)         :: output_file_name
    integer :: i_number
    logical :: i_open
    
    ! body
    dm_model_parent_dir    = parent_directory
    dm_template_model_dir  = template_directory
    if (debug) write(100,*) trim(dm_model_parent_dir), trim(dm_template_model_dir) 
    call chdir(dm_template_model_dir)
    call model_init

    if (debug) write(100,*) "integer: ", kind(NTC), ", real: ", kind(TIDALP)    
    TIMEDAY = TBEGIN* TCON / 86400.d0

    ! store sizes of time series (the global ones are redetermined each time we do a restart)
    ndaser_max = NDASER
    naser_max = NASERM
    ndpser_max = NDPSER
    npser_max = NPSERM
    ndqser_max = NDQSER
    nqser_max = NQSERM
    ndcser_max = NDCSER
    ncser_max = NCSERM

    NC_wq_start =  4+NTOX+NSED+NSND
    call model_cser_set_items()
    
    output_file_name = trim(dm_template_model_dir) // '/model-output.txt'
    
    inquire(file = output_file_name, opened=i_open, number=i_number) 
    if (i_open .and. (i_number == dm_general_log_handle)) close(i_number)
    open(dm_general_log_handle, file=output_file_name, status = 'replace')
    write(dm_general_log_handle,'(A)') 'EFDC initialized'
    call flush(dm_general_log_handle)
    
  end subroutine init

  ! --------------------------------------------------------------------------
  ! If all instances are finished this subroutine is called to  
  ! deallocate pointers to storage of model instances 
  ! and reset some module variables.
  ! --------------------------------------------------------------------------=
  subroutine destroy()

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_destroy_' :: destroy
#endif
    integer :: i_number
    logical :: i_open

    deallocate(model_instance_dirs)
    deallocate(dm_outfile_handle)
    deallocate(state)
    deallocate(aser)
    deallocate(psert)
    deallocate(qsert)
    deallocate(csert)
    
    ! close open file handles
    inquire(file = "calheat.dia", opened=i_open, number=i_number) 
    if (i_open .and. (i_number == 77)) close(i_number)

    ! reset variables related to number of instance 
    dm_max_dm_model_instance_count = 0
    dm_model_instance_count = 0  ! actual #instance
    dm_model_instance_in_memory = 0 ! index of the instance currenty in memory

    write(dm_general_log_handle,'(A)') 'EFDC destroy()'
    close(dm_general_log_handle)
    
  end subroutine destroy

  ! --------------------------------------------------------------------------
  ! Create storage for a new model instance
  ! Get initial state and time series from EFDC memory 
  ! If no compute has been called on a other instance this corresponds with 
  ! the state that is specified by EFDC setting files in the model directory.
  ! Return identifier for new model instance  
  ! --------------------------------------------------------------------------
  function get_model_instance(instance_dir, id_string_length) result(ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_model_instance_' :: get_model_instance
#endif

    use global, only: NDASER, NASERM, NDPSER, NPSERM, NDQSER, NQSERM, KCM, NDCSER, NCSERM,&
        TBEGIN, TCON, NTC, TIDALP
    
    ! arguments
    integer, intent(in)           :: id_string_length            ! lenght of instance dir string
    character(len=id_string_length), intent(in)  :: instance_dir ! model instance directory
    
    ! return value
    integer                       :: ret_val            ! >0 : instanceID ; <0 : Error

    ! locals
    character(len=id_string_length + 20)            :: output_file_name
    integer :: instance
    integer :: i_number
    logical :: i_open

    ! body: create new model 
    dm_model_instance_count = dm_model_instance_count + 1
    instance = dm_model_instance_count
    
    call add_instance_storage()
    model_instance_dirs(instance) = instance_dir
    ! open output file for this instance (and check if the same file is open from a previous FEWS run) 
    dm_outfile_handle(instance) = 100 + instance
    output_file_name = trim(model_instance_dirs(instance)) // '/model-output.txt'
    inquire(file = output_file_name, opened=i_open, number=i_number)
    if (i_open) close(i_number)
    open(dm_outfile_handle(instance), file=output_file_name, status = 'replace') 
    
    ! add wrapper storage for instance
    call model_state_allocate(instance)
    call model_aser_allocate(instance, NDASER, NASERM) 
    call model_pser_allocate(instance, NDPSER, NPSERM)
    call model_qser_allocate(instance, NDQSER, KCM, NQSERM)
    call model_cser_allocate(instance, NDCSER, KCM, NCSERM)        

    if (debug) print*, 'allocated state vector for instance ', instance  

    ! Initialize data that may be have been adjusted in the last model instance,
    ! and therefore has to be (re)initialized when creating
    ret_val =  model_get_state(instance)
    if (debug) print*, 'got model state'
    if (ret_val == 0) ret_val = model_get_aser(instance)
    if (ret_val == 0) ret_val = model_get_daily_solar_intensity(instance, .true.)
    if (ret_val == 0) ret_val = model_get_pser(instance)
    if (ret_val == 0) ret_val = model_get_qser(instance)
    if (ret_val == 0) ret_val = model_get_cser(instance)

    ! store begin and end time for instance
    ! round to minutes as real precision is not accurate enough for seconds
    state(instance)%start_time = dble(nint(TBEGIN*TCON/60))/1440.d0
    state(instance)%end_time  = dble(nint((TBEGIN*TCON + TIDALP*NTC)/60))/1440.d0

    ! save the initial instance
    !ret_val = save_instance(dm_model_instance_count)
    if (ret_val == 0) dm_model_instance_in_memory = instance

    if (ret_val == 0) then
       ! return instance 'handle'
       ret_val = instance
    endif

    write(dm_outfile_handle(instance), '(A,I2,A,I2)' ) &
         'Initialize #', instance, &
         ' ret_val: ', ret_val
    call flush(dm_outfile_handle(instance))

  end function get_model_instance

  ! --------------------------------------------------------------------------
  ! Subroutine for saving the state that is currently in EFDC memory
  ! --------------------------------------------------------------------------
  function save_instance(instance) result(ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_save_instance_' :: save_instance
#endif


    ! return value
    integer                       :: ret_val     ! ret_val < 0: Error; ret_val == 0 success
    ! arguments
    integer         , intent(in)  :: instance    ! model instance identifier

    ! body: save the state

    ret_val = -1
    if (valid_model_instance(instance)) then

       if (debug) print*, 'getting model state for id', instance 
       ! copy model instance data from data currently in memory
       ret_val = model_get_state(instance)
       if (ret_val == 0) ret_val = model_get_daily_solar_intensity(instance, .false.)
       IF(DEBUG)CALL DEPPLT 
 
    endif

    write(dm_outfile_handle(instance), '(A,I4,A,I2)') 'save_instance(', &
         instance, '), retval: ', ret_val
    call flush(dm_outfile_handle(instance))

  end function save_instance


  ! --------------------------------------------------------------------------
  ! Subroutine for restoring the state and time series from model instance
  ! memory to EFDC memory.
  ! --------------------------------------------------------------------------
  function restore_instance(instance) result(ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_restore_instance_' :: restore_instance
#endif

    ! return value
    integer                       :: ret_val     ! ret_val < 0: Error; ret_val == 0 success
    ! arguments
    integer         , intent(in)  :: instance    ! model instance identifier

    ! body: save the state

    ret_val = -1
    ! copy model instance data from data currently in memory
    ret_val = model_set_state(instance)
    if (ret_val == 0) ret_val = model_set_aser(instance)
    if (ret_val == 0) ret_val = model_set_daily_solar_intensity(instance)
    if (ret_val == 0) ret_val = model_set_pser(instance)
    if (ret_val == 0) ret_val = model_set_qser(instance)
    if (ret_val == 0) ret_val = model_set_cser(instance)
    if (ret_val == 0) dm_model_instance_in_memory = instance
    if (debug) print*, 'current instance in memory',  dm_model_instance_in_memory

    write(dm_outfile_handle(instance), '(A,I2,A,I2)') 'restore_instance(', instance, '), retval: ', ret_val
    call flush(dm_outfile_handle(instance))

  end function restore_instance

  ! --------------------------------------------------------------------------
  ! Write the restart files for the currently active instance to the instance
  ! directory. 
  ! --------------------------------------------------------------------------
  function store_current_instance_restart_files() &
       result (ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_store_current_instance_restart_files_' :: store_current_instance_restart_files
#endif

    use global, only : ISTRAN, IWQRST, IWQBEN, ISMRST

    ! return value
    integer                              :: ret_val ! ret_val < 0: Error; ret_val == 0 success
    !locals
    character(len=255) :: cwd
    integer :: instance

    ret_val = -1

    instance = dm_model_instance_in_memory
    ! change directory
    call getcwd(cwd) 
    if (debug) print*, "working directory ", trim(cwd) 
    call chdir( model_instance_dirs(dm_model_instance_in_memory))
    if (debug) print*, "changing directory to ", model_instance_dirs(instance)  
    call RESTOUT(0)
    IF(ISTRAN(8).GE.1)THEN  
       IF(IWQRST.EQ.1) CALL WWQRST(0)
       IF(IWQBEN.EQ.1 .AND. ISMRST.EQ.1) CALL WSMRST(0)
    ENDIF
    ! return to old working directory
    if (debug) print*, "changing directory to ", trim(cwd)
    call chdir(cwd)
    ret_val = 0
    
    write(dm_outfile_handle(instance), '(A,I2,A,I2)') 'store_current_instance_restart_files(', instance, '), retval: ', ret_val
    write(dm_outfile_handle(instance), '(A)') trim(model_instance_dirs(instance))
    call flush(dm_outfile_handle(instance))

  end function store_current_instance_restart_files

  ! --------------------------------------------------------------------------
  ! Read the restart files from the model instance directory for the currently
  ! active instance to EFDC memory and save a copy in model instance memory.
  ! --------------------------------------------------------------------------
  function select_instance_from_restart_files(instance) &
       result (ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_select_instance_from_restart_files_' :: select_instance_from_restart_files
#endif

    use global, only : ISTRAN, IWQRST, IWQBEN, ISMRST, ISRESTI, TIMESEC, TIMEDAY, TBEGIN, IWQAGR, HP

    
    ! return value
    integer :: ret_val              ! ret_val < 0: Error; ret_val == 0 success

    ! argument
    integer, intent(in) :: instance ! model instance identifier

    !locals
    character(len=255) :: cwd
    character(len=80) :: TITLE

    ret_val = -1
    ! change directory
    if (valid_model_instance(instance)) then
       call getcwd(cwd)
       call chdir( model_instance_dirs(instance))
       
       ! zero all arrays
       call VARZEROReal
       call VARZEROInt

       call INPUT(TITLE)
       ! Act like this is a restart
       ISRESTI = 1
       call model_init_2    

       if (debug) print*, "select_instance_frome_restart_file ",  model_instance_dirs(instance)
       !read restart files directly into EFDC memory

       ! time book keeping
       TIMESEC = state(instance)%TIMESEC
       TIMEDAY = TIMESEC / 86400.0
       TBEGIN = state(instance)%TBEGIN
       
       call RESTIN1
       call model_init_3

       ! store restored state in instance storage
       ret_val =  model_get_state(instance)
       if (ret_val == 0) ret_val = model_get_daily_solar_intensity(instance, .true.)
       ! reset latest used forcings
       if (ret_val == 0)  ret_val = model_set_aser(instance)
       if (ret_val == 0)  ret_val = model_set_daily_solar_intensity(instance)
       if (ret_val == 0)  ret_val = model_set_pser(instance)
       if (ret_val == 0)  ret_val = model_set_qser(instance)
       if (ret_val == 0)  ret_val = model_set_cser(instance)
       
       dm_model_instance_in_memory = instance
       call chdir(cwd)
       if (debug) print*, "select_instance_frome_restart_file ",  dm_template_model_dir
       ret_val = 0
    end if
    
    write(dm_outfile_handle(instance), '(A,I2,A,I2)') 'select_instance_from_restart_files(', instance, '), retval: ', ret_val
    write(dm_outfile_handle(instance), '(A)') trim(model_instance_dirs(instance))
    call flush(dm_outfile_handle(instance))

end function select_instance_from_restart_files

!-----------------------------------------------------------------------------


  ! --------------------------------------------------------------------------
  ! Get the reference year as specified in EVENT_TOX2.INP in the model 
  ! instance directory
  ! --------------------------------------------------------------------------
  function get_reference_year(instance) result(ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_reference_year_' :: get_reference_year
#endif

    ! return value
    integer :: ret_val  ! reference_year on succes, -1 = error

    ! argument
    integer, intent(in) :: instance ! model instance directory

    integer :: YEAR, MONTH, DAY, HR, MN 
    character(len=255) :: cwd
    
    ret_val = -1
    if (instance > dm_model_instance_count .or. instance < 1) then
        print*, 'instance id out of range:', instance  
        call exit(-1)    
    end if
    
    if (instance > dm_model_instance_count .or. instance < 1) then
        print*, 'instance id out of range:', instance  
        call exit(-1)    
    end if
    
    call getcwd(cwd)
    call chdir( model_instance_dirs(instance))
    
    OPEN(11,FILE='EVENT_TOX2.INP',STATUS='OLD')  
    READ(11,*) YEAR,MONTH,DAY, HR, MN  ! MODEL START TIME    
    CLOSE(11)
    if (debug) print*, "Reference year =",  YEAR
    ret_val = YEAR
    
    call chdir(cwd)
    
    write(dm_outfile_handle(instance), '(A,I4,A)') 'get_reference_year(', ret_val, ')'
    call flush(dm_outfile_handle(instance))

  end function get_reference_year


  ! --------------------------------------------------------------------------
  ! Get the model instance start time. The start time is saved per model 
  ! instance when the instance is initialized (get_model_instance).
  ! The model start time is from EVENT_TOX2.INP in the instance directory.
  ! Time is in days since the first day at 00:00 of the reference year 
  ! --------------------------------------------------------------------------
  function get_start_time(instance, start_time) result(ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_start_time_' :: get_start_time
#endif

    use global, only : tbegin, tcon

    ! return value
    integer :: ret_val   ! if succes ret_val=0 

    ! arguments
    integer, intent(in) :: instance             ! model instance identifier
    double precision, intent(out) :: start_time ! start time of computation in days
    
    ! body
    ret_val = -1
    start_time =  state(instance)%start_time 
    ret_val = 0

    write(dm_outfile_handle(instance), '(A,F14.10,A)') 'get_start_time(', start_time, ')'
    call flush(dm_outfile_handle(instance))

  end function get_start_time

  ! --------------------------------------------------------------------------
  ! Get the model instance end time. The start time is saved per model 
  ! instance when the instance is initialized (get_model_instance)
  ! The model end time is from EVENT_TOX2.INP in the instance directory.
  ! Time is in days since the first day at 00:00 of the reference year  
  ! --------------------------------------------------------------------------
  function get_end_time(instance, end_time) result(ret_val)

    use global, only : nts, ntc, ntc1, tbegin, tcon, tidalp

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_end_time_' :: get_end_time
#endif

    ! return value
    integer :: ret_val ! if succes ret_val=0

    ! arguments
    integer :: instance
    double precision, intent(out)   :: end_time  ! end time of computation in days

    ! body
    end_time = dble(state(instance)%end_time)
    ret_val = 0

    write(dm_outfile_handle(instance), '(A,F14.10,A)') 'get_end_time(', end_time, ')'
    call flush(dm_outfile_handle(instance))

  end function get_end_time

  ! --------------------------------------------------------------------------
  ! Get the model instance time step in days 
  ! --------------------------------------------------------------------------
  function get_delta_t(delta_t) result(ret_val)

    use global, only: dt

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_delta_t_' :: get_delta_t
#endif

    ! return value
    integer :: ret_val ! if succes ret_val=0

    ! arguments
    double precision, intent(out)   :: delta_t  ! #delta t for computation, in MJD (i.e. in days)

    ! body
    delta_t =  dble(dt) / 86400.0d0 ! converted to days
    ret_val = 0

    write(dm_general_log_handle, '(A,F8.4,A)') 'get_delta_t(', delta_t, ')'
    call flush(dm_general_log_handle)

  end function get_delta_t

  ! --------------------------------------------------------------------------
  ! Get the model reference period.
  ! The reference period is used in OpenDA as the output period
  ! --------------------------------------------------------------------------
  function get_reference_period(reference_period) result(ret_val)

    use global, only: tidalp

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_reference_period_' :: get_reference_period
#endif

    ! return value
    integer :: ret_val ! if succes ret_val=0

    ! arguments
    real(kind = 8), intent(out)   :: reference_period  ! reference period in days

    ! body
    ret_val = -1
    reference_period =  dble(tidalp) / 86400.0d0 ! converted to days
    ret_val = 0

    write(dm_general_log_handle, '(A,F8.4,A)') 'get_reference_period(', reference_period, ')'
    call flush(dm_general_log_handle)

  end function get_reference_period

  ! --------------------------------------------------------------------------
  ! Get the current time for given model instance 
  ! Time is in days since the first day at 00:00 of the reference year  
  ! --------------------------------------------------------------------------
  function get_current_time(instance, current_time) result(ret_val)

    use global, only : timesec

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_current_time_' :: get_current_time
#endif

    ! return value
    integer :: ret_val ! if succes ret_val=0

    ! arguments
    integer         , intent(in)    :: instance      ! model instance
    double precision, intent(out)   :: current_time  ! current time in days 

    ! body
    current_time = dble(state(instance)%timesec) / 86400.0d0
    ret_val = 0

    write(dm_outfile_handle(instance), '(A,F8.4,A)') 'get_current_time(', current_time, ')'
    call flush(dm_outfile_handle(instance))

  end function get_current_time

  ! --------------------------------------------------------------------------
  ! Integrate the EFDC model for the instance that is currently in memory
  ! for the given time window.
  ! Times are in days since the first day at 00:00 of the reference year    
  ! --------------------------------------------------------------------------
  function compute(instance, from_time_stamp, to_time_stamp) result(ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_compute_' :: compute
#endif


    use global, only:timesec, tbegin, tcon, dt, timeday

    ! return value
    integer :: ret_val

    ! arguments
    integer         , intent(in) :: instance        ! model instance identifier
    double precision, intent(in) :: from_time_stamp ! time stamp to compute from
    double precision, intent(in) :: to_time_stamp   ! time stamp to compute to ( > from_time_stamp )

    ! locals
    real :: time_period
    character(len=255) :: cwd

    ret_val = -1
    
    write(dm_outfile_handle(instance),'(A,F9.4,A,F9.4,A)') 'compute(', from_time_stamp, ',', to_time_stamp, '): '
    call flush(dm_outfile_handle(instance))
    
    if (debug) print*, "time interval" , from_time_stamp, to_time_stamp, dt
    if (valid_model_instance(instance)) then
       call getcwd(cwd)
       if (debug) print*, "working directory for compute :", cwd  
       call chdir(model_instance_dirs(instance))
       !if (debug) print*, "changing directory to :", model_instance_dirs(instance)  
       state(instance)%tbegin = real(from_time_stamp *86400.0/tcon) ! begin time scaled with tcon (EFDC.INP)
       state(instance)%timesec = real(from_time_stamp * 86400.0)     ! time in seconds
       time_period = real(to_time_stamp-from_time_stamp) * 86400.0
       TBEGIN = state(instance)%tbegin
       TIMESEC = state(instance)%timesec
       TIMEDAY = TIMESEC/86400.0  
       if (debug) print*, "Integrating over", time_period, nint(time_period/dt)
       call model_make_step(time_period)
       state(instance)%timesec = TIMESEC
       call chdir(cwd)
       ret_val = 0
    end if
    write(dm_outfile_handle(instance),'(A,F9.4,A,F9.4,A,I2)') 'compute(', from_time_stamp, ',', to_time_stamp, ') ret_val:  ', ret_val
    call flush(dm_outfile_handle(instance))

  end function compute


  ! --------------------------------------------------------------------------
  ! If OpenDA is finished with an instance this function is called.
  ! Model instance storage for the given instance is deallocated.
  ! --------------------------------------------------------------------------
  function finish(instance) result(ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_finish_' :: finish
#endif


    ! return value
    integer :: ret_val              ! >=0 : Success; <0 : Error

    !arguments
    integer, intent(in) :: instance ! model instance identifier 

    ret_val = -1
    call model_state_deallocate(instance)
    call model_aser_deallocate(instance)
    call model_pser_deallocate(instance)
    call model_qser_deallocate(instance)
    call model_cser_deallocate(instance)

    write(dm_outfile_handle(instance), '(A)') 'finish()'
    close(dm_outfile_handle(instance))
    ret_val = 0

  end function finish

! ----------------------------------------------------------------------------
! Exchange item functions
! ----------------------------------------------------------------------------


  ! --------------------------------------------------------------------------
  ! Pass a reference to a double precission array with time values 
  ! for given exchange item and given location number 
  ! --------------------------------------------------------------------------
  function get_times_for_ei(instance, exchange_item_id, bc_index, values_count, times) result (ret_val)

    use global, only: TCASER, TCPSER, TCQSER, TCCSER

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_times_for_ei_' :: get_times_for_ei
#endif

    ! return value
    integer :: ret_val     ! >=0 : Success; <0 : Error

    !arguments
    integer, intent(in) :: instance            ! model instance
    integer, intent(in) :: exchange_item_id    ! exchange item identifier
    integer, intent(in) :: bc_index            ! location number of the times series (from EFDC.INP or WQ3D.INP) 
    integer, intent(in) :: values_count        ! length of time array
    double precision, dimension(values_count), &
         intent(out) :: times                  ! refernce to times array

    !local
    integer :: NC

    ret_val = -1
    select case(exchange_item_id)
    case (101:107) ! atmospheric 
       times = dble(aser(instance)%TASER(1:values_count,bc_index))/86400.0d0 * dble(TCASER(bc_index))
       ret_val = 0
    case (WaterLevel) ! waterlevel
       times = dble(psert(instance)%TPSER(1:values_count,bc_index))/86400.0d0 * dble(TCPSER(bc_index))
       ret_val = 0
    case (Discharge) ! discharge
       times = dble(qsert(instance)%TQSER(1:values_count,bc_index))/86400.0d0 * dble(TCQSER(bc_index))
       ret_val = 0
    case (WaterTemperature) ! water temperature
       NC = 2;
       times = dble(csert(instance)%TCSER(1:values_count,bc_index,NC))/86400.0d0 * dble(TCCSER(bc_index, NC))
       ret_val = 0
    case (501:519) ! water quality
       NC = NC_wq_start + exchange_item_id - 500;
       times = dble(csert(instance)%TCSER(1:values_count,bc_index,NC))/86400.0d0 * dble(TCCSER(bc_index, NC))
       ret_val = 0
    case default
       ret_val = -2
    end select
    
    if (ret_val < 0) then
       write(dm_outfile_handle(instance),'(A,I2,A,I4,A,I4)') 'Error in get_times_for_ei: ', ret_val, ' for ', exchange_item_id
    else
       write(dm_outfile_handle(instance),'(A,I4,A,F8.4,A,F8.4)') 'get_times_for_ei(', &
            exchange_item_id, '): ', times(1), ', ', times(values_count)
    endif
    call flush(dm_outfile_handle(instance))   

  end function get_times_for_ei

  ! --------------------------------------------------------------------------
  ! Set the time values for given exchange item and given location number 
  ! times are passed as a refrence to a double precission array.
  ! --------------------------------------------------------------------------
  function set_times_for_ei(instance, exchange_item_id, bc_index, values_count, times) result (ret_val)

  use global, only: TCASER, TCPSER, TCQSER, TCCSER
#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_set_times_for_ei_' :: set_times_for_ei
#endif

    ! return value
    integer :: ret_val

    !arguments
    integer, intent(in) :: instance            ! model instance identifier
    integer, intent(in) :: exchange_item_id    ! exchange item identifier
    integer, intent(in) :: bc_index            ! location number of the times series (from EFDC.INP or WQ3D.INP) 
    integer, intent(in) :: values_count        ! count of the number of time points
    double precision, dimension(values_count), &
         intent(in) :: times                   ! reference to times array

    ! locals
    integer :: NC
    double precision :: factor

    ret_val = 0 
    select case(exchange_item_id)
    case (101:109) ! atmospheric 
       if (values_count > aser(instance)%NDASER) then
          ret_val = enlarge_aser_time_series(instance, values_count, aser(instance)%NASER)
       end if
       if (ret_val==0) then
          if (debug) print*, "setting times", instance, exchange_item_id, values_count, times(1), times(values_count)
          factor =  86400.d0/dble(TCASER(bc_index))
          aser(instance)%TASER(:,bc_index) = 1.0e38
          aser(instance)%TASER(1:values_count,bc_index) = real( times * factor )
          aser(instance)%MASER(bc_index) = values_count
       end if
    case (WaterLevel)
       if (values_count > psert(instance)%NDPSER) then
          ret_val = enlarge_pser_time_series(instance, values_count, psert(instance)%NPSER)
       end if
       if (ret_val==0) then
          if (debug) print*, "setting times", allocated(psert(instance)%tpser)
          factor =  86400.d0/dble(TCPSER(bc_index))
          psert(instance)%TPSER(:,bc_index) =  1.0e38
          psert(instance)%TPSER(1:values_count,bc_index) = real(times * factor)
          psert(instance)%MPSER(bc_index) = values_count
       end if
    case (Discharge)
       if (values_count > qsert(instance)%NDQSER) then
          ret_val = enlarge_qser_time_series(instance, values_count, &
                                             qsert(instance)%KCM, qsert(instance)%NQSER)
       end if
       if (ret_val==0) then
          if (debug) print*, "setting times", allocated(qsert(instance)%tqser)
          factor =  86400.d0/dble(TCQSER(bc_index))
          qsert(instance)%TQSER(:,bc_index) =  1.0e38
          qsert(instance)%TQSER(1:values_count,bc_index) = real(times * factor )
          qsert(instance)%MQSER(bc_index) = values_count
       end if
    case (WaterTemperature) 
       if (values_count > csert(instance)%NDCSER) then
          ret_val = enlarge_cser_time_series(instance, values_count, &
                                             csert(instance)%KCM, csert(instance)%NCSERM)
       end if
       if (ret_val==0) then
          NC = 2;
          if (debug) print*, "setting times", allocated(csert(instance)%tcser)
          factor =  86400.d0/dble(TCCSER(bc_index, NC))
          csert(instance)%TCSER(:,bc_index,NC) =1.0e38
          csert(instance)%TCSER(1:values_count,bc_index,NC) = real(times * factor)
          csert(instance)%MCSER(bc_index,NC) = values_count
       end if
    case (501:519) ! Water Quality 
       if (values_count > csert(instance)%NDCSER) then
          ret_val = enlarge_cser_time_series(instance, values_count, &
                                             csert(instance)%KCM, csert(instance)%NCSERM)
       end if
       if (ret_val==0) then
          NC = NC_wq_start + exchange_item_id - 500;
          if (debug) print*, "setting times", allocated(csert(instance)%tcser)
          factor =  86400.d0/dble(TCCSER(bc_index, NC))
          csert(instance)%TCSER(:,bc_index,NC) =  1.0e38
          csert(instance)%TCSER(1:values_count,bc_index,NC) = real(times * factor)
          csert(instance)%MCSER(bc_index,NC) = values_count
       end if
    case default
       ret_val = -1
    end select
    
    if (ret_val < 0) then
       write(dm_outfile_handle(instance),'(A,I2,A,I4)') 'Error in set_times_for_ei: ', ret_val, ' for ', exchange_item_id
    else
       write(dm_outfile_handle(instance),'(A,I4,A,F8.4,A,F8.4,A,F8.4)') 'set_times_for_ei(', &
            exchange_item_id, '): ', times(1), ', ', times(values_count), ', ', factor
       write(dm_outfile_handle(instance),*) times(1:values_count)
    endif
    call flush(dm_outfile_handle(instance)) 

  end function set_times_for_ei

  ! --------------------------------------------------------------------------
  ! Function returns the number of grid points for given exchange item 
  ! --------------------------------------------------------------------------
  function get_values_count(instance, exchange_item_id) result(ret_val)

    use global, only: LA 

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_values_count_' :: get_values_count
#endif

    ! return value
    integer :: ret_val    ! number of values for a certain exchange item

    ! arguments
    integer, intent(in) :: instance          ! model instance identifier
    integer, intent(in) :: exchange_item_id  ! exchange item identifier
 
    ! body
    ret_val = 0
    if (debug) print*, 'get_values_count', instance, exchange_item_id
    select case(exchange_item_id)
    case (Grid_WaterLevel)
       ret_val = LA-1
    case (Grid_Discharge)
       ret_val = LA-1
    case (Grid_WaterTemperature)
       ret_val = LA-1
    case (1501:1519)  ! water quality grid items
       ret_val = LA-1 ! do not yet support multiple layers
    case default
       ret_val = -1
    end select

    if (ret_val < 0) then
       write(dm_outfile_handle(instance),'(A,I4,A,I4)') 'Error in get_values_count: ', ret_val, ' for ', exchange_item_id
    else
       write(dm_outfile_handle(instance),'(A,I4,A,I8)') 'get_values_count(', &
            exchange_item_id, '): ', ret_val
    endif
    call flush(dm_outfile_handle(instance))

  end function get_values_count


  ! --------------------------------------------------------------------------
  ! Pass a reference to the array with values for the given exchange item
  ! A subset of the array can be specified by the start and end index 
  ! --------------------------------------------------------------------------
  function get_values(instance, exchange_item_id, start_index, end_index, values) result(ret_val)

    use global, only : LA

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_values_' :: get_values
#endif

    ! return value
    integer :: ret_val ! =0 ok; =-1 indices not ok;

    ! arguments
    integer         , intent(in) :: instance         ! model instance identifier
    integer         , intent(in) :: exchange_item_id ! exchange item identifier
    integer         , intent(in) :: start_index, end_index ! number of values
    double precision, &
         dimension(end_index - start_index + 1), &
         intent(out)  :: values           ! returned values
    ! locals
    integer :: NCi          ! index of exchange item variable in CSER time series
    integer :: index1, index2
    ! body

    if (debug) print*, "get_values ", instance, exchange_item_id, start_index, end_index

    ret_val = -1

    ! shift to EFDC grid indices EFDC (2:LA), OpenDA java (0:LA-2)
    index1 = start_index + 2;
    index2 = end_index + 2;
    if ( valid_model_instance(instance) ) then
       if ( check_grid_indices(instance, exchange_item_id, index1, index2) ) then
          select case(exchange_item_id)
          case (Grid_WaterLevel) !
             values = dble(state(instance)%HP(index1:index2)  +  state(instance)%BELV(index1:index2))
             ret_val = 0
          case (Grid_Discharge) ! Only one layer for now
             values = dble(state(instance)%QSUM(index1:index2,1)) 
             ret_val = 0
          case (Grid_WaterTemperature) ! Only one layer for now
             values = dble(state(instance)%TEM(index1:index2,1)) 
             ret_val = 0
          case (1501:1519) ! Water Quality fields,  Only one layer for now
             NCi = exchange_item_id - 1500
             values = dble(state(instance)%WQV(index1:index2,1, NCi))
             ret_val = 0
          case default
             ret_val = -2 ! unhandled item
          end select
       endif
    endif

    if (ret_val /= 0) then
       write(dm_outfile_handle(instance),'(A,I2)') 'Error in get_values: ', ret_val
    else
       if (debug) print*, 'get_values'
       write(dm_outfile_handle(instance),'(A,I4,A)') 'get_values(', &
            exchange_item_id, '):'
       write(dm_outfile_handle(instance),*) values(1:min(9,end_index - start_index+1) )
    endif
    call flush(dm_outfile_handle(instance))

  end function get_values

  ! --------------------------------------------------------------------------
  ! Set the values for the given exchange item in model instance memory
  ! A subset of the array can be specified by the start and end index 
  ! --------------------------------------------------------------------------
  function set_values(instance, exchange_item_id, start_index, end_index, values) result(ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_set_values_' :: set_values
#endif


    ! return value
    integer :: ret_val ! =0 ok; =-1 indices not ok;

    ! arguments
    integer         , intent(in) :: instance         ! model instance identifier
    integer         , intent(in) :: exchange_item_id ! exchange item identifier
    integer         , intent(in) :: start_index, end_index     ! number of values
    double precision, &
         dimension(end_index - start_index + 1), &
         intent(in)  :: values           ! returned values
    ! locals
    integer :: NCi            ! index of exchange item variable in CSER time series
    integer :: index1, index2 ! EFDC start and end grid indices

    ! body
    
    ! shift to EFDC grid indices EFDC (2:LA), OpenDA java (0:LA-2)
    index1 = start_index + 2;
    index2 = end_index + 2;
    ret_val = -1 ! indices not ok
    if (valid_model_instance(instance)) then
       if ( check_grid_indices(instance, exchange_item_id, index1, index2) ) then
          select case(exchange_item_id)
          case (Grid_WaterLevel) !
             state(instance)%HP(index1:index2) = real(values) - state(instance)%BELV(index1:index2)
             ret_val = 0
          case (Grid_Discharge) ! Only one layer for now
             state(instance)%QSUM(index1:index2,1) = real(values) 
             ret_val = 0
          case (Grid_WaterTemperature) ! Only one layer for now
             state(instance)%TEM(index1:index2,1) = real(values) 
             ret_val = 0
          case (1501:1519) ! Water Quality,  Only one layer for now
             NCi = exchange_item_id - 1500
             state(instance)%WQV(index1:index2,1, NCi) = real(values) 
             ret_val = 0
          case default
             ret_val = -2 ! unhandled item
          end select
       endif
    endif

    if (ret_val /= 0) then
       write(dm_outfile_handle(instance),'(A,I2)') 'Error in set_values: ', ret_val
    else
       write(dm_outfile_handle(instance),'(A,I4,A)') 'set_values(', &
            exchange_item_id, '):'
       write(dm_outfile_handle(instance),*) values(1:min(9,end_index - start_index+1) )
    endif
    call flush(dm_outfile_handle(instance))

  end function set_values

  ! --------------------------------------------------------------------------
  ! Get the number of values for given exchange item and location
  ! --------------------------------------------------------------------------
  function get_values_count_for_location(instance, exchange_item_id, bc_index) result(ret_val)

    !use global, only: MASER, MPSER, MQSER

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_values_count_for_location_' :: get_values_count_for_location
#endif

    ! return value
    integer :: ret_val    ! number of values for a certain exchange item

    ! arguments
    integer, intent(in) :: instance ! model instance identifier
    integer, intent(in) :: exchange_item_id  ! exchange item identifier
    integer, intent(in) :: bc_index  ! location index (as in EFDC.INP or WQ3D.INP)

    ! local
    integer :: NC  ! index of exchange item variable in CSER time series

    ! body
    if (debug) print*, exchange_item_id
    select case(exchange_item_id)
    case (101:107) ! atmospheric 
       ret_val = aser(instance)%MASER(bc_index)
    case (WaterLevel)
       ret_val = psert(instance)%MPSER(bc_index)
    case (Discharge)
       ret_val = qsert(instance)%MQSER(bc_index)
    case (WaterTemperature)
       NC = 2
       ret_val = csert(instance)%MCSER(bc_index,NC)
       if (debug) print*, "get_values_count_for_location", exchange_item_id, bc_index, NC
    case (501:519) ! Water Quality
       NC = NC_wq_start + exchange_item_id - 500
       if (debug) print*, "get_values_count_for_location", exchange_item_id, bc_index, NC
       ret_val = csert(instance)%MCSER(bc_index,NC)
    case default
       ret_val = -1
    end select

    if (ret_val < 0) then
       write(dm_outfile_handle(instance),'(A,I4,A,I4)') 'Error in get_values_count_for_location: ', ret_val, ' for ', exchange_item_id
    else
       write(dm_outfile_handle(instance),'(A,I4,A,I4)') 'get_values_count_for_location(', &
            exchange_item_id, '): ', ret_val
    endif
    call flush(dm_outfile_handle(instance))

  end function get_values_count_for_location

  ! --------------------------------------------------------------------------
  ! Get the number of locations for given exchange item
  ! --------------------------------------------------------------------------
  function get_time_series_count(instance, exchange_item_id) result(ret_val) 

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_time_series_count_' :: get_time_series_count
#endif

    use global, only: NCSER

    ! return value
    integer(kind=8) :: ret_val    ! number of locations for given exchange item

    ! arguments
    integer, intent(in) :: instance          ! model instance identifier
    integer, intent(in) :: exchange_item_id  ! exchange item identifier
 
    ! local
    integer :: NC   ! index of exchange item variable in CSER time series

    ! body
    ret_val = -1
    if (debug) print*, "get_time_series_count: ", instance, exchange_item_id 
    select case(exchange_item_id)
    case (101:107) ! atmospheric
       if (debug) print*, "number of locations: ", aser(instance)%naser
       ret_val = aser(instance)%NASER
    case (WaterLevel)
       if (debug) print*, "number of locations: ", psert(instance)%npser
       ret_val = psert(instance)%NPSER
    case (Discharge)
       if (debug) print*, "number of locations: ", qsert(instance)%nqser
       ret_val = qsert(instance)%NQSER
    case (WaterTemperature)
       NC = 2
       if (debug) print*, "number of locations: ", NC, NCSER(NC)
       ret_val = NCSER(NC)
    case (501:519) ! Water Quality 
       NC = NC_wq_start + exchange_item_id - 500
       if (debug) print*, "number of locations: ", NC, NCSER(NC)
       ret_val = NCSER(NC)
    case default
       ret_val = -1
    end select

    if (debug) print*, ret_val

    if (ret_val < 0) then
       write(dm_outfile_handle(instance),'(A,I2)') 'Error in get_time_series_count: ', ret_val
    else
       write(dm_outfile_handle(instance),'(A,I4,A,I4 )') 'get_time_series_count(', &
            exchange_item_id, '): ', ret_val
    endif
    call flush(dm_outfile_handle(instance))


  end function get_time_series_count

  ! --------------------------------------------------------------------------
  ! Get the number of values for exchange item and location within given time
  ! span.
  ! --------------------------------------------------------------------------
  function get_values_count_for_time_span(instance, exchange_item_id, bc_index, start_time, end_time) result(ret_val)


#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_values_count_for_time_span_' :: get_values_count_for_time_span
#endif

    ! return value
    integer :: ret_val

    ! arguments
    integer         , intent(in) :: instance         ! model instance
    integer         , intent(in) :: exchange_item_id ! type of time dependent boundary
    integer         , intent(in) :: bc_index         ! index of location
    double precision, intent(in) :: start_time       ! start time of bc values
    double precision, intent(in) :: end_time         ! end time of bc values

    ! locals
    integer :: start_index   ! index in bc's time series values for start_time
    integer :: end_index     ! index in bc's time series values for end_time

    ! body

    ret_val = -1 ! indices not ok

    if (valid_model_instance(instance)) then
       if (check_bc_indices(instance, exchange_item_id, bc_index , &
            start_time , end_time , &
            start_index, end_index) ) then
          ret_val = end_index - start_index + 1
       endif
    endif

    if (ret_val < 0) then
       write(dm_outfile_handle(instance),'(A,I2)') 'Error in get_values_count_for_time_span: ', ret_val
    else
       write(dm_outfile_handle(instance),'(A,I4,A,F8.2,A,F8.2,A,I4)') 'get_values_count_for_time_span(', &
            exchange_item_id,&
            ',', start_time, ',', end_time, '): ', ret_val
    endif
    call flush(dm_outfile_handle(instance))

  end function get_values_count_for_time_span

  ! --------------------------------------------------------------------------
  ! Pass the values as a reference to a double precission array for given 
  ! exchange item and location within given time span.
  ! --------------------------------------------------------------------------

  function get_values_for_time_span(instance, exchange_item_id, bc_index, start_time, end_time, values_count, values) result(ret_val)

    use global, only: RAINCVT

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_get_values_for_time_span_' :: get_values_for_time_span
#endif

    ! return value
    integer :: ret_val ! =0 ok; =-1 indices not ok; =-2 invalid exchange item id 

    ! arguments
    integer         , intent(in) :: instance         ! model instance
    integer         , intent(in) :: exchange_item_id ! type of time dependent boundary (e.g. discharge_on_laterals)
    integer         , intent(in) :: bc_index         ! index of boundary condition (e.g. discharge nr. 4)
    double precision, intent(in) :: start_time       ! start time of bc values
    double precision, intent(in) :: end_time         ! end time of bc values
    integer         , intent(in) :: values_count     ! #values
    double precision, &
         dimension(values_count), &
         intent(out)  :: values           ! returned values
    
    ! locals
    integer :: start_index   ! index in bc's time series values for start_time
    integer :: end_index     ! index in bc's time series values for end_time
    double precision :: factor, gravity        ! conversion factor
    integer :: NC           ! index of exchange item variable in CSER time series

    ! body

    ret_val = -1 ! indices not ok

    if (valid_model_instance(instance)) then

       if (check_bc_indices(instance, exchange_item_id, bc_index , &
            start_time , end_time , &
            start_index, end_index) ) then

          select case(exchange_item_id)
          ! atmospheric
          case (Precipitation)
             factor = 1.0d0/dble(RAINCVT)
             values = dble(aser(instance)%RAIN(start_index:end_index, bc_index)) 
             ret_val = 0 
          case (AirTemperature)
             values = dble(aser(instance)%TDRY(start_index:end_index, bc_index))
             ret_val = 0
          case (CloudCover)
             values = dble(aser(instance)%CLOUD(start_index:end_index, bc_index))
             ret_val = 0
          case (GlobalRadiation)
             values = dble(aser(instance)%SOLSWR(start_index:end_index, bc_index))
             ret_val = 0
          case (AtmosphericPressure)
             values = dble(aser(instance)%PATM(start_index:end_index, bc_index))
             ret_val = 0
          case (RelativeHumidity)
             values = dble(aser(instance)%TWET(start_index:end_index, bc_index))
             ret_val = 0
          case (PotentialEvaporation)
             factor = 1.0d0/dble(RAINCVT)
             values = dble(aser(instance)%EVAP(start_index:end_index, bc_index))
             ret_val = 0
          case (WaterLevel)
             gravity = 9.81d0
             values = dble(psert(instance)%PSER(start_index:end_index, bc_index))/gravity
             ret_val = 0 
          case (Discharge) ! Only one layer for now
             values = dble(qsert(instance)%QSER(start_index:end_index,1,bc_index)) 
             ret_val = 0 
          case (WaterTemperature) ! Only one layer for now
             NC = 2
             values = dble(csert(instance)%CSER(start_index:end_index,1,bc_index, NC)) 
             ret_val = 0
          case (501:519) ! Water Quality,  Only one layer for now
             NC = NC_wq_start + exchange_item_id - 500;
             values = dble(csert(instance)%CSER(start_index:end_index,1,bc_index, NC))
             ret_val = 0
          case default
             ret_val = -2 ! unhandled item
          end select

       endif

    endif

    if (ret_val /= 0) then
       write(dm_outfile_handle(instance),'(A,I2)') 'Error in get_values_for_time_span: ', ret_val
    else
       write(dm_outfile_handle(instance),'(A,I4,A,F8.2,A,F8.2,A)') 'get_values_for_time_span(', &
            exchange_item_id,&
            ',', start_time, ',', end_time, '):'
       write(dm_outfile_handle(instance),*) values(1:min(9,end_index-start_index+1))
    endif
    call flush(dm_outfile_handle(instance))

  end function get_values_for_time_span

  ! --------------------------------------------------------------------------
  ! Set the values in instance memory for given 
  ! exchange item and location within given time span.
  ! --------------------------------------------------------------------------
  function set_values_for_time_span(instance, exchange_item_id, bc_index, start_time, end_time, values_count, values) result(ret_val)

#if ( defined(_WIN32) && defined(__INTEL_COMPILER) )
    !DEC$ ATTRIBUTES DLLEXPORT, ALIAS : 'm_openda_wrapper_set_values_for_time_span_' :: set_values_for_time_span
#endif

    use global, only: RAINCVT

    ! return value
    integer :: ret_val

    ! arguments
    integer         , intent(in) :: instance         ! model instance
    integer         , intent(in) :: exchange_item_id ! type of time dependent boundary (e.g. discharge_on_laterals)
    integer         , intent(in) :: bc_index         ! index of boundary condition (e.g. discharge nr. 4)
    double precision, intent(in) :: start_time       ! start time of bc values
    double precision, intent(in) :: end_time         ! end time of bc values
    integer         , intent(in) :: values_count     ! number of values
    double precision, &
         dimension(values_count), &
         intent(in) :: values           ! incoming values

    ! locals
    integer :: start_index   ! index in bc's time series values for start_time
    integer :: end_index     ! index in bc's time series values for end_time
    integer :: NC           ! index of exchange item variable in CSER time series
    integer :: i
    double precision :: gravity !should be the same as in INPUT.for
    
    ! body

    ret_val = -1 ! indices not ok

    do i =1,values_count
        if (any(values == -9999)) then
             write(dm_outfile_handle(instance),'(A,I2)') 'Warning in set_values_for_time_span: missing values'
        end if    
    end do
    
    if (valid_model_instance(instance)) then

       if (check_bc_indices(instance, exchange_item_id, bc_index , &
            start_time , end_time , &
            start_index, end_index) ) then

          ret_val = 0

          select case(exchange_item_id)
          case (Precipitation)
             !RAINCVT = 1.0e-3/3600.0
             aser(instance)%RAIN(start_index:end_index, bc_index) = real(values)  
             ret_val = 0
          case (AirTemperature)
             aser(instance)%TDRY(start_index:end_index, bc_index) = real(values)
             ret_val = 0
          case (CloudCover)
             aser(instance)%CLOUD(start_index:end_index, bc_index) = real(values)
             ret_val = 0
          case (GlobalRadiation)
             aser(instance)%SOLSWR(start_index:end_index, bc_index) = real(values)  
             ret_val = 0
          case (AtmosphericPressure)
             aser(instance)%PATM(start_index:end_index, bc_index) = real(values)  
             ret_val = 0
          case (RelativeHumidity)
             aser(instance)%TWET(start_index:end_index, bc_index) = real(values)  
             ret_val = 0
          case (PotentialEvaporation)
             !RAINCVT = 1.0e-3/3600.0
             aser(instance)%EVAP(start_index:end_index, bc_index) = real(values)
             ret_val = 0
          case (WaterLevel)
             gravity = 9.81d0
             psert(instance)%PSER(start_index:end_index, bc_index) = real(values * gravity) 
             ret_val = 0 
          case (Discharge) ! Only one layer for now
             qsert(instance)%QSER(start_index:end_index,1,bc_index) = real(values)
             ret_val = 0 
          case (WaterTemperature) ! Only one layer for now
             NC = 2
             csert(instance)%CSER(start_index:end_index,1,bc_index, NC) = real(values)
             ret_val = 0
          case (501:519) !Water Quality, only one layer for now
             NC = NC_wq_start + exchange_item_id - 500; 
             csert(instance)%CSER(start_index:end_index,1,bc_index, NC) = real(values)
             ret_val = 0
          case default
             ret_val = -2 ! unhandled item
          end select

       endif

    endif

    if (ret_val /= 0) then
       write(dm_outfile_handle(instance),'(A,I2)') 'Error in set_values_for_time_span: ', ret_val
    else
       write(dm_outfile_handle(instance),'(A,I3,A,I4,A,F8.2,A,F8.2,A, F8.2, A, F8.2 )') 'set_values_for_time_span(', &
            exchange_item_id, ', ', bc_index, &
            ',', start_time, ',', end_time, '):', values(1), ',', values(values_count)
       write(dm_outfile_handle(instance),*) values(1:min(9,end_index-start_index+1))
    endif
    call flush(dm_outfile_handle(instance))

  end function set_values_for_time_span


!-----------------------------------------------------------------------------
! private methods
!-----------------------------------------------------------------------------

  ! --------------------------------------------------------------------------
  ! Check if the specified location index is within range and determines the
  ! start and end index for given start and end time
  ! --------------------------------------------------------------------------
  function check_bc_indices(instance, exchange_item_id, bc_index, start_time, end_time, start_index, end_index) result(success)
    
    use global, only: NDASER ,NASERM, TASER, NDPSER, NPSERM, TPSER, NDQSER, NQSERM, TQSER, NCSER, &
        TCASER, TCPSER, TCCSER, TCQSER ! time conversion to seconds
    

    ! return value
    logical :: success     ! .true.  indices determined ok.
    ! .false. location_index out of bounds

    ! arguments
    integer         , intent(in)  :: instance      ! model instance identifier
    integer         , intent(in)  :: exchange_item_id       ! type of boundary condition (discharge_on_laterals)
    integer         , intent(in)  :: bc_index      ! location index
    double precision, intent(in)  :: start_time    ! start time of values to be gotten/set
    double precision, intent(in)  :: end_time      ! end time of values to be gotten/set
    integer         , intent(out) :: start_index   ! index in bc's time series values for start_time
    integer         , intent(out) :: end_index     ! index in bc's time series values for end_time

    ! locals
    double precision :: epsilon = 1.0D-8
    double precision :: bc_start_time
    double precision :: bc_end_time 
    double precision :: bc_time_interval
    integer          :: NC                ! index of exchange item variable in CSER time series


    ! body
    success = .false.

    select case(exchange_item_id)
    case (101:107) ! atmospheric 
       success  =  bc_index >= 1 .and. bc_index <= aser(instance)%NASER
       if (success) then  
          bc_start_time = aser(instance)%TASER(1, bc_index) * dble(TCASER(bc_index)) / 86400.0d0
          bc_end_time = aser(instance)%TASER(aser(instance)%MASER(bc_index), bc_index) * dble(TCASER(bc_index)) / 86400.0d0
          bc_time_interval = (bc_end_time - bc_start_time) / dble(aser(instance)%MASER(bc_index)-1 )
       end if
    case (WaterLevel) 
       success  =  bc_index >= 1 .and. bc_index <= psert(instance)%NPSER
       if (success) then  
          bc_start_time = psert(instance)%TPSER(1, bc_index) * dble(TCPSER(bc_index)) / 86400.0d0
          bc_end_time = psert(instance)%TPSER(psert(instance)%MPSER(bc_index), bc_index) * dble(TCPSER(bc_index)) / 86400.0d0
          bc_time_interval =  (bc_end_time - bc_start_time) / dble(psert(instance)%MPSER(bc_index)-1)
       end if
    case (Discharge) 
       success  =  bc_index >= 1 .and. bc_index <= qsert(instance)%NQSER
       if (success) then  
          bc_start_time = qsert(instance)%TQSER(1, bc_index)
          bc_end_time = qsert(instance)%TQSER(qsert(instance)%MQSER(bc_index), bc_index) * dble(TCQSER(bc_index)) / 86400.0d0
          bc_time_interval = (bc_end_time - bc_start_time) / dble(qsert(instance)%MQSER(bc_index)-1) * dble(TCQSER(bc_index)) / 86400.0d0
       end if
    case (WaterTemperature) 
       NC = 2
       success  =  bc_index >= 1 .and. bc_index <= NCSER(NC)
       if (success) then  
          bc_start_time = csert(instance)%TCSER(1, bc_index, NC)
          bc_end_time = csert(instance)%TCSER(csert(instance)%MCSER(bc_index,NC), bc_index, NC) * dble(TCCSER(bc_index,NC)) / 86400.0d0
          bc_time_interval = (bc_end_time - bc_start_time) / dble(csert(instance)%MCSER(bc_index, NC)-1) * dble(TCCSER(bc_index,NC)) / 86400.0d0
       end if
    case (501:519) ! Water Quality 
       NC = NC_wq_start + exchange_item_id - 500
       success  =  bc_index >= 1 .and. bc_index <= NCSER(NC)
       if (success) then  
          bc_start_time = csert(instance)%TCSER(1, bc_index, NC)
          bc_end_time = csert(instance)%TCSER(csert(instance)%MCSER(bc_index,NC), bc_index, NC) * dble(TCCSER(bc_index,NC)) / 86400.0d0
          bc_time_interval =  (bc_end_time - bc_start_time) / dble(csert(instance)%MCSER(bc_index, NC)-1)* dble(TCCSER(bc_index,NC)) / 86400.0d0
       end if
    case default
       success = .false.
    end select
    if (debug) print*, "time in time series", bc_start_time, bc_end_time, exchange_item_id
    if (debug) print*, "requested times", start_time, end_time
    success = success .and. &
     start_time >= (bc_start_time - epsilon) .and. &
     end_time <= (bc_end_time + epsilon) .and. &
     end_time >= start_time

    if (success) then
       start_index = dint( (start_time - bc_start_time) / bc_time_interval  ) +1 
       end_index = dint( (end_time - bc_start_time) / bc_time_interval) +1
    end if
    if (debug) print*, "success ", success, start_index, end_index
    
  end function check_bc_indices


  ! --------------------------------------------------------------------------
  ! Check if start and end index are within the range of the grid size
  ! --------------------------------------------------------------------------
  function check_grid_indices(instance, exchange_item_id, start_index, end_index) result(success)

    use global, only : LA

    logical :: success     ! .true.  indices determined ok.
    ! .false. location_index out of bounds

    ! arguments
    integer         , intent(in)  :: instance      ! model instance identifier
    integer         , intent(in)  :: exchange_item_id       ! type of boundary condition (discharge_on_laterals)
    integer         , intent(in) :: start_index   ! index in grid 
    integer         , intent(in) :: end_index     ! index in grid

    select case (exchange_item_id)
    case (Grid_WaterLevel, Grid_Discharge, Grid_WaterTemperature)
       success =  (start_index >= 2) .and. (start_index <= end_index) .and. (end_index <= LA )
    case (1501:1519)
       success =  (start_index >= 2) .and. (start_index <= end_index) .and. (end_index <= LA )
    case default 
       success = .false.
    end select

    if (debug) print*, "success ", success, instance, start_index, end_index, 2, LA


  end function check_grid_indices


  ! --------------------------------------------------------------------------
  ! Enlarge the pointer arrays when we create a new model instance
  ! --------------------------------------------------------------------------
  subroutine add_instance_storage()

    ! locals
    character(len=256), dimension(:), pointer :: org_model_instance_dirs => NULL() ! current array of model instance dirs
    integer, dimension(:), pointer :: org_dm_outfile_handle
    type(state_vector), dimension(:), pointer :: org_model_instance_state => NULL() 
    type(aser_time_series), dimension(:), pointer :: org_model_instance_aser => NULL() 
    type(pser_time_series), dimension(:), pointer :: org_model_instance_pser => NULL() 
    type(qser_time_series), dimension(:), pointer :: org_model_instance_qser => NULL() 
    type(cser_time_series), dimension(:), pointer :: org_model_instance_cser => NULL() 


    !add additional storage for instance if necessary
    if (dm_model_instance_count > dm_max_dm_model_instance_count) then
       ! realloc directories
       org_model_instance_dirs => model_instance_dirs
       allocate(model_instance_dirs(dm_max_dm_model_instance_count + instances_realloc_size))
       if (associated(org_model_instance_dirs)) then
          model_instance_dirs(1:dm_max_dm_model_instance_count) = org_model_instance_dirs
          deallocate(org_model_instance_dirs)
       endif
       model_instance_dirs(dm_max_dm_model_instance_count+1) = ' '

       org_dm_outfile_handle => dm_outfile_handle
       allocate(dm_outfile_handle(dm_max_dm_model_instance_count + instances_realloc_size))
       if (associated(org_dm_outfile_handle)) then
          dm_outfile_handle(1:dm_max_dm_model_instance_count) = org_dm_outfile_handle
          deallocate(org_dm_outfile_handle)
       endif
       dm_outfile_handle(dm_max_dm_model_instance_count+1) = 0

       !realloc pointers to state vectors
       org_model_instance_state => state
       allocate(state(dm_max_dm_model_instance_count + instances_realloc_size))
       if (associated(org_model_instance_state)) then
          state(1:dm_max_dm_model_instance_count) = org_model_instance_state
          deallocate(org_model_instance_state)
       endif

       !realloc pointers to aser time series
       org_model_instance_aser => aser
       allocate(aser(dm_max_dm_model_instance_count + instances_realloc_size))
       if (associated(org_model_instance_aser)) then
          aser(1:dm_max_dm_model_instance_count) = org_model_instance_aser
          deallocate(org_model_instance_aser)
       end if

       !realloc pointers to pser time series
       org_model_instance_pser => psert
       allocate(psert(dm_max_dm_model_instance_count + instances_realloc_size))
       if (associated(org_model_instance_pser)) then
          psert(1:dm_max_dm_model_instance_count) = org_model_instance_pser
          deallocate(org_model_instance_pser)
       end if

       !realloc pointers to qser time series
       org_model_instance_qser => qsert
       allocate(qsert(dm_max_dm_model_instance_count + instances_realloc_size))
       if (associated(org_model_instance_qser)) then
          qsert(1:dm_max_dm_model_instance_count) = org_model_instance_qser
          deallocate(org_model_instance_qser)
       end if

       !realloc pointers to cser time series
       org_model_instance_cser => csert
       allocate(csert(dm_max_dm_model_instance_count + instances_realloc_size))
       if (associated(org_model_instance_cser)) then
          csert(1:dm_max_dm_model_instance_count) = org_model_instance_cser
          deallocate(org_model_instance_cser)
       end if

       dm_max_dm_model_instance_count = dm_max_dm_model_instance_count + instances_realloc_size

    endif

  end subroutine add_instance_storage

  ! --------------------------------------------------------------------------
  ! Check if given model instance is valid
  ! --------------------------------------------------------------------------
  function valid_model_instance(instance_id) result(success)

    ! return value
    logical :: success     ! .true.  instance_id ok.
    ! .false. instance_id out of bounds, or not in memory

    ! arguments
    integer, intent(in)  :: instance_id          ! model instance id to be checked

    success = instance_id >= 1 .and. instance_id <= dm_model_instance_count

  end function valid_model_instance

end module m_openDA_wrapper
