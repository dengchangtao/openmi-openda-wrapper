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
package org.openda.blackbox.io;
import junit.framework.TestCase;
import org.openda.blackbox.config.BBTemplateConfig;
import org.openda.blackbox.config.BBTemplateFile;
import org.openda.blackbox.config.BBUtils;
import org.openda.interfaces.IDataObject;
import org.openda.interfaces.IPrevExchangeItem;
import org.openda.utils.OpenDaTestSupport;

import java.io.File;
import java.io.IOException;

/**
 * Test class for testing BaseTemplateDataObject
 */
public class BaseTemplateDataObjectTest extends TestCase {
    private File testRunDataDir;
    private OpenDaTestSupport testData;
    static String TEST_SUBDIR = "template";

    protected void setUp() throws IOException {
        testData = new OpenDaTestSupport(BaseTemplateDataObject.class, "core");
        testRunDataDir = new File(testData.getTestRunDataDir(), TEST_SUBDIR);
    }

    public void testTemplateDataObject() {
        IDataObject baseTemplateDataObject = BBUtils.createDataObject(
                testRunDataDir, BaseTemplateDataObject.class.getName(),
                "BBTemplateConfig.xml", new String[]{});
        BBTemplateConfig bbTemplateConfig = ((BaseTemplateDataObject)baseTemplateDataObject).getBBTemplateConfig();
        assertTrue("bbTemplateConfig is null", bbTemplateConfig != null);
        assertTrue("templateFiles do not exist", bbTemplateConfig.getTemplateFiles() != null && !bbTemplateConfig.getTemplateFiles().isEmpty());
        assertEquals("Number of templateFiles not equal", 1, bbTemplateConfig.getTemplateFiles().size());
        BBTemplateFile bbTemplateFile = bbTemplateConfig.getTemplateFiles().iterator().next();

        IPrevExchangeItem[] exchangeItems = ((BaseTemplateDataObject) baseTemplateDataObject).getExchangeItems();
        assertTrue("exchangeItems list is null",exchangeItems != null);
        assertEquals("exchangeItems size is not equal",4,exchangeItems.length);
        assertEquals("BaseTemplateDataObject first id not equal", "endtime" , exchangeItems[0].getId());

        for (IPrevExchangeItem exchangeItem : exchangeItems) {
            if (exchangeItem.getId().equals("endtime")) {
                exchangeItem.setValues("14400.0");
            } else if (exchangeItem.getId().equals("starttime")) {
                exchangeItem.setValues("1440.0");
            } else if (exchangeItem.getId().equals("itdate")) {
                exchangeItem.setValues("46553.0");
            } else if (exchangeItem.getId().equals("iterations")) {
                exchangeItem.setValues("7");
            }
        }
        baseTemplateDataObject.finish();

        File outputRefFile=new File(testRunDataDir,bbTemplateFile.getTargetFileName() + "_expected");
        File targetFile = new File(testRunDataDir,bbTemplateFile.getTargetFileName());
        boolean identical = testData.FilesAreIdentical(targetFile, outputRefFile);
        assertTrue("Files are not identical: actual file " + targetFile + "\n does not equal expected file " + outputRefFile, identical);
    }

}
