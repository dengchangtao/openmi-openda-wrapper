package org.openda.model_damflow;

import junit.framework.TestCase;
import org.openda.interfaces.IExchangeItem;
import org.openda.utils.OpenDaTestSupport;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Julius Sumihar
 * Date: 4-7-12
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
public class DupuitGFileTest extends TestCase {
	private OpenDaTestSupport testData;

	protected void setUp() throws IOException {
		testData = new OpenDaTestSupport(DupuitPFileTest.class, "model_damflow");
	}

	public void testDupuitGFileTest(){
		DupuitGFile damflowGFile = new DupuitGFile();
		String fileName = "dupuit.G";
		damflowGFile.initialize(testData.getTestRunDataDir(), fileName, new String[]{});
		System.out.println(fileName);

		// test getIDs:
		String[] trueIDs = new String[]{"layer0.Kx","layer1.Kx","layer2.Kx","layer3.Kx","layer4.Kx","layer5.Kx"};
		String[] exchangeIDs = damflowGFile.getExchangeItemIDs();
		int nExchangeItem = exchangeIDs.length;
		for (int i=0; i<nExchangeItem; i++){
			assertEquals("exchangeIDs["+i+"]", trueIDs[i], exchangeIDs[i]);
			System.out.println("ExchangeItem id: "+exchangeIDs[i]);
		}

		// test getValues:
		double[] trueKxs = new double[]{0.0,1.0,0.01,25.0,0.01,75.0};
		for (int i=0; i<nExchangeItem; i++){
			IExchangeItem exchangeItem = damflowGFile.getDataObjectExchangeItem(exchangeIDs[i]);
			double Kx = exchangeItem.getValuesAsDoubles()[0];
			assertEquals("Kx layer"+i+"", trueKxs[i], Kx);
		}

		// test setValues:
		double[] setKx = new double[]{2.1,3.4,5.0,2.8,9.2,6.5};
		for (int i=0; i<nExchangeItem; i++){
			IExchangeItem exchangeItem = damflowGFile.getDataObjectExchangeItem(exchangeIDs[i]);
			exchangeItem.setValuesAsDoubles(new double[]{setKx[i]});
			double newKx = exchangeItem.getValuesAsDoubles()[0];
			assertEquals("Kx layer"+i+"", setKx[i], newKx);
		}

		// test axpyOnValues:
		double alpha = 0.3;
		double[] axpyValue = new double[]{0.8};
		for (int i=0; i<nExchangeItem; i++){
			IExchangeItem exchangeItem = damflowGFile.getDataObjectExchangeItem(exchangeIDs[i]);
			double oldKx = exchangeItem.getValuesAsDoubles()[0];
			exchangeItem.axpyOnValues(alpha,axpyValue);
			double Kx = exchangeItem.getValuesAsDoubles()[0];
			assertEquals("Kx layer"+i+"", setKx[i]+alpha*axpyValue[0], Kx);
		}

		damflowGFile.finish();

	}

}
