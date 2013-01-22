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
package org.openda.exchange;

import org.openda.interfaces.IQuantityInfo;

/**
 * Direct implementation of IQuantityInfo interface.
 * 
 * @author verlaanm
 *
 */
public class QuantityInfo implements IQuantityInfo {

	private String quantity=null;
	private String unit=null;
	
	public QuantityInfo(String quantity, String unit){
		this.quantity=quantity;
		this.unit=unit;
	}
	
	@Override
	public String getQuantity() {
		return quantity;
	}

	@Override
	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit){
		this.unit = unit;
	}
	
	public void setQuantity(String quantity){
		this.quantity = quantity;
	}
	
	public String toString(){
		String result = "{QuantityInfo quantity="+this.quantity+", unit="+this.unit+" }";
		return result;
	}
}
