#
#
# Makefile for EFDC shared library
#
#-----	Known suffixes  ---------------------------------------------
#
.SUFFIXES: .f90 .for .o .po


#-----	Fortran Compiler Settings  ---------------------------------------------# gfortran compiler
MAKE_FF =gfortran
# gfortran compiler
#MAKE_FF =ifort


#MAKE_FOPTIBUG = -g -fbounds-check -Wall -fbacktrace -finit-real=nan -ffpe-trap=invalid,zero,overflow	# debug
MAKE_FOPTIBUG = -O   	# optimize
MAKE_FFLAGS_SPECIFIC = -fPIC -ffree-line-length-none -x f95-cpp-input # gfortran

MAKE_FFLAGS = \
    $(MAKE_FFLAGS_SPECIFIC) \
    $(MAKE_FOPTIBUG) \

# additional libs (if required)
MAKE_INC = -I../original_efdc_files 

#-----	Link Settings  --------------------------------------------------

MAKE_SO_OPTIONS_SPECIFIC = -shared -lm	# gfortran

MAKE_SO =  $(MAKE_FF) $(MAKE_SO_OPTIONS_SPECIFIC)

MAKE_EXE_OPTIONS_SPECIFIC =  	# gfortran
MAKE_EXE =  $(MAKE_FF) $(MAKE_EXE_OPTIONS_SPECIFIC)

#-----	Source and binary names  ----------------------------------------
BINDIR = .
ARCH := $(shell getconf LONG_BIT)

ifeq ($(MAKE_FF), ifort)
  NATIVE_BIN = ../../../native_bin/linux$(ARCH)_ifort
endif
ifeq ($(MAKE_FF), gfortran)
  NATIVE_BIN = ../../../native_bin/linux$(ARCH)_gnu
endif

SO = $(BINDIR)/libEFDC.so

SRC_OPENDA_F90 = \
	model_extra_global.f90 model_state.f90 \
	model_aser_time_series.f90 model_end.f90 model_init.f90 \
	model_pser_time_series.f90 \
	model_cser_time_series.f90 \
	model_make_step.f90 model_qser_time_series.f90 openDA_wrapper.f90 \

SRC_OPENDA_FOR = \
	model_init_1.for model_init_2.for model_init_3.for  \
	model_restart_init.for \

OBJS_LIB = \
	$(SRC_ORIGINAL_EFDC_F90:.f90=.o) \
	$(SRC_ORIGINAL_EFDC_FOR:.for=.o) \
	$(SRC_OPENDA_F90:.f90=.o) \
	$(SRC_OPENDA_FOR:.for=.o) \

ORIG_EFDC = ../original_efdc_files/libEfdcOrig.a \

TEST_DATA_BIN_DIR = ../../../java/test/org/openda/model_efdc_dll/testData/bin
#BIN_EXTERNAL_DIR = ../../bin_external

#-----	Targets  ---------------------------------------------------------------

all: $(SO)

# EFDC wrapper library
$(SO): $(OBJS_LIB)
	@echo Creating library $(SO)
	@mkdir -p $(BINDIR)
	@rm -f $(SO)
	$(MAKE_SO)  -o $(SO) $(OBJS_LIB) -Wl,--whole-archive $(ORIG_EFDC)  -Wl,--no-whole-archive

install: $(SO)
	@cp $(SO) $(TEST_DATA_BIN_DIR)
	@echo Copying lib to $(NATIVE_BIN)
	@cp $(SO) $(NATIVE_BIN)

clean:
	@rm -f \
	    *.o \
	    *.mod \

clobber:
	@make clean
	@rm -f $(SO) $(EXE)

%.o: %.for
	$(MAKE_FF) $(MAKE_INC) -c $(MAKE_FFLAGS) $<

%.o: %.f90
	$(MAKE_FF) $(MAKE_INC) -c $(MAKE_FFLAGS) $<

