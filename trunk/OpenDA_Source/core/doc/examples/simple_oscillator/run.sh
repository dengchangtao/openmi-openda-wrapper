#! /bin/sh

. ../set_path.sh 

#
# simulation and generation of observations
#
Application.sh Simulation.oda 

#
# Kalman filtering
#
Application.sh Enkf.oda 
Application.sh RRF.oda 
Application.sh Ensr.oda 

#
# calibration without constraint
#

Application.sh Simplex.oda 
Application.sh Powell.oda
Application.sh Dud.oda
Application.sh GriddedFullSearch.oda

#
# calibration with a weak constraint
#

Application.sh SimplexWithConstraint.oda 
Application.sh PowellWithConstraint.oda
Application.sh DudWithConstraint.oda

