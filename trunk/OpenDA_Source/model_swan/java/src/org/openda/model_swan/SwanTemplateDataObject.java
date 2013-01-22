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

import org.openda.blackbox.config.KeyTypeSwanRestartString;
import org.openda.blackbox.config.KeyTypeSwanStartDateString;
import org.openda.blackbox.io.BaseTemplateDataObject;
import org.openda.exchange.DoubleExchangeItem;
import org.openda.exchange.SwanRestartTemplateKeyExchangeItem;
import org.openda.exchange.TemplateKeyExchangeItem;
import org.openda.interfaces.IExchangeItem;
import org.openda.interfaces.IPrevExchangeItem;

import java.io.File;
import java.io.PrintWriter;

import static java.lang.System.arraycopy;

/**
 * SwanTemplate IDataObject to replace key strings with values from valuesFile using templateConfig xml
 * for Swan blackbox wrapper
 */
public class SwanTemplateDataObject extends BaseTemplateDataObject {
    static int numberOfExtraExchangeItems = 3;
    static String RESTART_ID = "RESTART_ID";
    private SwanRestartTemplateKeyExchangeItem restartExchangeItem = null;

    private IExchangeItem startTimeExchangeItem = null;
    private IExchangeItem endTimeExchangeItem = null;


    @Override
    public IPrevExchangeItem[] getExchangeItems() {
        IPrevExchangeItem[] tempExchangeItems = super.getExchangeItems();
        exchangeItems = new IExchangeItem[tempExchangeItems.length + numberOfExtraExchangeItems];
        int length = tempExchangeItems.length;
        arraycopy(tempExchangeItems, 0, exchangeItems, 0, length);
        exchangeItems[length] = startTimeExchangeItem;
        exchangeItems[length+1] = endTimeExchangeItem;
        exchangeItems[length+2] = restartExchangeItem;
        return exchangeItems;

    }

    @Override
    public void initialize(File workingDir, String fileName, String[] arguments) {
        super.initialize(workingDir, fileName, arguments);
        this.startTimeExchangeItem = new DoubleExchangeItem(arguments[0], 0);
        this.endTimeExchangeItem = new DoubleExchangeItem(arguments[1], 0);

        //For the processing of the restart identifier we need to process the arguments for the restart file and experiment name
        // - r  : restart file
        int argLength = arguments.length;
        if (argLength <= 0) {
            throw new IllegalArgumentException(this.getClass().getName() + ": No arguments passed ");
        }

        //create exchange items.
        if (arguments.length < 2) {
            throw new IllegalArgumentException("No exchange item ids arguments specified for " + this.getClass().getSimpleName()
                    + " The first and second arguments should be the ids of the startTime and endTime exchangeItems respectively.");
        }

        this.startTimeExchangeItem = new DoubleExchangeItem(arguments[0], 0);
        this.endTimeExchangeItem = new DoubleExchangeItem(arguments[1], 0);

        KeyTypeSwanRestartString restartKeyType = new KeyTypeSwanRestartString();
        restartExchangeItem = new SwanRestartTemplateKeyExchangeItem(RESTART_ID,restartKeyType);
        if (arguments.length > 2) {
            for (int i = 2; i < arguments.length; i++) {
                String argument = arguments[i];
                if (argument.toLowerCase().trim().startsWith("-r")) {
                    i++;  // next argument is restart file name
                    if (argLength <= i) {
                        throw new IllegalArgumentException(this.getClass().getName() + ": No arguments passed -r ");
                    }
                    File restartFile = new File(workingDir, arguments[i]);
                    restartExchangeItem.setRestartFileName(arguments[i]);
                    restartExchangeItem.setRestartFile(restartFile);
                }
            }
        }

    }

    @Override
    public void writeValuesFile(PrintWriter printer) {
        double startDate = (Double) startTimeExchangeItem.getValues();
        double endDate = (Double) endTimeExchangeItem.getValues();
        String restartValue = restartExchangeItem.calculateValue();

        for (IPrevExchangeItem exchangeItem: exchangeItems) {
            if (exchangeItem != null && exchangeItem instanceof TemplateKeyExchangeItem) {
                TemplateKeyExchangeItem keyExchangeItem = (TemplateKeyExchangeItem) exchangeItem;
                if (keyExchangeItem.getId().equals(RESTART_ID)) continue;
                if (keyExchangeItem.getKeyType() instanceof KeyTypeSwanRestartString) {
                    printer.println(exchangeItem.getId() + "=" + restartValue);
                }else if (keyExchangeItem.getKeyType() instanceof KeyTypeSwanStartDateString) {
                    keyExchangeItem.setStartDate(startDate);
                    keyExchangeItem.setEndDate(endDate);
                    printer.println(exchangeItem.getId() + "=" + ((TemplateKeyExchangeItem) exchangeItem).calculateValue());
                } else {
                    printer.println(exchangeItem.getId() + "=" + keyExchangeItem.getValues());
                }
            }
        }

        printer.flush();

    }

    public void setPeriod(double startDate, double endDate) {
        if (startTimeExchangeItem == null || endTimeExchangeItem == null) return;
        startTimeExchangeItem.setValues(startDate);
        endTimeExchangeItem.setValues(endDate);
    }
}
