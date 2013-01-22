-------------------------------------------------------
                     COSTA test bank
-------------------------------------------------------

The subdirectories `unit' and 'individual' contain several files which, in turn, contain elementary tests. Most
COSTA-functions are tested in these files. New users can read the files and thus
get a first impression of the COSTA-tools available. 

The tests in the 'test_individual' directory are partly obsolete (some are written in
fortran) but they are kept there for convenience.  

All tests can be run sequentially by running the executable 'unit' in the
subdirectory 'unit'.


The following tests in the directory `unit' are available:


nr File    subroutine       subject
---------------------------------------
1) test1.c > test1a         cta_func add
2)         > test1b         cta_func mult
3)         > test1c         cta_func handle check
4)         > test1d         cta_func arguments check

5) test4.c                  cta_vector 
6) test6.c                  cta_state
7) test_interface.c > test1 cta_interface compatibility handle-handle
8) test_interface.c > test2 cta_interface compatibility handle-argument
9) test_interface.c > test3 cta_interface illegal handles

10) test_string_01.c        cta_string
11) test_tree_01.c          cta_tree
12) file_test.c             cta_file
13) test_matrix.c           cta_matrix

