/* MOD_V2.0
* Copyright (c) 2012 OpenDA Association
* All rights reserved.
* 
* This file is part of OpenDA. 
* 
* OpenDA is free software: you can redistribute it and/or modify 
* it under the terms of the GNU Lesser General Public License as 
* published by the Free Software Foundation, either version 3 of 
* the License, or (at your option) any later version. 
* 
* OpenDA is distributed in the hope that it will be useful, 
* but WITHOUT ANY WARRANTY; without even the implied warranty of 
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
* GNU Lesser General Public License for more details. 
* 
* You should have received a copy of the GNU Lesser General Public License
* along with OpenDA.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.openda.model_efdc_dll;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.TimeZone;

import org.openda.exchange.timeseries.TimeUtils;
import org.openda.interfaces.ITime;
import org.openda.utils.Results;

import com.sun.jna.Native;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;

/**
 * Thin 'wrapping class' around the efdc model's native dll.
 * The only purposes of the class is to translate java arguments into f90 arguments (and vice versa)
 */
public class EfdcDLL {

	private static double modelTimeZoneOffsetInDays;
    //date of reference for the time used in EFDC (often the begin of a year)
    private double referenceDateInMjd;
    // Flag indicating whether the native code has been initialized or not 
    private static boolean dllYetToBeInitialized = true; 
    // Flag indicating whether we are searching for dll or so 
    public static final boolean RUNNING_ON_WINDOWS = System.getProperty("os.name").startsWith("Windows");
    // handle to native library
    private static IEfdcFortranNativeDLL nativeDLL;

    // keep track the model instance identifier that is currently in memory.
    private static int currentModelInstance = -1;

    //store the model instance identifier for this instance.
    private int myModelInstanceId = -1;

    /**
     * Initialize the dummy library
     *
     * @param modelInstanceParentDir  Directory path that serves as parent for the model instances
     * @param modelDll                Full path specification of the native DLL
     * @param modelTemplateDir        Directory with the template model
     * @param modelTimeZone           The timeZone that is used by the model
     */
    public static void initialize(File modelDll, File modelInstanceParentDir, File modelTemplateDir, TimeZone modelTimeZone) {

    	//Native.setProtected(true) will attempt to convert invalid accesses into exceptions.
        Native.setProtected(true);

        // Initialize the DLL, if not done yet
        if (dllYetToBeInitialized) {
    		Results.putMessage(EfdcDLL.class.getSimpleName() + ": initializing EFDC dll.");

            // determine full paths for parent dir and model template dir
            String modelInstanceParentDirPath;
            String modelTemplateDirPath;
            try {
                modelInstanceParentDirPath = modelInstanceParentDir.getCanonicalPath();
            } catch (IOException e) {
                throw new RuntimeException("Could not create canonical path for " + modelInstanceParentDir.getAbsolutePath());
            }
            try {
                modelTemplateDirPath = modelTemplateDir.getCanonicalPath();
            } catch (IOException e) {
                throw new RuntimeException("Could not create canonical path for " + modelTemplateDir.getAbsolutePath());
            }

            String nativeDllPath = modelDll.getAbsolutePath();
            File nativeDllFile = new File(nativeDllPath);
            if (!nativeDllFile.exists()) {
                throw new RuntimeException("Native DLL/SO does not exist: " + modelDll.getAbsolutePath());
            }

            System.out.println("Java:" +  System.getProperty("sun.arch.data.model"));
            System.out.println("Operating System:" +  System.getProperty("os.name"));
            System.out.println("Architecture:" +  System.getProperty("os.arch"));
            if(EfdcDLL.RUNNING_ON_WINDOWS){ //TODO create a better test here
                nativeDLL = (IEfdcFortranNativeDLL) Native.loadLibrary(nativeDllPath, IEfdcFortranNativeDLL.class);
            	
            }else{
            	// For now assumes that gfortran is used for linux and ifort for windows
            	GfortranFunctionMapper fortranMapper = new GfortranFunctionMapper();
            	HashMap<String, String> fortranMap = fortranMapper.getMap();
                nativeDLL = (IEfdcFortranNativeDLL) Native.loadLibrary(nativeDllPath, IEfdcFortranNativeDLL.class,fortranMap);            	
            }
            
            //nativeDLL = (IEfdcFortranNativeDLL) Native.loadLibrary(nativeDllPath, IEfdcFortranNativeDLL.class);
            nativeDLL.m_openda_wrapper_init_(modelInstanceParentDirPath, modelTemplateDirPath,
                    new IntByReference(modelInstanceParentDirPath.length()), new IntByReference(modelTemplateDirPath.length()));
            dllYetToBeInitialized = false;
            
			modelTimeZoneOffsetInDays = (double) modelTimeZone.getRawOffset() / (1000.0 * 3600.0 * 24.0);


        }else{
        	EfdcDLL.currentModelInstance = -1;
        }
    }

    public static void free() {
        // TODO: check how DLL can be unloaded
        nativeDLL.m_openda_wrapper_destroy_();
    }

    /**
     * Creates and returns an EfdcDll object that corresponds to the modelInstance in the given modelInstanceDir.
     *
     * @param modelInstanceDir Directory path containing the model instance
     * @return                 An instance of the native model
     */
    public static EfdcDLL getForModelInstance(File modelInstanceDir) {
        return new EfdcDLL(modelInstanceDir);
    }

    /**
     * Creates an EfdcDll object that corresponds to the modelInstance in the given modelInstanceDir.
     *
     * @param modelInstanceDir        Directory path containing the model instance
     */
    private EfdcDLL(File modelInstanceDir) {
        // determine full model instance path
        String modelInstanceDirPath;
        try {
            modelInstanceDirPath = modelInstanceDir.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("Could not create canonical path for " + modelInstanceDir.getAbsolutePath());
        }

        // initialize the model instance
        //startModelInstanceAccess();
        myModelInstanceId = nativeDLL.m_openda_wrapper_get_model_instance_(
                modelInstanceDirPath, new IntByReference(modelInstanceDirPath.length()) );
        currentModelInstance = myModelInstanceId;
        //endModelInstanceAccess();

        if (myModelInstanceId <= 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from nativeDLL.GET_MODEL_INSTANCE() call, retVal= "
                    + myModelInstanceId);
        } else {
            //currently for the EFDC model the reference time is defined
            //so that 1 January 00:00:00 (in local model timeZone) of the year that contains the startTime
            //of the run, corresponds to a time of 1.0 days (see EVENT_TOX2.INP file).
            // get reference date from model
            System.out.println("myModelInstanceID" + myModelInstanceId);
			int referenceYear = nativeDLL.m_openda_wrapper_get_reference_year_(new IntByReference(myModelInstanceId));
			String dateString = Integer.toString(referenceYear - 1) + "12310000";
			try {
				//dateString is in the timeZone used by the model.
				double referenceDateMjdInModelTimeZone = TimeUtils.date2Mjd(dateString);
				//referenceDateInMjd has to be in GMT, since OpenDA uses GMT internally.
				//Therefore here convert reference date from modelTimeZone to GMT.
				referenceDateInMjd = referenceDateMjdInModelTimeZone - modelTimeZoneOffsetInDays;
			} catch (ParseException e) {
				throw new RuntimeException("Could not parse reference date '" + dateString + "' returned by nativeDLL.GET_REFERENCE_YEAR()"); 
			}
        }
    }

    /**
     * Returns the time step used by the EFDC model in days
     *
     * @return detlaT.getValue()
     */
    public double getDeltaT() {
        DoubleByReference deltaT = new DoubleByReference();
        int retVal = nativeDLL.m_openda_wrapper_get_delta_t_(deltaT);
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_DELTA_T call, retVal= " + retVal);
        }
        return deltaT.getValue();
    }
    
    /**
     * Returns the reference period as set in the EFDC.INP file in days
     * The EFDC model can only be run in multiples of the reference period
     *
     * @return referencePeriod.getValue()
     */
    public double getReferencePeriod() {
        DoubleByReference referencePeriod = new DoubleByReference();
        int retVal = nativeDLL.m_openda_wrapper_get_reference_period_(referencePeriod);
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_REFERENCE_PERIOD call, retVal= " + retVal);
        }
        return referencePeriod.getValue();
    }
    

    /**
     * Returns the start time of simulation in MJD (i.e. in GMT timeZone).
     *
     * @return startTime.getValue() + referenceDateInMjd
     */
    public double getStartTime() {
        DoubleByReference startTime = new DoubleByReference();
        int retVal = nativeDLL.m_openda_wrapper_get_start_time_(new IntByReference(myModelInstanceId), startTime);
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_START_TIME call, retVal= " + retVal);
        }
        return startTime.getValue() + referenceDateInMjd;
        //return startTime.getValue();
    }

    /**
     * Returns the end time of simulation in MJD (i.e. in GMT timeZone).
     *
     * @return endTime.getValue() + referenceDateInMjd
     */
    public double getEndTime() {
        DoubleByReference endTime = new DoubleByReference();
        int retVal = nativeDLL.m_openda_wrapper_get_end_time_(new IntByReference(myModelInstanceId), endTime);
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_END_TIME call, retVal= " + retVal);
        }
        return endTime.getValue() + referenceDateInMjd;
        //return endTime.getValue();
    }

    /**
     * Returns the current time of the model in MJD (i.e. in GMT timeZone).
     *
     * @return current time of the model.
     */
    public double getCurrentTime() {
        DoubleByReference currentTime = new DoubleByReference();
        //startModelInstanceAccess();
        int retVal = nativeDLL.m_openda_wrapper_get_current_time_(new IntByReference(myModelInstanceId), currentTime);
        //endModelInstanceAccess();
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_CURRENT_TIME call, retVal= " + retVal);
        }
        return currentTime.getValue() + referenceDateInMjd;
        //return currentTime.getValue();
    }

    /**
     * Returns locationCount for a scalar time series parameter.
     *
     * @param parameterNumber
     * @return locationCount
     */
    public int getTimeSeriesCount(int parameterNumber) {
        int timeSeriesCount = nativeDLL.m_openda_wrapper_get_time_series_count_(new IntByReference(myModelInstanceId), new IntByReference(parameterNumber));
        if (timeSeriesCount < 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_TIME_SERIES_COUNT call, timeSeriesCount= " + timeSeriesCount);
        }
        return timeSeriesCount;
    }
   
    /**
     * Returns cellCount for grid parameter.
     *
     * @param parameterNumber
     * @return cellCount
     */
    public int getValuesCount(int parameterNumber) {
        int valuesCount = nativeDLL.m_openda_wrapper_get_values_count_(
                new IntByReference(myModelInstanceId),
                new IntByReference(parameterNumber) );
        if (valuesCount < 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_VALUES_COUNT call, valuesCount= " + valuesCount);
        }
        return valuesCount;
    }
    
    /**
     * Returns timeCount for a scalar time series parameter.
     *
     * @param parameterNumber
     * @param locationNumber
     * @return timeCount
     */
    private int getValuesCount(int parameterNumber, int locationNumber) {
        int valuesCount = nativeDLL.m_openda_wrapper_get_values_count_for_location_(
                new IntByReference(myModelInstanceId), 
                new IntByReference(parameterNumber), 
                new IntByReference(locationNumber));
        if (valuesCount < 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(myModelInstanceId));
            throw new RuntimeException("Invalid result from dll.GET_VALUES_COUNT_FOR_LOCATION call, valuesCount= " + valuesCount);
        }
        return valuesCount;
    }
    
    /**
     * Returns timeCount for the given subPeriod of a scalar time series parameter.
     *
     * @param parameterNumber
     * @param locationNumber
     * @param startTime
     * @param endTime
     * @return timeCount
     */
    private int getValuesCount(int parameterNumber, int locationNumber, ITime startTime, ITime endTime) {
        // The dll can handle different length time series, but we do not in this function
        int valuesCount = nativeDLL.m_openda_wrapper_get_values_count_for_time_span_(
                new IntByReference(myModelInstanceId),
                new IntByReference(parameterNumber), new IntByReference(locationNumber),
                new DoubleByReference(startTime.getMJD() - referenceDateInMjd ),
                new DoubleByReference(endTime.getMJD() - referenceDateInMjd ));
        if (valuesCount < 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_VALUES_COUNT_FOR_TIME_SPAN call, valuesCount= " + valuesCount);
        }
        return valuesCount;
    }

    /**
     * Returns all times for a scalar time series parameter.
     *
     * @param parameterNumber
     * @param locationNumber
     * @return times
     */
    public double[] getTimesForExchangeItem(int parameterNumber, int locationNumber) {
        int valuesCount = getValuesCount(parameterNumber, locationNumber);
        double[] times = new double[valuesCount];
        //startModelInstanceAccess();
        int retVal = nativeDLL.m_openda_wrapper_get_times_for_ei_(
                new IntByReference(myModelInstanceId),
                new IntByReference(parameterNumber),
                new IntByReference(locationNumber),
                new IntByReference(valuesCount),
                times);
        //endModelInstanceAccess();
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_TIMES_FOR_EI call, retVal= " + retVal);
        }
        // correct for reference date
        for (int i = 0; i < times.length; i++) {
            times[i] = times[i] + referenceDateInMjd;
        }
        return times;
    }
    
    /**
     * Sets all times for a scalar time series parameter.
     *
     * @param parameterNumber
     * @param locationNumber
     * @param times
     */
    public void setTimesForExchangeItem(int parameterNumber, int locationNumber, double[] times) {
        int valuesCount = times.length;
        // correct for reference date
        double[] myTimes = new double[valuesCount]; 
        
        for (int i = 0; i < myTimes.length; i++) {
            myTimes[i] = times[i] - referenceDateInMjd;
        }
        
        int retVal = nativeDLL.m_openda_wrapper_set_times_for_ei_(
                new IntByReference(myModelInstanceId),
                new IntByReference(parameterNumber),
                new IntByReference(locationNumber),
                new IntByReference(valuesCount),
                myTimes);
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.SET_TIMES_FOR_EI call, retVal= " + retVal);
        }
    }

    /**
     * Returns all values for the current time for a grid parameter.
     *
     * @param parameterNumber
     * @return values
     */
    public double[] getValues(int parameterNumber) {
        return getValues(parameterNumber, 0, getValuesCount(parameterNumber) - 1);
    }

    /**
     * Returns selected values for the current time for a grid parameter.
     *
     * @param parameterNumber
     * @param startIndex inclusive
     * @param endIndex inclusive
     * @return values
     */
    public double[] getValues(int parameterNumber, int startIndex, int endIndex) {
        double[] values = new double[endIndex-startIndex+1];
        //startModelInstanceAccess();
        int retVal = nativeDLL.m_openda_wrapper_get_values_(
                new IntByReference(myModelInstanceId),
                new IntByReference(parameterNumber),
                new IntByReference(startIndex), new IntByReference(endIndex),
                values);
        //endModelInstanceAccess();
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_VALUES call, retVal= " + retVal);
        }
        return values;
    }

    /**
     * Returns selected values for a scalar time series parameter.
     *
     * @param parameterNumber
     * @param locationNumber
     * @param startTime
     * @param endTime
     * @return values
     */
    public double[] getValues(int parameterNumber, int locationNumber, ITime startTime, ITime endTime) {
        int valuesCount = getValuesCount(parameterNumber, locationNumber, startTime, endTime);
        double[] values = new double[valuesCount];
        int retVal = nativeDLL.m_openda_wrapper_get_values_for_time_span_(
                new IntByReference(myModelInstanceId),
                new IntByReference(parameterNumber), new IntByReference(locationNumber),
                new DoubleByReference(startTime.getMJD() - referenceDateInMjd ),
                new DoubleByReference(endTime.getMJD() - referenceDateInMjd),
                new IntByReference(valuesCount), values);
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.GET_VALUES_FOR_TIME_SPAN call, retVal= " + retVal);
        }
        return values;
    }

    /**
     * Sets all values for a grid parameter.
     *
     * @param parameterNumber
     * @param values
     */
    public void setValues(int parameterNumber, double[] values) {
        setValues(parameterNumber, values, 0, getValuesCount(parameterNumber) - 1);
    }

    /**
     * Sets selected values for a grid parameter.
     *
     * @param parameterNumber
     * @param values
     * @param startIndex inclusive
     * @param endIndex inclusive
     */
    public void setValues(int parameterNumber, double[] values, int startIndex, int endIndex) {
        int valuesCount = endIndex-startIndex+1;
        if (valuesCount != values.length) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid number of values in setValues(exchangeItemId=" +
                    parameterNumber + "). #Values=" + values.length + ", #expected=" + valuesCount);
        }
        //startModelInstanceAccess();
        int retVal = nativeDLL.m_openda_wrapper_set_values_(
                new IntByReference(myModelInstanceId),
                new IntByReference(parameterNumber),
                new IntByReference(startIndex), new IntByReference(endIndex),
                values);
        //endModelInstanceAccess();
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.SET_VALUES call, retVal= " + retVal);
        }
    }

    /**
     * Sets selected values for a scalar time series parameter.
     *
     * @param parameterNumber
     * @param values
     * @param locationNumber
     * @param startTime
     * @param endTime
     */
    public void setValues(int parameterNumber, double[] values, int locationNumber, ITime startTime, ITime endTime) {
        int valuesCount = getValuesCount(parameterNumber, locationNumber, startTime, endTime);
        if (valuesCount != values.length) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid number of values in setValues(exchangeItemId=" +
                    parameterNumber + "). #Values=" + values.length + ", #expected=" + valuesCount);
        }
        int retVal = nativeDLL.m_openda_wrapper_set_values_for_time_span_(
                new IntByReference(myModelInstanceId),
                new IntByReference(parameterNumber), new IntByReference(locationNumber),
                new DoubleByReference(startTime.getMJD() - referenceDateInMjd),
                new DoubleByReference(endTime.getMJD() - referenceDateInMjd),
                new IntByReference(valuesCount), values);
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.SET_VALUES_FOR_TIME_SPAN call, retVal= " + retVal);
        }
    }

	/**
	 * In the EFDC model the model run period is divided in a number of referenceTimePeriods.
	 * Each referenceTimePeriod is in turn divided in a number of timeSteps.
	 * This method can only be called for a time period that is equal to an integer number of referenceTimePeriods.
	 *
	 * @param fromTime
	 * @param toTime
	 */
    public void compute(ITime fromTime, ITime toTime) {
        startModelInstanceAccess();
        int retVal = nativeDLL.m_openda_wrapper_compute_(
        		new IntByReference(myModelInstanceId),
                new DoubleByReference(fromTime.getMJD() - referenceDateInMjd ),
                new DoubleByReference(toTime.getMJD() - referenceDateInMjd ));
        endModelInstanceAccess();
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.COMPUTE call, retVal= " + retVal);
        }
    } 
    
    public void storeCurrentInstanceToRestartFiles() {
        startModelInstanceAccess(); 
        int retVal = nativeDLL.m_openda_wrapper_store_current_instance_restart_files_();
        if (retVal != 0) {
        	nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
        	throw new RuntimeException("Invalid result from dll.STORE_CURRENT_INSTANCE_RESTART_FILES call, retVal= " + retVal);
        }
    }
    
    public void restoreInstanceFromRestartFiles() {
        startModelInstanceAccess();
        int retVal = nativeDLL.m_openda_wrapper_select_instance_from_restart_files_(
                    new IntByReference(myModelInstanceId));
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.SELECT_INSTANCE_FROM_RESTART_FILES call, retVal= " + retVal);
        }
    }
   
    
    public void finish() {
        int retVal = nativeDLL.m_openda_wrapper_finish_(new IntByReference(myModelInstanceId));
        myModelInstanceId=-1;
        if (retVal != 0) {
            nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
            throw new RuntimeException("Invalid result from dll.FINISH call, retVal= " + retVal);
        }
    }

    private void startModelInstanceAccess() {
        
        // load required model instance
        if (myModelInstanceId >= 0) {
        	// Model InstancSystem.out.print("Switching states");e switch, restore required instance
        	int retVal = nativeDLL.m_openda_wrapper_restore_instance_(new IntByReference(myModelInstanceId));
        	if (retVal != 0) {
        		nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
        		throw new RuntimeException("Error restoring model instance " + retVal);
        	}
        }
        // store id of currently active model instance
        currentModelInstance = myModelInstanceId;
    }

    private void endModelInstanceAccess() {
        // store currently active model instance
        if (currentModelInstance >= 0) {
        	int retVal = nativeDLL.m_openda_wrapper_save_instance_(new IntByReference(currentModelInstance));
        	if (retVal != 0) {
        		nativeDLL.m_openda_wrapper_finish_(new IntByReference(currentModelInstance));
        		throw new RuntimeException("Error saving model instance " + retVal);
        	}
        }
    }
}
