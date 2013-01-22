#! /bin/sh

# Runs all tests in the tests directories one by one, and puts the output and 
# results in a single directory test_results

export ErrorOccurred=0
export TestDisabled=0

run_single_test(){
   echo "Running test: $CURDIR/$1.oda"
   export odafile="$PWD/$CURDIR/$1.oda"
   java -Xms200m -Xmx600m org.openda.application.OpenDaApplication $odafile 1>test_results/$CURDIR/$1.out 2>test_results/$CURDIR/$1.err
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
cd ../../bin
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

cd ../model_swan/tests
rm -rf test_results
mkdir test_results

echo

export CURDIR=s00c2
mkdir test_results/$CURDIR
run_single_test SwanEnKF             SwanEnKFResults.txt

export CURDIR=swan_l21triad
mkdir test_results/$CURDIR
run_single_test swanSimulation             SimulationResults.txt
run_single_test swanSimplex                SwanSimplexResults.txt    
run_single_test swanPowell                 SwanPowelResults.txt    
run_single_test swanDud                    SwanDudResults.txt    
run_single_test swanGriddedFullSearch      SwanGriddedFullsearchResults.txt    
run_single_test swanSimplexWithConstr      SwanSimplexWithConstrResults.txt    
run_single_test swanPowellWithConstr       SwanPowellwithConstrResults.txt    
run_single_test swanDudWithConstr          SwanDudwithConstrResults.txt    

export CURDIR=swan_l21triad_two_twin
mkdir test_results/$CURDIR
run_single_test Dud1                         Dud1Results.txt
run_single_test Dud2                         Dud2Results.txt
run_single_test Dud_combined12               Dudcombined12Results.txt
run_single_test Simplex_combined12           Simplex_combined12.txt
run_single_test Powell_combined12            Powell_combined12.txt
run_single_test GriddedFullSearch_combined12 GriddedFullSearch_combined12.txt

run_single_test DudWithConstraint_combined12     DudWithConstraint_combined12.txt
run_single_test SimplexWithConstraint_combined12 SimplexWithConstraint_combined12.txt
run_single_test PowellWithConstraint_combined12  PowellWithConstraint_combined12.txt

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

