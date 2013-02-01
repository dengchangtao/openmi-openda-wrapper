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
package org.openda.observers;
import org.openda.interfaces.*;
import org.openda.interfaces.IExchangeItem.ValueType;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * TODO: korte beschrijving
 * TODO: implement the IExchangeItem methods. Consult Stef.
 */
public class NonMissingStochObserverExchangeItem implements IPrevExchangeItem, IExchangeItem, Serializable {

    private IPrevExchangeItem ioExchangeItem;
    private double[] values;
    private double[] times;

    public NonMissingStochObserverExchangeItem(IPrevExchangeItem ioExchangeItem, double missingValue){
        this.ioExchangeItem = ioExchangeItem;
        // get rid of missing data:
        missingValue = Double.NaN;
        Hashtable<Integer,Double> eiValues = new Hashtable<Integer,Double>();
        Hashtable<Integer,Double> eiTimes = new Hashtable<Integer,Double>();
        boolean isMissing = false;
        int iNonMissing = 0;
        double[] orgValues = ioExchangeItem.getValuesAsDoubles();
        double[] orgTimes = ioExchangeItem.getTimes();
        //TODO: determine the 'most' appropriate value of epsilon
        double epsilon = 1.E-5;
        if (orgValues!=null && orgTimes!=null) {
            for (int iValues=0; iValues<orgValues.length; iValues++){
                if (orgTimes.length != orgValues.length) {
                    throw new RuntimeException("Inconsisten #times/#values");
                }
                if (Double.isNaN(missingValue) || Math.abs(orgValues[iValues]-missingValue)<epsilon){
                    if (!Double.isNaN(orgValues[iValues])){
                        eiValues.put(iNonMissing,orgValues[iValues]);
                        eiTimes.put(iNonMissing,orgTimes[iValues]);
                        iNonMissing++;
                    } else {
                        isMissing = true;
                    }
                }
            }
        }
        if (isMissing){
            double[] newValues = new double[eiValues.size()];
            double[] newTimes = new double[eiValues.size()];
            for (int iNewValues=0; iNewValues<newValues.length; iNewValues++){
                newValues[iNewValues] = eiValues.get(iNewValues);
                newTimes[iNewValues] = eiTimes.get(iNewValues);
            }
            this.values = newValues;
            this.times = newTimes;
        } else {
            values = this.ioExchangeItem.getValuesAsDoubles();
            times = this.ioExchangeItem.getTimes();
        }
    }

    @Override
    public void copyValuesFromItem(IExchangeItem sourceItem) {
        throw new UnsupportedOperationException("org.openda.observers.NonMissingStochObserverExchangeItem.copyValuesFromItem(): Not implemented yet.");
    }

    @Override
    public ITimeInfo getTimeInfo() {
        throw new UnsupportedOperationException("org.openda.observers.NonMissingStochObserverExchangeItem.getTimeInfo(): Not implemented yet.");
    }

    @Override
    public IQuantityInfo getQuantityInfo() {
        throw new UnsupportedOperationException("org.openda.observers.NonMissingStochObserverExchangeItem.getQuantityInfo(): Not implemented yet.");
    }

    @Override
    public IGeometryInfo getGeometryInfo() {
        throw new UnsupportedOperationException("org.openda.observers.NonMissingStochObserverExchangeItem.getGeometryInfo(): Not implemented yet.");
    }

    @Override
    public String getId() {
        return this.ioExchangeItem.getId();
    }

    @Override
    public String getDescription() {
        return this.ioExchangeItem.getDescription();
    }

    @Override
    public Class getValueType() {
        return this.ioExchangeItem.getValueType();
    }

    public ValueType getValuesType() {
    	ValueType result;
    	if(ioExchangeItem.getValueType()==double.class){
    		result=ValueType.doubleType;
    	}else if(ioExchangeItem.getValueType()==double[].class){
    		result=ValueType.doublesType;
    	}else if(ioExchangeItem.getValueType()==float[].class){
    		result=ValueType.floatsType;
    	}else if(ioExchangeItem.getValueType()==double[][].class){
    		result=ValueType.doubles2dType;
    	}else if(ioExchangeItem.getValueType()==int.class){
    		result=ValueType.intType;
    	}else if(ioExchangeItem.getValueType()==String.class){
    		result=ValueType.StringType;
    	}else if(ioExchangeItem.getValueType()==IVector.class){
    		result=ValueType.IVectorType;
    	}else if(ioExchangeItem.getValueType()==IArray.class){
    		result=ValueType.IArrayType;
    	}else{
    		throw new RuntimeException("NonMissingStochObserver: unsupported ValueType");
    	}
        return result;
    }


    @Override
    public Role getRole() {
        return this.ioExchangeItem.getRole();
    }

    @Override
    public Object getValues() {
        return this.values;
    }

    @Override
    public double[] getValuesAsDoubles() {
        return this.values;
    }

    @Override
    public void axpyOnValues(double alpha, double[] axpyValues) {
        this.ioExchangeItem.axpyOnValues(alpha,axpyValues);
    }

    @Override
    public void multiplyValues(double[] multiplicationFactors) {
        this.ioExchangeItem.multiplyValues(multiplicationFactors);
    }

    @Override
    public void setValues(Object values) {
        throw new RuntimeException("setValues not allowed for an observer");
    }

    @Override
    public void setValuesAsDoubles(double[] values) {
        throw new RuntimeException("setValuesAsDoubles not allowed for an observer");
    }

    @Override
    public double[] getTimes() {
        return this.times;
    }

    @Override
    public void setTimes(double[] times) {
        throw new RuntimeException("setTimes not allowed for an observer");
    }
}