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
import org.openda.interfaces.IArray;
import org.openda.interfaces.IArrayGeometryInfo;
import org.openda.interfaces.IQuantityInfo;

/**
 * Geometry info for spatial gridded data, that is for values stored in an IArray.
 * Horizontal coordinates are preferably in WGS84 latitude and longitude in degrees.
 * 
 * @author verlaanm
 *
 */
//TODO rename to LatLonGridGeometryInfo or 2DGridGeometryInfo? AK
public class ArrayGeometryInfo implements IArrayGeometryInfo {

	private IArray latitudeCoordinateValues=null;
	private IArray longitudeCoordinateValues=null;
	private IArray heightCoordinateValues=null;
	private int[] latitudeValueIndices=null;
	private int[] longitudeValueIndices=null;
	private int[] heightValueIndices=null;
	private IQuantityInfo latitudeQuantityInfo=null;
	private IQuantityInfo longitudeQuantityInfo=null;
	private IQuantityInfo heightQuantityInfo=null;
	private int[] activeCellMask=null;

	public ArrayGeometryInfo(){
		//empty constructor - use setInfo to set values
	}
	
	public ArrayGeometryInfo(IArray latitudeArray, int[] latitudeValueIndices, IQuantityInfo latitudeQuantityInfo,
			IArray longitudeArray, int[] longitudeValueIndices, IQuantityInfo longitudeQuantityInfo,
			IArray heightArray, int[] heightValueIndices, IQuantityInfo heightQuantityInfo) {
		this(latitudeArray, latitudeValueIndices, latitudeQuantityInfo,
				longitudeArray, longitudeValueIndices, longitudeQuantityInfo,
				heightArray, heightValueIndices, heightQuantityInfo, null);
	}

	public ArrayGeometryInfo(IArray latitudeArray, int[] latitudeValueIndices, IQuantityInfo latitudeQuantityInfo,
			IArray longitudeArray, int[] longitudeValueIndices, IQuantityInfo longitudeQuantityInfo,
			IArray heightArray, int[] heightValueIndices, IQuantityInfo heightQuantityInfo, int[] activeCellMask) {
		this.latitudeCoordinateValues = latitudeArray;
		this.longitudeCoordinateValues = longitudeArray;
		this.heightCoordinateValues = heightArray;
		this.latitudeValueIndices = latitudeValueIndices;
		this.longitudeValueIndices = longitudeValueIndices;
		this.heightValueIndices = heightValueIndices;
		this.latitudeQuantityInfo = latitudeQuantityInfo;
		this.longitudeQuantityInfo = longitudeQuantityInfo;
		this.heightQuantityInfo = heightQuantityInfo;
		this.activeCellMask = activeCellMask;
	}

	public void setInfo(IArray latitudeArray, int[] latitudeValueIndices, IQuantityInfo latitudeQuantityInfo,
			IArray longitudeArray, int[] longitudeValueIndices, IQuantityInfo longitudeQuantityInfo,
			IArray heightArray, int[] heightValueIndices, IQuantityInfo heightQuantityInfo) {
		this.latitudeCoordinateValues = latitudeArray;
		this.longitudeCoordinateValues = longitudeArray;
		this.heightCoordinateValues = heightArray;
		this.latitudeValueIndices = latitudeValueIndices;
		this.longitudeValueIndices = longitudeValueIndices;
		this.heightValueIndices = heightValueIndices;
		this.latitudeQuantityInfo = latitudeQuantityInfo;
		this.longitudeQuantityInfo = longitudeQuantityInfo;
		this.heightQuantityInfo = heightQuantityInfo;
	}

	/**
	 * Degrees north of the equator. 
	 * Horizontal coordinates are preferably in WGS84.
	 * The latitude grid may form a multidimensional array.
	 * @return latitude
	 */
	public IArray getLatitudeArray(){
		return latitudeCoordinateValues;
	}

	/**
	 * Degrees east of Greenwich 0 meridian. 
	 * Horizontal coordinates are preferably in WGS84.
	 * The latitude grid may form a multidimensional array.
	 * @return longitude
	 */
	public IArray getLongitudeArray(){
		return longitudeCoordinateValues;
	}

	/**
	 * Pointer or pointers to latitude index in values array.
	 * See ArrayBasedExchangeItem for details
	 * @return pointers to array dimension(s)
	 */
	public int[] getLatitudeValueIndices(){
		return latitudeValueIndices;
	}

	/**
	 * Pointer or pointers to longitude index in values array.
	 * See ArrayBasedExchangeItem for details
	 * @return pointers to array dimension(s)
	 */
	public int[] getLongitudeValueIndices(){
		return longitudeValueIndices;
	}

	/**
	 * Returns information about the latitude coordinate values.
	 *
	 * @return quantity info
	 */
	public IQuantityInfo getLatitudeQuantityInfo() {
		return latitudeQuantityInfo;
	}

	/**
	 * Returns information about the longitude coordinate values.
	 *
	 * @return quantity info
	 */
	public IQuantityInfo getLongitudeQuantityInfo() {
		return longitudeQuantityInfo;
	}
	
	public String toString(){
		String result="{ArrayGeometryInfo \n";
		result+="{longitude ";
		if(this.longitudeQuantityInfo!=null){ result+=" "+this.longitudeQuantityInfo; }
		if(this.longitudeCoordinateValues!=null){ result+=" "+this.longitudeCoordinateValues.toString(); }
		result+="}\n";
		result+="{latitude ";
		if(this.latitudeQuantityInfo!=null){ result+=" "+this.latitudeQuantityInfo; }
		if(this.latitudeCoordinateValues!=null){ result+=" "+this.latitudeCoordinateValues.toString(); }
		result+="}}";
		return result;
	}

	@Override
	public IArray getHeightArray() {
		return this.heightCoordinateValues;
	}

	@Override
	public int[] getHeightValueIndices() {
		return heightValueIndices;
	}

	@Override
	public IQuantityInfo getHeightQuantityInfo() {
		return heightQuantityInfo;
	}

	@Override
	public int[] getActiveCellMask() {
		return this.activeCellMask;
	}
}
