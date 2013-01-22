#! /bin/bash
#
# Compile native code for a linux system
# !!! Only tested for ubuntu 8.04 lts 32bit
#
# requirements:
# - install: (apt-get install <prog> )
#  - g++ gfortan automake
#  - sun-java6-jdk
#  - libxml2-dev
#  - ant subversion
#  - settings
#  - java: export JAVA_HOME=/usr/lib/jvm/java-6-sun
#          export PATH=$JAVA_HOME/bin:$PATH

# Get info about this system
export arch=`uname -m`
if [ "$arch" == "x86_64" ]; then
        export SYSTEM=linux64_gnu
else
        export SYSTEM=linux32_gnu
fi

if [ ! -f $JAVA_HOME/include/jni.h ]; then
   echo You need to set the environment variable JAVA_HOME
   echo so that the interface libraries can be built
   echo Current value: JAVA_HOME = \"$JAVA_HOME\"
   exit
fi

# this dir is the toplevel dir
export TOPDIR=$PWD


# create target dir if it does not exist
if [ ! -d $TOPDIR/openda/public/core/native/$SYSTEM/bin ]; then
   mkdir -p $TOPDIR/openda/public/core/native_bin/$SYSTEM/bin
   mkdir -p $TOPDIR/openda/public/core/native_bin/$SYSTEM/lib
   mkdir -p $TOPDIR/openda/public/core/native_bin/$SYSTEM/include
else
   echo "remove target dir to rebuild mpi and netcdf : "
   echo "target=$TOPDIR/openda/public/core/native_bin/$SYSTEM"
fi

# mpi binaries
if [ ! -d $TOPDIR/openda/public/core/native_bin/$SYSTEM/bin/mpirun ]; then
   pushd $TOPDIR/openda/public/core/native/external/mpi
   ./linux_install.sh
   rsync -ruav $PWD/$SYSTEM/lib/ $TOPDIR/openda/public/core/native_bin/$SYSTEM/lib
   rsync -ruav $PWD/$SYSTEM/bin/ $TOPDIR/openda/public/core/native_bin/$SYSTEM/bin
   rsync -ruav $PWD/$SYSTEM/include/ $TOPDIR/openda/public/core/native_bin/$SYSTEM/include
   popd
fi

# netcdf binaries
if [ ! -d $TOPDIR/openda/public/core/native_bin/$SYSTEM/bin/ncdump ]; then
   pushd $TOPDIR/openda/public/core/native/external/netcdf
   ./linux_install.sh
   rsync -ruav $PWD/$SYSTEM/lib/ $TOPDIR/openda/public/core/native_bin/$SYSTEM/lib
   rsync -ruav $PWD/$SYSTEM/bin/ $TOPDIR/openda/public/core/native_bin/$SYSTEM/bin
   rsync -ruav $PWD/$SYSTEM/include/ $TOPDIR/openda/public/core/native_bin/$SYSTEM/include
   popd
fi

# now compile openda native
pushd $TOPDIR/openda/public/core/native
./linux_install.sh
popd

# and the Java libraries
pushd $TOPDIR/openda/public
ant build
popd

# create a suitable local settings script
sed -e "s/SYSTEM/$SYSTEM/" $TOPDIR/openda/public/bin/settings_local_base.sh > $TOPDIR/openda/public/bin/settings_local_`hostname`.sh

# just let the user know we are done
echo --------------
echo Build complete
echo --------------
echo Post-build actions:
echo Edit the file settings_local_`hostname`.sh for any additional
echo settings you need
echo It is located in the $TOPDIR/openda/public/bin directory
