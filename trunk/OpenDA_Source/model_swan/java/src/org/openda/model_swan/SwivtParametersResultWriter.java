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

package org.openda.model_swan;

import org.openda.blackbox.config.BBUtils;
import org.openda.interfaces.IInstance;
import org.openda.interfaces.IResultWriter;
import org.openda.interfaces.ITreeVector;
import org.openda.interfaces.IVector;

import java.io.File;

/**
 * ResultWriter that produces swivt result files
 */
public class SwivtParametersResultWriter implements IResultWriter {

    private File optimalParametersFile;
    private File parameterSettingsFile;

    public SwivtParametersResultWriter(File workingDir, String parameterSettingsFileName) {
        String optimalParametersFileName = BBUtils.getFileNameWithoutExtension(parameterSettingsFileName) + "-opt.xml";
        parameterSettingsFile = new File(workingDir, parameterSettingsFileName);
        optimalParametersFile = new File(workingDir, optimalParametersFileName);
    }

    public void putMessage(Source source, String message) {
        // no action
    }

    public void putMessage(IInstance source, String message) {
        // no action
    }

    public void putValue(Source source, String id, Object result) {
        putValue("", id, result);
    }

    public void putValue(IInstance source, String id, Object result) {
        putValue("", id, result);
    }

    public void putValue(String source, String id, Object result) {
        if (!(result instanceof ITreeVector)) {
            throw new UnsupportedOperationException("SwivtParametersResultWriter.putMessage(): unexpected result type " + result.getClass());
        }
        SwanXMLParameterSettings parameterSettings = new SwanXMLParameterSettings(parameterSettingsFile);
        ITreeVector treeVector = (ITreeVector) result;
        for (String subTreeVectorId : treeVector.getSubTreeVectorIds()) {
            double parameterValue = treeVector.getSubTreeVector(subTreeVectorId).getValues()[0];
            parameterSettings.setValue(subTreeVectorId, parameterValue);
        }
        parameterSettings.writeToFile(optimalParametersFile);
    }

    public void putValue(Source source, String id, Object result, int iteration) {
        throw new UnsupportedOperationException("SwivtParametersResultWriter.putMessage(..., iteration) should not be called");
    }

	@Override
	public void putValue(Source source, String id, Object result, String outputLevel, String context, int iteration) {
		throw new UnsupportedOperationException("Class Name : org.openda.model_swan.SwivtParametersResultWriter - Method Name : putValue");
	}

	public void putValue(IInstance source, String id, Object result, int iteration) {
        throw new UnsupportedOperationException("SwivtParametersResultWriter.putMessage(..., iteration) should not be called");
    }

    public void putIterationReport(IInstance source, int iteration, double cost, IVector parameters) {}

    public void free() {
        // for now: no action needed
        // TODO: write parameters only here.
    }
}
