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

#
# SKIP NATIVE BUILD
#

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
