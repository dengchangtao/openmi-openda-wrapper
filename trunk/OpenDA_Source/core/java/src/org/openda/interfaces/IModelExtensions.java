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

package org.openda.interfaces;

/**
 * Created with IntelliJ IDEA.
 * User: nils
 * Date: 6-3-13
 * Time: 13:31
 * Extensions to the IModelInterface to optimize/customize the behaviour of Models without braking old models
 */
public interface IModelExtensions {
	/**
	 * Get the localization vector
	 * @param observationDescriptions observation description
	 * @param distance characteristic distance for Cohn's formula
	 * @return weight vector for each observation location.
	 */
	public IVector[] getObservedLocalization(String exchangeItemID, IObservationDescriptions observationDescriptions, double distance);

	/**
	 * Get the observed values of the Model.
	 * @param observationDescriptions observation description
	 * @return Model prediction interpolated to each observation (location).
	 */
	public IVector getObservedValues(IObservationDescriptions observationDescriptions);
}