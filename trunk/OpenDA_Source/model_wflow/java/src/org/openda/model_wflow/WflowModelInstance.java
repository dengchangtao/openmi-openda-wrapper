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

package org.openda.model_wflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openda.blackbox.config.BBUtils;
import org.openda.exchange.ArrayGeometryInfo;
import org.openda.exchange.NetcdfGridTimeSeriesExchangeItem;
import org.openda.exchange.QuantityInfo;
import org.openda.exchange.TimeInfo;
import org.openda.exchange.dataobjects.NetcdfDataObject;
import org.openda.exchange.iotools.DataCopier;
import org.openda.exchange.timeseries.TimeUtils;
import org.openda.interfaces.IArray;
import org.openda.interfaces.IDataObject;
import org.openda.interfaces.IExchangeItem;
import org.openda.interfaces.IModelInstance;
import org.openda.interfaces.IModelState;
import org.openda.interfaces.IObservationDescriptions;
import org.openda.interfaces.IPrevExchangeItem;
import org.openda.interfaces.IPrevExchangeItem.Role;
import org.openda.interfaces.IQuantityInfo;
import org.openda.interfaces.ITime;
import org.openda.interfaces.IVector;
import org.openda.utils.Array;
import org.openda.utils.Instance;
import org.openda.utils.Results;
import org.openda.utils.Time;
import org.openda.utils.io.FileBasedModelState;

/**
 * Model instance of a WFLOW model. This communicates in-memory with the WFLOW model.
 *
 * For information about the WFLOW model see www.openstreams.org
 * and https://publicwiki.deltares.nl/display/OpenS/wflow+-+PCRaster-Python+based+distributed+hydrological+models
 * For information about the PCRaster framework see http://pcraster.geo.uu.nl/
 *
 * @author Arno Kockx
 */
public class WflowModelInstance extends Instance implements IModelInstance {
	private static final String WFLOW_MASK_VARIABLE_NAME = "TopoId";

	private final File modelRunDir;
	private final ITime timeHorizon;
	private final String pythonModuleNameOfModelToUse;
	private final File caseDirectory;
	private final String instanceRunId;
	private final String configFileName;
	private final String cloneMapFileName;
	/**
	 * Length of one timeStep in milliseconds.
	 */
	private final long timeStepLength;
	private final int numberOfTimeSteps;
	private final File stateInputDir;
	private final File stateOutputDir;

	private WflowPythonToJavaAdapter adapter = null;
	private Map<String, IExchangeItem> modelExchangeItems = new HashMap<String, IExchangeItem>();
	private int currentTimeStep = 0;
	/**
	 * State stored on disk. The wflow model can only store one state at a time.
	 */
	private FileBasedModelState stateStoredOnDisk = null;

	private final IDataObject[] inputDataObjects;
	private IDataObject modelOutputDataObject;
	private IDataObject analysisOutputDataObject;

	/**
	 * @param timeHorizon ITime object that includes the startTime and endTime for the model run.
	 * @param pythonModuleNameOfModelToUse
	 * @param caseDirectory
	 * @param instanceRunId
	 * @param configFileName
	 * @param cloneMapFileName
	 * @param inputFilePaths relative to the instanceDir
	 * @param modelOutputFilePath relative to the instanceDir
	 * @param analysisOutputFilePath relative to the instanceDir
	 */
	public WflowModelInstance(ITime timeHorizon, String pythonModuleNameOfModelToUse, File caseDirectory,
			String instanceRunId, String configFileName, String cloneMapFileName,
			IDataObject[] inputDataObjects, String modelOutputFilePath, String analysisOutputFilePath) {
		this.caseDirectory = caseDirectory;
		this.instanceRunId = instanceRunId;
		this.modelRunDir = new File(caseDirectory, instanceRunId);
		this.configFileName = configFileName;
		this.cloneMapFileName = cloneMapFileName;
		this.inputDataObjects = inputDataObjects;
		this.pythonModuleNameOfModelToUse = pythonModuleNameOfModelToUse;

		//set timeStep length from model config in timeHorizon.
		this.timeStepLength = getTimeStepLengthFromIniFile(new File(caseDirectory, configFileName));
		double timeStepLengthInDays = (double) this.timeStepLength / (24.0 * 3600.0 * 1000.0);
		this.timeHorizon = new Time(timeHorizon.getBeginTime().getMJD(), timeHorizon.getEndTime().getMJD(), timeStepLengthInDays);
		//initialize number of time steps.
		long startTimeMillies = Time.mjdToMillies(this.timeHorizon.getBeginTime().getMJD());
		long endTimeMillies = Time.mjdToMillies(this.timeHorizon.getEndTime().getMJD());
		this.numberOfTimeSteps = (int) Math.ceil((double) (endTimeMillies - startTimeMillies)/(double) this.timeStepLength);

		//all WflowModelInstances refer to the same input state folder, because the wflow model
		//expects the input state files to be in that folder. In practice this is not a problem,
		//since the methods WflowModelInstance.loadPersistentState and WflowModelInstance.restoreInternalState
		//are always called in succession for one WflowModelInstance before they are called for the next WflowModelInstance.
		//So any existing input state files are overwritten and immediately read by one WflowModelInstance,
		//then overwritten and immediately read by the next WflowModelInstance.
		this.stateInputDir = new File(modelRunDir, "../instate");
		this.stateOutputDir = new File(modelRunDir, "outstate");

		//initialize model.
		initModel();

		//initialize exchangeItems.
		createModelExchangeItems();

		//initialize dataObjects.
		createOutputDataObjects(modelOutputFilePath, analysisOutputFilePath);

		//initialize data in boundary condition exchange items already for the first timeStep.
		//This cannot be done at the start of this.compute method, because this data
		//is already needed by the noiseModel before this.compute method is called.
		if (this.inputDataObjects.length > 0) {
			putInputDataInBoundaryConditionExchangeItems();
		}
	}

	private void initModel() {
		this.adapter = new WflowPythonToJavaAdapter();
		this.adapter.performPythonImports(this.pythonModuleNameOfModelToUse);
		this.adapter.createWflowModel(this.pythonModuleNameOfModelToUse, this.caseDirectory,
				this.instanceRunId, this.configFileName, this.cloneMapFileName);
		this.adapter.createWfDynamicFramework(this.pythonModuleNameOfModelToUse, this.numberOfTimeSteps);
		this.adapter.createRunId(this.pythonModuleNameOfModelToUse);
		this.adapter.runInitial();
		this.adapter.runResume();
	}

	private void createModelExchangeItems() {
		String[] variableNames = this.adapter.getVariableNames();
		int[] variableUnits = this.adapter.getVariableUnits();
		int[] variableRoles = this.adapter.getVariableRoles();
		if (variableRoles.length != variableNames.length || variableUnits.length != variableNames.length) {
			throw new RuntimeException("Lists with names, roles and units returned from model are not the same size.");
		}
		int rowCount = this.adapter.getRowCount();
		int columnCount = this.adapter.getColumnCount();
		int[] activeGridCellMask = BBUtils.toIntArray(this.adapter.getMapAsList(WFLOW_MASK_VARIABLE_NAME));

		//create one exchangeItem for each variable.
		this.modelExchangeItems.clear();
		for (int n = 0; n < variableNames.length; n++) {
			String variableName = variableNames[n];
			String variableUnit = String.valueOf(variableUnits[n]);

			int variableRole = variableRoles[n];
			//role: 0 = input (to the model)
			//      1 = is output (from the model)
			//      2 = input/output (state information)
			//      3 = model parameter
			IPrevExchangeItem.Role role;
			switch (variableRole) {
				case 0:
					//boundary condition.
					role = Role.Input;
					break;
				case 1:
					//result.
					role = Role.Output;
					break;
				case 2:
					//state.
					role = Role.InOut;
					break;
				case 3:
					//parameter.
					role = Role.InOut;
					break;
				default:
					throw new RuntimeException("Model returned invalid role integer " + variableRole + " for variable " + variableName);
			}

			//create geometryInfo.
			IArray latitudeArray = new Array(rowCount);
			IArray longitudeArray = new Array(columnCount);
			//here for the purpose of writing to Netcdf the geometryInfo only needs to contain rowCount, columnCount and activeGridCellMask.
			ArrayGeometryInfo geometryInfo = new ArrayGeometryInfo(latitudeArray, null, null, longitudeArray, null, null,
					null, null, null, activeGridCellMask);

			//create exchangeItem.
			IQuantityInfo quantityInfo = new QuantityInfo(variableName, variableUnit);
			IExchangeItem item = new Wflow2DMapStateExchangeItem(variableName, role, quantityInfo, geometryInfo, this.timeHorizon, this.adapter);
			this.modelExchangeItems.put(item.getId(), item);
		}
	}

	private void createOutputDataObjects(String modelOutputFilePath, String analysisOutputFilePath) {
		//remove existing output files.
		File modelOutputFile = new File(this.modelRunDir, modelOutputFilePath);
		if (modelOutputFile.exists()) {
			if (!modelOutputFile.delete()) {
				throw new RuntimeException("Cannot delete existing output file " + modelOutputFile.getAbsolutePath());
			}
		}
		File analysisOutputFile = new File(this.modelRunDir, analysisOutputFilePath);
		if (analysisOutputFile.exists()) {
			if (!analysisOutputFile.delete()) {
				throw new RuntimeException("Cannot delete existing output file " + analysisOutputFile.getAbsolutePath());
			}
		}

		//create output dataObjects.
		double[] modelOutputTimes = TimeUtils.getOutputTimes((Time) this.timeHorizon);
		this.modelOutputDataObject = createOutputDataObject(modelOutputFilePath, modelOutputTimes);
		//analysisOutputDataObject can use the same times as modelOutputDataObject, only
		//for the analysisOutputDataObject not all times will be filled with data.
		this.analysisOutputDataObject = createOutputDataObject(analysisOutputFilePath, modelOutputTimes);
	}

	private IDataObject createOutputDataObject(String netcdfOutputFilePath, double[] outputTimes) {
		NetcdfDataObject netcdfOutputDataObject = new NetcdfDataObject();
		netcdfOutputDataObject.initialize(this.modelRunDir, new String[]{netcdfOutputFilePath, "true", "false"});

		for (IExchangeItem item : this.modelExchangeItems.values()) {
			if (item.getRole() != Role.Output && item.getRole() != Role.InOut) {
				continue;
			}

			IExchangeItem newItem = new NetcdfGridTimeSeriesExchangeItem(item.getId(), item.getRole(),
					new TimeInfo(outputTimes), item.getQuantityInfo(), item.getGeometryInfo(), netcdfOutputDataObject, 0);
			netcdfOutputDataObject.addExchangeItem(newItem);
		}

		return netcdfOutputDataObject;
	}

	/**
	 * Initialize the configurable. Specify what its "working directory" is (usually meaning: the directory
	 * where its configuration file is), and provide its arguments.
	 *
	 * @param workingDir The directory indicating the where the configurable is started (not as 'current
	 *				   working directory', but as the root path for its configuration files etc).
	 * @param arguments The arguments needed to initialize. Typically the first argument can be a configuration
	 *				  file name string, speficied relative to the working dir.
	 */
	public void initialize(File workingDir, String[] arguments) {
		//no action needed (handled by constructor).
		//also this method is never called.
	}

	/*************************************
	 * Time information / Computing
	 *************************************/

	/**
	 * Get the computational time horizon of the model (begin and end time).
	 *
	 * @return ITime containing begin and end time.
	 */
	public ITime getTimeHorizon() {
		return this.timeHorizon;
	}

	/**
	 * Get the model instance's current simulation time stamp.
	 *
	 * @return ITime currentTime.
	 */
	public ITime getCurrentTime() {
		return this.adapter.getCurrentTime(this.timeHorizon);
	}

	/**
	 * Let the model instance compute to the requested target time stamp.
	 * This function can not be used to go back in time. Use saveInternalState and restoreInternalState for that.
	 *
	 * @param targetTime time stamp to compute to.
	 */
	public void compute(ITime targetTime) {
		//write output data after analysis (state update).
		writeAnalysisOutputData();

		//time update.
		ITime endTime = targetTime.getEndTime();
		ITime currentTime = getCurrentTime();
		while (currentTime.getMJD() < endTime.getMJD()) {
			this.currentTimeStep++;

			//compute one timeStep.
			//the call to this.adapter.runDynamic also advances the currentTime returned by method getCurrentTime.
			this.adapter.runDynamic(this.currentTimeStep, this.currentTimeStep);
			currentTime = getCurrentTime();

			//write output data after model run (time update).
			writeModelOutputData();

			//initialize data in boundary condition exchange items already for the next timeStep.
			//This cannot be done at the start of this.compute method, because this data
			//is already needed by the noiseModel before this.compute method is called.
			if (this.inputDataObjects.length > 0 && currentTime.getMJD() < endTime.getMJD()) {
				putInputDataInBoundaryConditionExchangeItems();
			}
		}
	}

	/**
	 * Get the localization vector.
	 *
	 * @param observationDescriptions
	 * @param distance characteristic distance for Cohn's formula.
	 * @return weight vector for each observation location.
	 */
	public IVector[] getObservedLocalization(IObservationDescriptions observationDescriptions, double distance) {
		throw new UnsupportedOperationException(getClass().getName() + ": getObservedLocalization not implemented.");
	}

	/*************************************
	 * Save/restore full internal state
	 *************************************/

	/**
	 * Load an internal model state from file.
	 *
	 * Ask the model to internally store the state from the given zip file (with multiple files),
	 * this can be stored in file or in memory. This does not change the model's current state,
	 * this only stores the state and returns a ModelState object that is just an identifier
	 * for the stored state. Updating the current model state can be done after this
	 * by calling the restoreInternalState method.
	 * This method is the inverse of method IModelState.savePersistentState.
	 *
	 * @param persistentStateZipFile file to read state from.
	 * @return modelState object that refers to the saved state.
	 */
	public IModelState loadPersistentState(File persistentStateZipFile) {
		//unzip the given persistentStateZipFile to the folder with input state files.
		WflowModelState persistentState = new WflowModelState(this.stateInputDir);
		persistentState.setZippedStateFile(persistentStateZipFile);
		persistentState.restoreState();
		this.stateStoredOnDisk = persistentState;
		return persistentState;
	}

	/**
	 * Restore a previously saved state of the model.
	 *
	 * Set the model instance's current state to the state identified by the given ModelState object.
	 *
	 * @param savedInternalState handle to a (previously saved) state to be restored.
	 */
	public void restoreInternalState(IModelState savedInternalState) {
		if (savedInternalState != this.stateStoredOnDisk) {
			throw new IllegalStateException("Requested state does not exist anymore. A wflow model instance can only store one ModelState at a time.");
		}
		//load state from disk.
		this.adapter.runResume();
	}

	/**
	 * Save the current state of the model to disk.
	 *
	 * Ask the model to store its current state (either to file or in memory) for future reference
	 * and return a ModelState object that is just an identifier for the stored state.
	 *
	 * @return modelState object that refers to the saved state.
	 */
	public IModelState saveInternalState() {
		//save state to disk.
		this.adapter.runSuspend();
		//create a FileBasedModelState object that refers to the folder with output state files.
		WflowModelState persistentState = new WflowModelState(this.stateOutputDir);
		this.stateStoredOnDisk = persistentState;
		return persistentState;
	}

	private class WflowModelState extends FileBasedModelState {
		/**
		 * @param stateFilesDirectory the directory that contains the model state files.
		 */
		public WflowModelState(File stateFilesDirectory) {
			super(stateFilesDirectory);
		}

		/**
		 * Write the algorithm state to file.
		 *
		 * Ask the model to save the state identified by this ModelState object to the given file.
		 * If the state consists of multiple files, these can be zipped to collect them in a single file.
		 *
		 * @param savedStateFile the file to which this state has to be saved.
		 */
		public void savePersistentState(File savedStateFile) {
			if (this != stateStoredOnDisk) {
				throw new IllegalStateException("This state does not exist anymore. A wflow model instance can only store one ModelState at a time.");
			}
			super.savePersistentState(savedStateFile);
		}
	}

	/**
	 * Release resources used to save a state at some earlier time.
	 *
	 * Ask the model to delete the file/memory of the state identified by the given ModelState object.
	 *
	 * @param savedInternalState handle to the (previously saved) state to be released.
	 */
	public void releaseInternalState(IModelState savedInternalState) {
		//do nothing. Only one state stored at a time, so no need to release resources.
	}

	/**
	 * Returns the directory where this model instance runs.
	 *
	 * @return model instance run directory with result files.
	 */
	public File getModelRunDir() {
		return modelRunDir;
	}

	/*************************************
	 * Exchange items
	 *************************************/

	/**
	 * Returns the ids of the exchange items for this model instance.
	 *
	 * @return exchangeItemIds.
	 */
	public String[] getExchangeItemIDs() {
		Set<String> ids = new HashSet<String>();
		ids.addAll(this.modelExchangeItems.keySet());
		return ids.toArray(new String[ids.size()]);
	}

	/**
	 * Returns the ids of the exchange items for this model instance with the given role.
	 *
	 * @param role Input, Output, or InOut.
	 * @return exchangeItemIds.
	 */
	public String[] getExchangeItemIDs(IPrevExchangeItem.Role role) {
		List<String> ids = new ArrayList<String>();
		for (IExchangeItem exchangeItem : this.modelExchangeItems.values()) {
			if (exchangeItem.getRole() == role) {
				ids.add(exchangeItem.getId());
			}
		}
		return ids.toArray(new String[ids.size()]);
	}

	/**
	 * Returns the exchange item with the given exchangeItemId, if it exists.
	 *
	 * @param exchangeItemId
	 * @return IExchangeItem.
	 */
	public IExchangeItem getDataObjectExchangeItem(String exchangeItemId) {
		IExchangeItem exchangeItem = this.modelExchangeItems.get(exchangeItemId);
		if (exchangeItem == null) {
			throw new RuntimeException("Exchange item with id '" + exchangeItemId + "' not found in " + getClass().getSimpleName());
		}
		return exchangeItem;
	}

	/**
	 * Returns the exchange item with the given exchangeItemId, if it exists.
	 *
	 * @param exchangeItemId
	 * @return IPrevExchangeItem.
	 */
	//TODO this method is only present for backwards compatibility. This should be replaced by the IDataObject.getDataObjectExchangeItem
	//method once all ioObjects and exchange items have been migrated to the new IDataObject/IExchangeItem approach. AK
	@Deprecated
	public IPrevExchangeItem getExchangeItem(String exchangeItemId) {
		//delegate to new getDataObjectExchangeItem method.
		return getDataObjectExchangeItem(exchangeItemId);
	}

	/**
	 * Read the timeStepLength from the given configFile. If not configured, then the default (1 day) is used.
	 *
	 * @param configFile
	 * @return timeStepLength in milliseconds.
	 */
	private long getTimeStepLengthFromIniFile(File configFile) {
		if (!configFile.exists()) {
			throw new RuntimeException("config file " + configFile.getAbsolutePath() + " does not exist.");
		}

		ArrayList<String> content = readFile(configFile);
		for (String line : content) {
			if (line.toLowerCase().contains("timestepsecs")) {
				int indexOfEqualsSign = line.indexOf('=');
				if (indexOfEqualsSign == -1) {
					continue;
				}

				String timeStepLengthString = line.substring(indexOfEqualsSign + 1).trim();
				int timeStepLength;
				try {
					timeStepLength = Integer.parseInt(timeStepLengthString);
				} catch (NumberFormatException e) {
					throw new RuntimeException("Invalid value '" + timeStepLengthString
							+ "' configured for timestepsecs in config file " + configFile.getAbsolutePath(), e);
				}

				return timeStepLength * 1000;
			}
		}

		//if not configured, then return 1 day by default.
		return 24*3600*1000;
	}

	/**
	 * Read content of the given file and store it in a list with Strings.
	 *
	 * @param inputFile
	 * @return ArrayList<String>
	 */
	private static ArrayList<String> readFile(File inputFile) {
		ArrayList<String> content = new ArrayList<String>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			try {
				String line = reader.readLine();
				while (line != null) {
					content.add(line);
					line = reader.readLine();
				}
			} finally {
			   reader.close();
			}
		} catch (IOException e){
			throw new RuntimeException("Problem while reading file '" + inputFile.getAbsolutePath() + "'.", e);
		}

		return content;
	}

	/**
	 * Set input data for the current time in the model input exchange items.
	 */
	private void putInputDataInBoundaryConditionExchangeItems() {
		Results.putMessage(getClass().getSimpleName() + ": setting model input data for instance " + this.instanceRunId);

		String[] inputExchangeItemIds = getExchangeItemIDs(Role.Input);
		DataCopier.copyDataFromDataObjectsToDataObject(inputExchangeItemIds, this.inputDataObjects, this);
	}

	/**
	 * Write the current output data to the output exchange items.
	 */
	private void writeModelOutputData() {
		Results.putMessage(getClass().getSimpleName() + ": writing model output data for time "
				+ new Date(Time.mjdToMillies(getCurrentTime().getMJD())) + " for instance " + this.instanceRunId);

		String[] outputExchangeItemIds = this.modelOutputDataObject.getExchangeItemIDs();
		DataCopier.copyDataFromDataObjectsToDataObject(outputExchangeItemIds, new IDataObject[]{this}, this.modelOutputDataObject);
	}

	/**
	 * Write the current output data to the output exchange items.
	 */
	private void writeAnalysisOutputData() {
		Results.putMessage(getClass().getSimpleName() + ": writing analysis output data for time "
				+ new Date(Time.mjdToMillies(getCurrentTime().getMJD())) + " for instance " + this.instanceRunId);

		String[] outputExchangeItemIds = this.analysisOutputDataObject.getExchangeItemIDs();
		DataCopier.copyDataFromDataObjectsToDataObject(outputExchangeItemIds, new IDataObject[]{this}, this.analysisOutputDataObject);
	}

	/**
	 * Writes the final model state to disk.
	 */
	public void finish() {
		//write output data after last analysis (state update).
		writeAnalysisOutputData();

		this.modelExchangeItems.clear();
		this.modelOutputDataObject.finish();
		this.analysisOutputDataObject.finish();
		for (IDataObject inputDataObject : this.inputDataObjects) {
			inputDataObject.finish();
		}

		this.adapter.runSuspend();
		this.adapter.close();
	}
}
