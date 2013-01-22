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
import org.openda.blackbox.interfaces.IoObjectInterface;
import org.openda.blackbox.interfaces.SelectorInterface;
import org.openda.exchange.timeseries.TimeUtils;
import org.openda.interfaces.*;
import org.openda.utils.Instance;
import org.openda.utils.Time;
import org.openda.utils.io.FileBasedModelState;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Black box module's implementation of a model instance
 */
public class BBModelInstance extends Instance implements IModelInstance {

    protected BBModelConfig bbModelConfig;

    private File instanceFileOrDir;
    private HashMap<String, IoObjectInterface> ioObjects;
    private HashMap<String, IDataObject> dataObjects;
    private HashMap<String, BBExchangeItem> bbExchangeItems = new HashMap<String, BBExchangeItem>();
    protected HashMap<String, SelectorInterface> selectors;
    private boolean doAdditionalComputeActions = false;
    private ITime currentTime = null;
    private ITime timeHorizon = null;
    boolean newInstanceDir = true;

	public BBModelInstance(BBModelConfig bbModelConfig, int instanceNumber, ITime timeHorizon) {

		this.bbModelConfig = bbModelConfig;
		File configRootDir = bbModelConfig.getConfigRootDir();
		this.timeHorizon = timeHorizon;

		// Update alias with instance number
        bbModelConfig.getWrapperConfig().getAliasDefinitions().setAliasValue("instanceNumber", String.valueOf(instanceNumber));

        BBWrapperConfig bbWrapperConfig = bbModelConfig.getWrapperConfig();
        if (bbWrapperConfig.getCloneType() != BBWrapperConfig.CloneType.None) {
			File templateFileOrDir = new File(configRootDir, bbModelConfig.getWrapperConfig().getTemplateName());
			instanceFileOrDir = new File(configRootDir, bbModelConfig.getWrapperConfig().getInstanceName());
			if (instanceFileOrDir.exists() && bbModelConfig.skipModelActionsIfInstanceDirExists()) {
                newInstanceDir = false;
			} else {
				if (bbWrapperConfig.getCloneType() == BBWrapperConfig.CloneType.Directory) {
					BBUtils.makeDirectoryClone(templateFileOrDir, instanceFileOrDir);
				} else if (bbWrapperConfig.getCloneType() == BBWrapperConfig.CloneType.File) {
					BBUtils.makeFileClone(templateFileOrDir, instanceFileOrDir);
				}
			}
		}

        ioObjects = new HashMap<String, IoObjectInterface>();
        dataObjects = new HashMap<String, IDataObject>();
        selectors = new HashMap<String, SelectorInterface>();
        System.out.println("Start Instance initialization");
		if (newInstanceDir || !bbModelConfig.skipModelActionsIfInstanceDirExists()) {
			for (BBAction action : bbWrapperConfig.getInitializeActions()) {
				action.run(configRootDir);
			}
		}
		System.out.println("Instance initialization done");

		if (this.timeHorizon == null) {
			// no time horizon specified by 'outside world', determine the time horizon
			// from the BBModelConfig or from the StartTime/StopTime-exchange items.
			determineTimeHorizon();
		}

		currentTime = this.timeHorizon.getBeginTime();
		// Feed the time horizon to the model.
		feedTimeHorizonToModel();
	}

    public boolean isNewDirectory(){
        return this.newInstanceDir;
    }

    public ITime getTimeHorizon() {
        return this.timeHorizon;
    }

    public ITime getCurrentTime() {
        return currentTime;
    }

    public void compute(ITime targetTime) {

        checkForPendingComputeActions();

        if (targetTime != null) {

			AliasDefinitions aliasDefinitions = bbModelConfig.getWrapperConfig().getAliasDefinitions();
			if (aliasDefinitions.containsKey("currentTime")) {
				aliasDefinitions.setAliasValue(
						"currentTime", TimeUtils.mjdToString(getCurrentTime().getMJD()));
			}
			if (aliasDefinitions.containsKey("targetTime")) {
			    aliasDefinitions.setAliasValue(
						"targetTime", TimeUtils.mjdToString(targetTime.getMJD()));
			}
			feedComputeTimeToModel(targetTime);
        }

        // flush and remove all IoObjects
        for (IoObjectInterface ioObjectInterface : ioObjects.values()) {
            ioObjectInterface.finish();
        }
        ioObjects.clear();
        // flush and remove all IoObjects
        for (IDataObject dataObject : dataObjects.values()) {
            dataObject.finish();
        }
        dataObjects.clear();

        bbExchangeItems.clear();

        // perform computation actions
        System.out.println("Start Instance computation, targetTime=" + targetTime);
		if (newInstanceDir | !bbModelConfig.skipModelActionsIfInstanceDirExists()) {
			for (BBAction action : bbModelConfig.getWrapperConfig().getComputeActions()) {
				action.run(instanceFileOrDir);
			}
		}
		System.out.println("Instance computation done");

        setCheckForPendingComputeActions();

        if (targetTime != null) {
            currentTime = targetTime;
        }
    }

    public String[] getExchangeItemIDs() {
        checkForPendingComputeActions();
        return bbModelConfig.getExchangeItemIds();
    }

    public String[] getExchangeItemIDs(IPrevExchangeItem.Role role) {
		checkForPendingComputeActions();
		return bbModelConfig.getExchangeItemIds(role);
    }

	public IExchangeItem getDataObjectExchangeItem(String exchangeItemID) {
		IPrevExchangeItem exchangeItem = getExchangeItem(exchangeItemID);
		if (!(exchangeItem instanceof BBExchangeItem)) {
			throw new RuntimeException("Unexpected exchange item type " + exchangeItem.getClass().toString());
		}
		return (BBExchangeItem) exchangeItem;
	}

	public IPrevExchangeItem getExchangeItem(String exchangeItemId) {
        checkForPendingComputeActions();
        BBExchangeItem bbExchangeItem = bbExchangeItems.get(exchangeItemId);
        if (bbExchangeItem == null) {
			List<String> newExchangeItemIDs = new ArrayList<String>();
			BBModelVectorConfig vectorConfig = findVectorConfig(exchangeItemId);
			if (vectorConfig != null) {
                IoObjectInterface ioObject = findOrCreateIoObject(vectorConfig.getIoObjectConfig());
                if (ioObject != null) {
                    for (IPrevExchangeItem ioObjectExchangeItem : ioObject.getExchangeItems()) {
                        if (ioObjectExchangeItem.getId().equalsIgnoreCase(vectorConfig.getSourceId())) {
                            bbExchangeItem = new BBExchangeItem(exchangeItemId, vectorConfig, ioObjectExchangeItem,
                                    selectors, bbModelConfig.getConfigRootDir());
                            break;
                        }
                        newExchangeItemIDs.add(ioObjectExchangeItem.getId());
                    }
                } else {
                    IDataObject dataObject = findOrCreateDataObject(vectorConfig.getIoObjectConfig());
                    if (dataObject != null) {
                        for (String exchangeItemID : dataObject.getExchangeItemIDs()) {
                            if (exchangeItemID.equalsIgnoreCase(vectorConfig.getSourceId())) {
                                bbExchangeItem = new BBExchangeItem(exchangeItemId, vectorConfig,
                                        dataObject.getDataObjectExchangeItem(exchangeItemID),
                                        selectors, bbModelConfig.getConfigRootDir());
                                break;
                            }
                            newExchangeItemIDs.add(exchangeItemID);
                        }
                    } else {
                        throw new IllegalArgumentException("IoObject or DataObject could not be created for \"" +
                                vectorConfig.getIoObjectConfig().getId() + "\"");
                    }
                }
			} else {
				List<BBModelVectorConfig> allElementVectorConfigs = findAllElementsVectorConfig();
				if (allElementVectorConfigs.size() == 0) {
					throw new IllegalArgumentException("IO selection subvector not found for \"" + exchangeItemId + "\"");
				}
                for (BBModelVectorConfig allElementVectorConfig : allElementVectorConfigs) {
                    IoObjectInterface ioObject = findOrCreateIoObject(allElementVectorConfig.getIoObjectConfig());
                    if (ioObject != null) {
                        for (IPrevExchangeItem ioObjectExchangeItem : ioObject.getExchangeItems()) {
                            if (ioObjectExchangeItem.getId().equalsIgnoreCase(exchangeItemId)) {
                                bbExchangeItem = new BBExchangeItem(exchangeItemId, allElementVectorConfig,
                                        ioObjectExchangeItem,
                                        selectors, bbModelConfig.getConfigRootDir());
                                break;
                            }
                            newExchangeItemIDs.add(ioObjectExchangeItem.getId());
                        }
                    } else {
                        IDataObject dataObject = findOrCreateDataObject(allElementVectorConfig.getIoObjectConfig());
                        if (dataObject != null) {
                            for (String exchangeItemID : dataObject.getExchangeItemIDs()) {
                                if (exchangeItemID.equalsIgnoreCase(exchangeItemId)) {
                                    bbExchangeItem = new BBExchangeItem(exchangeItemId, allElementVectorConfig,
                                            dataObject.getDataObjectExchangeItem(exchangeItemID),
                                            selectors, bbModelConfig.getConfigRootDir());
                                    break;
                                }
                                newExchangeItemIDs.add(exchangeItemID);
                            }
                        } else {
                            throw new IllegalArgumentException("IoObject or DataObject could not be created for \"" +
                                    allElementVectorConfig.getIoObjectConfig().getId() + "\"");
                        }
                        if (bbExchangeItem != null) {
                            break;
                        }
                    }
                }
            }

			if (bbExchangeItem != null) {
				bbExchangeItems.put(exchangeItemId, bbExchangeItem);
				return bbExchangeItem;
			}

			String allItems = "";
			for (String availableExchangeItemID : newExchangeItemIDs) {
				allItems += "   " + availableExchangeItemID + "\n";
			}
            throw new RuntimeException(
                      "BBModelInstance.findExchangeItem(" + exchangeItemId + "):\n" +
                      "Exchange item '" + exchangeItemId + "' not found\n" +
                      "Existing exchange items are:\n" +
                      allItems + "\n");
        }
        return bbExchangeItem;
    }

    public IModelState saveInternalState() {
		// copy the model's restart files to a subdirectory for the current time stap,
		// and gather them in a file based model state
		File savedStateDir = checkRestartDir(getCurrentTime(), false);
		for (String restartFileName : bbModelConfig.getRestartFileNames()) {
			File modelStateFile = new File(getModelRunDir(), restartFileName);
			File copyOfModelStateFile = new File(savedStateDir, restartFileName);
			try {
				BBUtils.copyFile(modelStateFile, copyOfModelStateFile);
			} catch (IOException e) {
				throw new RuntimeException("Could not copy " + modelStateFile.getAbsolutePath() + " to " +
				copyOfModelStateFile.getAbsolutePath() + ": " + e.getMessage());
			}
		}
		return new FileBasedModelState(savedStateDir);
	}

    public void restoreInternalState(IModelState savedInternalState) {
		if (!(savedInternalState instanceof FileBasedModelState)) {
			throw new IllegalArgumentException("Unknown state type (" + savedInternalState.getClass().getName() +
					" for " + this.getClass().getName() + ".restoreInternalState");
		}
		FileBasedModelState modelState = (FileBasedModelState) savedInternalState;
		File savedStateDir = checkRestartDir(getCurrentTime(), false);
		modelState.setDirContainingModelstateFiles(savedStateDir);
		modelState.restoreState();
		for (String restartFileName : bbModelConfig.getRestartFileNames()) {
			File modelStateFileInModelState = new File(savedStateDir, restartFileName);
			File modelStateFile = new File(getModelRunDir(), restartFileName);
			try {
				BBUtils.copyFile(modelStateFileInModelState, modelStateFile);
			} catch (IOException e) {
				throw new RuntimeException("Could not copy " + modelStateFileInModelState.getAbsolutePath() + " to " +
						modelStateFile.getAbsolutePath() + ": " + e.getMessage());
			}
		}

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

   public IVector[] getObservedLocalization(IObservationDescriptions observationDescriptions, double distance){
		throw new UnsupportedOperationException("org.openda.blackbox.wrapper.BBModelInstance..getObservedLocalization(): Not implemented yet.");
	}

    public File getModelRunDir() {
        return instanceFileOrDir;
    }

    public void finish() {

        checkForPendingComputeActions();

		// flush and remove all IoObjects
		for (IoObjectInterface ioObjectInterface : ioObjects.values()) {
			ioObjectInterface.finish();
		}
		ioObjects.clear();
		bbExchangeItems.clear();

        System.out.println("Start Instance finalization");
		if (!bbModelConfig.skipModelActionsIfInstanceDirExists()) {
			for (BBAction action : bbModelConfig.getWrapperConfig().getFinalizeActions()) {
				action.run(instanceFileOrDir);
			}
		}
		if (bbModelConfig.doCleanUp()) {
            if (instanceFileOrDir.isDirectory()) {
                BBUtils.deleteDirectory(instanceFileOrDir);
            } else {
                if (!instanceFileOrDir.delete()) {

                }
            }
        }
	}

	public static File createDirectoryForSavedState(ITime time, boolean mustExist, File savedStatesRootDir, String savedStatesDirPrefix) {
		String timeString = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Time.timeStampToDate(time));
		File dirForRestartFiles = new File(savedStatesRootDir, savedStatesDirPrefix + timeString);
		if (mustExist) {
			if (!dirForRestartFiles.exists()) {
				throw new RuntimeException("Dir for restart files not found: " + dirForRestartFiles.getAbsolutePath());
			}
		} else {
			if (dirForRestartFiles.exists()) {
				dirForRestartFiles.delete();
			}
			dirForRestartFiles.mkdirs();
		}
		return dirForRestartFiles;
	}

	private File checkRestartDir(ITime time, boolean mustExist) {
		if (this.bbModelConfig.getSavedStatesDirPrefix() == null) {
			throw new RuntimeException("Dir for restart files not specified in black box model config file on dir. " +
					bbModelConfig.getConfigRootDir().getAbsolutePath());
		}
		return BBModelInstance.createDirectoryForSavedState(time, mustExist,
				getModelRunDir(), this.bbModelConfig.getSavedStatesDirPrefix());
	}

	private BBModelVectorConfig findVectorConfig(String exchangeItemId) {
		for (BBModelVectorConfig vectorConfig : bbModelConfig.getExchangeItems()) {
			if (vectorConfig.getId().equalsIgnoreCase(exchangeItemId)) {
				return vectorConfig;
			}
		}
		return null;
	}

	private List<BBModelVectorConfig> findAllElementsVectorConfig() {
		List<BBModelVectorConfig> allElementsVectorConfigs = new ArrayList<BBModelVectorConfig>();
		for (BBModelVectorConfig vectorConfig : bbModelConfig.getExchangeItems()) {
			if (vectorConfig.getId().equalsIgnoreCase("allElementsFromIoObject")) {
				allElementsVectorConfigs.add(vectorConfig);
			}
		}
		return allElementsVectorConfigs;
	}


	private IoObjectInterface findOrCreateIoObject(IoObjectConfig ioObjectConfig) {

		// find or create io object
		IoObjectInterface ioObject = ioObjects.get(ioObjectConfig.getId());
		if (ioObject == null) {
			File workingDir = null;
			if (instanceFileOrDir != null) {
				workingDir = instanceFileOrDir.isDirectory() ? instanceFileOrDir : instanceFileOrDir.getParentFile();
			}
			ioObject = BBUtils.createIoObjectInstance(
                    workingDir, ioObjectConfig.getClassName(), ioObjectConfig.getFileName(), ioObjectConfig.getArguments());
            if (ioObject != null) {
                ioObjects.put(ioObjectConfig.getId(), ioObject);
            }
        }
		return ioObject;
	}

    private IDataObject findOrCreateDataObject(IoObjectConfig ioObjectConfig) {

        // find or create io object
        IDataObject dataObject = dataObjects.get(ioObjectConfig.getId());
        if (dataObject == null) {
            File workingDir = null;
            if (instanceFileOrDir != null) {
                workingDir = instanceFileOrDir.isDirectory() ? instanceFileOrDir : instanceFileOrDir.getParentFile();
            }
            dataObject = BBUtils.createDataObject(
                    workingDir, ioObjectConfig.getClassName(), ioObjectConfig.getFileName(), ioObjectConfig.getArguments());
            if (dataObject != null) {
                dataObjects.put(ioObjectConfig.getId(), dataObject);
            }
        }
        return dataObject;
    }

	private void setCheckForPendingComputeActions() {
        if (bbModelConfig.getWrapperConfig().getAdditionalComputeActions().size() > 0) {
            doAdditionalComputeActions = true;
        }
    }

    private void checkForPendingComputeActions() {
        if (doAdditionalComputeActions) {
			System.out.println("Start Additional ComputeActions");
			if (!bbModelConfig.skipModelActionsIfInstanceDirExists()) {
				for (BBAction action : bbModelConfig.getWrapperConfig().getAdditionalComputeActions()) {
					action.run(instanceFileOrDir);
				}
			}
			System.out.println("Additional ComputeActions done");
			doAdditionalComputeActions = false;
        }
    }

    private void determineTimeHorizon() {
        ITime startTime = bbModelConfig.getStartTime();
        if (startTime == null) {
                // start time not in model config get it from exchange item
                checkForPendingComputeActions();

            //use first found time that is not null.
            String[] startTimeExchangeItemIds = bbModelConfig.getStartTimeExchangeItemIds();
            if (startTimeExchangeItemIds != null) {
                for (int n = 0; n < startTimeExchangeItemIds.length; n++) {
                    startTime = getStartOrEndTime(startTimeExchangeItemIds[n]);
                    if (startTime != null) {
                        break;
                    }
                }
            }
        }
        ITime endTime = bbModelConfig.getEndTime();
        if (endTime == null) {
                // end time not in model config get it from exchange item
                checkForPendingComputeActions();

            //use first found time that is not null.
            String[] endTimeExchangeItemIds = bbModelConfig.getEndTimeExchangeItemIds();
            if (endTimeExchangeItemIds != null) {
                for (int n = 0; n < endTimeExchangeItemIds.length; n++) {
                    endTime = getStartOrEndTime(endTimeExchangeItemIds[n]);
                    if (endTime != null) {
                        break;
                    }
                }
            }
        }
        this.timeHorizon = new Time(startTime, endTime);
    }

	private void feedTimeHorizonToModel() {
		// TODO
		// No action performed yet. Needed for black box models that - in case of EnKF -
		// need to know both:
		// - the next 'time horizon', i.e. the next run period (e.g. 24 uur)
		// - the startTime/endTime per computation period between observations (e.g. 2 hours)
		// Example of such a black box model: The sobeksim wrapper needs the time horizon to
		// adjust the mda/mdf file (containing the model schematization and the time series,
		// and the filtering computation period to indicate which time slice has to be computed
		// for the next filtering step.
		// Proposed solution: use the exchange items and aliases currentTime/startTime and
		// targetTime/endTime for the filtering period, introduce exchange items and aliases timeHorizonStart,
		// timeHorizonEnd and timeHorizonStep for the time horizon.
	}

    /**
     * Set startTime and endTime in timeInfoExchangeItems and in aliases.
     *
     * @param targetTime
     */
    private void feedComputeTimeToModel(ITime targetTime) {
        AliasDefinitions aliasDefinitions = bbModelConfig.getWrapperConfig().getAliasDefinitions();

		//set startTime.
		if (aliasDefinitions.containsKey("startTime")) {
			aliasDefinitions.setAliasValue("startTime", TimeUtils.mjdToString(currentTime.getMJD()));
		}
		String[] startTimeExchangeItemIds = bbModelConfig.getStartTimeExchangeItemIds();
		if (startTimeExchangeItemIds != null) {
			for (int n = 0; n < startTimeExchangeItemIds.length; n++) {
				setStartOrEndTime(currentTime, startTimeExchangeItemIds[n]);
			}
		}

		//set endTime.
		if (aliasDefinitions.containsKey("endTime")) {
			aliasDefinitions.setAliasValue("endTime", TimeUtils.mjdToString(targetTime.getMJD()));
		}
		String[] endTimeExchangeItemIds = bbModelConfig.getEndTimeExchangeItemIds();
		if (endTimeExchangeItemIds != null) {
			for (int n = 0; n < endTimeExchangeItemIds.length; n++) {
				setStartOrEndTime(targetTime, endTimeExchangeItemIds[n]);
			}
		}
	}

    private ITime getStartOrEndTime(String timeExchangeItemId) {
        ITime startOrEndTime = null;
        if (timeExchangeItemId != null) {
            IPrevExchangeItem timeExchangeItem = getExchangeItem(timeExchangeItemId);
            if (timeExchangeItem.getRole() != IPrevExchangeItem.Role.Input) {
                if (timeExchangeItem.getValueType() == Date.class) {
                    startOrEndTime = new Time((Date)timeExchangeItem.getValues());
                } else if (timeExchangeItem.getValueType() == ITime.class) {
                    startOrEndTime = (ITime) timeExchangeItem.getValues();
                } else if (timeExchangeItem.getValueType() == double.class ||
                        timeExchangeItem.getValueType() == Double.class) {
                    startOrEndTime = new Time((Double)timeExchangeItem.getValues());
                }
            }
        }
        return startOrEndTime;
    }

    private void setStartOrEndTime(ITime startOrEndTime, String timeExchangeItemId) {
        if (timeExchangeItemId != null) {
            IPrevExchangeItem timeExchangeItem = getExchangeItem(timeExchangeItemId);
            if (timeExchangeItem.getRole() != IPrevExchangeItem.Role.Output) {
                if (timeExchangeItem.getValueType() == Date.class) {
                    Date startOrEndTimeAsJavaDate = new Date(Time.mjdToMillies(startOrEndTime.getMJD()));
                    timeExchangeItem.setValues(startOrEndTimeAsJavaDate);
                } else if (timeExchangeItem.getValueType() == ITime.class) {
                    timeExchangeItem.setValues(startOrEndTime);
                } else if (timeExchangeItem.getValueType() == double.class ||
                        timeExchangeItem.getValueType() == Double.class) {
                    timeExchangeItem.setValues(startOrEndTime.getMJD());
                }
            }
        }
    }

    public void initialize(File workingDir, String[] arguments) {
        // no action needed (handled by model factory)
    }
}
