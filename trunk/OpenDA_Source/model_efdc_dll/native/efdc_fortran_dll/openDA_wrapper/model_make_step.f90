subroutine model_make_step(time_period)

use global
use model_state

implicit none

real, intent(in) :: time_period ! period to integrate the model in seconds

! Set the number of time steps to make
NTS = floor(time_period/DT)

! **  SELECT FULL HYDRODYNAMIC AND MASS TRANSPORT CALCULATION OR
! **  LONG-TERM MASS TRANSPORT CALCULATION
NITERAT=0
IF(IS2TIM.EQ.0) then 
    print*, "HDMT", TIMESEC, TBEGIN, TIMEDAY
    CALL HDMT
elseif (IS2TIM.GE.1) then
    print*, "HDMT2T", TIMESEC, TBEGIN, TIMEDAY
    CALL HDMT2T
end if

end subroutine model_make_step
