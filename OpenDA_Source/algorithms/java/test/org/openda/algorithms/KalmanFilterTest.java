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
import junit.framework.TestCase;

import org.openda.algorithms.kalmanFilter.*;
import org.openda.interfaces.*;
import org.openda.models.lorenz.LorenzStochModelFactory;
import org.openda.models.oscillator.OscillatorStochModelFactory;
import org.openda.observers.NoosTimeSeriesStochObserver;
import org.openda.resultwriters.MatlabResultWriter;
import org.openda.utils.CsvStochObserver;
import org.openda.utils.Results;
import org.openda.utils.OpenDaTestSupport;
import org.openda.utils.StochVector;
import org.openda.utils.Vector;

import java.io.*;

public class KalmanFilterTest extends TestCase {

	private File testRunDataDir;
	private OpenDaTestSupport testData;
	private long initSeed=1234567890;

	protected void setUp() throws IOException {
		testData = new OpenDaTestSupport(KalmanFilterTest.class,"algorithms");
		testRunDataDir = testData.getTestRunDataDir();
		generateObservations();
	}

	private void generateObservations() {
		System.out.println("========================================================");
		System.out.println("Generate observations, evaluate cost at 1st guess and opt.");
		System.out.println("Noise is added for this twinexperiment");
		System.out.println("========================================================");
		//generate observations
		StochVector.setSeed(1234); //fix random numbers for repeatable results
		IStochModelFactory fact = new OscillatorStochModelFactory();
		fact.initialize(null, new String[]{""});
		IStochModelInstance model = fact.getInstance(IStochModelFactory.OutputLevel.Suppress);
		model.setAutomaticNoiseGeneration(false);
		// Dummy observations
		IVector time   = Vector.range(0.0, 10.0, 1.0);
		System.out.println("obstimes = "+time);
		System.out.println("Should be obstimes = [0.0,1.0,1.0,2.0,3.0,4.0,5.0,6.0,7.0,8.0,9.0,10.0]");
		int n         = time.getSize();
		IVector values = new Vector(n);
		IVector stdVal = new Vector(n); stdVal.setConstant(0.1);
		IVector indVal = new Vector(n); indVal.setConstant(0.0);
		IVector[] columns= new IVector[4];
		columns[0] = time;
		columns[1] = indVal;
		columns[2] = values;
		columns[3] = stdVal;
		String[] keys = {"time","index","value","std"};
		IStochObserver obs = new CsvStochObserver(columns,keys);
		// Now run model to generate data for these obs
		IObservationDescriptions descr = obs.getObservationDescriptions();
		model.announceObservedValues(descr);
		model.compute(model.getTimeHorizon().getEndTime());
		IVector prd = model.getObservedValues(descr);
		// create new observer with these generated values
		columns[2] = prd;
		CsvStochObserver obsGenerated = new CsvStochObserver(columns,keys);

		//write generated observations to file
		obsGenerated.toFile(testRunDataDir, "observations_oscillator_generated.csv");

		//get true state at final time for reference
		IVector xTrue = model.getState();
		System.out.println("xTrue = "+xTrue);
		model.finish();
	}


	public void testEnKF() {
		System.out.println("========================================================");
		System.out.println(" Test EnKF (Classical Ensemble Kalman Filter.");
		System.out.println("========================================================");
		//generate observations
		StochVector.setSeed(initSeed);
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated = new CsvStochObserver();
		obsGenerated.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		AbstractSequentialEnsembleAlgorithm enkf = new EnKF();
		enkf.initialize(null, new String[]{"<algorithm><ensembleModel stochParameter=\"false\" stochForcing=\"true\" stochInit=\"true\" /></algorithm>"});
		enkf.setStochComponents(obsGenerated,factory);
		enkf.prepare();
		enkf.run();
		// state at final time
		IVector x = ((EnKF) enkf).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be close to x = [-0.2315739613743379,0.028205645374715847]");
		double delta=0.0001;
		assertEquals("x[0]",-0.2315739613743379,x.getValue(0),delta);
		assertEquals("x[1]",0.028205645374715847,x.getValue(1),delta);

	}

	public void testEnSSKF() {
		System.out.println("=====================================================================");
		System.out.println(" The EnSSKF (Ensemble based Steady State Kalman Filter)");
		System.out.println(" has been replaced with the option to save the gain for the EnKF algorithm");
		System.out.println("=====================================================================");
	}

	public void testParticleFilter() {
		System.out.println("========================================================");
		System.out.println(" Test Residual Resampling Particle Filter");
		System.out.println("========================================================");
		//generate observations
		StochVector.setSeed(initSeed);
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated = new CsvStochObserver();
		obsGenerated.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		IAlgorithm sir = new ParticleFilter();
		sir.initialize(null, new String[]{""});
		sir.setStochComponents(obsGenerated,factory);
		sir.prepare();
		sir.run();
		// state at final time
		IVector x = ((ParticleFilter) sir).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be close to x = [-0.2201282418216555,-0.016207562672629516]");
		assertEquals("x",x.toString(),"[-0.2201282418216555,-0.016207562672629516]");

	}

	public void testEnSR() {
		System.out.println("========================================================");
		System.out.println(" Test EnSR (Ensemble square-root Kalman Filter.");
		System.out.println("========================================================");
		StochVector.setSeed(initSeed);
		IResultWriter results = new MatlabResultWriter(testRunDataDir,"ensr_output.m");
		Results.addResultWriter(results);
		//generate observations
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated = new CsvStochObserver();
		obsGenerated.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		IAlgorithm ensr = new EnSR();
		ensr.initialize(null, new String[]{""});
		ensr.setStochComponents(obsGenerated,factory);
		ensr.prepare();
		ensr.run();
		// state at final time
		IVector x = ((EnSR) ensr).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be close to x = [-0.2302707957065198,-0.012500723757634615]");
		assertEquals("x",x.toString(),"[-0.2302707957065198,-0.012500723757634615]");

	}

	public void testDudEnKF() {
		System.out.println("========================================================");
		System.out.println(" Test IterEnKF (Iterative Ensemble Kalman Filter.");
		System.out.println("========================================================");
		StochVector.setSeed(initSeed);
		//generate observations
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,1.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated = new CsvStochObserver();
		obsGenerated.initialize(testRunDataDir,new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		IAlgorithm dudEnkf = new DudEnKF();
		dudEnkf.initialize(testRunDataDir,new String[]{"<EnkfConfig><ensembleSize>30</ensembleSize></EnkfConfig>"});
		dudEnkf.setStochComponents(obsGenerated,factory);
		dudEnkf.prepare();
		dudEnkf.run();
		// state at final time
		IVector x = ((DudEnKF) dudEnkf).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be close to x = [-0.024586564771001445,-0.9477825878310395]");
		System.out.println("Result from EnKF x= [0.05954607572438088,-0.9517998843748028]");
		assertEquals("x",x.toString(),"[-0.024586564771001445,-0.9477825878310395]");
	}

	public void testDudEnKF2() {
		System.out.println("========================================================");
		System.out.println(" Test IterEnKF (Iterative Ensemble Kalman Filter.");
		System.out.println(" Lorenz with non-linear observations x^2 , y^2 , z^2.");
		System.out.println("========================================================");
		StochVector.setSeed(initSeed);
		//generate observations
		IStochModelFactory factory = new LorenzStochModelFactory();
		factory.initialize(null, new String[]{"<lorenzConfig> <simulationTimespan>[0.0,0.05,0.05]</simulationTimespan>  <systemNoise>{[0.0,0.0,0.0],[0.01,0.01,0.01]}</systemNoise><initialState>[1.508870, -1.531271, 25.46091]</initialState><initialStateUncertainty>[0.5,0.5,0.5]</initialStateUncertainty></lorenzConfig>"});
		String content = "time,index,value,std,transform\n"
			+ "0.05,0.0,0.25,0.5,2.0\n"
			+ "0.05,1.0,2.25,0.5,2.0\n"
			+ "0.05,2.0,484,0.5,2.0";
		//xInit = [1.508870, -1.531271, 25.46091]
		// several instances depends on initial errors and forecast step, through random number
		// generator
		//x_f = [0.2613880289792195,-1.4277572641360385,22.22610906587824]
		//x_f = [0.4261966847488037,-1.2594074531669859,22.151107007795208]
		// y= [(0.5)^2=0.25 , (-1.5)^2=2.25 , (22)^2=484 ]
		IStochObserver obsGenerated = new CsvStochObserver(content);

		//create and run filter
		IAlgorithm dudEnkf = new DudEnKF();
		dudEnkf.initialize(testRunDataDir,new String[]{"<EnkfConfig><ensembleSize>30</ensembleSize></EnkfConfig>"});
		dudEnkf.setStochComponents(obsGenerated,factory);
		dudEnkf.prepare();
		dudEnkf.run();
		// state at final time
		IVector x = ((DudEnKF) dudEnkf).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be x = [0.25640433874292234,-1.4990550803890883,22.16077875314083]");
		assertEquals("x",x.toString(),"[0.25640433874292234,-1.4990550803890883,22.16077875314083]");
	}

	public void testDudENSR() {
		System.out.println("========================================================");
		System.out.println(" Test DudENSR (Iterative Ensemble Square Root Filter.");
		System.out.println("========================================================");
		StochVector.setSeed(initSeed);
		//generate observations
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,0.05]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated = new CsvStochObserver();
		obsGenerated.initialize(testRunDataDir,new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		IAlgorithm dudENSR = new DudENSR();
		dudENSR.initialize(testRunDataDir,new String[]{"<DudENSRConfig><ensembleSize>30</ensembleSize></DudENSRConfig>"});
		dudENSR.setStochComponents(obsGenerated,factory);
		dudENSR.prepare();
		dudENSR.run();
		// state at final time
		IVector x = ((DudENSR) dudENSR).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be x = [0.7470077663835176,-0.1335742967929108]");
		double eps=0.0001;
		assertEquals("x",x.getValue(0),0.7470077663835176,eps);
		dudENSR.finish();
	}

	public void testDudENSR2() {
		System.out.println("========================================================");
		System.out.println(" Test IterEnKF (Iterative Ensemble Kalman Filter.");
		System.out.println(" Lorenz with non-linear observations x^2 , y^2 , z^2.");
		System.out.println("========================================================");
		StochVector.setSeed(initSeed);
		//generate observations
		IStochModelFactory factory = new LorenzStochModelFactory();
		factory.initialize(null, new String[]{"<lorenzConfig> <simulationTimespan>[0.0,0.05,0.05]</simulationTimespan>  <systemNoise>{[0.0,0.0,0.0],[0.01,0.01,0.01]}</systemNoise><initialState>[1.508870, -1.531271, 25.46091]</initialState><initialStateUncertainty>[0.5,0.5,0.5]</initialStateUncertainty></lorenzConfig>"});
		String content = "time,index,value,std,transform\n"
			+ "0.05,0.0,0.25,0.5,2.0\n"
			+ "0.05,1.0,2.25,0.5,2.0\n"
			+ "0.05,2.0,484,0.5,2.0";
		//xInit = [1.508870, -1.531271, 25.46091]
		// several instances depends on initial errors and forecast step, through random number
		// generator
		//x_f = [0.2613880289792195,-1.4277572641360385,22.22610906587824]
		//x_f = [0.4261966847488037,-1.2594074531669859,22.151107007795208]
		// y= [(0.5)^2=0.25 , (-1.5)^2=2.25 , (22)^2=484 ]
		IStochObserver obsGenerated = new CsvStochObserver(content);

		//create and run filter
		IAlgorithm dudENSR = new DudENSR();
		dudENSR.initialize(testRunDataDir,new String[]{"<DudENSRConfig><ensembleSize>30</ensembleSize></DudENSRConfig>"});
		dudENSR.setStochComponents(obsGenerated,factory);
		dudENSR.prepare();
		dudENSR.run();
		// state at final time
		IVector x = ((DudENSR) dudENSR).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be x = [0.37471476165521217,-1.3119835245493154,22.306240560178487]");
		assertEquals("x",x.toString(),"[0.37471476165521217,-1.3119835245493154,22.306240560178487]");
		dudENSR.finish();
	}
	
	public void testEnKF_input() {
		System.out.println("========================================================");
		System.out.println(" Test input parsing and settings.");
		System.out.println("========================================================");
		//generate observations
		StochVector.setSeed(initSeed);
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated = new CsvStochObserver();
		obsGenerated.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		String kalmanConfig = "<EnKF><ensembleSize>100</ensembleSize><analysisTimes type=\"fixed\" timeFormat=\"mjd\" >0.0,1.0,...,10.0</analysisTimes></EnKF>";
		//String kalmanConfig = "<EnKF><ensembleSize>100</ensembleSize></EnKF>";
		IAlgorithm enkf = new EnKF();
		enkf.initialize(null, new String[]{kalmanConfig});
		enkf.setStochComponents(obsGenerated,factory);
		enkf.prepare();
		enkf.run();
		// state at final time
		IVector x = ((EnKF) enkf).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be close to x = [-0.2315739613743379,0.028205645374715847]");
		double delta=0.0001;
		assertEquals("x[0]",-0.2315739613743379,x.getValue(0),delta);
		assertEquals("x[1]",0.028205645374715847,x.getValue(1),delta);
		enkf.finish();
	}

	public void testSteadyStateFromEnkf() {
		System.out.println("========================================================");
		System.out.println(" Test EnKF with gain storage");
		System.out.println("========================================================");
		//generate observations
		StochVector.setSeed(initSeed);
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated = new CsvStochObserver();
		obsGenerated.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		AbstractSequentialEnsembleAlgorithm enkf = new EnKF();
		enkf.initialize(testRunDataDir, new String[]{"<algorithm><ensembleModel stochParameter=\"false\" stochForcing=\"true\" stochInit=\"true\" />"+
				"<saveGain><times type=\"fixed\" timeFormat=\"mjd\" >5.0,10.0</times></saveGain></algorithm>"});
		enkf.setStochComponents(obsGenerated,factory);
		enkf.prepare();
		enkf.run();
		// state at final time
		IVector x = ((EnKF) enkf).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be close to x = [-0.2315739613743379,0.028205645374715847]");
		double delta=0.0001;
		assertEquals("x[0]",-0.2315739613743379,x.getValue(0),delta);
		assertEquals("x[1]",0.028205645374715847,x.getValue(1),delta);
		
		// check for steady state gain files
		File gainFile1 = new File(testRunDataDir,"kgStorage_185811220000/kalmanGainStorage.xml");
		File refFile1 = new File(testRunDataDir,"kalman_5_ref.xml");
		assertTrue(testData.FilesAreIdentical(gainFile1, refFile1));
		File gainFile2 = new File(testRunDataDir,"kgStorage_185811270000/kalmanGainStorage.xml");
		File refFile2 = new File(testRunDataDir,"kalman_10_ref.xml");
		assertTrue(testData.FilesAreIdentical(gainFile2, refFile2));

		System.out.println(" 2nd part of test with steady state filter");
		//IStochModelFactory factory2 = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated2 = new CsvStochObserver();
		obsGenerated2.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		SteadyStateFilter steadySate = new SteadyStateFilter();
		steadySate.initialize(testRunDataDir, new String[]{"<algorithm><readGain>"
				+"<dirPrefix>kgStorage_</dirPrefix>"
				+"<time timeFormat=\"mjd\">10.0</time>"
				+"<file>kalmanGainStorage.xml</file>"
				+"</readGain></algorithm>"});
		steadySate.setStochComponents(obsGenerated2,factory);
		steadySate.prepare();
		steadySate.run();
		// state at final time
		IVector x2 = ((SteadyStateFilter) steadySate).currentState;
		System.out.println("x = "+x2);
		System.out.println("Should be close to x = [-0.22800872082392284,-0.017985528417106378]");
		assertEquals("x[0]",-0.22800872082392284,x2.getValue(0),delta);
		assertEquals("x[1]",-0.017985528417106378,x2.getValue(1),delta);		
	}

	public void testSteadyStateFromAsynchronousEnkf() {
		System.out.println("========================================================");
		System.out.println(" Test asynchronous EnKF with gain storage");
		System.out.println("========================================================");
		//generate observations
		StochVector.setSeed(initSeed);
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated = new CsvStochObserver();
		obsGenerated.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		AbstractSequentialEnsembleAlgorithm enkf = new EnKF();
		enkf.initialize(testRunDataDir, new String[]{"<algorithm><ensembleModel stochParameter=\"false\" stochForcing=\"true\" stochInit=\"true\" />"
				+"<analysisTimes type=\"fixed\" timeFormat=\"mjd\" >0.0,2.0,...,10.0</analysisTimes>"
				+"<saveGain><file dirPrefix=\"kg_async_\"/><times type=\"fixed\" timeFormat=\"mjd\" >5.0,10.0</times></saveGain></algorithm>"});
		enkf.setStochComponents(obsGenerated,factory);
		enkf.prepare();
		enkf.run();
		// state at final time
		IVector x = ((EnKF) enkf).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be close to x = [-0.22916138922578846,0.06338004479763534]");
		double delta=0.0001;
		assertEquals("x[0]",-0.22916138922578846,x.getValue(0),delta);
		assertEquals("x[1]",0.06338004479763534,x.getValue(1),delta);
		
		// check for steady state gain files
		File gainFile2 = new File(testRunDataDir,"kg_async_185811270000/kalmanGainStorage.xml");
		File refFile2 = new File(testRunDataDir,"kalman_async_10_ref.xml");
		assertTrue(testData.FilesAreIdentical(gainFile2, refFile2));

		System.out.println(" 2nd part of test with steady state filter");
		//IStochModelFactory factory2 = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated2 = new CsvStochObserver();
		obsGenerated2.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		SteadyStateFilter steadySate = new SteadyStateFilter();
		steadySate.initialize(testRunDataDir, new String[]{"<algorithm><readGain>"
				+"<analysisTimes type=\"fixed\" timeFormat=\"mjd\" >0.0,2.0,...,10.0</analysisTimes>"
				+"<dirPrefix>kg_async_</dirPrefix>"
				+"<time timeFormat=\"mjd\">10.0</time>"
				+"<file>kalmanGainStorage.xml</file>"
				+"</readGain></algorithm>"});
		steadySate.setStochComponents(obsGenerated2,factory);
		steadySate.prepare();
		steadySate.run();
		// state at final time
		IVector x2 = ((SteadyStateFilter) steadySate).currentState;
		System.out.println("x = "+x2);
		System.out.println("Should be close to x = [-0.22800872082392284,-0.017985528417106378]");
		assertEquals("x[0]",-0.22800872082392284,x2.getValue(0),delta);
		assertEquals("x[1]",-0.017985528417106378,x2.getValue(1),delta);		
	}

	public void testEnKF_missing_observations() {
		System.out.println("========================================================");
		System.out.println(" Test EnKF with some missing observations.");
		System.out.println("========================================================");
		//generate observations
		StochVector.setSeed(initSeed);
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obs = new NoosTimeSeriesStochObserver();
		obs.initialize(new File(testRunDataDir,"oscillator_with_noos_obs"), new String[]{"noosObservations.xml"});
		//create and run filter
		String kalmanConfig = "<EnKF><ensembleSize>30</ensembleSize></EnKF>";
		IAlgorithm enkf = new EnKF();
		enkf.initialize(null, new String[]{kalmanConfig});
		enkf.setStochComponents(obs,factory);
		enkf.prepare();
		enkf.run();
		// state at final time
		IVector x = ((EnKF) enkf).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be close to x = [-0.004697794789992467,0.01386540584302215]");
		double delta=0.0001;
		assertEquals("x[0]",-0.004697794789992467,x.getValue(0),delta);
		assertEquals("x[1]",0.01386540584302215,x.getValue(1),delta);
		enkf.finish();
	}

    public void testSteadyStateFilter() {
		System.out.println("========================================================");
		System.out.println(" Test SteadyStateFilter with different Kalman gain at different time");
		System.out.println("========================================================");
		//generate observations
		StochVector.setSeed(initSeed);
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated = new CsvStochObserver();
		obsGenerated.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		AbstractSequentialEnsembleAlgorithm enkf = new EnKF();
		enkf.initialize(testRunDataDir, new String[]{"<algorithm><ensembleModel stochParameter=\"false\" stochForcing=\"true\" stochInit=\"true\" />"+
				"<saveGain><times type=\"fixed\" timeFormat=\"mjd\" >5.0,10.0</times></saveGain></algorithm>"});
		enkf.setStochComponents(obsGenerated,factory);
		enkf.prepare();
		enkf.run();
		// state at final time
		IVector x = ((EnKF) enkf).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be close to x = [-0.2315739613743379,0.028205645374715847]");
		double delta=0.0001;
		assertEquals("x[0]",-0.2315739613743379,x.getValue(0),delta);
		assertEquals("x[1]",0.028205645374715847,x.getValue(1),delta);

		// check for steady state gain files
		File gainFile1 = new File(testRunDataDir,"kgStorage_185811220000/kalmanGainStorage.xml");
		File refFile1 = new File(testRunDataDir,"kalman_5_ref.xml");
		assertTrue(testData.FilesAreIdentical(gainFile1, refFile1));
		File gainFile2 = new File(testRunDataDir,"kgStorage_185811270000/kalmanGainStorage.xml");
		File refFile2 = new File(testRunDataDir,"kalman_10_ref.xml");
		assertTrue(testData.FilesAreIdentical(gainFile2, refFile2));

		System.out.println(" 2nd part of test with steady state filter");
		//IStochModelFactory factory2 = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obsGenerated2 = new CsvStochObserver();
		obsGenerated2.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		SteadyStateFilter steadySate = new SteadyStateFilter();
		steadySate.initialize(testRunDataDir, new String[]{"<algorithm><readGain>"
				+"<dirPrefix>kgStorage_</dirPrefix>"
				+"<time timeFormat=\"mjd\" readGainTime=\"9.0\">10.0</time>"
				+"<file>kalmanGainStorage.xml</file>"
				+"</readGain></algorithm>"});
		steadySate.setStochComponents(obsGenerated2,factory);
		steadySate.prepare();
		steadySate.run();
		// state at final time
		IVector x2 = ((SteadyStateFilter) steadySate).currentState;
		System.out.println("x = "+x2);
		System.out.println("Should be close to x = [-0.22800873058223528,-0.01798443792618797]");
		assertEquals("x[0]",-0.22800873058223528,x2.getValue(0),delta);
		assertEquals("x[1]",-0.01798443792618797,x2.getValue(1),delta);
	}

	public void testSteadyStateFilter_2() {
		System.out.println("========================================================");
		System.out.println(" Test SteadyStateFilter with different Kalman gain at different time");
		System.out.println("========================================================");
		//generate observations
		StochVector.setSeed(initSeed);
		IStochModelFactory factory = new OscillatorStochModelFactory();
		factory.initialize(null, new String[]{"<oscillatorConfig><simulationTimespan>[0.0,0.05,10.0]</simulationTimespan><parameters names=\"t_damp,omega\">[8.0,1.5708]</parameters><parameterUncertainty names=\"t_damp,omega\">[1.0,0.1257]</parameterUncertainty><systemNoise>{[0.0,0.0],[0.3,0.3]}</systemNoise><initialState>[0.0,0.0]</initialState><initialStateUncertainty>[0.8,0.8]</initialStateUncertainty></oscillatorConfig>"});
		IStochObserver obs = new NoosTimeSeriesStochObserver();
		obs.initialize(new File(testRunDataDir,"oscillator_with_noos_obs"), new String[]{"noosObservationsUnbiasedDud.xml"});
//		IStochObserver obs = new CsvStochObserver();
//		obs.initialize(testRunDataDir, new String[]{"observations_oscillator_generated.csv"});
		//create and run filter
		AbstractSequentialEnsembleAlgorithm enkf = new EnKF();
		enkf.initialize(testRunDataDir, new String[]{"<algorithm><ensembleModel stochParameter=\"false\" stochForcing=\"true\" stochInit=\"true\" />"+
				"<saveGain><times type=\"fixed\" timeFormat=\"mjd\" >5.0,10.0</times></saveGain></algorithm>"});
		enkf.setStochComponents(obs,factory);
		enkf.prepare();
		enkf.run();
		// state at final time
		IVector x = ((EnKF) enkf).currentState;
		System.out.println("x = "+x);
		System.out.println("Should be close to x = [-0.21918065777234383,-0.04209128520660418]");
		double delta=0.0001;
		assertEquals("x[0]",-0.21918065777234383,x.getValue(0),delta);
		assertEquals("x[1]",-0.04209128520660418,x.getValue(1),delta);

		System.out.println("========================================================");
		System.out.println(" Test SteadyStateFilter skipAtInitialTime=true");
		System.out.println("========================================================");
		IStochObserver obsGenerated1 = new NoosTimeSeriesStochObserver();
		obsGenerated1.initialize(new File(testRunDataDir, "oscillator_with_noos_obs"), new String[]{"noosObservationsUnbiasedDud.xml"});
		//create and run filter
		SteadyStateFilter steadySate1 = new SteadyStateFilter();
		steadySate1.initialize(testRunDataDir, new String[]{"<algorithm>"
				+ "<analysisTimes type=\"fromObservationTimes\" skipAtInitialTime=\"true\"></analysisTimes>"
				+ "<readGain>"
				+ "<dirPrefix>kgStorage_</dirPrefix>"
				+ "<time timeFormat=\"mjd\" readGainTime=\"9.0\">10.0</time>"
				+ "<file>kalmanGainStorage.xml</file>"
				+ "</readGain></algorithm>"});
		steadySate1.setStochComponents(obsGenerated1, factory);
		steadySate1.prepare();
		steadySate1.run();
		// state at final time
		IVector x1 = ((SteadyStateFilter) steadySate1).currentState;
		System.out.println("x = "+x1);
		System.out.println("Should be close to x = [-0.22709920343645265,-0.04670193872902787]");
		assertEquals("x[0]",-0.22709920343645265,x1.getValue(0),delta);
		assertEquals("x[1]",-0.04670193872902787,x1.getValue(1),delta);

		System.out.println("========================================================");
		System.out.println(" Test SteadyStateFilter skipAtInitialTime=true");
		System.out.println("========================================================");
		IStochObserver obsGenerated2 = new NoosTimeSeriesStochObserver();
		obsGenerated2.initialize(new File(testRunDataDir, "oscillator_with_noos_obs"), new String[]{"noosObservationsUnbiasedDud.xml"});
		//create and run filter
		SteadyStateFilter steadySate2 = new SteadyStateFilter();
		steadySate2.initialize(testRunDataDir, new String[]{"<algorithm>"
				+ "<analysisTimes type=\"fromObservationTimes\" skipAtFinalTime=\"true\"></analysisTimes>"
				+ "<readGain>"
				+ "<dirPrefix>kgStorage_</dirPrefix>"
				+ "<time timeFormat=\"mjd\" readGainTime=\"9.0\">10.0</time>"
				+ "<file>kalmanGainStorage.xml</file>"
				+ "</readGain></algorithm>"});
		steadySate2.setStochComponents(obsGenerated2, factory);
		steadySate2.prepare();
		steadySate2.run();
		// state at final time
		IVector x2 = ((SteadyStateFilter) steadySate2).currentState;
		System.out.println("x = "+x2);
		System.out.println("Should be close to x = [-0.22709920343645265,-0.04670193872902787]");
		assertEquals("x[0]",-0.22709920343645265,x2.getValue(0),delta);
		assertEquals("x[1]",-0.04670193872902787,x2.getValue(1),delta);

		System.out.println("========================================================");
		System.out.println(" Test SteadyStateFilter without observation (run the filter in a long forecast mode)");
		System.out.println("========================================================");
		IStochObserver obsGenerated3 = new NoosTimeSeriesStochObserver();
		obsGenerated3.initialize(new File(testRunDataDir,"oscillator_with_noos_obs"), new String[]{"noosObservationsEmpty.xml"});
		//create and run filter
		SteadyStateFilter steadySate3 = new SteadyStateFilter();
		steadySate3.initialize(testRunDataDir, new String[]{"<algorithm>"
				+"<analysisTimes type=\"fromObservationTimes\" skipAtInitialTime=\"true\" skipAtFinalTime=\"true\" continueWithoutObservation=\"true\"></analysisTimes>"
				+"<readGain>"
				+"<dirPrefix>kgStorage_</dirPrefix>"
				+"<time timeFormat=\"mjd\" readGainTime=\"9.0\">10.0</time>"
				+"<file>kalmanGainStorage.xml</file>"
				+"</readGain></algorithm>"});
		steadySate3.setStochComponents(obsGenerated3,factory);
		steadySate3.prepare();
		steadySate3.run();
		// state at final time
		IVector x3 = ((SteadyStateFilter) steadySate3).currentState;
		System.out.println("x = "+x3);
		System.out.println("Should be close to x = [0.0,0.0]");
		assertEquals("x[0]",0.0,x3.getValue(0),delta);
		assertEquals("x[1]",0.0,x3.getValue(1),delta);

	}
}
