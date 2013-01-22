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
package org.openda.observers;

import org.openda.blackbox.config.BBUtils;
import org.openda.blackbox.interfaces.IoObjectInterface;
import org.openda.interfaces.*;
import org.openda.uncertainties.UncertaintyEngine;
import org.openda.uncertainties.pdfs.NormalDistribution;
import org.openda.uncertainties.pdfs.PDF;
import org.openda.utils.*;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Stochastic Observer based on IOobjects
 */
public class IoObjectStochObserver extends Instance implements IStochObserver {

	IoObjectStochObserverConfig ioObjectStochObserverConfig = null;
	private UncertaintyEngine uncertaintyEngine = null;

	private List<IoObjectInterface> ioObjects = new ArrayList<IoObjectInterface>();
	private List<IDataObject> dataObjects = new ArrayList<IDataObject>();
	private HashMap<String, IPrevExchangeItem> exchangeItems = null;
	private List<String> exchangeItemIds = null;
	private HashMap<String, Integer> valueSizes = null;

	private HashMap<IPrevExchangeItem, SelectedIndices> selectedTimeIndices = null;
	private boolean timeIndicesEqualForAllItems = false;

	private int count = 0;

	private IoObjectStochObsDescriptions observationDescriptions;
	private TreeVector valuesAsTreeVector = null;
	private static final int maxStorageSizeForValuesAsTreeVector = 4096;
	private boolean atLeastOneStdDevIsFactor = false;

	// IConfigurable methods

	public void initialize(File workingDir, String[] arguments) {

		// Read the configuration
		File configFile;
		configFile = new File(workingDir, arguments[0]);
		if (!configFile.exists()) {
			throw new RuntimeException("IoObjectStochObserver config file not found: " + configFile.getAbsolutePath());
		}
		IoObjectStochObserverConfigReader configReader = new IoObjectStochObserverConfigReader(configFile);
		ioObjectStochObserverConfig = configReader.getIoObjectStochObserverConfig();

		// Create the uncertainty module.
		// For the time being, we always expect the openda UncertaintyEngine.
		// This may change if we introduce IUncertainty.
		Class<UncertaintyEngine> expectedClassType = UncertaintyEngine.class;
		IoObjectStochObserverConfig.IoStochObsUncertaintyConfig uncertaintyModuleConfig = ioObjectStochObserverConfig.getUncertaintyModuleConfig();
		String className = (uncertaintyModuleConfig.getClassName() != null) ?
				uncertaintyModuleConfig.getClassName() : UncertaintyEngine.class.getName();
		uncertaintyEngine = (UncertaintyEngine) ObjectSupport.createNewInstance(className, expectedClassType);
		uncertaintyEngine.initialize(uncertaintyModuleConfig.getWorkingDir(), uncertaintyModuleConfig.getArguments());
		uncertaintyEngine.startCheckingUncertainItems();

		// Create the ioObjects, and gather the (output) exchange items.
		this.ioObjects.clear();
		this.dataObjects.clear();
		exchangeItems = new HashMap<String, IPrevExchangeItem>();
		exchangeItemIds = new ArrayList<String>();
		boolean timeTypeKnown = false;
		boolean isTimeDependent = false;
		for (IoObjectStochObserverConfig.IoStochObsIoObjectConfig ioObjectConfig : ioObjectStochObserverConfig.getIoObjectConfigs()) {
			IoObjectInterface ioObject = BBUtils.createIoObjectInstance(ioObjectConfig.getWorkingDir(), ioObjectConfig.getClassName(),
					ioObjectConfig.getFileName(), ioObjectConfig.getArguments());
            IPrevExchangeItem[] ioExchangeItems;
            if (ioObject != null) {
            	this.ioObjects.add(ioObject);
			    ioExchangeItems = ioObject.getExchangeItems();
            } else {
                IDataObject iDataObject = BBUtils.createDataObject(ioObjectConfig.getWorkingDir(), ioObjectConfig.getClassName(),
					ioObjectConfig.getFileName(), ioObjectConfig.getArguments());
                this.dataObjects.add(iDataObject);
                String[] iDOexchangeItemIDs = iDataObject.getExchangeItemIDs();
                ioExchangeItems = new IPrevExchangeItem[iDOexchangeItemIDs.length];
                for (int i=0; i<iDOexchangeItemIDs.length; i++){
                    ioExchangeItems[i] = iDataObject.getDataObjectExchangeItem(iDOexchangeItemIDs[i]);
                }
            }

			for (IPrevExchangeItem ioExchangeItem : ioExchangeItems) {
				if (uncertaintyEngine.checkIfItemIsUncertain(ioExchangeItem.getId())) {
				  //can only determine whether depends on time if ioExchangeItem has values.
				  if (ioExchangeItem.getValuesAsDoubles() != null && ioExchangeItem.getValuesAsDoubles().length > 0) {
					if (ioExchangeItem.getTimes() != null && ioExchangeItem.getTimes().length > 0) {
						if (timeTypeKnown && !isTimeDependent) throw createInconsistentTimeSettingException(configFile);
						isTimeDependent = true;
					} else {
						if (timeTypeKnown && isTimeDependent) throw createInconsistentTimeSettingException(configFile);
						isTimeDependent = false;
					}
					timeTypeKnown = true;
				  }
                    // get rid off missing values. So far it works only for IPrevExchangeItem.
                    // TODO: make it run also for IExchangeItem.
                    // WAS: if (ioExchangeItem instanceof SwanResults.SwanResult){
                    if (ioExchangeItem instanceof IPrevExchangeItem){
                        exchangeItemIds.add(ioExchangeItem.getId());
					    exchangeItems.put(ioExchangeItem.getId(), ioExchangeItem);
                    } else {
                        //TODO: read missingValue from input file (stochObserverConfig)
                        if (ioObjectStochObserverConfig.removeMissingValues()) {
                            double missingValue = Double.NaN;
                            NonMissingStochObserverExchangeItem ioNonMissingExchangeItem =
                                    new NonMissingStochObserverExchangeItem(ioExchangeItem,missingValue);
                            exchangeItems.put(ioExchangeItem.getId(), ioNonMissingExchangeItem);
                            exchangeItemIds.add(ioExchangeItem.getId());
                        } else {
                            exchangeItems.put(ioExchangeItem.getId(), ioExchangeItem);
                            exchangeItemIds.add(ioExchangeItem.getId());
                        }
                    }
				}
			}
		}
		if (exchangeItems.isEmpty()) {
			throw new RuntimeException(
					"The stoch observer is empty. Did you specify the right uncertain items? Stoch obs config file: " +
							configFile.getAbsolutePath());
		}
		if (isTimeDependent) {
			selectedTimeIndices = new HashMap<IPrevExchangeItem, SelectedIndices>();
			SelectedIndices lastSelectedIndices = new SelectedIndices(0, -1);
			timeIndicesEqualForAllItems = true;
			for (String exchangeItemId : exchangeItemIds) {
				IPrevExchangeItem exchangeItem = exchangeItems.get(exchangeItemId);
				boolean canReuse = true;
				if (lastSelectedIndices.getEnd() != -1) {
					canReuse = exchangeItem.getTimes().length == lastSelectedIndices.getEnd();
				}
				timeIndicesEqualForAllItems &= canReuse;
				SelectedIndices selectedIndices =
						(canReuse && lastSelectedIndices.getEnd() != -1) ? lastSelectedIndices :
								new SelectedIndices(0, exchangeItem.getTimes().length);
				selectedTimeIndices.put(exchangeItem, selectedIndices);
				lastSelectedIndices = selectedIndices;
			}
		}
		determineCountAndSizes();
		checkIfAtLeastOneStdDevIsFactor();
		uncertaintyEngine.endCheckingUncertainItems();
		observationDescriptions = new IoObjectStochObsDescriptions(exchangeItemIds, exchangeItems, null, this);
	}

	private RuntimeException createInconsistentTimeSettingException(File configFile) {
		return new RuntimeException(
				"The IoObject(s) contain(s) a mixture of " +
						" time dependent and time independent exchange items. IoObjectStochObserver config file: " +
						configFile.getAbsolutePath());
	}

	private void determineCountAndSizes() {

		// Assume 1 value (per time stamp) per exchange item (i.e.: no spatial definition yet)

		count = 0;

		if (selectedTimeIndices == null) {
			// Time independent.
			count = exchangeItems.size();
			return;
		}

		valueSizes = new HashMap<String, Integer>();
		if (timeIndicesEqualForAllItems) {
			// Time dependent, but the selected exchange items have the same selected time indices
			if (selectedTimeIndices.size() > 0) {
				IPrevExchangeItem firstExchangeItem = exchangeItems.get(exchangeItemIds.get(0));
				int timeCount = selectedTimeIndices.get(firstExchangeItem).getSize();
				for (String exchangeItemId : exchangeItemIds) {
					valueSizes.put(exchangeItemId, timeCount);
				}
				count = timeCount * selectedTimeIndices.size();
			}
			return;
		}

		// Time dependent, differences in #selected times between the exchange items
		for (String exchangeItemId : exchangeItemIds) {
			IPrevExchangeItem exchangeItem = exchangeItems.get(exchangeItemId);
			SelectedIndices selectedIndices = selectedTimeIndices.get(exchangeItem);
			valueSizes.put(exchangeItem.getId(), selectedIndices.getSize());
			count += selectedIndices.getSize();
		}
	}

	// IStochObserver methods

	public IStochObserver createSelection(String selection) {
		throw new UnsupportedOperationException("org.openda.observers.IoObjectStochObserver.createSelection(String selection): Not implemented yet.");
	}

	public IStochObserver createSelection(ITime selectedTime) {

		IoObjectStochObserver child = this.createChild();

		if (selectedTimeIndices != null) {

			// Time dependent observation values. For each exchange item, select the time indices

			// Time stamp or interval
			double beginTimeAsMJD;
			double endTimeAsMJD;
			boolean isSpan = selectedTime.isSpan();
			if (isSpan) {
				beginTimeAsMJD = selectedTime.getBeginTime().getMJD();
				endTimeAsMJD = selectedTime.getEndTime().getMJD();
			} else {
				beginTimeAsMJD = endTimeAsMJD = selectedTime.getMJD();
			}

			child.selectedTimeIndices = new HashMap<IPrevExchangeItem, SelectedIndices>();

			SelectedIndices lastSelectedIndices = new SelectedIndices(-1, -1);

			for (String exchangeItemId : exchangeItemIds) {
				IPrevExchangeItem exchangeItem = exchangeItems.get(exchangeItemId);

				double[] times = exchangeItem.getTimes();

				// skip times before the begin time
				// TODO: let IPrevExchangeItem.getTimes() return List<Time> instead of double[]
				double compareEpsilon = 1e-6;
				int startIndex = Integer.MAX_VALUE;
				int i = 0;
				while (startIndex == Integer.MAX_VALUE && i < times.length) {
					if (isSpan) {
						if (times[i] > beginTimeAsMJD + compareEpsilon) startIndex = i;
					} else {
						if ((times[i] > (beginTimeAsMJD - compareEpsilon)) &&
								(times[i] < (endTimeAsMJD + compareEpsilon)))
							startIndex = i;
					}
					i++;
				}

				if (startIndex == Integer.MAX_VALUE) {
					// this exchange item has no time stamps for the selected times()
					continue;
				}

				// truncate after the end time
				int endIndex = startIndex + 1;
				if (isSpan) {
					while (endIndex < times.length && !(times[endIndex] > endTimeAsMJD))
						endIndex++;
				}
				// store selection indices, reuse if possible
				boolean canReuse = true;
				if (lastSelectedIndices.getStart() != -1) {
					canReuse = startIndex == lastSelectedIndices.getStart();
				}
				if (lastSelectedIndices.getEnd() != -1) {
					canReuse = endIndex == lastSelectedIndices.getEnd();
				}
				timeIndicesEqualForAllItems &= canReuse;
				SelectedIndices selectedIndices =
						(canReuse && (lastSelectedIndices.getStart() != -1 && lastSelectedIndices.getEnd() != -1)) ?
								lastSelectedIndices :
								new SelectedIndices(startIndex, endIndex);
				child.selectedTimeIndices.put(exchangeItem, selectedIndices);
				child.exchangeItemIds.add(exchangeItem.getId());
				child.exchangeItems.put(exchangeItem.getId(), exchangeItem);
				lastSelectedIndices = selectedIndices;
			}
		} else {
			// time independent values, add all items to child
			child.exchangeItemIds = this.exchangeItemIds;
			child.exchangeItems = this.exchangeItems;
		}
		child.determineCountAndSizes();
		checkIfAtLeastOneStdDevIsFactor();
		child.observationDescriptions = new IoObjectStochObsDescriptions(child.exchangeItemIds, child.exchangeItems, child.selectedTimeIndices, this);
		return child;
	}

	public IStochObserver createSelection(Type observationType) {

		IoObjectStochObserver child = this.createChild();
		if (observationType == Type.Assimilation) {
			List<String> selectedObsIds = this.ioObjectStochObserverConfig.getAssimilationObsIds();
			fillObservationTypeChildWithSelectedObservations(child, selectedObsIds);
		} else {
			fillObservationTypeChildWithSelectedObservations(child, this.ioObjectStochObserverConfig.getValidationObsIds());
		}
		child.determineCountAndSizes();
		checkIfAtLeastOneStdDevIsFactor();
		child.observationDescriptions = new IoObjectStochObsDescriptions(child.exchangeItemIds, child.exchangeItems, child.selectedTimeIndices, this);
		return child;
	}

    public ISelector createSelector(Type observationType) {
        throw new UnsupportedOperationException("org.openda.observers.IoObjectStochObserver.createSelector(): Not implemented yet.");
    }

    private IoObjectStochObserver createChild() {

		IoObjectStochObserver child = new IoObjectStochObserver();

		child.ioObjectStochObserverConfig = this.ioObjectStochObserverConfig;
		child.uncertaintyEngine = this.uncertaintyEngine;
		uncertaintyEngine.increaseTimeStepSeed();

		child.exchangeItemIds = new ArrayList<String>();
		child.exchangeItems = new HashMap<String,IPrevExchangeItem>();

		child.selectedTimeIndices = this.selectedTimeIndices;
		child.timeIndicesEqualForAllItems = this.timeIndicesEqualForAllItems;

		return child;
	}

	private void fillObservationTypeChildWithSelectedObservations(IoObjectStochObserver child, List<String> selectedObsIds) {
		if (selectedObsIds != null) {
			for (String selectedObsId : selectedObsIds) {
				child.exchangeItemIds.add(selectedObsId);
				child.exchangeItems.put(selectedObsId, this.exchangeItems.get(selectedObsId));
			}
		} else {
			child.exchangeItemIds = this.exchangeItemIds;
			child.exchangeItems = this.exchangeItems;
		}
	}

	public int getCount() {
		return count;
	}

	public IVector getRealizations() {
		return uncertaintyEngine.getRealization(getValues());
	}

	public IVector getExpectations() {
		// For now, we assume that all PDF's are normal, so the expectations are the values
		// TODO: use expectations of other PFD
		return getValues();
	}

	public double evaluatePDF(IVector values) {
		throw new UnsupportedOperationException("org.openda.observers.IoObjectStochObserver.evaluatePDF(): Not implemented yet.");
	}

	public IVector evaluateMarginalPDFs(IVector values) {
		throw new UnsupportedOperationException("org.openda.observers.IoObjectStochObserver.evaluateMarginalPDFs(): Not implemented yet.");
	}

	public ISqrtCovariance getSqrtCovariance() {
		IVector mean = this.getExpectations();
		IVector std = this.getStandardDeviations();
		IStochVector stochVector = new StochVector(mean, std);
		return stochVector.getSqrtCovariance();
	}

	public IVector getStandardDeviations() {
		double[] standardDeviations;

		checkIfAtLeastOneStdDevIsFactor();  //added (VORtech)
		if (atLeastOneStdDevIsFactor) {
			standardDeviations = uncertaintyEngine.getStandardDeviations(exchangeItemIds, valueSizes,
					getValues().getValues());
		} else {
			standardDeviations = uncertaintyEngine.getStandardDeviations(exchangeItemIds, valueSizes);
		}
		return new Vector(standardDeviations);
	}

	public ITime[] getTimes() {

		if (selectedTimeIndices == null) {
			return null;
		}

		if (timeIndicesEqualForAllItems) {
			// Time dependent, but the selected exchange items have the same selected time indices
			if (selectedTimeIndices.size() > 0) {
				IPrevExchangeItem firstEI = exchangeItems.get(exchangeItemIds.get(0));
				double[] timesAsMJD = firstEI.getTimes();
				ITime[] times = new ITime[timesAsMJD.length];
				for (int i = 0; i < times.length; i++) {
					times[i] = new Time(timesAsMJD[i]);
				}
				return times;
			}
			return null;
		}

		// Time dependent, differences in #selected times between the exchange items
		valueSizes = new HashMap<String, Integer>();
		for (String exchangeItemId : exchangeItemIds) {
			IPrevExchangeItem exchangeItem = exchangeItems.get(exchangeItemId);
			SelectedIndices selectedIndices = selectedTimeIndices.get(exchangeItem);
			valueSizes.put(exchangeItem.getId(), selectedIndices.getSize());
			count += selectedIndices.getSize();
		}

		throw new UnsupportedOperationException(
				"org.openda.observers.IoObjectStochObserver.getTimes() " +
						" not implemented yet for different times per Exchange Item.");
	}

	public void free() {
		for (IoObjectInterface ioObject : this.ioObjects) {
			ioObject.finish();
		}
		for (IDataObject dataObject : this.dataObjects) {
			dataObject.finish();
		}
	}

	public IObservationDescriptions getObservationDescriptions() {
		return observationDescriptions;
	}

	public ITreeVector getValues() {
		TreeVector treeVector;
		if (valuesAsTreeVector == null) {
			treeVector = new TreeVector("ObsValues");
			for (String exchangeItemId : exchangeItemIds) {
				IPrevExchangeItem exchangeItem = exchangeItems.get(exchangeItemId);
				double[] values = exchangeItem.getValuesAsDoubles();
				Vector childVector;
				if (selectedTimeIndices == null) {
					childVector = new Vector(values);
				} else {
					SelectedIndices selectedIndices = selectedTimeIndices.get(exchangeItem);
					childVector = new Vector(selectedIndices.getSize());
					int indexInVector = 0;
					for (int i = selectedIndices.getStart(); i < selectedIndices.getEnd(); i++) {
						childVector.setValue(indexInVector++, values[i]);
					}
				}
				treeVector.addChild(new TreeVector(exchangeItem.getId(), childVector));
			}
			if (treeVector.getSize() <= maxStorageSizeForValuesAsTreeVector) {
				valuesAsTreeVector = treeVector;
			}
		} else {
			treeVector = valuesAsTreeVector;
		}
		return treeVector;
	}

	// set flag if at least one StdDev is a factor of the actual value

	private void checkIfAtLeastOneStdDevIsFactor() {
		for (String uncertaintyId : exchangeItemIds) {
			PDF pdf = uncertaintyEngine.getPdf(uncertaintyId);
			if (pdf instanceof NormalDistribution) {
				atLeastOneStdDevIsFactor |= ((NormalDistribution) pdf).isStdFactor();
			}
		}
	}

	/**
	 * Store begin and end index of the series of times that were selected for a certain exchangeitem
	 */
	private class SelectedIndices implements Serializable{

		private int start;
		private int end;

		public SelectedIndices(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		public int getSize() {
			return end - start;
		}
	}

	private class IoObjectStochObsDescriptions implements IObservationDescriptions, Serializable {

		private List<String> exchangeItemIds = null;
		private HashMap<String, IPrevExchangeItem> exchangeItems = null;
		private IoObjectStochObserver stochObserver;


		public IoObjectStochObsDescriptions(List<String> exchangeItemIds,
											HashMap<String, IPrevExchangeItem> exchangeItems,
											HashMap<IPrevExchangeItem, SelectedIndices> selectionIndices,
											IoObjectStochObserver stochObserver) {
			this.stochObserver = stochObserver;

			if (selectedTimeIndices == null) {
				// Time independent, or no selection on times
				if (selectionIndices != null && selectionIndices.size() > 0) {
					throw new RuntimeException(
							"org.openda.observers.IoObjectStochObsDescriptions.IoObjectStochObsDescriptions() " +
							": selection times can not be specified for time dependent exchange item.");
				}
				this.exchangeItemIds = exchangeItemIds;
				this.exchangeItems = exchangeItems;
				return;
			}

			if (selectionIndices == null || selectionIndices.size() == 0) {
				// no selection on times, take all exchange items
				this.exchangeItemIds = exchangeItemIds;
				this.exchangeItems = exchangeItems;
			} else {
				// create time selection exchange item around exchange item
				SelectedIndices sharedSelectedIndices = null;
				double[] sharedTimes = null;
				this.exchangeItemIds = new ArrayList<String>();
				this.exchangeItems = new HashMap<String, IPrevExchangeItem>();
				for (String exchangeItemId : exchangeItemIds) {
					IPrevExchangeItem exchangeItem = exchangeItems.get(exchangeItemId);
					SelectedIndices selectedIndices = selectionIndices.get(exchangeItem);
					double[] selectedTimes;
					if (timeIndicesEqualForAllItems) {
						if (sharedSelectedIndices == null) {
							sharedSelectedIndices = selectedIndices;
							sharedTimes = getTimeSelection(exchangeItem.getTimes(), selectedIndices);
						}
						selectedIndices = sharedSelectedIndices;
						selectedTimes = sharedTimes;
					} else {
						selectedTimes = getTimeSelection(exchangeItem.getTimes(), selectedIndices);
					}
					IoObjectStochObsTimeSelectionExchangeItem timeSelectionExchangeItem =
							new IoObjectStochObsTimeSelectionExchangeItem(exchangeItem, selectedIndices, selectedTimes);
					this.exchangeItemIds.add(timeSelectionExchangeItem.getId());
					this.exchangeItems.put(timeSelectionExchangeItem.getId(), timeSelectionExchangeItem);
				}
			}
		}

		private double[] getTimeSelection(double[] values, SelectedIndices selectedIndices) {
			double[] selection = new double[selectedIndices.getSize()];
			int index = 0;
			for (int i = selectedIndices.getStart(); i < selectedIndices.getEnd(); i++) {
				selection[index++] = values[i];
			}
			return selection;
		}

		public List<IPrevExchangeItem> getExchangeItems() {
			List<IPrevExchangeItem> exchangeItems = new ArrayList<IPrevExchangeItem>();
			for (String exchangeItemId : exchangeItemIds) {
				exchangeItems.add(this.exchangeItems.get(exchangeItemId));
			}
			return exchangeItems;
		}

		public IVector getValueProperties(String key) {

			IVector properties;
			if (keyIsXcoord(key) || keyIsYcoord(key)) {
				properties = new Vector(count);
				int index = 0;
				boolean isY = keyIsYcoord(key);
				for (String exchangeItemId : exchangeItemIds) {
					IPrevExchangeItem exchangeItem = exchangeItems.get(exchangeItemId);
					// x/y currently only known in case of Swan.
					// TODO: add X/Y to IPrevExchangeItem
					// TODO MVL fix properly through IGeometry
					//SwanResults.SwanResult.SwanResultLocation swanResultLocation = ((SwanResults.SwanResult) exchangeItem).getLocation();
					//properties.setValue(index++, isY ? swanResultLocation.getY() : swanResultLocation.getX());
					if(exchangeItemId.indexOf("@")>=0){
						int indAt=exchangeItemId.indexOf("@");
						int indComma=exchangeItemId.lastIndexOf(",");
						double pos;
						if(isY){ //Y
						   pos=Double.parseDouble(exchangeItemId.substring(indComma+1));
						}else{ //X
						   pos=Double.parseDouble(exchangeItemId.substring(indAt+1, indComma));							
						}
						properties.setValue(index, pos);
					}else{
						throw new RuntimeException("No coordinates available in observer");
					}
					index++;
				}
			} else if (keyIsTime(key)) {
				properties = new Vector(count);
				if (selectedTimeIndices == null) {
					// Time independent. Return zero's
				} else {
					int index = 0;
					for (String exchangeItemId : exchangeItemIds) {
						IPrevExchangeItem exchangeItem = exchangeItems.get(exchangeItemId);
						SelectedIndices selectedIndices = selectedTimeIndices.get(exchangeItem);
						for (int i = selectedIndices.getStart(); i < selectedIndices.getEnd(); i++) {
							properties.setValue(index++, exchangeItem.getTimes()[i]);
						}
					}
				}
			} else if (keyIsStdDev(key)) {
				properties = getStandardDeviations();
			} else if (keyIsValues(key)) {
				properties = getStandardDeviations();
			} else {
				throw new RuntimeException("IoObjectStochObserver.getValueProperties(): unhandled property key: " + key);
			}

			return properties;
		}

		public String[] getStringProperties(String key) {
			if (key.equalsIgnoreCase("id")) {
				return exchangeItemIds.toArray(new String[exchangeItemIds.size()]);
			}
			throw new RuntimeException("IoObjectStochObserver.getStringProperties(): unknown property key: " + key);
		}

		public String[] getPropertyKeys() {
			throw new UnsupportedOperationException("org.openda.observers.IoObjectStochObserver.IoObjectStochObsDescriptions.getPropertyKeys(): Not implemented yet.");
		}

		public int getPropertyCount() {
			throw new UnsupportedOperationException("org.openda.observers.IoObjectStochObserver.IoObjectStochObsDescriptions.getPropertyCount(): Not implemented yet.");
		}

		public int getObservationCount() {
			return this.stochObserver.count;
		}

		public ITime[] getTimes() {
			return stochObserver.getTimes();
		}

		private boolean keyIsXcoord(String key) {
			return key.equalsIgnoreCase("x") || key.equalsIgnoreCase("xp") ||
					key.equalsIgnoreCase("xCoord") || key.equalsIgnoreCase("x-Coord");
		}

		private boolean keyIsYcoord(String key) {
			return key.equalsIgnoreCase("y") || key.equalsIgnoreCase("yp") ||
					key.equalsIgnoreCase("yCoord") || key.equalsIgnoreCase("y-Coord");
		}

		private boolean keyIsTime(String key) {
			return key.equalsIgnoreCase("t") || key.equalsIgnoreCase("time");
		}

		private boolean keyIsStdDev(String key) {
			return key.equalsIgnoreCase("stdDev") || key.equalsIgnoreCase("std") ||
					key.equalsIgnoreCase("stDev") || key.equalsIgnoreCase("standardDev") ||
					key.equalsIgnoreCase("standardDeviation");
		}

		private boolean keyIsValues(String key) {
			return key.equalsIgnoreCase("val") || key.equalsIgnoreCase("value") || key.equalsIgnoreCase("values");
		}
	}

	private class IoObjectStochObsTimeSelectionExchangeItem implements IPrevExchangeItem, Serializable {

		IPrevExchangeItem exchangeItem;
		SelectedIndices selectedIndices;
		private double[] times;

		public IoObjectStochObsTimeSelectionExchangeItem(IPrevExchangeItem exchangeItem, SelectedIndices selectedIndices, double[] times) {
			this.exchangeItem = exchangeItem;
			this.selectedIndices = selectedIndices;
			this.times = times;
		}

		public String getId() {
			return exchangeItem.getId();
		}

		public String getDescription() {
			return exchangeItem.getDescription();
		}

		public Class getValueType() {
			return double[].class;
		}

		public Role getRole() {
			return exchangeItem.getRole();
		}

		public Object getValues() {
			return getValuesAsDoubles();
		}

		public double[] getValuesAsDoubles() {
			double[] allValues = exchangeItem.getValuesAsDoubles();
			double[] values = new double[selectedIndices.getSize()];
			int index = 0;
			for (int i = selectedIndices.getStart(); i < selectedIndices.getEnd(); i++) {
				values[index++] = allValues[i];
			}
			return values;
		}

		public void axpyOnValues(double alpha, double[] axpyValues) {
			double[] allValues = exchangeItem.getValuesAsDoubles();
			int index = 0;
			for (int i = selectedIndices.getStart(); i < selectedIndices.getEnd(); i++) {
				allValues[i] += alpha * axpyValues[index++];
			}
			exchangeItem.setValuesAsDoubles(allValues);
		}

		public void multiplyValues(double[] multiplicationFactors) {
			double[] allValues = exchangeItem.getValuesAsDoubles();
			int index = 0;
			for (int i = selectedIndices.getStart(); i < selectedIndices.getEnd(); i++) {
				allValues[i] *= multiplicationFactors[index++];
			}
			exchangeItem.setValuesAsDoubles(allValues);
		}

		public void setValues(Object values) {
			if (!(values instanceof double[])) {
				throw new RuntimeException(this.getClass().getName() +
						"setValues: unknown object type: " + values.getClass().getName());
			}
			setValuesAsDoubles((double[]) values);
		}

		public void setValuesAsDoubles(double[] values) {
			double[] allValues = exchangeItem.getValuesAsDoubles();
			int index = 0;
			for (int i = selectedIndices.getStart(); i < selectedIndices.getEnd(); i++) {
				allValues[i] = values[index++];
			}
			exchangeItem.setValuesAsDoubles(allValues);
		}

		public double[] getTimes() {
			return times;
		}

		public void setTimes(double[] times) {
			throw new UnsupportedOperationException(
					"IoObjectStochObsTimeSelectionExchangeItem.setTimes(): Setting times not allowed.");
		}
	}
}
