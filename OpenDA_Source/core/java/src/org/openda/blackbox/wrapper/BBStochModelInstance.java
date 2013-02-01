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


package org.openda.blackbox.wrapper;

import org.openda.blackbox.config.*;
import org.openda.blackbox.interfaces.SelectorInterface;
import org.openda.exchange.ArrayGeometryInfo;
import org.openda.exchange.timeseries.TimeUtils;
import org.openda.interfaces.*;
import org.openda.uncertainties.UncertaintyEngine;
import org.openda.utils.*;
import org.openda.utils.Vector;
import org.openda.utils.io.FileBasedModelState;
import org.openda.utils.performance.OdaGlobSettings;
import org.openda.utils.performance.OdaTiming;

import java.io.File;
import java.util.*;

/**
 * Black box module's implementation of a stochastic model instance
 */
public class BBStochModelInstance extends Instance implements IStochModelInstance {

	// In case of parallel runs we use the Distributed counter to generate unique IDs
	static DistributedCounter lastGlobInstanceNr = new DistributedCounter();
	int InstanceNr;
	String ModelID;
	OdaTiming timerAxpyState  = null;
	OdaTiming timerCompute    = null;
	OdaTiming timerGetObs     = null;
	OdaTiming timerGetState   = null;




	private File configRootDir;
	protected IModelInstance model;
	protected BBStochModelVectorsConfig bbStochModelVectorsConfig;
	protected LinkedHashMap<String, SelectorInterface> selectors;
	protected LinkedHashMap<BBNoiseModelConfig, IStochModelInstance> noiseModels;
	protected LinkedHashMap<BBUncertOrArmaNoiseConfig, ArmaNoiseModel> armaNoiseModels;
	protected int[] stateNoiseModelsEndIndices = null;
	protected int[] stateVectorsEndIndices = null;
	private boolean doAutomaticNoiseGeneration = false;
	private String savedStatesDirPrefix = null;
	private String savedStatesNoiseModelPrefix = null;
	private String modelSavedStateFile = null;
	private UncertaintyEngine uncertaintyEngine = null;
	private IStochVector parameterUncertainty;
	private ITreeVector paramsTreeVector;
	private HashMap<String,Double> lastNoiseTimes;//avoid adding noise more than once

	public BBStochModelInstance(File configRootDir, IModelInstance model,
			UncertaintyEngine uncertaintyEngine,
			IStochVector parameterUncertainty,
			ITreeVector paramsTreeVector,
			LinkedHashMap<BBNoiseModelConfig, IStochModelInstance> noiseModels,
			BBStochModelVectorsConfig bbStochModelVectorsConfig,
			String savedStatesDirPrefix,
			String savedStatesNoiseModelPrefix,
			String modelSavedStateFile
			) {
		/* Set modelID and instance Nr */
		synchronized(lastGlobInstanceNr){
			lastGlobInstanceNr.inc();
			InstanceNr = lastGlobInstanceNr.val();
			ModelID="BB Model:"+InstanceNr;
		}

		this.configRootDir = configRootDir;
		this.model = model;
		this.uncertaintyEngine = uncertaintyEngine;
		this.parameterUncertainty = parameterUncertainty;
		this.paramsTreeVector = paramsTreeVector;
		this.noiseModels = noiseModels;
		this.bbStochModelVectorsConfig = bbStochModelVectorsConfig;
		this.savedStatesDirPrefix = savedStatesDirPrefix;
		this.savedStatesNoiseModelPrefix = savedStatesNoiseModelPrefix;
		this.modelSavedStateFile = modelSavedStateFile;
		selectors = new LinkedHashMap<String, SelectorInterface>();
		armaNoiseModels = new LinkedHashMap<BBUncertOrArmaNoiseConfig, ArmaNoiseModel>();
		createStateNoiseModels();
        
		this.lastNoiseTimes=new HashMap<String,Double>(); //avoid adding noise more than once
	}

	//
	// ModelInstance Functions
	//

	public ITime getTimeHorizon() {
		return model.getTimeHorizon();
	}

	public ITime getCurrentTime() {
		return model.getCurrentTime();
	}

	public void compute(ITime targetTime) {
		if ( timerCompute == null){
			timerCompute = new OdaTiming(ModelID);
		}
		System.out.println("Compute from "+Thread.currentThread().getStackTrace()[1].getClassName()+":"+Thread.currentThread().getStackTrace()[2].getClassName());
		timerCompute.start();


		//if (doAutomaticNoiseGeneration) {
			propagateNoiseModelsAndAddNoiseToExchangeItems(model.getCurrentTime(), targetTime);
		//}
		model.compute(targetTime);

		timerCompute.stop();
	}

	public String[] getExchangeItemIDs() {
		return model.getExchangeItemIDs();
	}

	public String[] getExchangeItemIDs(IPrevExchangeItem.Role role) {
		return model.getExchangeItemIDs(role);
	}

	public IExchangeItem getDataObjectExchangeItem(String exchangeItemID) {
		Object exchangeItemObject = model.getDataObjectExchangeItem(exchangeItemID);
		if (!(exchangeItemObject instanceof IExchangeItem)) {
			throw new RuntimeException("Model " + model.getClass().toString() + " does not support IBaseExchangeItems");
		}
		return (IExchangeItem) exchangeItemObject;
	}

	public IPrevExchangeItem getExchangeItem(String exchangeItemID) {
		return model.getExchangeItem(exchangeItemID);
	}

	public ITreeVector getState() {

		if ( timerGetState == null){
			timerGetState = new OdaTiming(ModelID);
		}
		timerGetState.start();

		TreeVector stateTreeVector = new TreeVector("state", "State From Black Box Stoch Model Instance");

		Collection<BBNoiseModelConfig> noiseModelConfigs =
				this.bbStochModelVectorsConfig.getStateConfig().getNoiseModelConfigs();
		Collection<BBUncertOrArmaNoiseConfig> uncertaintyOrArmaNoiseConfigs =
				this.bbStochModelVectorsConfig.getStateConfig().getUncertaintyOrArmaNoiseConfigs();

		stateNoiseModelsEndIndices = new int[noiseModelConfigs.size()+uncertaintyOrArmaNoiseConfigs.size()];

		int i = 0;

		// add external noise model variables to state
		for (BBNoiseModelConfig noiseModelConfig : noiseModelConfigs) {
			IStochModelInstance noiseModel = noiseModels.get(noiseModelConfig);
			IVector noiseModelState = noiseModel.getState();
			//add this part to state
			if(noiseModelState instanceof ITreeVector){
				stateTreeVector.addChild((ITreeVector)noiseModelState);
			}else{
				String id = "noise_part_"+i;
				ITreeVector tv = new TreeVector(id, noiseModelState);
				stateTreeVector.addChild(tv);
			}
			//keep count of lengths for later use
			stateNoiseModelsEndIndices[i] = noiseModelState.getSize();
			if (i > 0) {
				stateNoiseModelsEndIndices[i] += stateNoiseModelsEndIndices[i - 1];
			}
			i++;
		}

		// add blackbox internal noise model contributions to the state vector
		ITime currentTime = getCurrentTime();
		for (BBUncertOrArmaNoiseConfig noiseModelStateNoiseConfig : uncertaintyOrArmaNoiseConfigs) {
			ArmaNoiseModel noiseModel = armaNoiseModels.get(noiseModelStateNoiseConfig);
			if (noiseModelStateNoiseConfig.getNoiseModelType() !=
					BBUncertOrArmaNoiseConfig.NoiseModelType.UncertainItem) {
				double[] noiseStateVector = noiseModel.getNoiseStateVector(currentTime);
				stateTreeVector.addChild(noiseModelStateNoiseConfig.getId(), noiseStateVector);

				stateNoiseModelsEndIndices[i] = noiseStateVector.length;
				if (i > 0) {
					stateNoiseModelsEndIndices[i] += stateNoiseModelsEndIndices[i - 1];
				}
			}
			i++;
		}

		// add deterministic mode variables to state vector
		Collection<BBStochModelVectorConfig> vectorCollection =
				this.bbStochModelVectorsConfig.getStateConfig().getVectorCollection();

		stateVectorsEndIndices = new int[vectorCollection.size()];
		i = 0;
		for (BBStochModelVectorConfig vectorConfig : vectorCollection) {
			double[] values = getExchangeItem(vectorConfig.getId()).getValuesAsDoubles();
			stateTreeVector.addChild(vectorConfig.getId(), values);

			stateVectorsEndIndices[i] = values.length;
			if (i > 0) {
				stateVectorsEndIndices[i] += stateVectorsEndIndices[i - 1];
			}
			i++;
		}
		timerGetState.stop();
		return stateTreeVector;
	}

	public void axpyOnState(double alpha, IVector vector) {

		//TODO If vector is a treevector we can look at the children 
		//without asking for values. If not we can ask for the state and
		//overwrite the values. Also then we can look at the children.
		
		
		if ( timerAxpyState == null){
			timerAxpyState = new OdaTiming(ModelID);
		}
		timerAxpyState.start();

		if (stateVectorsEndIndices == null) {
			// no state sizes known, get state first
			getState();
		}

		double[] axpyValues = vector.getValues();

		int i = 0;
		int j = 0;

		Collection<BBNoiseModelConfig> noiseModelConfigs =
				this.bbStochModelVectorsConfig.getStateConfig().getNoiseModelConfigs();
		for (BBNoiseModelConfig noiseModelConfig : noiseModelConfigs) {
			IStochModelInstance noiseModel = noiseModels.get(noiseModelConfig);
			IVector noiseModelState = noiseModel.getState();
			double[] partOfAxpyValues = new double[noiseModelState.getSize()];
			System.arraycopy(axpyValues, j, partOfAxpyValues, 0, partOfAxpyValues.length);
			j+=partOfAxpyValues.length;
			IVector partOfAxpyValuesVector = new Vector(partOfAxpyValues);
			noiseModel.axpyOnState(alpha, partOfAxpyValuesVector);
		}


		ITime currentTime = getCurrentTime();

		Collection<BBUncertOrArmaNoiseConfig> stateNoiseModelCollection =
				this.bbStochModelVectorsConfig.getStateConfig().getUncertaintyOrArmaNoiseConfigs();
		for (BBUncertOrArmaNoiseConfig stateNoiseModelConfig : stateNoiseModelCollection) {
			if (stateNoiseModelConfig.getNoiseModelType() !=
					BBUncertOrArmaNoiseConfig.NoiseModelType.UncertainItem) {
				ArmaNoiseModel noiseModel = armaNoiseModels.get(stateNoiseModelConfig);
				if (noiseModel == null) {
					throw new RuntimeException("Noise model not created: " + stateNoiseModelConfig.getId());
				}
				double[] noiseStateVector = noiseModel.getNoiseStateVector(currentTime);
				for (int k = 0; k < noiseStateVector.length; k++) {
					noiseStateVector[k] += alpha * axpyValues[j++];
				}
				if (!(j == stateNoiseModelsEndIndices[i])) {
					throw new RuntimeException("Inconsistent noise model sizes: " + stateNoiseModelConfig.getId());
				}
				noiseModel.setNoiseStateVector(currentTime, noiseStateVector);
			}
			i++;
		}

		Collection<BBStochModelVectorConfig> vectorCollection =
				this.bbStochModelVectorsConfig.getStateConfig().getVectorCollection();
		i = 0;
		for (BBStochModelVectorConfig vectorConfig : vectorCollection) {
			int start = (i > 0) ? stateVectorsEndIndices[i - 1] : 0;
			int subSize = stateVectorsEndIndices[i] - start;
			double[] values = new double[subSize];
			System.arraycopy(axpyValues, j + start, values, 0, subSize);
			getExchangeItem(vectorConfig.getId()).axpyOnValues(alpha, values);
			i++;
		}
		timerAxpyState.stop();
	}

	public ITreeVector getParameters() {
		return paramsTreeVector;
	}

	public void setParameters(IVector parameters) {

		if (model instanceof BBModelInstance) {
			if (!((BBModelInstance) model).isNewDirectory()) return;
		}

		if (!(parameters instanceof ITreeVector)) {
			throw new RuntimeException("BBStochModelInstance.setParameters(): unexpected vector type: " +
					parameters.getClass().getName());
		}

		for (BBRegularisationConstantConfig regularisationConstantConfig : bbStochModelVectorsConfig.getRegularisationConstantCollection()) {
			String parameterId = BBStochModelFactory.composeRelatedParametersId(regularisationConstantConfig);
			IVector paramChild = getAndCheckParamChild(parameters, parameterId);
			double parameterDelta = paramChild.getValue(0) * regularisationConstantConfig.getScale();

			for (BBStochModelVectorConfig stochModelVectorConfig : regularisationConstantConfig.getVectorConfigs()) {
				IPrevExchangeItem sourceExchangeItem = model.getExchangeItem(stochModelVectorConfig.getSourceId());
				if (sourceExchangeItem == null) {
					throw new RuntimeException("BBStochModelInstance.setParameters(): parameter not found: " +
							stochModelVectorConfig.getSourceId());
				}
				IPrevExchangeItem exchangeItem = new BBExchangeItem(stochModelVectorConfig.getId(), stochModelVectorConfig,
						sourceExchangeItem, selectors, configRootDir);
				addParameterDeltaToExchangeItem(parameterDelta, exchangeItem,
						regularisationConstantConfig.getTransformation());
			}
		}

		for (BBCartesianToPolarConfig cartesianToPolarConfig : bbStochModelVectorsConfig.getCartesianToPolarCollection()) {

			String dxID = BBStochModelFactory.composeCartesionToPolarParameterId(cartesianToPolarConfig, false);
			String dyID = BBStochModelFactory.composeCartesionToPolarParameterId(cartesianToPolarConfig, true);
			IVector paramChildDeltaX = getAndCheckParamChild(parameters, dxID);
			IVector paramChildDeltaY = getAndCheckParamChild(parameters, dyID);

			double deltaX = paramChildDeltaX.getValue(0) * cartesianToPolarConfig.getXScale();
			double deltaY = paramChildDeltaY.getValue(0) * cartesianToPolarConfig.getYScale();

			int vectorConfigNr = 0;
			while (vectorConfigNr < cartesianToPolarConfig.getVectorConfigs().size()) {
				IPrevExchangeItem radiusExchangeItem = model.getExchangeItem(
						cartesianToPolarConfig.getVectorConfigs().get(vectorConfigNr).getSourceId());
				if (radiusExchangeItem == null) {
					throw new RuntimeException("BBStochModelInstance.setParameters(): radius parameter not found: " +
							cartesianToPolarConfig.getVectorConfigs().get(vectorConfigNr).getSourceId());
				}
				IPrevExchangeItem angleExchangeItem = model.getExchangeItem(
						cartesianToPolarConfig.getVectorConfigs().get(vectorConfigNr + 1).getSourceId());
				if (angleExchangeItem == null) {
					throw new RuntimeException("BBStochModelInstance.setParameters(): angle parameter not found: " +
							cartesianToPolarConfig.getVectorConfigs().get(vectorConfigNr + 1).getSourceId());
				}

				double radius = radiusExchangeItem.getValuesAsDoubles()[0];
				double angle = angleExchangeItem.getValuesAsDoubles()[0];
				double angleInRadians = angle * Math.PI / 180d;

				if (Double.compare(radius, 0d) == 0) {
					throw new RuntimeException("BBStochModelInstance.setParameters(): " +
							"cartesian to polar transformation allowed for radius == 0: " +
							cartesianToPolarConfig.getVectorConfigs().get(vectorConfigNr).getSourceId());
				}

				double xCoord = Math.cos(angleInRadians) * radius;
				double yCoord = Math.sin(angleInRadians) * radius;

				double adjustedXCoord = xCoord + deltaX;
				double adjustedYCoord = yCoord + deltaY;

				double adjustedRadius = Math.sqrt(Math.pow(adjustedXCoord, 2) + Math.pow(adjustedYCoord, 2));
				double adjustedAngleInRadians = Math.atan2(adjustedYCoord, adjustedXCoord);
				if (adjustedAngleInRadians < 0) adjustedAngleInRadians += 2 * Math.PI;
				double adjustedAngle = adjustedAngleInRadians * 180d / Math.PI;

				radiusExchangeItem.setValuesAsDoubles(new double[]{adjustedRadius});
				angleExchangeItem.setValuesAsDoubles(new double[]{adjustedAngle});

				vectorConfigNr += 2;
			}
		}

		for (BBNoiseModelConfig noiseConfig : bbStochModelVectorsConfig.getParamsUncertaintyModelConfigs()) {
			for (NoiseModelExchangeItemConfig exchangeItemConfig : noiseConfig.getExchangeItemConfigs()) {
				IVector paramChild = getAndCheckParamChild(parameters, exchangeItemConfig.getId());
				for (String modelExchangeItemId : exchangeItemConfig.getModelExchangeItemIds()) {
					IPrevExchangeItem exchangeItem = model.getExchangeItem(modelExchangeItemId);
					if (exchangeItem == null) {
						throw new RuntimeException("BBStochModelInstance.setParameters(): parameter not found: " +
								modelExchangeItemId);
					}
					addParameterDeltaToExchangeItem(paramChild.getValue(0), exchangeItem,
							exchangeItemConfig.getTransformation());
				}
			}
		}
	}

	public void axpyOnParameters(double alpha, IVector vector) {
		IVector parameters = getParameters();
		parameters.axpy(alpha, vector);
		setParameters(parameters);
	}

	public IModelState saveInternalState() {
		File dirForRestartFiles = checkRestartDir(getCurrentTime(), false);
		FileBasedModelState stochSavedModelState = new FileBasedModelState();
		stochSavedModelState.setDirContainingModelstateFiles(dirForRestartFiles);
		int i = 0;
		for (Map.Entry<BBUncertOrArmaNoiseConfig, ArmaNoiseModel> noiseModelEntry : this.armaNoiseModels.entrySet()) {
			ArmaNoiseModel noiseModel = noiseModelEntry.getValue();
			File noiseModelStateFile = new File(dirForRestartFiles, this.savedStatesNoiseModelPrefix + i + ".txt");
			noiseModel.saveState(getCurrentTime(), noiseModelStateFile);
			stochSavedModelState.addFile(noiseModelStateFile);
			i++;
		}
		for (Map.Entry<BBNoiseModelConfig, IStochModelInstance> noiseModelEntry : noiseModels.entrySet()) {
			IStochModelInstance noiseModel = noiseModelEntry.getValue();
			File noiseModelStateFile = new File(dirForRestartFiles, this.savedStatesNoiseModelPrefix + i + ".txt");
			IModelState noiseModelState = noiseModel.saveInternalState();
			noiseModelState.savePersistentState(noiseModelStateFile);
			stochSavedModelState.addFile(noiseModelStateFile);
			i++;
		}
		IModelState savedModelState = model.saveInternalState();
		File modelRestartStateFile = new File(dirForRestartFiles, this.modelSavedStateFile);
		savedModelState.savePersistentState(modelRestartStateFile);
		model.releaseInternalState(savedModelState);
		stochSavedModelState.addFile(modelRestartStateFile);
		return stochSavedModelState;
	}

	public void restoreInternalState(IModelState savedInternalState) {
		if (!(savedInternalState instanceof FileBasedModelState)) {
			throw new IllegalArgumentException("Unknown state type (" + savedInternalState.getClass().getName() +
					" for " + this.getClass().getName() + ".releaseInternalState");
		}
		FileBasedModelState stochModelState = (FileBasedModelState) savedInternalState;
		File dirForRestartFiles = checkRestartDir(getCurrentTime(), false);
		stochModelState.setDirContainingModelstateFiles(dirForRestartFiles);
		stochModelState.restoreState();
		int i = 0;
		for (Map.Entry<BBUncertOrArmaNoiseConfig, ArmaNoiseModel> noiseModelEntry : this.armaNoiseModels.entrySet()) {
			ArmaNoiseModel noiseModel = noiseModelEntry.getValue();
			File noiseModelStateFile = new File(dirForRestartFiles, this.savedStatesNoiseModelPrefix + i + ".txt");
			noiseModel.loadState(noiseModelStateFile);
			i++;
		}
		for (Map.Entry<BBNoiseModelConfig, IStochModelInstance> noiseModelEntry : this.noiseModels.entrySet()) {
			IStochModelInstance noiseModel = noiseModelEntry.getValue();
			File noiseModelStateFile = new File(dirForRestartFiles, this.savedStatesNoiseModelPrefix + i + ".txt");
			IModelState noiseModelState = noiseModel.loadPersistentState(noiseModelStateFile);
			noiseModel.restoreInternalState(noiseModelState);
			i++;
		}
		File modelRestartStateFile = new File(dirForRestartFiles, this.modelSavedStateFile);
		model.restoreInternalState(model.loadPersistentState(modelRestartStateFile));
	}

	public void releaseInternalState(IModelState savedInternalState) {
		if (!(savedInternalState instanceof FileBasedModelState)) {
			throw new IllegalArgumentException("Unknown state type (" + savedInternalState.getClass().getName() +
					" for " + this.getClass().getName() + ".releaseInternalState");
		}
		File dirForRestartFiles = checkRestartDir(getCurrentTime(), false);
		FileBasedModelState modelState = (FileBasedModelState) savedInternalState;
		modelState.releaseState(dirForRestartFiles);
	}

	public IModelState loadPersistentState(File persistentStateFile) {
		File dirForRestartFiles = checkRestartDir(getCurrentTime(), false);
		return FileBasedModelState.loadPersistenState(persistentStateFile, dirForRestartFiles);
	}

	//
	// StochModelInstance Functions
	//

	public IStochVector getStateUncertainty() {

		if (stateVectorsEndIndices == null) {
			// no state sizes known, get state first
			getState();
		}

		// state size is equal to the last sub index
		int fullStateSize = stateNoiseModelsEndIndices[stateNoiseModelsEndIndices.length - 1] +
				stateVectorsEndIndices[stateVectorsEndIndices.length - 1];

		Collection<BBUncertOrArmaNoiseConfig> stateNoiseModelCollection =
				this.bbStochModelVectorsConfig.getStateConfig().getUncertaintyOrArmaNoiseConfigs();
		ArrayList<double[]> stdDevList = new ArrayList<double[]>();
		for (BBUncertOrArmaNoiseConfig stateNoiseModelConfig : stateNoiseModelCollection) {
			int noiseStateSize = 1; // TODO elaborate
			double[] stdDevs = new double[noiseStateSize];
			for (int i = 0; i < stdDevs.length; i++) {
				stdDevs[i] = stateNoiseModelConfig.getStdDev();
			}
			stdDevList.add(stdDevs);
		}
		double[] means = new double[fullStateSize];  // value = 0
		double[] stdDevs = new double[fullStateSize];
		int copyPos = 0;
		for (double[] boundaryStdDevs : stdDevList) {
			int count = boundaryStdDevs.length;
			System.arraycopy(boundaryStdDevs, 0, stdDevs, copyPos, count);
			copyPos += count;
		}
		return new StochVector(means, stdDevs);
	}


	public IStochVector getParameterUncertainty() {
		return parameterUncertainty;
	}

	public IStochVector[] getWhiteNoiseUncertainty(ITime time) {
		throw new UnsupportedOperationException("org.openda.blackbox.wrapper.BBStochModelInstance.getWhiteNoiseUncertainty(): Not implemented yet.");
	}

	public boolean isWhiteNoiseStationary() {
		throw new UnsupportedOperationException("org.openda.blackbox.wrapper.BBStochModelInstance.isWhiteNoiseStationary(): Not implemented yet.");
	}

	public ITime[] getWhiteNoiseTimes(ITime timeSpan) {
		throw new UnsupportedOperationException("org.openda.blackbox.wrapper.BBStochModelInstance.getWhiteNoiseTimes(): Not implemented yet.");
	}

	public IVector[] getWhiteNoise(ITime timeSpan) {
		throw new UnsupportedOperationException("org.openda.blackbox.wrapper.BBStochModelInstance.getWhiteNoise(): Not implemented yet.");
	}

	public void setWhiteNoise(IVector whiteNoise[]) {
		throw new UnsupportedOperationException("org.openda.blackbox.wrapper.BBStochModelInstance.setWhiteNoise(): Not implemented yet.");
	}

	public void axpyOnWhiteNoise(double alpha, IVector vector[]) {
		throw new UnsupportedOperationException("org.openda.blackbox.wrapper.BBStochModelInstance.axpyOnWhiteNoise(): Not implemented yet.");
	}

	public void setAutomaticNoiseGeneration(boolean value) {
		this.doAutomaticNoiseGeneration = value;
		for (IStochModelInstance noiseModel : noiseModels.values()) {
			noiseModel.setAutomaticNoiseGeneration(value);
		}
	}

	public IVector getObservedValues(IObservationDescriptions observationDescriptions) {
		if ( timerGetObs == null){
			timerGetObs = new OdaTiming(ModelID);
		}
		timerGetObs.start();

		TreeVector treeVector = new TreeVector("predictions");
		String errorMessage = "";
		for (IPrevExchangeItem observationExchangeItem : observationDescriptions.getExchangeItems()) {
			BBStochModelVectorConfig vectorConfig = findPredictionVectorConfig(observationExchangeItem.getId());
			IPrevExchangeItem sourceExchangeItem = model.getExchangeItem(vectorConfig.getSourceId());
			if (sourceExchangeItem == null) {
				errorMessage += "\n\tExchange item not found: "
						+ vectorConfig.getSourceId();
			}
			IPrevExchangeItem mappedExchangeItem = new BBExchangeItem(vectorConfig.getId(), vectorConfig,
					sourceExchangeItem, selectors, configRootDir);
			double[] computedValues = mappedExchangeItem.getValuesAsDoubles();

			ITreeVector treeVectorLeaf;
			double[] observationTimes = observationExchangeItem.getTimes();
			if (observationTimes != null) {
				double[] computedTimes = mappedExchangeItem.getTimes();
				if (computedTimes != null) {
					if (computedTimes.length != computedValues.length) {
						errorMessage += "\n\tInconsistency in #times (" +
								computedTimes.length + ") and #values (" + computedValues.length + ") for" +
								vectorConfig.getId();
					}
					double[] sortedValues = computedValues;
					if (computedTimes.length != observationTimes.length) {
						double tolerance = 1d / 24d / 60d / 2; // half a minute (expressed as MJD)
						int[][] indices = SortUtils.mergeDoubleIndex(observationTimes, computedTimes, SortUtils.MergeType.left, tolerance);
						int[] observedIndices = indices[0];
						int[] computedIndices = indices[1];
						String missingTimeStepsString = "";
						for (int i = 0; i < computedIndices.length; i++) {
							if (computedIndices[i] == -1) {
								missingTimeStepsString += ", " + TimeUtils.mjdToString(observationTimes[observedIndices[i]]);
							}
						}
						if (missingTimeStepsString.length() > 0) {
							errorMessage += "\n\tNo computed values XX available for " +
									vectorConfig.getId() + missingTimeStepsString;
							continue;
						}
						sortedValues = SortUtils.applyIndexToDoubles(computedValues, computedIndices, -999);
					}
					treeVectorLeaf = new TreeVector(mappedExchangeItem.getId(), new Vector(sortedValues));
				} else {
					errorMessage += "\n\tNo times defined for " + vectorConfig.getSourceId();
					continue;
				}
			} else {
				treeVectorLeaf = new TreeVector(mappedExchangeItem.getId(), new Vector(computedValues));
			}
			treeVector.addChild(treeVectorLeaf);
		}
		if (errorMessage.length() > 0) {
			throw new RuntimeException("Error(s) in getting observed values from black box model" + errorMessage);
		}
		timerGetObs.stop();
		return treeVector;
	}

	public IVector[] getObservedLocalization(IObservationDescriptions observationDescriptions, double distance) {
		int startOfModelState = stateNoiseModelsEndIndices[stateNoiseModelsEndIndices.length - 1];
		IVector[] modelObservedLocalization = model.getObservedLocalization(observationDescriptions, distance);
		int modelStateSize = modelObservedLocalization[0].getSize();
		IVector[] stochModelobservedLocalization = new IVector[modelObservedLocalization.length];
		for (int i = 0; i < stochModelobservedLocalization.length; i++) {
			double[] obsLocalizationValues = new double[startOfModelState + modelStateSize];
			for (int j = 0; j < modelStateSize; j++) {
				obsLocalizationValues[j + startOfModelState] =
						modelObservedLocalization[i].getValue(j);
			}
			stochModelobservedLocalization[i] = new Vector(obsLocalizationValues);
		}
		return stochModelobservedLocalization;
	}

	public void announceObservedValues(IObservationDescriptions observationDescriptions) {
		// No action
	}

	public IVector getStateScaling() {
		throw new UnsupportedOperationException("org.openda.blackbox.wrapper.BBStochModelInstance.getStateScaling(): Not implemented yet.");
	}

	public IVector[] getStateScaling(IObservationDescriptions observationDescriptions) {
		throw new UnsupportedOperationException("org.openda.blackbox.wrapper.BBStochModelInstance.getStateScaling(): Not implemented yet.");
	}

	public File getModelRunDir() {
		return model.getModelRunDir();
	}

	public void finish() {
		for (IStochModelInstance noiseModel : noiseModels.values()) {
			noiseModel.finish();
		}
		for (ArmaNoiseModel armaNoiseModel : armaNoiseModels.values()) {
			armaNoiseModel.finish();
		}
		model.finish();
	}

	public void initialize(File workingDir, String[] arguments) {
		// no action needed (handled by model factory)
	}

	public IModelInstance getModel() {
		return model;
	}

	public String toString() {
		return "BBStochModelInstance(" + model.toString() + ")";
	}

	private void createStateNoiseModels() {
		if (this.bbStochModelVectorsConfig.getStateConfig() != null) {
			for (BBUncertOrArmaNoiseConfig noiseModelStateNoiseConfig :
				this.bbStochModelVectorsConfig.getStateConfig().getUncertaintyOrArmaNoiseConfigs()) {
				ArmaNoiseModel noiseModel = armaNoiseModels.get(noiseModelStateNoiseConfig);
				if (noiseModel == null) {
					int noiseModelStateSize = 1; // TODO elaborate
					boolean doNoiseModelLogging = true; // TODO read from config
					if (noiseModelStateNoiseConfig.getNoiseModelType() == BBUncertOrArmaNoiseConfig.NoiseModelType.ArmaModel) {
						boolean useRandomSeed = false; // TODO read from config
						noiseModel = new ArmaNoiseModel(this.model.getModelRunDir(), noiseModelStateSize,
								noiseModelStateNoiseConfig.getArmaConstants(), noiseModelStateNoiseConfig.getStdDev(),
								useRandomSeed, doNoiseModelLogging);
					} else if (noiseModelStateNoiseConfig.getNoiseModelType() == BBUncertOrArmaNoiseConfig.NoiseModelType.Ar1Model) {
						boolean useRandomSeed = false; // TODO MVL read from config
						noiseModel = new ArmaNoiseModel(this.model.getModelRunDir(), noiseModelStateSize,
								noiseModelStateNoiseConfig.getArmaConstants(), noiseModelStateNoiseConfig.getStdDev(),
								useRandomSeed, doNoiseModelLogging);
					} else {
						noiseModel = new ArmaNoiseModel(this.model.getModelRunDir(), noiseModelStateSize,
								noiseModelStateNoiseConfig.getArmaConstants(), doNoiseModelLogging);
						addWhiteNoiseFromUncertEngineToNoiseModel(noiseModelStateNoiseConfig, noiseModel,
								this.getTimeHorizon().getBeginTime());
					}
					armaNoiseModels.put(noiseModelStateNoiseConfig, noiseModel);
				}
			}
		}
	}

	private BBStochModelVectorConfig findPredictionVectorConfig(String obsId) {

		for (BBStochModelVectorConfig bbStochModelVectorConfig : this.bbStochModelVectorsConfig.getPredictorVectorCollection()) {
			if (bbStochModelVectorConfig.getId().equalsIgnoreCase(obsId)) {
				return bbStochModelVectorConfig;
			}
		}

		String allVectors = "";
		for (BBStochModelVectorConfig bbStochModelVectorConfig :
			this.bbStochModelVectorsConfig.getPredictorVectorCollection()) {
			allVectors += "      " + bbStochModelVectorConfig.getId() + "\n";
		}
		throw new RuntimeException(
				"announceObservedValues:\n" +
						"No prediction subvector found for obs id \"" +
						obsId + "\"\n" +
						"Available subvectors are:\n" +
						allVectors + "\n");
	}

	private void addParameterDeltaToExchangeItem(double parameterDelta, IPrevExchangeItem exchangeItem, int transformation) {
		double[] values = exchangeItem.getValuesAsDoubles();
		if (transformation == BBRegularisationConstantConfig.TRANSFORMATION_IDENTITY) {
			for (int i = 0; i < values.length; i++) {
				values[i] += parameterDelta;
			}
		} else if (transformation == BBRegularisationConstantConfig.TRANSFORMATION_LN) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] > 0) { //avoid taking log of value <=0.0
					values[i] = Math.exp(Math.log(values[i]) + parameterDelta);
				}
			}
		} else {
			throw new RuntimeException("BBStochModelInstance.setParameters(): unexpected transformation typ " +
					exchangeItem.getId());
		}
		exchangeItem.setValuesAsDoubles(values);
	}

	private IVector getAndCheckParamChild(IVector parameters, String parameterId) {
		ITreeVector parametersAsTreeVector = (ITreeVector) parameters;
		IVector paramChild;
		try {
			paramChild = parametersAsTreeVector.getSubTreeVector(parameterId);
		} catch (RuntimeException e) {
			paramChild = null;
			for (String subTreeVectorId : parametersAsTreeVector.getSubTreeVectorIds()) {
				ITreeVector childVector = parametersAsTreeVector.getSubTreeVector(subTreeVectorId);
				try {
					paramChild = childVector.getSubTreeVector(parameterId);
				} catch (Exception e1) {
					paramChild = null;
				}
			}
		}
		if (paramChild != null) {
			if (paramChild.getSize() != 1) {
				throw new RuntimeException("BBStochModelInstance.setParameters(): invalid size for parameter " +
						parameterId + " in incoming parameters");
			}
			return paramChild;
		}
		throw new RuntimeException("BBStochModelInstance.setParameters(): can not find parameter " +
				parameterId + " in incoming parameters");
	}

	private void propagateNoiseModelsAndAddNoiseToExchangeItems(ITime currentTime, ITime targetTime) {

		// propagate the stochastic noise models
		for (Map.Entry<BBNoiseModelConfig, IStochModelInstance> noiseModelEntry : noiseModels.entrySet()) {

			BBNoiseModelConfig noiseModelConfig = noiseModelEntry.getKey();
			IStochModelInstance noiseModel = noiseModelEntry.getValue();

			System.out.println("noisemodel.compute until "+targetTime);
			//TODO add check on time-span of noise model.
			noiseModel.compute(targetTime);
			for (NoiseModelExchangeItemConfig exchangeItemConfig : noiseModelConfig.getExchangeItemConfigs()) {
				double[] noiseModelEITimes;
				IPrevExchangeItem noiseModelExchangeItem = noiseModel.getExchangeItem(exchangeItemConfig.getId());
				if (noiseModelExchangeItem == null) {
					noiseModelExchangeItem = noiseModel.getDataObjectExchangeItem(exchangeItemConfig.getId());
				}
				if (noiseModelExchangeItem == null) {
					throw new RuntimeException("Could not find " + exchangeItemConfig.getId());
				}
				noiseModelEITimes = noiseModelExchangeItem.getTimes();
				if (noiseModelEITimes == null) {
					throw new RuntimeException("No times for noise model item " + noiseModelExchangeItem.getId());
				}
				for (String modelExchangeItemId : exchangeItemConfig.getModelExchangeItemIds()) {
					IPrevExchangeItem modelExchangeItem = model.getExchangeItem(modelExchangeItemId);
					double modelTimes[] = modelExchangeItem.getTimes();
					if (modelTimes == null) {
						throw new RuntimeException("No times available for model exchange item " +
								modelExchangeItem.getId());
					}
					List<Double> modelEiTimes = determineTimeStampsInInterval(
							modelTimes, currentTime, targetTime);
					// The model exchange item has more then one time stamp. Find matching times in
					// both lists.
					for (int t = 0; t < modelEiTimes.size(); t++) { 
						double time = modelEiTimes.get(t);
						if(!this.lastNoiseTimes.containsKey(modelExchangeItemId)){
							this.lastNoiseTimes.put(modelExchangeItemId, Double.NEGATIVE_INFINITY);
						}
						if(time>this.lastNoiseTimes.get(modelExchangeItemId)){
							// we need values for each of the model times in this forecast
							double timePrecision = OdaGlobSettings.getTimePrecision();
							// look for index in ALL times for noise model exchange item
							int ti= TimeUtils.findMatchingTimeIndex(noiseModelEITimes, time, timePrecision);
							if(ti==-1){
								System.out.println("Noise model times for "+noiseModelExchangeItem.getId());
								for(int j=0;j<noiseModelEITimes.length;j++){
									System.out.println(noiseModelEITimes[j]);
								}
								throw new RuntimeException("Could not match times between model and noise model:\n"
										+"model_time="+modelEiTimes.get(t));
							}
							double[] noiseModelEIValuesForTimeStep =
									getNoiseModelValuesForTimeStep(noiseModelExchangeItem, ti);
							int modelTimeIndex= TimeUtils.findMatchingTimeIndex(modelTimes, time, timePrecision);
							addNoiseToExchangeItemForOneTimeStep(modelExchangeItem, modelTimeIndex,
									noiseModelEIValuesForTimeStep, exchangeItemConfig.getOperation());
							this.lastNoiseTimes.put(modelExchangeItemId,time);
						}
					}
				}
			}// for noise model
		}


		// Propagate the (deprecated) 'old fashioned' arma models
		// Note: these arma models will become deprecated one the TimeSeriesNoiseModel that implements
		// the IStochModelInstance interface is fully elaborated and tested
		// Configurations that contain old ArmaModels then need to be replaced by a configuration
		// that is based on the new noise model.
		for (Map.Entry<BBUncertOrArmaNoiseConfig, ArmaNoiseModel> armaNoiseModelEntry : armaNoiseModels.entrySet()) {

			BBUncertOrArmaNoiseConfig stateNoiseModelConfig = armaNoiseModelEntry.getKey();
			ArmaNoiseModel armaNoiseModel = armaNoiseModelEntry.getValue();

			int numBcTimeStepsInPeriod = determineNumBcTimeSteps(currentTime, targetTime,
					stateNoiseModelConfig.getVectorConfigs());

			// Update the noise model, and add the noise to the exhange item.
			// TODO: this has to become a time step loop, handling
			// each boundary time step in the interval <currentTime,targetTime]
			double[] noiseForExchangItem = new double[numBcTimeStepsInPeriod];
			double deltaT = (targetTime.getMJD() - currentTime.getMJD()) / (double) numBcTimeStepsInPeriod;
			for (int t = 0; t < numBcTimeStepsInPeriod; t++) {

				// propagate noise one noiseModel timeStep:
				ITime noiseModelCurrentTime = new Time(currentTime.getMJD() + (double) t * deltaT);
				ITime noiseModelTargetTime = new Time(currentTime.getMJD() + ((double) t + 1.0d) * deltaT);
				if (!(stateNoiseModelConfig.getNoiseModelType() ==
						BBUncertOrArmaNoiseConfig.NoiseModelType.UncertainItem)) {
					armaNoiseModel.compute(noiseModelCurrentTime, noiseModelTargetTime);
				}

				// store (colored) noise realization at this noiseModelCurrentTime (white noise is not yet added):
				double[] noiseStateVector = armaNoiseModel.getNoiseStateVector(noiseModelCurrentTime);
				if (noiseStateVector.length != 1) {
					throw new RuntimeException("Incompatible noise state vector length for noise model "
							+ stateNoiseModelConfig.getId());
				}
				noiseForExchangItem[t] = noiseStateVector[0];

				if ((stateNoiseModelConfig.getNoiseModelType() ==
						BBUncertOrArmaNoiseConfig.NoiseModelType.UncertainItem) ||
						(stateNoiseModelConfig.getNoiseModelType() ==
						BBUncertOrArmaNoiseConfig.NoiseModelType.UncertainItemWithArmaConstants)) {
					// Add white noise from UncertainyEngine to noise state
					// (Noise state is empty in case op "UncertainItem", which is pure white noise)
					addWhiteNoiseFromUncertEngineToNoiseModel(stateNoiseModelConfig, armaNoiseModel, noiseModelTargetTime);
				} else {
					armaNoiseModel.updateNoise(noiseModelTargetTime);
				}
			}
			// add colored noise to exchangeItem at targetTime:
			addNoiseToExchangeItem(stateNoiseModelConfig, targetTime.getMJD(),
					numBcTimeStepsInPeriod, noiseForExchangItem);
		}
	}

	/**
	 * Get values for this time index timeStep=-1 for last timeStep available, ie count negative values from end
	 * @param noiseModelExchangeItem
	 * @param timeStepIndex
	 * @return
	 */
	private double[] getNoiseModelValuesForTimeStep(IPrevExchangeItem noiseModelExchangeItem, int timeStepIndex) {
		if (noiseModelExchangeItem instanceof IExchangeItem) {
			IExchangeItem exchangeItem = (IExchangeItem) noiseModelExchangeItem;
			if (exchangeItem.getTimeInfo() == null) {
				throw new RuntimeException(
						"No time info available in exchange item " + exchangeItem.getId());
			}
			double[] timeStamps = exchangeItem.getTimeInfo().getTimes();
			if (timeStamps == null) {
				throw new RuntimeException(
						"No times set for exchange item " + exchangeItem.getId());
			}
			if(timeStepIndex<0) timeStepIndex+=timeStamps.length; //negative indices count from end

			IGeometryInfo iGeometryInfo = exchangeItem.getGeometryInfo();
			if (iGeometryInfo != null) {
				if (!(iGeometryInfo instanceof ArrayGeometryInfo)) {
					throw new RuntimeException(
							"Unknown geometry info type " + iGeometryInfo.getClass().getName() +
							" for exchange item " + exchangeItem.getId());
				}
				ArrayGeometryInfo geometryInfo = (ArrayGeometryInfo) iGeometryInfo;
				if (geometryInfo.getLatitudeArray().getNumberOfDimensions() != 1 ||
						geometryInfo.getLongitudeArray().getNumberOfDimensions() != 1) {
					throw new RuntimeException(
							"Currently only able to handle 1D long/lat arrays, exchange item " + exchangeItem.getId());
				}
			}
			Object valuesObject = exchangeItem.getValues();
			if (!(valuesObject instanceof IArray)) {
				throw new RuntimeException(
						"Values object type " + valuesObject.getClass().getName() +
						" not support, for exchange item " + exchangeItem.getId());
			}
			// TODO assume time index is at position=0, which is often true
			return ((IArray) valuesObject).getSliceAsDoubles(0, timeStepIndex, timeStepIndex);
		} else {
			// Previous version of exchange item, no spatial and/or time info available. Simply return the value
			// for the index 'timeStep'
			double[] values = noiseModelExchangeItem.getValuesAsDoubles();
			if (timeStepIndex >= values.length) {
				throw new RuntimeException(
						"Not enough values available in noise model exchange item " + noiseModelExchangeItem.getId());
			}
			if(timeStepIndex<0) timeStepIndex+=values.length; //negative to count from end
			return new double[]{values[timeStepIndex]};
		}
	}

	private void addWhiteNoiseFromUncertEngineToNoiseModel(BBUncertOrArmaNoiseConfig stateNoiseModelConfig,
			ArmaNoiseModel noiseModel, ITime noiseModelTargetTime) {
		double[] actualValues = new double[noiseModel.getNoiseStateVectorSize()];
		// TODO: stdDev is Factor
		int realizationCounter = noiseModel.getNextRealizationCounter();
		double[] noise = uncertaintyEngine.getNoise(stateNoiseModelConfig.getUncertainItemId(),
				realizationCounter, actualValues);
		noiseModel.addWhiteNoise(noiseModelTargetTime, noise);
	}

	private void addNoiseToExchangeItem(BBUncertOrArmaNoiseConfig stateNoiseModelConfig,
			double startTime,
			int numBcTimeStepsInPeriod,
			double[] noiseForExchangItem) {

		for (BBStochModelVectorConfig vectorConfig : stateNoiseModelConfig.getVectorConfigs()) {
			String exchangeItemID = vectorConfig.getSourceId();
			IPrevExchangeItem exchangeItem = getExchangeItem(exchangeItemID);
			int tStart = 0;
			double[] exchangeItemTimes = exchangeItem.getTimes();
			if (exchangeItemTimes != null) {
				DoubleArraySearch doubleArraySearch = new DoubleArraySearch(exchangeItemTimes);
				tStart = Math.max(doubleArraySearch.search(startTime), 0);
			}
			for (int t = tStart; t < tStart + numBcTimeStepsInPeriod; t++) {
				addNoiseToExchangeItemForOneTimeStep(exchangeItem, t,
						new double[]{noiseForExchangItem[t-tStart]}, stateNoiseModelConfig.getOperation());
			}
		}
	}

	private int determineNumBcTimeSteps(ITime currentTime, ITime targetTime, BBStochModelVectorConfig[] vectorConfigs) {
		int numBcTimeStepsInPeriod = Integer.MIN_VALUE;
		for (BBStochModelVectorConfig vectorConfig : vectorConfigs) {
			IPrevExchangeItem exchangeItem = getExchangeItem(vectorConfig.getSourceId());
			if (exchangeItem.getTimes() != null) {
				int exchangeItemNumTimestepsInComputationSpan = determineNumTimeStepsInSpan(exchangeItem.getTimes(), currentTime, targetTime);
				if (numBcTimeStepsInPeriod == Integer.MIN_VALUE) {
					numBcTimeStepsInPeriod = exchangeItemNumTimestepsInComputationSpan;
				} else {
					if (numBcTimeStepsInPeriod != exchangeItemNumTimestepsInComputationSpan) {
						throw new RuntimeException("Incompatible Times in Exchange Items");
					}
				}
			}
		}
		if (numBcTimeStepsInPeriod == Integer.MIN_VALUE) {
			// When no time is available apparently the exchangeItem is a constant.
			numBcTimeStepsInPeriod = 1;
		}
		return numBcTimeStepsInPeriod;
	}

	private List<Double> determineTimeStampsInInterval(double[] exchangeItemTimes, ITime currentTime, ITime targetTime) {
		List<Double> timeStampsInInterval = new ArrayList<Double>();
		for (Double exchangeItemTime : exchangeItemTimes) {
			if (isTimeIncludedInInterval(currentTime.getMJD(), targetTime.getMJD(), exchangeItemTime)) {
				timeStampsInInterval.add(exchangeItemTime);
			}
		}
		return timeStampsInInterval;
	}

	private int determineNumTimeStepsInSpan(double[] exchangeItemTimes, ITime currentTime, ITime targetTime) {
		int numTimeStepsInSpan = 0;
		for (double exchangeItemTime : exchangeItemTimes) {
			if (isTimeIncludedInInterval(currentTime.getMJD(), targetTime.getMJD(), exchangeItemTime)) {
				numTimeStepsInSpan++;
			}
		}
		return numTimeStepsInSpan;
	}

	private boolean isTimeIncludedInInterval(double currentTime, double targetTime, double exchangeItemTime) {
		return ((exchangeItemTime + 1.e-6) >= currentTime) && ((exchangeItemTime - 1.e-6) < targetTime);
	}

	private void addNoiseToExchangeItemForOneTimeStep(IPrevExchangeItem exchangeItem,
			int timeIndex, double[] noise,
			BBUncertOrArmaNoiseConfig.Operation operation) {
		double[] times = exchangeItem.getTimes();
		if (times == null || times.length == 0) {
			throw new RuntimeException("ExchangeItem " + exchangeItem.getId() + " has no time stamps");
		}
		int numValuesInExchangeItem;
		try {
			// check the number of input values
			numValuesInExchangeItem = exchangeItem.getValuesAsDoubles().length;
		} catch (Exception e) {
			// input exchangem is not able to tell it's #values. assume 1
			// note: this should be handled more intelligent when the
			// IExchangeItem interface is fully elaborated
			numValuesInExchangeItem = 1;
		}
		boolean addFullArray = false;
		if (noise.length == numValuesInExchangeItem) {
			if (times.length == 1) {
				// Noise is meant for all values
				addFullArray = true;
			}
		} else {
			if (times.length == numValuesInExchangeItem) {
				if (noise.length > 1) {
					// Noise value for all time stamps
					addFullArray = true;
				}
			}
		}

		if (addFullArray) {
			switch (operation) {
			case Add:
				exchangeItem.axpyOnValues(1.0d, noise);
				break;
			case Multiply:
				double[] factors = new double[noise.length];
				for (int i = 0; i < noise.length; i++) {
					factors[i] = 1 + noise[i];
				}
				exchangeItem.multiplyValues(factors);
				break;
			case Set:
				throw new RuntimeException("addNoiseToExchangeItemForOneTimeStep on " + exchangeItem.getId() +
						": invalid call for setting values");
			}
		} else {
			int numValuesToBeSet=noise.length;
			if (numValuesInExchangeItem%numValuesToBeSet != 0) {
				throw new RuntimeException("The number of values in the exchangeItem is"
			      +" not a multiple of the number of times: "+exchangeItem.getId());
			}
			if(timeIndex>numValuesInExchangeItem/numValuesToBeSet){
				throw new RuntimeException("time index out of bounds for "+exchangeItem.getId());
			}
			// add noise 'slice'
			int startOfNoise = timeIndex * numValuesToBeSet;
			int endOfNoise = (timeIndex + 1) * numValuesToBeSet;
			switch (operation) {
			case Add:
				double[] values = new double[numValuesInExchangeItem];
				System.arraycopy(noise, 0, values, startOfNoise, endOfNoise - startOfNoise);
				exchangeItem.axpyOnValues(1.0, values);
				break;
			case Multiply:
				double[] factors = new double[numValuesInExchangeItem];
				for (int i = 0; i < numValuesToBeSet; i++) {
					factors[startOfNoise+i] = 1d + noise[i];
				}
				exchangeItem.multiplyValues(factors);
				break;
			case Set:
				throw new RuntimeException("addNoiseToExchangeItemForOneTimeStep on " + exchangeItem.getId() +
						": invalid call for setting values");
			}
		}
	}

	private File checkRestartDir(ITime time, boolean mustExist) {
		if (this.savedStatesDirPrefix == null) {
			throw new RuntimeException("Dir for restart files not specified in black box stoch model config file on dir. " +
					configRootDir.getAbsolutePath());
		}
		File savedStatesRootDir = configRootDir;
		String savedStatesDirPrefix = this.savedStatesDirPrefix;
		return BBModelInstance.createDirectoryForSavedState(time, mustExist, savedStatesRootDir, savedStatesDirPrefix);
	}
}