ln -s ../src/pollute2d_worker .
mpiexec -np 2 costawb -p ens_pollute2d_mw.xml : -np 1 pollute2d_worker ens_pollute2d_mw.xml
