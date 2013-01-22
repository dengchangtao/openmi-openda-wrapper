package org.openda.model_damflow;

import junit.framework.TestCase;
import org.openda.interfaces.IExchangeItem;
import org.openda.utils.OpenDaTestSupport;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Julius Sumihar
 * Date: 2-7-12
 * Time: 14:29
 * To change this template use File | Settings | File Templates.
 */
public class DupuitPFileTest extends TestCase {

	private OpenDaTestSupport testData;

	protected void setUp() throws IOException {
		testData = new OpenDaTestSupport(DupuitPFileTest.class, "model_damflow");
	}

	public void testDupuitPFileTest_1(){
		DupuitPFile	damflowPFile = new DupuitPFile();
		String fileName = "dupuit.P";
		damflowPFile.initialize(testData.getTestRunDataDir(),fileName,new String[]{});

		// test getIDs:
		String[] trueIDs = new String[]{"sTime","eTime"};
		String[] exchangeIDs = damflowPFile.getExchangeItemIDs();
		assertEquals("ExchangeID 1: ", exchangeIDs[0], trueIDs[0]);
		assertEquals("ExchangeID 2: ", exchangeIDs[1], trueIDs[1]);

		// test getValues:
		IExchangeItem exchangeItem = damflowPFile.getDataObjectExchangeItem(exchangeIDs[0]);
		double[] timeValue = exchangeItem.getValuesAsDoubles();
		assertEquals("sTime: ", 0.0, timeValue[0]);
		exchangeItem = damflowPFile.getDataObjectExchangeItem(exchangeIDs[1]);
		timeValue = exchangeItem.getValuesAsDoubles();
		assertEquals("eTime: ", 10.0, timeValue[0]);

		// test setValues:
		exchangeItem = damflowPFile.getDataObjectExchangeItem(exchangeIDs[0]);
		double newSTime = 5.0d;
		exchangeItem.setValuesAsDoubles(new double[]{newSTime});
		assertEquals("new sTime: ", newSTime, exchangeItem.getValuesAsDoubles()[0]);
		exchangeItem = damflowPFile.getDataObjectExchangeItem(exchangeIDs[1]);
		double newETime = 15.0d;
		exchangeItem.setValuesAsDoubles(new double[]{newETime});
		assertEquals("new eTime: ", newETime, exchangeItem.getValuesAsDoubles()[0]);

		damflowPFile.finish();
	}

}
