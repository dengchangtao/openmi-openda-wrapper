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

package org.openda.blackbox.config;

import org.openda.interfaces.IPrevExchangeItem;
import org.openda.interfaces.ITime;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TODO: description
 */
public class BBModelConfig {

    private BBWrapperConfig wrapperConfig;
    private Collection<BBModelVectorConfig> exchangeItems;
	private boolean skipModelActionsIfInstanceDirExists;
	private boolean doCleanUp;
    private File configRootDir;
    private ITime startTime;
    private ITime endTime;
    private String[] startTimeExchangeItemIds;
    private String[] endTimeExchangeItemIds;
	private String[] restartFileNames;
	private String savedStatesDirPrefix;

	public BBModelConfig(File configRootDir, BBWrapperConfig wrapperConfig,
						 ITime startTime, ITime endTime,
						 String[] startTimeExchangeItemIds, String[] endTimeExchangeItemIds,
						 Collection<BBModelVectorConfig> exchangeItems,
						 boolean skipModelActionsIfInstanceDirExists,
						 boolean doCleanUp,
						 String[] restartFileNames, String savedStatesDirPrefix) {
        this.configRootDir = configRootDir;
        this.wrapperConfig = wrapperConfig;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startTimeExchangeItemIds = startTimeExchangeItemIds;
        this.endTimeExchangeItemIds = endTimeExchangeItemIds;
        this.exchangeItems = exchangeItems;
		this.skipModelActionsIfInstanceDirExists = skipModelActionsIfInstanceDirExists;
		this.doCleanUp = doCleanUp;
		this.restartFileNames = restartFileNames;
		this.savedStatesDirPrefix = savedStatesDirPrefix;
	}

    public BBWrapperConfig getWrapperConfig() {
        return wrapperConfig;
    }

    public Collection<BBModelVectorConfig> getExchangeItems() {
        return exchangeItems;
    }

    public boolean doCleanUp() {
        return doCleanUp;
    }

	public boolean skipModelActionsIfInstanceDirExists() {
		return skipModelActionsIfInstanceDirExists;
	}

	public File getConfigRootDir() {
        return configRootDir;
    }

    public ITime getStartTime() {
        return startTime;
    }

    public ITime getEndTime() {
        return endTime;
    }

    public String[] getStartTimeExchangeItemIds() {
        return startTimeExchangeItemIds;
    }

    public String[] getEndTimeExchangeItemIds() {
        return endTimeExchangeItemIds;
    }

	public String[] getExchangeItemIds(IPrevExchangeItem.Role role) {
		ArrayList<String> exchangeItemIdList = new ArrayList<String>();
		for (BBModelVectorConfig subVectorConfig : exchangeItems) {
            BBModelVectorConfig exchangeItem = subVectorConfig;
			if (role.equals(exchangeItem.getRole())) {
				exchangeItemIdList.add(subVectorConfig.getId());
			}
		}
		return exchangeItemIdList.toArray(new String[exchangeItemIdList.size()]);
	}

    public String[] getExchangeItemIds() {
        ArrayList<String> exchangeItemIdList = new ArrayList<String>();
        for (BBModelVectorConfig subVectorConfig : exchangeItems) {
            exchangeItemIdList.add(subVectorConfig.getId());
        }
        return exchangeItemIdList.toArray(new String[exchangeItemIdList.size()]);
    }

	public String[] getRestartFileNames() {
		return restartFileNames;
	}

	public String getSavedStatesDirPrefix() {
		return savedStatesDirPrefix;
	}
}
