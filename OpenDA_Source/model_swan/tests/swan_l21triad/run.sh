#! /bin/sh

. ../set_path.sh

#
# simulations
#
Application.sh swanSimulation.oda


#
# calibration without constraint
#

Application.sh swanSimplex.oda
Application.sh swanPowell.oda
Application.sh swanDud.oda
Application.sh swanGriddedFullSearch.oda

#
# with a weak constraint
#

Application.sh swanSimplexWithConstr.oda
Application.sh swanPowellWithConstr.oda
Application.sh swanDudWithConstr.oda
