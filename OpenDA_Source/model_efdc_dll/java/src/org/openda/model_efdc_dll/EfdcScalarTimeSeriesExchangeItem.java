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
package org.openda.model_efdc_dll;

import org.openda.exchange.TimeInfo;
import org.openda.interfaces.IExchangeItem;
import org.openda.interfaces.IGeometryInfo;
import org.openda.interfaces.IQuantityInfo;
import org.openda.interfaces.ITime;
import org.openda.interfaces.ITimeInfo;
import org.openda.interfaces.IVector;
import org.openda.utils.Time;
import org.openda.utils.Vector;

/**
 * Exchange item representing values for a time series for a single location
 * that are stored in the dll version of the EFDC model.
 *
 * @author Arno Kockx
 */
public class EfdcScalarTimeSeriesExchangeItem implements IExchangeItem {
	private static final long serialVersionUID = -8587168149103121915L;

	private final String id;
	/**
	 * Integer that corresponds to a certain parameter within the efdc model.
	 */
	private final int locationNumber;
	/**
	 * Integer that corresponds to a certain parameter within the efdc model.
	 */
	private final int parameterNumber;
	private final Role role;
	private final EfdcDLL modelDll;

	public EfdcScalarTimeSeriesExchangeItem(int locationNumber, int parameterNumber, String parameterId, Role role, EfdcDLL modelDll) {
		//id = "locationId.parameterId"
		this.id = locationNumber + "." + parameterId;
		this.locationNumber = locationNumber;
		this.parameterNumber = parameterNumber;
		this.role = role;
		this.modelDll = modelDll;
	}

	public String getId() {
		return this.id;
	}

	public String getDescription() {
		return null;
	}

	public Role getRole() {
		return this.role;
	}

	public ITimeInfo getTimeInfo() {
		return new TimeInfo(this.modelDll.getTimesForExchangeItem(this.parameterNumber, this.locationNumber));
	}

	//TODO this method is only present for backwards compatibility. This method should be removed
	//once all exchange items have been migrated to the new IExchangeItem approach. AK
	@Deprecated
	public double[] getTimes() {
		return getTimeInfo().getTimes();
	}

	//TODO this method is only present for backwards compatibility. This method should be removed
	//once all exchange items have been migrated to the new IExchangeItem approach. AK
	@Deprecated
	public void setTimes(double[] times) {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": setTimes not implemented.");
	}

	void setTimesForUnitTest(double[] times) {
		this.modelDll.setTimesForExchangeItem(this.parameterNumber, this.locationNumber, times);
	}

	public IQuantityInfo getQuantityInfo() {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": getQuantityInfo not implemented.");
	}

	public IGeometryInfo getGeometryInfo() {
		throw new UnsupportedOperationException(getClass().getSimpleName() + ": getGeometryInfo not implemented.");
	}

	public ValueType getValuesType() {
		return ValueType.IVectorType;
	}

	public Class<?> getValueType() {
		return IVector.class;
	}

	/**
	 * Returns all values for this scalar time series.
	 */
	public Object getValues() {
		double[] values = getValuesAsDoubles();
		IVector vector = new Vector(values);
		return vector;
	}

	/**
	 * Returns all values for this scalar time series.
	 */
	public double[] getValuesAsDoubles() {
		double[] times = getTimeInfo().getTimes();
		ITime firstTime = new Time(times[0]);
		ITime lastTime = new Time(times[times.length - 1]);
		return this.modelDll.getValues(this.parameterNumber, this.locationNumber, firstTime, lastTime);
	}

	public void axpyOnValues(double alpha, double[] axpyValues) {
		double[] values = getValuesAsDoubles();
		if (axpyValues.length != values.length) {
			throw new IllegalStateException(getClass().getSimpleName() + ": axpyValues.length (" + axpyValues.length
					+ ") must equal valueCount (" + values.length + ") for variable " + this.id + ".");
		}

		for (int n = 0; n < values.length; n++) {
			values[n] += alpha * axpyValues[n];
		}
		setValuesAsDoubles(values);
	}

	public void multiplyValues(double[] multiplicationFactors) {
		double[] values = getValuesAsDoubles();
		if (multiplicationFactors.length != values.length) {
			throw new IllegalStateException(getClass().getSimpleName() + ": multiplicationFactors.length (" + multiplicationFactors.length
					+ ") must equal valueCount (" + values.length + ") for variable " + this.id + ".");
		}

		for (int n = 0; n < values.length; n++) {
			values[n] *= multiplicationFactors[n];
		}
		setValuesAsDoubles(values);
	}

	/**
	 * Sets all values for this scalar time series.
	 */
	public void setValues(Object vector) {
		if (!(vector instanceof IVector)) {
			throw new IllegalArgumentException(getClass().getSimpleName() + ": supply values as an IVector not as " + vector.getClass().getSimpleName());
		}

		setValuesAsDoubles(((IVector) vector).getValues());
	}

	/**
	 * Sets all values for this scalar time series.
	 */
	public void setValuesAsDoubles(double[] values) {
		double[] times = getTimeInfo().getTimes();
		if (values.length != times.length) {
			throw new IllegalArgumentException(getClass().getSimpleName() + ": number of values (" + values.length
					+ ") should be equal to number of times (" + times.length + ").");
		}

		ITime firstTime = new Time(times[0]);
		ITime lastTime = new Time(times[times.length - 1]);
		this.modelDll.setValues(this.parameterNumber, values, this.locationNumber, firstTime, lastTime);
	}

	/**
	 * This method reads a scalar time series from the given sourceItem
	 * and stores all times and values in this exchangeItem.
	 */
	public void copyValuesFromItem(IExchangeItem sourceItem) {
		if (sourceItem.getTimeInfo() == null || sourceItem.getTimeInfo().getTimes() == null) {
			throw new RuntimeException(getClass().getSimpleName() + ": cannot copy data from sourceExchangeItem '"
					+ sourceItem.getId() + "' of type " + sourceItem.getClass().getSimpleName()
					+ " because it contains no time info.");
		}
		if (sourceItem.getValuesType() != ValueType.IVectorType) {
			throw new RuntimeException(getClass().getSimpleName() + ": cannot copy data from sourceExchangeItem '"
					+ sourceItem.getId() + "' of type " + sourceItem.getClass().getSimpleName()
					+ " because its value type is not " + ValueType.IVectorType);
		}

		this.modelDll.setTimesForExchangeItem(this.parameterNumber, this.locationNumber,
				sourceItem.getTimeInfo().getTimes());
		setValues(sourceItem.getValues());
	}
}
