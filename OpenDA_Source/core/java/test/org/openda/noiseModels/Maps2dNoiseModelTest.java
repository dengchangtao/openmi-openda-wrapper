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
package org.openda.noiseModels;

import java.io.File;
import java.io.IOException;

import org.openda.exchange.ArrayExchangeItem;
import org.openda.exchange.ArrayGeometryInfo;
import org.openda.exchange.timeseries.TimeUtils;
import org.openda.interfaces.IArray;
import org.openda.interfaces.IQuantityInfo;
import org.openda.interfaces.IStochModelFactory;
import org.openda.interfaces.IStochModelInstance;
import org.openda.interfaces.IStochModelFactory.OutputLevel;
import org.openda.utils.OpenDaTestSupport;
import org.openda.utils.StochVector;
import org.openda.utils.Time;
import org.openda.utils.Vector;

import junit.framework.TestCase;

public class Maps2dNoiseModelTest extends TestCase {

	//File testDir = null;
	private File testRunDataDir;

	protected void setUp() throws IOException {
		OpenDaTestSupport testData = new OpenDaTestSupport(Maps2dNoiseModelTest.class, "core");
		testRunDataDir = testData.getTestRunDataDir();
	}

	public void testMaps2dNoise_basics(){
		IStochModelFactory factory = new MapsNoiseModelFactory();
		String configString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+"<mapsNoiseModelConfig>"
				+"	<simulationTimespan timeFormat=\"dateTimeString\">201008241200,201008241210,...,201008241220</simulationTimespan>"
				+"	<noiseItem id=\"windU\" quantity=\"wind-u\" unit=\"m/s\" height=\"10.0\" "
				+"	 standardDeviation=\"1.0\" timeCorrelationScale=\"12.0\" timeCorrelationScaleUnit=\"hours\" "
				+"	 initialValue=\"0.0\" horizontalCorrelationScale=\"500000\" horizontalCorrelationScaleUnit=\"km\" >"
				+"	 	<grid type=\"cartesian\" coordinates=\"wgs84\">"
				+"	 		<x>-5,-2.5,...,5</x>"
				+"	 		<y>50,55,...,60</y>"
				+"	 	</grid>"
				+"	</noiseItem>"
				+"	<noiseItem id=\"windV\" quantity=\"wind-v\" unit=\"m/s\" height=\"10.0\" "
				+"	 standardDeviation=\"1.0\" timeCorrelationScale=\"12.0\" timeCorrelationScaleUnit=\"hours\" "
				+"	 initialValue=\"0.0\" horizontalCorrelationScale=\"500000\" horizontalCorrelationScaleUnit=\"km\" >"
				+"	 	<grid type=\"cartesian\" coordinates=\"wgs84\">"
				+"	 		<x>-5,-2.5,...,5</x>"
				+"	 		<y>50,55,...,60</y>"
				+"	 	</grid>"
				+"	</noiseItem>"
				+"</mapsNoiseModelConfig>";
		factory.initialize(testRunDataDir, new String[]{configString});
		IStochModelInstance model = factory.getInstance(OutputLevel.Debug);

		StochVector.setSeed(10111213);
		model.setAutomaticNoiseGeneration(true);

		double targetTime=0.0;
		try {
			targetTime=TimeUtils.date2Mjd("201008241220");
		} catch (Exception e) {
			throw new RuntimeException("invalid dateformat for targetTime");
		}
		model.compute(new Time(targetTime));

		//
		//check output items
		//
		//id's
		String[] ids=model.getExchangeItemIDs();
		assertEquals(2, ids.length);
		System.out.println("ids[0]="+ids[0]);
		assertEquals("windU", ids[0]);
		//item 'windU'
		ArrayExchangeItem item0 = (ArrayExchangeItem)model.getExchangeItem(ids[0]);
		String idCheck=item0.getId();
		System.out.println("item['windU']="+item0.toString());
		assertEquals("windU", idCheck);
		// values
		IArray valuesArray = item0.getValues();
		int dimensions[]=valuesArray.getDimensions();
		assertEquals(3,dimensions.length);
		assertEquals(3,dimensions[0]);  // time
		assertEquals(5,dimensions[1]);  // longitude x
		assertEquals(3,dimensions[2]);  // latitude y
		double values[] = valuesArray.getValuesAsDoubles();
		System.out.println("values="+new Vector(values));
		assertEquals(3*5*3,values.length);
		double delta=1.0E-8;
		assertEquals(-0.09584754453722896, values[15+0], delta); //initial value of 0 is boring.    	
		assertEquals(-0.0512200744385678, values[15+29], delta);
		// quantity
		IQuantityInfo qInfo = item0.getQuantityInfo();
		String unit = qInfo.getUnit();
		assertEquals("m/s",unit);
		String quantity = qInfo.getQuantity();
		assertEquals("wind-u",quantity);
		// geometry
		ArrayGeometryInfo geomInfo = (ArrayGeometryInfo)item0.getGeometryInfo();
		IArray lon = geomInfo.getLongitudeArray();
		System.out.println("lon="+lon);
		assertEquals("lon","{-5.0,-2.5,0.0,2.5,5.0}",lon.toString());
		IArray lat = geomInfo.getLatitudeArray();
		System.out.println("lat="+lat);
		assertEquals("lat","{50.0,55.0,60.0}",lat.toString());

		ArrayExchangeItem item1 = (ArrayExchangeItem)model.getExchangeItem(ids[1]);
		String idCheck1=item1.getId();
		System.out.println("item['windV']="+item1.toString());
		assertEquals("windV", idCheck1);
		//Values
		IArray valuesArray1 = item1.getValues();
		int dimensions1[]=valuesArray1.getDimensions();
		double values1[] = valuesArray1.getValuesAsDoubles();
		System.out.println("values1="+new Vector(values1));
		assertEquals(3*5*3,values1.length);
		assertEquals(0.015106050031291882, values1[15+0], delta);    	
		assertEquals(-0.005391284541902594, values1[15+29], delta);
		// quantity
		IQuantityInfo qInfo1 = item1.getQuantityInfo();
		String unit1 = qInfo1.getUnit();
		assertEquals("m/s",unit1);
		String quantity1 = qInfo1.getQuantity();
		assertEquals("wind-v",quantity1);
	}

	/*    public void testMaps2dNoise_config(){
    	IStochModelFactory factory = new TimeSeriesNoiseModelFactory();
    	String inputFile="timeseries_noise.xml";
    	factory.initialize(testRunDataDir, new String[]{inputFile});
    	IStochModelInstance model = factory.getInstance(OutputLevel.Debug);

    	StochVector.setSeed(10L);
    	model.setAutomaticNoiseGeneration(true);
    	ITime targetTime = model.getTimeHorizon().getEndTime();
    	model.compute(targetTime);


    	String[] ids=model.getExchangeItemIDs();
    	assertEquals(2, ids.length);
    	System.out.println("ids[0]="+ids[0]);
    	assertEquals("waterlevel@aberdeen", ids[0]);
    	IPrevExchangeItem series1=model.getExchangeItem("waterlevel@aberdeen");
    	assertTrue(series1 instanceof TimeSeries);
    	System.out.println("location1.quantity1 =>"+series1.toString());
    	double[] times=series1.getTimes();
    	assertEquals("times.length",70, times.length);
    	assertEquals("times[69]",55432.98611111117, times[69],0.00001);
    	double[] values=series1.getValuesAsDoubles();
    	assertEquals("values.length",70, values.length);
    	assertEquals("values[69]",0.05608590539600699, values[69],0.00001);
    }

    public void testMaps2dNoise_multistart(){
    	IStochModelFactory factory = new TimeSeriesNoiseModelFactory();
    	String configString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
    			+"<timeSeriesNoiseModelConfig>"
    			+"	<simulationTimespan timeFormat=\"mjd\">0.0,0.05,...,10.0</simulationTimespan>"
    			+"	<timeSeries location=\"location1\" quantity=\"quantity1\" standardDeviation=\"10.0\""
    			+"		timeCorrelationScale=\"6.0\" timeCorrelationScaleUnit=\"hours\" />"
    			+"	<timeSeries location=\"location2\" quantity=\"quantity2\" standardDeviation=\"1.0\""
    			+"		timeCorrelationScale=\"12.0\" timeCorrelationScaleUnit=\"hours\" />"
    			+"	<timeSeries location=\"location3\" quantity=\"quantity3\" standardDeviation=\"0.1\""
    			+"		timeCorrelationScale=\"1.0\" timeCorrelationScaleUnit=\"days\" />"
    			+"</timeSeriesNoiseModelConfig>";
    	factory.initialize(testRunDataDir, new String[]{configString});

    	IStochModelInstance model1 = factory.getInstance(OutputLevel.Debug);
    	StochVector.setSeed(10111213);
    	model1.setAutomaticNoiseGeneration(true);
    	model1.compute(new Time(10.0));
    	IVector state1_t10 = model1.getState();

    	//multiple starts with internal state
    	IStochModelInstance model2 = factory.getInstance(OutputLevel.Debug);
    	StochVector.setSeed(10111213);
    	model2.setAutomaticNoiseGeneration(true);
    	model2.compute(new Time(5.0));
    	IVector state2_t5 = model2.getState();
    	model2.compute(new Time(10.0));
    	IVector state2_t10 = model2.getState();
    	IVector diff2_t10 = state2_t10.clone();
    	diff2_t10.axpy(-1.0,state1_t10);
    	assertEquals(0.0, diff2_t10.norm2(), 0.0001);

    	//multiple starts with restart
    	IStochModelInstance model3 = factory.getInstance(OutputLevel.Debug);
    	StochVector.setSeed(10111213);
    	model3.setAutomaticNoiseGeneration(true);
    	model3.compute(new Time(5.0));
        IVector state3_t5_pre = model3.getState();

    	IVector diff3_t5_pre = state3_t5_pre.clone();
    	diff3_t5_pre.axpy(-1.0,state2_t5);
    	assertEquals(0.0, diff3_t5_pre.norm2(), 0.0001);

        IModelState restart3_t5_pre = model3.saveInternalState();
    	File savedStateFile=new File(testRunDataDir,"noise_series_restart");
       	restart3_t5_pre.savePersistentState(savedStateFile);
       	//change state so we know that restart file is realy used
    	model3.axpyOnState(1.0, new Vector("[1000.0,1000.0,1000.0]"));

    	IModelState restart3_t5_post =model3.loadPersistentState(savedStateFile);
    	model3.restoreInternalState(restart3_t5_post);
    	IVector state3_t5_post = model3.getState();

    	IVector diff3_t5_post = state3_t5_post.clone();
    	diff3_t5_post.axpy(-1.0,state2_t5);
    	assertEquals(0.0, diff3_t5_post.norm2(), 0.0001);

    	model3.compute(new Time(10.0));
    	IVector state3_t10 = model3.getState();

    	IVector diff3_t10 = state3_t10.clone();
    	diff3_t10.axpy(-1.0,state2_t10);
    	assertEquals(0.0, diff3_t10.norm2(), 0.0001);
    }
	 */
}
