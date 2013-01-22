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

import org.openda.interfaces.IGeometryInfo;

/**
 * @author Arno Kockx
 */
public class IrregularGridGeometryInfo implements IGeometryInfo {
	private final int cellCount;

	public IrregularGridGeometryInfo(int cellCount) {
		this.cellCount = cellCount;
	}

	public int getCellCount() {
		return this.cellCount;
	}

	@Override
	public String toString(){
		return "{" + getClass().getSimpleName() + " cellCount=" + this.cellCount + "}";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass() || o.hashCode() != hashCode()) {
			return false;
		}

		IrregularGridGeometryInfo that = (IrregularGridGeometryInfo) o;
		if (this.cellCount != that.cellCount) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int h = this.cellCount;
		return h;
	}
}
