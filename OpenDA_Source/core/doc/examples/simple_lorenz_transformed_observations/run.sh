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
Application.sh Ensr.oda
Application.sh ParticleFilter.oda

