#! /bin/sh

# Runs all tests in the tests directories one by one, and puts the output and 
# results in a single directory test_results

export ErrorOccurred=0
export TestDisabled=0

run_single_test(){
   echo "Running test: $CURDIR/$1.oda"
   export odafile="$PWD/$CURDIR/$1.oda"
   java org.openda.application.OpenDaApplication $odafile 1>test_results/$CURDIR/$1.out 2>test_results/$CURDIR/$1.err
   if [ $? != 0 ]; then
      echo "***Error occurred in test $CURDIR/$1"
      export ErrorOccurred=1
      return -1
   fi
   if [ $# -ge 1 ]; then 
      cp $CURDIR/$2 test_results/$CURDIR/$1_$2 >/dev/null 2>&1
   fi
   if [ $# -ge 2 ]; then 
      cp $CURDIR/$2 test_results/$CURDIR/$1_$3 >/dev/null 2>&1
   fi
   if [ $# -ge 3 ]; then 
      cp $CURDIR/$2 test_results/$CURDIR/$1_$4 >/dev/null 2>&1
   fi
   if [ $# -ge 4 ]; then 
      cp $CURDIR/$2 test_results/$CURDIR/$1_$5 >/dev/null 2>&1
   fi
   if [ $# -ge 5 ]; then 
      cp $CURDIR/$2 test_results/$CURDIR/$1_$6 >/dev/null 2>&1
   fi
}

donotrun_single_test(){
   echo "***Warning: $CURDIR/$1.oda test is disabled"
   export TestDisabled=1
}

# export OPENDA_BINDIR, PATH and CLASSPATH
cd ../bin
export OPENDADIR=$PWD
export LD_LIBRARY_PATH=$OPENDADIR/linux32_gnu/lib:$LD_LIBRARY_PATH

chmod ug+rx $OPENDADIR/*.sh
chmod ug+rx $OPENDADIR/linux32_gnu/bin/**
chmod ug+rx $OPENDADIR/linux32_gnu/lib/**

for file in $OPENDADIR/*.jar ; do
   if [ -f "$file" ] ; then
       export CLASSPATH=$CLASSPATH:$file
   fi
done

cd ../tests
rm -rf test_results
mkdir test_results

echo

export CURDIR=native_oscillator
mkdir test_results/$CURDIR
run_single_test OscillEnKFOpenDaConfig         EnKF-results.txt    
run_single_test OscillEnKFOpenDaConfig_javaobs EnKF-results.txt
run_single_test OscillSimplexOpenDaConfig      Simplex-results.txt

# Only deactivate this part if the correct version of MPICH2 is installed. 
# ----
# since the tests in native_parallel work with a special script to start
# a parallel run, the approach is quite different here

# Environment variable MPI_DIR should be available or set here
# export MPI_DIR=/installation_directory_mpich2

# export CURDIR=native_parallel
#mkdir test_results/$CURDIR
#echo "Running test: $CURDIR/masterworker/run.sh"
#cd $CURDIR/masterworker
#run.sh 1>../../test_results/$CURDIR/PolluteEnKFOpenDaConfig_mw_results.out 2>../../test_results/$CURDIR/PolluteEnKFOpenDaConfig_mw_results.err
#cp EnKF-results.txt ../../test_results/$CURDIR/PolluteEnKFOpenDaConfig_mw_results.txt
#echo "***See log files for possible errors"

#echo "Running test: $CURDIR/workerworker/run.sh"
#cd ../workerworker
#run.sh 1>../../test_results/$CURDIR/PolluteEnKFOpenDaConfig_ww_results.out 2>../../test_results/$CURDIR/PolluteEnKFOpenDaConfig_ww_results.err
#cp EnKF-results.txt ../../test_results/$CURDIR/PolluteEnKFOpenDaConfig_ww_results.txt
#echo "***See log files for possible errors"
#cd ../../
# ----

export CURDIR=simple_lorenz
mkdir test_results/$CURDIR
run_single_test lorenzEnkf               enkf_results.m
run_single_test lorenzEnkf_write_restart enkf_results_write_restart.m
run_single_test lorenzEnsr               ensr_results.m
run_single_test lorenzRRF                particle_filter_results.m
run_single_test lorenzSimulation         simulation_results.m

export CURDIR=simple_lorenz_transformed_observations
mkdir test_results/$CURDIR
run_single_test DudEnkf        dudenkf_results.m
run_single_test Enkf           enkf_results.m
run_single_test Ensr           ensr_results.m
run_single_test ParticleFilter particle_filter_results.m
run_single_test Simulation     simulation_results.m

export CURDIR=simple_lorenz96
mkdir test_results/$CURDIR
run_single_test Enkf           enkf_results.m
run_single_test Ensr           ensr_results.m
run_single_test ParticleFilter particle_filter_results.m
run_single_test Simulation     simulation_results.m
run_single_test ThreeDVar      threedvar_results.m

export CURDIR=simple_oscillator
mkdir test_results/$CURDIR
run_single_test BFGS                  bfgs_results.m
run_single_test ConGrad               congrad_results.m
run_single_test Dud                   dud_results.m
run_single_test DudEnkf               dudenkf_results.m
donotrun_single_test Dudensr               dudensr_results.m
run_single_test DudWithConstraint     dud_constraint_results.m
run_single_test Enkf                  enkf_results.m
run_single_test Enkf_async_generate_gain enkf_async_generate_gain_results.m
run_single_test Enkf_fixedAnalysis    enkf_fixed_results.m
run_single_test Enkf_generate_gain    enkf_generate_gain_results.m
run_single_test Enkf_missing_obs      enkf_missingdata_results.m
run_single_test Enkf_write_restart    enkf_write_restart_results.m
run_single_test Enkf_startfrom_restart enkf_read_restart_results.m
run_single_test Ensr                  ensr_results.m
run_single_test Ensr_fixedAnalysis    ensr_fixed_results.m
run_single_test GriddedFullSearch     gfs_results.m
run_single_test ParticleFilter        particle_filter_results.m
run_single_test ParticleFilter_fixedAnalysis particle_filter_fixed_results.m
run_single_test Powell                powell_results.m
run_single_test PowellWithConstraint  powell_constraint_results.m
run_single_test SequentialEnsembleSimulation sequentialEnsembleSimulation_results.m
run_single_test SequentialSimulation sequentialSimulation_results.m
run_single_test SequentialSimulation_fixedAnalysisTimes sequentialSimulation_fixedAnalysisTimes_results.m
run_single_test Simplex               simplex_results.m
run_single_test SimplexWithConstraint simplex_constraint_results.m
run_single_test Simulation            simulation_results.m
run_single_test Steadystate           steadystate_results.m
run_single_test Steadystate_async     steadystate_async_results.m

export CURDIR=simple_resultwriters
mkdir test_results/$CURDIR
run_single_test DudMultipleResultwriters  results_dud.m results_dud.csv results_dud_.nc
run_single_test DudWritersWithSelections  results_dud_algorithm.m results_dud_model.m results_dud_observer.m results_dud_other.m results_dud_costTotal_only.m
run_single_test EnsrMultipleResultwriters results_ensr.m results_ensr_.nc

export CURDIR=simple_two_oscillators
mkdir test_results/$CURDIR
run_single_test Dud                    dud_results.m
run_single_test Dud1                   dud1_results.m 
run_single_test Dud2                   dud2_results.m
run_single_test DudWithConstraint      dud_constraint_results.m
donotrun_single_test Enkf                   enkf_results.m
donotrun_single_test Ensr                   ensr_results.m
run_single_test GriddedFullSearch      gfs_results.m  
donotrun_single_test ParticleFilter         particle_filter_results.m
run_single_test Powell                 powell_results.m
run_single_test PowellWithConstraint   powell_constraint_results.m
run_single_test Simplex                simplex_results.m
run_single_test SimplexWithConstraint  simplex_constraint_results.m
run_single_test Simulation             simulation_results.m  
run_single_test Simulation1            simulation1_results.m
run_single_test Simulation2            simulation1_results.m

echo
if [ "$ErrorOccurred"!=0 ]; then
   echo "One or more tests were not performed or finished with an error!"
   exit -1 
fi
if [ "$TestDisabled"!=0 ]; then
   echo "WARNING: one or more tests were disabled, the remaining tests finished without error"
else  
   echo "All tests were performed and finished without error"
fi
exit 0

