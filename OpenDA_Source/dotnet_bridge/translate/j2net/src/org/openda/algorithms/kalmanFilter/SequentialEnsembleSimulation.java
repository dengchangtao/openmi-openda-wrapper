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
package org.openda.algorithms.kalmanFilter;
import org.openda.interfaces.IResultWriter;
import org.openda.interfaces.IStochModelInstance;
import org.openda.interfaces.IStochObserver;
import org.openda.interfaces.ITime;
import org.openda.interfaces.IVector;
import org.openda.utils.Results;

/**
 * Simulate a model n times. The user can control the noise generation.
 * @author verlaanm
 *
 */
public class SequentialEnsembleSimulation extends AbstractSequentialEnsembleAlgorithm {

	

	@Override
	public void analysis(IStochObserver observations, IVector obsValues, IVector predictions,
			IStochModelInstance mainModel, ITime analysisTime) {
		IVector[] xi = new IVector[this.ensembleSize];
		IVector[] pred = new IVector[this.ensembleSize];
		for (int i = 0; i < this.ensembleSize; i++) {
			// collect ensemble
			xi[i] = this.ensemble[i].getState();
			// collect predictions
			pred[i] = this.ensemble[i].getObservedValues(observations.getObservationDescriptions());
			Results.putValue("pred_f_"+i, pred[i], IResultWriter.MessageType.Step);
		}
		int m = observations.getCount(); // number of observations
		int n = xi[0].getSize(); // length of the state vector
		int q = this.ensembleSize; // number of ensemble members
		// compute ensemble average and subtract it from ensemble
		IVector xiAvg = ensembleAverage(xi);
		IVector x_f = mainModel.getState();
		IVector increment = xiAvg.clone();
		increment.axpy(-1.0, x_f);
		mainModel.axpyOnState(1.0, increment);
		IVector xiStd = ensembleStd(xi);
		x_f = mainModel.getState(); //possibly modified by the model
		Results.putValue("x_f", xiAvg, IResultWriter.MessageType.Step);
		Results.putValue("x_f_std", xiStd, IResultWriter.MessageType.Step);
		removeAverage(xi, xiAvg);
		IVector predAvg = ensembleAverage(pred);
		Results.putValue("pred_f", predAvg, IResultWriter.MessageType.Step);
		IVector pred_f_std = ensembleStd(pred);
		Results.putValue("pred_f_std", pred_f_std, IResultWriter.MessageType.Step);
	}

}
