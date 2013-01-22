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

import org.openda.interfaces.ISqrtCovariance;
import org.openda.interfaces.IVector;
import org.openda.noiseModels.SpatialCorrelationStochVector.CoordinatesType;
import org.openda.utils.OpenDaTestSupport;
import org.openda.utils.StochVector;
import org.openda.utils.Vector;

import junit.framework.TestCase;

public class SpatialCorrelationTest extends TestCase {

    //File testDir = null;
    private File testRunDataDir;

    protected void setUp() throws IOException {
        OpenDaTestSupport testData = new OpenDaTestSupport(TimeSeriesNoiseModelTest.class, "core");
        testRunDataDir = testData.getTestRunDataDir();
    }

    public void testSpatialCorrelation_xy(){
    	double standardDeviation=1.0; 
		double lengthscale=1.0; 
		double[] x= new double[]{0.0,1.0,1.0,0.0,0.0}; //counterclock square 
		double[] y= new double[]{0.0,0.0,1.0,1.0,0.0};
    	SpatialCorrelationStochVector sv = new SpatialCorrelationStochVector(CoordinatesType.XY
    			, standardDeviation, lengthscale, x, y);
    	IVector mean = sv.getExpectations();
    	System.out.println("mean="+mean);
    	System.out.println("Should be mean=[0.0,0.0,0.0,0.0,0.0]");
    	assertEquals("[0.0,0.0,0.0,0.0,0.0]",mean.toString());
    	
    	IVector std = sv.getStandardDeviations();
    	System.out.println("std="+std);
    	System.out.println("Should be std=[1.0,1.0,1.0,1.0,1.0]");
    	assertEquals("[1.0,1.0,1.0,1.0,1.0]",std.toString());
    	
    	ISqrtCovariance sqrtCov = sv.getSqrtCovariance();
    	System.out.println("sqrtCov="+sqrtCov);
    	System.out.println("Should be sqrtCov=full([0.6565246076963052,0.2533417300316849,0.09791228026928459,0.25334173003168486,0.6565246076963053;0.253341730031685,0.8802290355167743,0.2992975654761666,0.08516893789612522,0.25334173003168503;0.09791228026928409,0.29929756547616676,0.8953593163334019,0.2992975654761668,0.09791228026928411;0.2533417300316849,0.08516893789612498,0.2992975654761666,0.8802290355167746,0.25334173003168486;0.6565246076963053,0.2533417300316851,0.09791228026928445,0.25334173003168503,0.6565246076963053])");
    	assertEquals("full([0.6565246076963052,0.2533417300316849,0.09791228026928459,0.25334173003168486,0.6565246076963053;0.253341730031685,0.8802290355167743,0.2992975654761666,0.08516893789612522,0.25334173003168503;0.09791228026928409,0.29929756547616676,0.8953593163334019,0.2992975654761668,0.09791228026928411;0.2533417300316849,0.08516893789612498,0.2992975654761666,0.8802290355167746,0.25334173003168486;0.6565246076963053,0.2533417300316851,0.09791228026928445,0.25334173003168503,0.6565246076963053])",sqrtCov.toString());
    	
    	StochVector.setSeed(123456);
    	IVector random = sv.createRealization();
    	System.out.println("random="+random);
    	System.out.println("Should be random=[-0.3838886494002265,-0.32903950436630147,-0.7024362227358004,0.24645496286476012,-0.38388864940022616]");
    	assertEquals("[-0.3838886494002265,-0.32903950436630147,-0.7024362227358004,0.24645496286476012,-0.38388864940022616]",random.toString());
    	
    	IVector someVector1 = new Vector("[0.0,0.0,0.0,0.0,0.0]");
    	IVector someVector2 = new Vector("[1.0,0.0,0.0,0.0,0.0]");
    	double pdfVal1 = sv.evaluatePdf(someVector1);
    	double pdfVal2 = sv.evaluatePdf(someVector2);
    	assertEquals(1423371.952647495, pdfVal1, 0.000001);
    	assertEquals(1041016.2334227595, pdfVal2, 0.000001);
    }

    public void testSpatialCorrelation_latlon(){
    	double standardDeviation=1.0; 
		double lengthscale=1.1123e+05; //approx 1 degree at the equator
		double[] x= new double[]{0.0,1.0,1.0,0.0,0.0}; //counterclock square 
		double[] y= new double[]{0.0,0.0,1.0,1.0,0.0};
    	SpatialCorrelationStochVector sv = new SpatialCorrelationStochVector(CoordinatesType.WGS84
    			, standardDeviation, lengthscale, x, y);
    	IVector mean = sv.getExpectations();
    	System.out.println("mean="+mean);
    	System.out.println("Should be mean=[0.0,0.0,0.0,0.0,0.0]");
    	assertEquals("[0.0,0.0,0.0,0.0,0.0]",mean.toString());
    	
    	IVector std = sv.getStandardDeviations();
    	System.out.println("std="+std);
    	System.out.println("Should be std=[1.0,1.0,1.0,1.0,1.0]");
    	assertEquals("[1.0,1.0,1.0,1.0,1.0]",std.toString());
    	
    	ISqrtCovariance sqrtCov = sv.getSqrtCovariance();
    	System.out.println("sqrtCov="+sqrtCov);
    	System.out.println("Should be sqrtCov=full([0.6565208964327816,0.25334955112875074,0.09792247783227047,0.2533492024306464,0.6565208964327819;0.2533495511287502,0.8802206825124356,0.2993063802412671,0.08517775947942266,0.25334955112875024;0.09792247783227007,0.2993063802412674,0.8953317495585582,0.2993645363927716,0.09792247783227029;0.25334920243064585,0.08517775947942272,0.2993645363927717,0.8802011059388181,0.25334920243064596;0.6565208964327817,0.2533495511287503,0.0979224778322704,0.2533492024306458,0.656520896432782])");
    	assertEquals("full([0.6565208964327816,0.25334955112875074,0.09792247783227047,0.2533492024306464,0.6565208964327819;0.2533495511287502,0.8802206825124356,0.2993063802412671,0.08517775947942266,0.25334955112875024;0.09792247783227007,0.2993063802412674,0.8953317495585582,0.2993645363927716,0.09792247783227029;0.25334920243064585,0.08517775947942272,0.2993645363927717,0.8802011059388181,0.25334920243064596;0.6565208964327817,0.2533495511287503,0.0979224778322704,0.2533492024306458,0.656520896432782])",sqrtCov.toString());
    	
    	StochVector.setSeed(123456);
    	IVector random = sv.createRealization();
    	System.out.println("random="+random);
    	System.out.println("Should be random=[-0.3838887644308977,-0.3290483731175597,-0.7023584661001218,0.24635798307523635,-0.3838887644308983]");
    	assertEquals("[-0.3838887644308977,-0.3290483731175597,-0.7023584661001218,0.24635798307523635,-0.3838887644308983]",random.toString());
    	
    	IVector someVector1 = new Vector("[0.0,0.0,0.0,0.0,0.0]");
    	IVector someVector2 = new Vector("[1.0,0.0,0.0,0.0,0.0]");
    	double pdfVal1 = sv.evaluatePdf(someVector1);
    	double pdfVal2 = sv.evaluatePdf(someVector2);
    	assertEquals(1343747.538109409, pdfVal1, 0.000001);
    	assertEquals(982746.9501046452, pdfVal2, 0.000001);
    }

}
