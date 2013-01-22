#! /bin/sh

. ../set_path.sh

#
# simulation and generation of observations
#
Application.sh lorenzSimulation.oda

#
# Kalman filtering
#
Application.sh lorenzEnkf.oda
Application.sh lorenzEnsr.oda
Application.sh lorenzRRF.oda

#
# calibration without constraint
# !! DOES NOT MAKE SENSE FOR THIS MODEL WITH THIS SETUP !!
#

#Application.sh lorenzSimplexOpenDaConfig_linux.xml 
#Application.sh lorenzPowellOpenDaConfig_linux.xml
#Application.sh lorenzDudOpenDaConfig_linux.xml

#
# calibration with a weak constraint
# !! DOES NOT MAKE SENSE FOR THIS MODEL WITH THIS SETUP !!
#

#Application.sh lorenzSimplexOpenDaConfig_withConstraint_linux.xml 
#Application.sh lorenzPowellOpenDaConfig_withConstraint_linux.xml
#Application.sh lorenzDudOpenDaConfig_withConstraint_linux.xml

