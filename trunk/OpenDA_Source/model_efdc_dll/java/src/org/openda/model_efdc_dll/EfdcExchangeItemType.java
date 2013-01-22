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

/**
 * Exchange item types.
 */
public enum EfdcExchangeItemType {
	//atmospheric forcing variables (input).
	PRECIPITATION			(101, "Precipitation", EfdcExchangeItemRole.FORCING),
	AIR_TEMPERATURE			(102, "AirTemperature", EfdcExchangeItemRole.FORCING),
	CLOUD_COVER				(103, "CloudCover", EfdcExchangeItemRole.FORCING),
	GLOBAL_RADIATION		(104, "GlobalRadiation", EfdcExchangeItemRole.FORCING),
	ATMOSPHERIC_PRESSURE	(105, "AtmosphericPressure", EfdcExchangeItemRole.FORCING),
	RELATIVE_HUMIDITY		(106, "RelativeHumidity", EfdcExchangeItemRole.FORCING),
	POTENTIAL_EVAPORATION	(107, "PotentialEvaporation", EfdcExchangeItemRole.FORCING),

	//boundary condition variables (input).
	WATER_LEVEL					(201, "WaterLevel", EfdcExchangeItemRole.BOUNDARY),
	DISCHARGE					(301, "Discharge", EfdcExchangeItemRole.BOUNDARY),
	WATER_TEMPERATURE			(401, "WaterTemperature", EfdcExchangeItemRole.BOUNDARY),

	ALGAL_CYANOBACTERIA			(501, "AlgalCyanobacteria", EfdcExchangeItemRole.BOUNDARY),
	ALGAL_DIATOM				(502, "AlgalDiatom", EfdcExchangeItemRole.BOUNDARY),
	ALGAL_GREEN_ALGAE			(503, "AlgalGreenAlgae", EfdcExchangeItemRole.BOUNDARY),
	REFRACTORY_PO_CARBON		(504, "RefractoryPOCarbon", EfdcExchangeItemRole.BOUNDARY),
	LABILE_PO_CARBON			(505, "LabilePOCarbon", EfdcExchangeItemRole.BOUNDARY),
	DISSOLVED_O_CARBON			(506, "DissolvedOCarbon", EfdcExchangeItemRole.BOUNDARY),
	REFRACTORY_PO_PHOSPHORUS	(507, "RefractoryPOPhosphorus", EfdcExchangeItemRole.BOUNDARY),
	LABILE_PO_PHOSPHORUS		(508, "LabilePOPhosphorus", EfdcExchangeItemRole.BOUNDARY),
	DISSOLVED_O_PHOSPHORUS		(509, "DissolvedOPhosphorus", EfdcExchangeItemRole.BOUNDARY),
	PHOSPHATE					(510, "Phosphate", EfdcExchangeItemRole.BOUNDARY),
	REFRACTORY_PO_NITROGEN		(511, "RefractoryPONitrogen", EfdcExchangeItemRole.BOUNDARY),
	LABILE_PO_NITROGEN			(512, "LabilePONitrogen", EfdcExchangeItemRole.BOUNDARY),
	DISSOLVED_O_NITROGEN		(513, "DissolvedONitrogen", EfdcExchangeItemRole.BOUNDARY),
	AMMONIA						(514, "Ammonia", EfdcExchangeItemRole.BOUNDARY),
	NITRATE						(515, "Nitrate", EfdcExchangeItemRole.BOUNDARY),
	DISSOLVED_OXYGEN			(519, "DissolvedOxygen", EfdcExchangeItemRole.BOUNDARY),

	//state variables (output).
	GRID_WATER_LEVEL				(1201, "WaterLevel", EfdcExchangeItemRole.STATE),
	GRID_DISCHARGE					(1301, "Discharge", EfdcExchangeItemRole.STATE),
	GRID_WATER_TEMPERATURE			(1401, "WaterTemperature", EfdcExchangeItemRole.STATE),

	GRID_ALGAL_CYANOBACTERIA		(1501, "AlgalCyanobacteria", EfdcExchangeItemRole.STATE),
	GRID_ALGAL_DIATOM				(1502, "AlgalDiatom", EfdcExchangeItemRole.STATE),
	GRID_ALGAL_GREEN_ALGAE			(1503, "AlgalGreenAlgae", EfdcExchangeItemRole.STATE),
	GRID_REFRACTORY_PO_CARBON		(1504, "RefractoryPOCarbon", EfdcExchangeItemRole.STATE),
	GRID_LABILE_PO_CARBON			(1505, "LabilePOCarbon", EfdcExchangeItemRole.STATE),
	GRID_DISSOLVED_O_CARBON			(1506, "DissolvedOCarbon", EfdcExchangeItemRole.STATE),
	GRID_REFRACTORY_PO_PHOSPHORUS	(1507, "RefractoryPOPhosphorus", EfdcExchangeItemRole.STATE),
	GRID_LABILE_PO_PHOSPHORUS		(1508, "LabilePOPhosphorus", EfdcExchangeItemRole.STATE),
	GRID_DISSOLVED_O_PHOSPHORUS		(1509, "DissolvedOPhosphorus", EfdcExchangeItemRole.STATE),
	GRID_PHOSPHATE					(1510, "Phosphate", EfdcExchangeItemRole.STATE),
	GRID_REFRACTORY_PO_NITROGEN		(1511, "RefractoryPONitrogen", EfdcExchangeItemRole.STATE),
	GRID_LABILE_PO_NITROGEN			(1512, "LabilePONitrogen", EfdcExchangeItemRole.STATE),
	GRID_DISSOLVED_O_NITROGEN		(1513, "DissolvedONitrogen", EfdcExchangeItemRole.STATE),
	GRID_AMMONIA					(1514, "Ammonia", EfdcExchangeItemRole.STATE),
	GRID_NITRATE					(1515, "Nitrate", EfdcExchangeItemRole.STATE),
	GRID_DISSOLVED_OXYGEN			(1519, "DissolvedOxygen", EfdcExchangeItemRole.STATE);

	public enum EfdcExchangeItemRole {FORCING, BOUNDARY, STATE}

	private final int parameterNumber;
	private final String parameterId;
	private final EfdcExchangeItemRole role;

	/**
	 * @param parameterNumber integer identifier of a variable as defined in the efdc model
	 * @param parameterId name of a quantity
	 * @param role
	 */
	private EfdcExchangeItemType(int parameterNumber, String parameterId, EfdcExchangeItemRole role) {
		this.parameterNumber = parameterNumber;
		this.parameterId = parameterId;
		this.role = role;
	}

	public int getParameterNumber() {
		return this.parameterNumber;
	}

	public String getParameterId() {
		return this.parameterId;
	}

	public EfdcExchangeItemRole getRole() {
		return this.role;
	}
}
