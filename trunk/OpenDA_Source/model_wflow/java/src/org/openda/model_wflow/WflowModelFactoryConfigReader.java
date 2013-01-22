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

import java.io.File;
import java.util.Date;
import java.util.TimeZone;

import org.openda.exchange.timeseries.TimeUtils;
import org.openda.model_wflow.io.castorgenerated.WflowDateTimeXML;
import org.openda.model_wflow.io.castorgenerated.WflowModelFactoryConfigXML;
import org.openda.model_wflow.io.castorgenerated.WflowTimeHorizonXML;
import org.openda.utils.io.CastorUtils;

/**
 * Configuration reader for WflowModelFactoryConfig for WFLOW model.
 *
 * @author Arno Kockx
 */
public class WflowModelFactoryConfigReader {
	private final String pythonModuleName;
	private final Date startDateTime;
	private final Date endDateTime;
	private final File caseDirectory;
	private final String templateRunId;
	private final String modelConfigFileName;
	private final String cloneMapFileName;
	private final String[] inputFilePaths;
	private final String relativeModelOutputFilePath;
	private final String relativeAnalysisOutputFilePath;

	public WflowModelFactoryConfigReader(File configFile) {
		WflowModelFactoryConfigXML castor = (WflowModelFactoryConfigXML) CastorUtils.parse(configFile, WflowModelFactoryConfigXML.class);

		this.pythonModuleName = castor.getPythonModuleName();
		if (this.pythonModuleName == null || this.pythonModuleName.isEmpty()) {
			throw new RuntimeException(getClass().getSimpleName() + ": Configured pythonModuleName must not be an empty string.");
		}

		WflowTimeHorizonXML timeHorizonCastor = castor.getTimeHorizon();
		if (timeHorizonCastor != null) {//if timeHorizon configured.
			TimeZone timeZone = TimeUtils.createTimeZoneFromDouble(timeHorizonCastor.getTimeZoneOffset());
			this.startDateTime = getDateTimeFromCastor(timeHorizonCastor.getStartDateTime(), timeZone);
			this.endDateTime = getDateTimeFromCastor(timeHorizonCastor.getEndDateTime(), timeZone);
			if (this.startDateTime.after(this.endDateTime)) {
				throw new RuntimeException(getClass().getSimpleName() + ": Configured startDateTime > configured endDateTime.");
			}
		} else {//if no timeHorizon configured.
			this.startDateTime = null;
			this.endDateTime = null;
		}

		File configDir = configFile.getParentFile();
		this.caseDirectory = new File(configDir, castor.getCaseDirectory());
		if (!this.caseDirectory.exists()) {
			throw new RuntimeException(getClass().getSimpleName() + ": Cannot find configured case directory " + this.caseDirectory.getAbsolutePath());
		}

		this.templateRunId = castor.getTemplateRunId();
		if (this.templateRunId == null || this.templateRunId.isEmpty()) {
			throw new RuntimeException(getClass().getSimpleName() + ": Configured runId must not be an empty string.");
		}

		this.modelConfigFileName = castor.getModelConfigFileName();
		File modelConfigFile = new File(this.caseDirectory, this.modelConfigFileName);
		if (!modelConfigFile.exists()) {
			throw new RuntimeException(getClass().getSimpleName() + ": Cannot find configured model config file " + modelConfigFile.getAbsolutePath());
		}

		this.cloneMapFileName = castor.getCloneMapFileName();
		File cloneMapFile = new File(this.caseDirectory, "staticmaps/" + this.cloneMapFileName);
		if (!cloneMapFile.exists()) {
			throw new RuntimeException(getClass().getSimpleName() + ": Cannot find configured clone map file " + cloneMapFile.getAbsolutePath());
		}

		this.inputFilePaths = castor.getInputFile();
		this.relativeModelOutputFilePath = castor.getModelOutputFile();
		this.relativeAnalysisOutputFilePath = castor.getAnalysisOutputFile();
	}

	public String getPythonModuleName() {
		return this.pythonModuleName;
	}

	public Date getStartDateTime() {
		return this.startDateTime;
	}

	public Date getEndDateTime() {
		return this.endDateTime;
	}

	public File getCaseDirectory() {
		return this.caseDirectory;
	}

	public String getTemplateRunId() {
		return this.templateRunId;
	}

	public String getModelConfigFileName() {
		return this.modelConfigFileName;
	}
	public String getCloneMapFileName() {
		return this.cloneMapFileName;
	}

	public String[] getRelativeInputFilePaths() {
		return this.inputFilePaths;
	}

	public String getRelativeModelOutputFilePath() {
		return this.relativeModelOutputFilePath;
	}

	public String getRelativeAnalysisOutputFilePath() {
		return this.relativeAnalysisOutputFilePath;
	}

	private static Date getDateTimeFromCastor(WflowDateTimeXML castor, TimeZone timeZone) {
		return new Date(TimeUtils.getDateTimeFromCastor(castor.getDate(), castor.getTime(), timeZone));
	}
}
