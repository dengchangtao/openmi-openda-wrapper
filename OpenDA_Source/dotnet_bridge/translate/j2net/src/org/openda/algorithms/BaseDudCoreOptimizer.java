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
package org.openda.algorithms;

import org.openda.interfaces.*;
import org.openda.utils.Matrix;
import org.openda.utils.Results;
import org.openda.utils.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseDudCoreOptimizer {

	// fields of this class
	protected IVector pCurrent[] = null;    // parametervalues under consideration
	protected IVector predCurrent[] = null; // predictions for each parameter vector
	protected double fCurrent[] = null;     // costs for each
	protected LeastSquaresCostFunction f = null;

	protected int number_of_stored_runs;
	protected int number_of_evaluations;  //number of evaluations (without initial evaluation)
	protected double initialStep = 1.0; // scaling for initial stepsize
	protected double costFactor;            // multiplication factor for cost

	// settings
	public int maxit = 3;                // maximum number of outer iterations
	public int maxInnerIter = 6;         // maximum number of inner iterations
	public double innerScaleFac = 0.5;   // scaling factor for bactracking
	public int minInnerNegativeLook = 3; // when to start looking into the negative direction
	public double maxStep = 10.0;        // maximum relative step size (compared to the spread of the parameters)
	public double absTol = 0.01;         // absolute criterion
	public double relTol = 0.01;         // relative criterion

	// required for the additional stopping criteria:
	public List<IStopCriterion> stopCriteria = new ArrayList<IStopCriterion>();
	public List<Double> stopCriteriaThreshold = new ArrayList<Double>();
	public double stopCritThresDefault = 0.01;
	public IObservationDescriptions obsDescr=null;

	// caching
	protected ISqrtCovariance LPar = null;
	protected IVector parInit = null;
	protected IStochVector svObs = null;
	protected IVector obs = null;
	protected IVector sigmaObs = null;


	// stopping
	boolean moreToDo = true; // is this optimization finished
	int imain = 0;           // main iterations done

	protected IMatrix sqrtCovEst = null;   // estimate of square root of error covariance
	public double relTolLinCost = 0.01;  // compares linearity error to improvement
	public boolean computeErrorEstimate=true;

	protected int numberOfSearchDirections() {
		IStochVector parameterUncertainty = f.getParameterUncertainty();
		ISqrtCovariance sqrtCovariance = parameterUncertainty.getSqrtCovariance();
		IVector[] vectorArray = sqrtCovariance.asVectorArray(); 
		return vectorArray.length; 
	}

	protected abstract double InitialSearchStep(int i, int j);
	
	/**
	 * Initialization for Dud minimization - used for restarting the optimization process
	 * @param pars : parameters as Vector at each node of Dud
	 * @param costValues : corresponding cost values, so there is no need to recompute them (saves time)
	 * @param predictions : corresponding predictor values
	 */
	public void initialize(IVector pars[], double costValues[], IVector[] predictions){

		this.pCurrent = new IVector[number_of_stored_runs];
		this.fCurrent = new double[number_of_stored_runs];
		this.predCurrent = new IVector[number_of_stored_runs];
		for (int i = 0; i < pars.length; i++) {
			this.pCurrent[i] = pars[i].clone();
			this.fCurrent[i] = costValues[i];
			this.predCurrent[i] = predictions[i].clone();
		}

		// Initialize the cost functions with invalid values (so they'll be at the end when sorted)
		for (int i = pars.length; i < number_of_stored_runs ; i++) { this.fCurrent[i] = 1.0E30 ;}

		f.evaluate(pars[number_of_evaluations], "initialization node " + number_of_evaluations);

		number_of_evaluations = pars.length-1;
		IStochVector parameterUncertainty = f.getParameterUncertainty();
		this.LPar = parameterUncertainty.getSqrtCovariance();
		this.parInit = parameterUncertainty.getExpectations();
		this.svObs = f.getObservationUncertainty();
		sortByCostAndCashObservationData();
	}

	public void initialize(IVector pInit){
		IStochVector parameterUncertainty = f.getParameterUncertainty();
		this.LPar = parameterUncertainty.getSqrtCovariance();
		this.parInit = parameterUncertainty.getExpectations();

		IVector[] searchDirections = this.LPar.asVectorArray();
		int m = searchDirections.length; // Number of independent directions in uncertainty; often equal to number of pars


		// generate initial perturbations
		this.pCurrent = new IVector[number_of_stored_runs];
		this.predCurrent = new IVector[number_of_stored_runs];
		this.fCurrent = new double[number_of_stored_runs];

		// Initialize the cost functions with invalid values (so they'll be at the end when sorted)
		for (int i = 1;	i < number_of_stored_runs ; i++) { this.fCurrent[i] = 1.0E30 ;}

		// Calculate a number of parameter sets to be evaluated. 
		this.pCurrent[0] = pInit.clone();
		for (int i = 0; i < number_of_evaluations; i++) {
			pCurrent[i+1] = pInit.clone();
			for (int j = 0; j < m; j++) {
				pCurrent[i+1].axpy(initialStep * InitialSearchStep(i,j), searchDirections[j]);
			}
		}
		
		// evaluate for these parameters
		if(this.f.getTryParallel()){ 
			//start models in parallel by splitting loop in two loops
			LeastSquaresCostFunction costFunctions[] = new LeastSquaresCostFunction[number_of_evaluations+1];
			
			for(int i=0;i<=number_of_evaluations;i++){ //start all the evaluations in parallel
				Results.putMessage("Prepare for evaluating with parameters "+pCurrent[i].printString(" "));
				if (i>0) { costFunctions[i]=f.clone();}
				else     { costFunctions[i]=f;} //no need to copy first one
				costFunctions[i].prepare(pCurrent[i]);
			}
			
			for (int i=0;i<=number_of_evaluations;i++) { //collect the results
				Results.putMessage("Evaluating with parameters "+pCurrent[i].printString(" "));
				fCurrent[i] = costFunctions[i].evaluate(pCurrent[i],"initialization node "+i);
				predCurrent[i] = costFunctions[i].getLastPredictions();
			}
			
		} else { //this loop uses a sequential blocking call to each model
			
			for (int i=0;i<=number_of_evaluations;i++) {
				Results.putMessage("Evaluating with parameters "+pCurrent[i].printString(" "));
				fCurrent[i] = f.evaluate(pCurrent[i],"initialization node "+i);
				predCurrent[i] = f.getLastPredictions();
			}
		}
		this.svObs = f.getObservationUncertainty();
		sortByCostAndCashObservationData();
	}


	protected void sortByCostAndCashObservationData(){

		// Sort parameters tried so far by cost
		this.costFactor = this.f.getMultiplicationFactor();
		double[] costs = this.fCurrent;
		IVector[] pars = this.pCurrent;
		IVector[] preds = this.predCurrent;
		int[] isort=this.sortedIndex(costs);
		double[] costs_sort = this.applyIndexToDoubles(costs, isort);
		IVector[] pars_sort=this.applyIndexToVectors(pars, isort);
		IVector[] preds_sort=this.applyIndexToVectors(preds, isort);
		this.pCurrent  = pars_sort;
		this.fCurrent = costs_sort;
		this.predCurrent = preds_sort;

		// cache some data from observations
		this.obs = this.svObs.getExpectations();
		if (this.svObs.hasCorrelatedElements()) {
			throw new RuntimeException(
					"Dud optimization not implemented for correlated observations yet.");
		}
		this.sigmaObs = this.svObs.getStandardDeviations();
	}

	/**
	 * Get the optimal parameters, i.e. the result of the minimization
	 *
	 * @return parameters as Vector
	 */
	public IVector getOptimalValue() {
		return this.f.getOptimalParameters();
	}

	/**
	 * Get the cost value at the optimum
	 *
	 * @return cost
	 */
	public double getOptimalCost() {
		return this.f.getOptimalCost();
	}

	/**
	 * Get parameters for each point of the Dud. The current Dud equals the initial
	 * Dud if requested before optimization and final 
	 * Dud if f.optimize() has been called.
	 *
	 * @return Vector for parameters at each nod of the Dud vectors
	 */
	public IVector[] getCurrentValues() {
		IVector result[] = new IVector[number_of_evaluations+1];
		for (int i = 0; i <= number_of_evaluations; i++) {
				result[i] = this.pCurrent[i].clone();
		}
		return result;
	}

	/**
	 * Get the current cost values corresponding to the nodes of the current
	 * Dud.
	 *
	 * @return Cost at each node as array of doubles
	 */
	public double[] getCurrentCosts() {
		double result[] = new double[number_of_evaluations+1];
		for (int i = 0; i <= number_of_evaluations; i++) {result[i] = this.fCurrent[i];} 
		return result;
	}

	/**
	 * Main optimization routine
	 */
	public void optimize() {
		while (this.hasNext()) {
			this.next();
		}
	}

	/**
	 * Are there any more steps for this algorithm
	 *
	 * @return has next step
	 */
	boolean hasNext() {
		return this.moreToDo;
	}

	/**
	 * Return an estimate of the uncertainty of the parameters as square-root of the covariance
	 * @return sqrt cov
	 */
	public IMatrix getSqrtCovariance(){
		IMatrix result = null;
		if(this.sqrtCovEst!=null){
			result=this.sqrtCovEst.clone();
		}
		return result;
	}
	
	
	boolean isThereMoreToDo(double costs[], double relErrorLinCost, IVector predTry, IVector pars[], int innerIter) {
		/* This function checks using all the criteria whether another iteration is still needed. */
		double diff = costs[1] - costs[0];
		double relDiff=diff/Math.abs(costs[0]);
		Results.putMessage("stop criterion 1, imain                            > maxit :\t " + imain   + " < " + maxit);
		Results.putMessage("stop criterion 2, |new - previous cost|            < abstol:\t " + diff    + " > " + absTol);
		Results.putMessage("stop criterion 3, |new - previous cost|/|new cost| < reltol:\t " + relDiff + " > " + relTol);
		Results.putMessage("stop criterion 4, linearized cost relative error: "+relErrorLinCost+" < "+relTolLinCost);

		boolean moreToDo = 
				imain < this.maxit  
				&& diff > this.absTol
				&& diff > relTol * Math.abs(costs[0])
				&& relErrorLinCost>relTolLinCost;

		// additional stop criteria:
		if (stopCriteria.size()>0) {
			IVector residual = obs.clone();
			residual.axpy(-1.0,predTry);
			boolean isStop = false;
			for (int i=0; i<stopCriteria.size(); i++){
				IStopCriterion object = stopCriteria.get(i);
				double threshold = stopCriteriaThreshold.get(i);
				if (obsDescr!=null) {
					isStop = object.checkForStop(pars[0],residual,obsDescr,costs[0],threshold);
				} else {
					isStop = object.checkForStop(pars[0],residual,costs[0],threshold);
				}
				Results.putMessage(object.toString());
				this.moreToDo = this.moreToDo & !isStop;
			}
		}
		
		// do not stop on a backtracking iteration
		if (innerIter>0 && (imain < maxit)) moreToDo = true;

		return moreToDo;
	}
	
	protected abstract Matrix CalculateGradSimu();

	protected abstract Matrix CalculateParsMatrix();

	protected abstract Matrix BackgroundMatrix();

	/**
	 * Run next step of the algorithm
	 */
	void next() {
		
		if (!this.moreToDo) {
			f.writeResults();
			return;
		}

		/*
		 * This function performs one outer iteration for the minimization of the 
		 * nonlinear least squares system, given by
		 * 
		 *      [ sigmaObs\f.evaluate(pars) ]   [ sigmaObs\obs  ]
		 *      [                           ] = [               ]
		 *      [        LPar\pars          ]   [  LPar\parInit ]
		 * 
		 * The system involves the following notations:
		 * 
		 * Solvable values:
		 *    p          - the parameters: the values which are the solution of this optimization process
		 *    
		 * The observation-prediction equations:
		 *    f.evaluate - the function which calculates the predictions from the parameters. These 
		 *                 predictions should correspond to the observations obs
		 *    obs        - the observations, which the predictions should be made to correspond to by
		 *                 choosing the parameters right
		 *    sigmaObs   - a diagonal matrix: standard deviations of observations, used to scale 
		 *                 the observation-prediction equations
		 *                 
		 * The background term (optional):
		 *    LPar       - sqrt-covariance matrix for parameters: scaling for background term 
		 *    parInit    - the values for the parameters which the parameters should be as close as possible to:
		 *                 a reasonable parameter set.
		 */
		
		// get data needed by the algorithm
		double[] costs    = this.fCurrent; // local copies
		IVector[] pars    = this.pCurrent;
		IVector[] preds   = this.predCurrent;
		int ndirs         = number_of_evaluations; // number of search directions
		int npars         = pars[0].getSize();     // number of elements for a parameter vector

		imain++;
		Results.putProgression("======================================================");
		Results.putProgression("DUD outer iteration no." + imain);
		Results.putProgression("======================================================");
		Results.putMessage("-----------------------------------------------------");
		Results.putValue("costs", new Vector(costs), costs.length, "essential", "any", IResultWriter.MessageType.OuterIteration);
		Results.putMessage("-----------------------------------------------------");

		/* 
		 * Build least squares problem.
		 * The best iterand we have is pars[0] (with predictions preds[0] = f.evaluate(pars[0]))
		 * 
		 * A linear system will be set up for pStep, which from which the next iterand pars can be 
		 * calulated as
		 * 
		 *    pars = pars[0] + P_matrix * pStep
		 * or (when backtracking)
		 *    pars = pars[0] + scale_step * P_matrix * pStep
		 */
		Matrix P_matrix = CalculateParsMatrix();

		/* The linearization of the observation-prediction equations wrt pStep is
		 * 
		 *    sigmaObs \ (f.evaluate(pars) - obs) =.= 
		 *    
		 *    sigmaObs \ (preds[0]-obs) + 
		 *    sigmaObs \ (preds[1]-preds[0], ... , preds[n]-preds[0]) * pStep.
		 *    
		 * The matrix A_obs and the vector rhs_obs are now calculated, so that this linearization 
		 * can be written as
		 * 
		 *    sigmaObs \ (f.evaluate(pars) - obs) =.= -rhs_obs + A_obs * pStep
		 */		
		IVector rhs_obs = obs.clone(); 
		rhs_obs.axpy(-1.0, preds[0]);
		rhs_obs.pointwiseDivide(sigmaObs);
		Results.putMessage("RHS norm: "+rhs_obs.norm2());
		

		/* Calculate the gradient A_obs using the selected scaling */
		Matrix A_obs = CalculateGradSimu();

		Matrix A;
		IVector rhs; // right-hand-side for solve

		// additional term for background errors
		if (this.f.doAddBackgroundTerm()) {
			/*
			 * The background term is added. It is linear, and given in terms of pStep by
			 * 
			 *    LPar \ (par - parInit) = 
			 *          LPar \ (pars[0] - parInit) +  LPar \ P_matrix * pStep
			 *          
			 * The matrix A_back and the vector rhs_back are now calculated so that 
			 * the background term is written as
			 *
			 *    LPar \ (par - parInit) =  -rhs_back + A_obs * pStep
			 */
			IVector rhs_back = parInit.clone();
			rhs_back.axpy(-1.0,pars[0]);
			LPar.rightSolve(rhs_back,rhs_back);

			Matrix A_back = BackgroundMatrix(); 

			/* The complete Least Square system for pStep is now composed: */
			A = Matrix.concatVertical(A_obs,A_back);
			rhs = Vector.concatenate(rhs_obs,rhs_back);
		} else {
			A = A_obs; 
			rhs = rhs_obs;
		}
		
		// solve for next step (relative)
		IVector pStep = new Vector(npars);
		A.rightSolve(rhs, pStep);

		Results.putProgression("Start search until improvement,");

		/* Reduce step size if any of the parameter steps is too large 
		 * The scaling is with respect to the pStep, which means we prefer to stay 
		 * inside or not too far outside the convex hull of the available parameter sets.
		 */
		double maxStep = infNorm(pStep);
		if (maxStep > this.maxStep) {
			Results.putMessage("% Reducing stepsize! Relative step was:"+pStep);
			pStep.scale(this.maxStep / maxStep);
		}

		// Evaluate 'linear cost'
		IVector res_lin = rhs.clone();
		A.rightMultiply(1.0, pStep, -1.0, res_lin);  // res_lin = A * pStep - rhs
		double costLinTry = res_lin.norm2();         // costLin = |res_lin|^2
		costLinTry = costLinTry*costLinTry*this.costFactor;

		Results.putValue("linearCost", costLinTry, 1, "normal", "outer iteration "+imain, IResultWriter.MessageType.OuterIteration);		
		System.out.println("linearized cost for new estimate "+costLinTry);

		// use backtracking (smaller steps) if cost has not improved
		int innerIter = 0;
		double scale_step = 1.0;
		IVector predTry, pTry;
		double costTry=0;
		double relErrorLinCost = 0;

		while (true) {
			
			/* Calculate the new parameter set */
			pTry = pars[0].clone();
			P_matrix.rightMultiply(scale_step, pStep, 1.0, pTry);
			Results.putMessage("Next try p="+pTry.printString("   "));

			/* Evaluate predictions for this parameter */
			if (innerIter==0) {
				costTry = this.f.evaluate(pTry,"outer iteration "+imain);
				System.out.println("non-linear cost for new estimate "+costTry);
				relErrorLinCost = Math.abs((costTry-costLinTry)/(costs[0]-costLinTry));
			} else {
				costTry = f.evaluate(pTry,"inner iteration "+innerIter);
			}
			predTry = f.getLastPredictions();

			if (costTry <= costs[0] || innerIter > this.maxInnerIter) break;
			innerIter++;

			Results.putMessage("Cost is worse! Reducing stepsize.");
			if ( innerIter == minInnerNegativeLook+2 && innerScaleFac > 0.0) {
				System.out.println("This was inner it " + innerIter + ": start looking in negative direction");
				Results.putMessage("Start looking in negative direction");
				innerScaleFac *= -1.0;
			}
			scale_step *= innerScaleFac;
		}

		// compute analysis error covariance from approximate Hessian
		// Pest = P_matrix*inv(A'*A)*P_matrix'
		if(this.computeErrorEstimate){
			boolean transpose = true;
				
			Matrix Pinv = Matrix.mult(A,A,transpose,!transpose);
			
			/* Singular value decomposition of Pinv:
			 *  Pinv =: svd[2] * svd[1] * svd[0]' */
			Matrix svd[] = Pinv.svd();
			
			// svd[1] = inv(svd[1])
			int np = A.getNumberOfColumns();
			for(int i=0;i<np;i++){
				svd[1].setValue(i, i, 1.0/svd[1].getValue(i,i));
			}
						
			/* Calculate the covariance matrix 
			 *   Pest = P_matrix* svd[0] * svd[1] * svd[2]' *P_matrix'
			 */
			Matrix Pest = Matrix.mult(Matrix.mult(Matrix.mult(P_matrix,svd[0]),svd[1]),
									Matrix.mult(svd[2],P_matrix,transpose,transpose));
			
			// standard deviations
			Vector std = (Vector) Pest.diag();
			std.sqrt();
			
			// now make this pretty for treeVectors
			IVector stdWithStructure = pTry.clone();
			stdWithStructure.setValues(std.getValues());
			Results.putMessage("Error estimate for this outer iteration");
			Results.putValue(	"parameterErrorEstimateStd", stdWithStructure, stdWithStructure.getSize(), 
								"verbose", "outer iteration "+imain, IResultWriter.MessageType.OuterIteration);
			/*
			 * compute correlations
			 */
			Vector stdInverse = std.clone();
			stdInverse.invert();
			IMatrix stdOnDiag = Matrix.diag(stdInverse);
			IMatrix correlations = Matrix.mult(stdOnDiag, Matrix.mult(Pest,stdOnDiag));
			//TODO: use the extended putValue that takes information on size, output level and context.
			Results.putValue("parameterErrorCorrelations", correlations, IResultWriter.MessageType.OuterIteration);

			// sqrtP : required as output for some algorithms that extend DUD
			for(int i=0;i<np;i++){
				svd[1].setValue(i, i, Math.sqrt(svd[1].getValue(i, i)));
			}
			this.sqrtCovEst = Matrix.mult(Matrix.mult(Matrix.mult(P_matrix,svd[0]),svd[1]),svd[2],!transpose, transpose);
		}

		//setup for next iteration   (worst evaluation is overwritten!)
		preds[ndirs] = predTry;
		costs[ndirs] = costTry;
		pars[ndirs]  = pTry;

		// Sort parameters tried by cost
		int[] isort  = this.sortedIndex(costs);
		costs = applyIndexToDoubles(costs, isort);
		pars  = applyIndexToVectors(pars, isort);
		preds = applyIndexToVectors(preds, isort);
		
		this.moreToDo = isThereMoreToDo(costs, relErrorLinCost, predTry, pars, innerIter);

		// Display final results:
		if (!this.moreToDo) this.f.writeResults();

		// store results to object
		this.pCurrent = pars;
		this.predCurrent = preds;
		this.fCurrent = costs;

	}

	// ============================================================================================
	// only supporting routines below this line



	/**
	 * Sort an array and return the result as an array of indices
	 *
	 * @param values : to be sorted
	 * @return indices : first value points to smallest value and last one to largest
	 */
	private int[] sortedIndex(double[] values) {

		class indexValuePair {
			int index;
			double value;

			public indexValuePair(int index, double value) {
				this.index = index;
				this.value = value;
			}
		}
		class ValueComparator implements java.util.Comparator<indexValuePair> {
			public int compare(indexValuePair o1, indexValuePair o2) {
				return new Double(o1.value).compareTo(o2.value);
			}
		}

		int[] result = new int[values.length];
		java.util.ArrayList<indexValuePair> sortedValues = new java.util.ArrayList<indexValuePair>();
		for (int i = 0; i < values.length; i++) {
			sortedValues.add(new indexValuePair(i, values[i]));
		}
		ValueComparator vc = new ValueComparator();
		java.util.Collections.sort(sortedValues, vc);
		int j = 0;
		for (java.util.Iterator<indexValuePair> it = sortedValues.iterator(); it.hasNext();) {
			result[j] = it.next().index;
			j++;
		}
		return result;
	}

	/**
	 * Use indices from sorting to sort an additions array of Vectors
	 * Vectors are not copied, only the references are switched
	 * @param vs Vectors to be sorted
	 * @param index indices from sorting
	 * @return sorted vectors
	 */
	private IVector[] applyIndexToVectors(IVector[] vs, int[] index) {
		IVector[] result = new IVector[vs.length];
		for (int i = 0; i < vs.length; i++) {
			result[i] = vs[index[i]];
		}
		return result;
	}

	/**
	 * Use indices from sorting to sort an additions array of doubles
	 * Sorts on a copy.
	 * @param cs doubles to be sorted
	 * @param index indices from sorting
	 * @return sorted doubles
	 */
	private double[] applyIndexToDoubles(double[] cs, int[] index) {
		double[] result = new double[cs.length];
		for (int i = 0; i < cs.length; i++) {
			result[i] = cs[index[i]];
		}
		return result;
	}

	private double infNorm(IVector v) {
		double result = 0.0;
		int n = v.getSize();
		double temp;
		for (int i = 0; i < n; i++) {
			temp = Math.abs(v.getValue(i));
			if (temp > result) { result = temp; }
		}
		return result;
	}

	public IVector[] getPredCurrent() {
		IVector result[] = new IVector[number_of_evaluations+1];
		for (int i = 0; i <= number_of_evaluations; i++) {
				result[i] = this.predCurrent[i].clone();
		}
		return result;
	}
}
