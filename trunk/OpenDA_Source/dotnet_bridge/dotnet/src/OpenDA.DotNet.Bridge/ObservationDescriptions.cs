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


using System;
using System.Collections.Generic;
using OpenDA.DotNet.Interfaces;

namespace OpenDA.DotNet.Bridge
{
	public class ObservationDescriptions : IObservationDescriptions
	{
		private readonly List<IExchangeItem> _exchangeItems;

		public ObservationDescriptions(IExchangeItem[] exchangeItems)
		{
			_exchangeItems = new List<IExchangeItem>(exchangeItems);
		}

		public List<IExchangeItem> ExchangeItems
		{
			get { return _exchangeItems; }
		}

		public IVector GetValueProperties(string key)
		{
			throw new NotImplementedException();
		}

		public string[] GetStringProperties(string key)
		{
			throw new NotImplementedException();
		}

		public string[] PropertyKeys
		{
			get { throw new NotImplementedException(); }
		}

		public int PropertyCount
		{
			get { throw new NotImplementedException(); }
		}

		public int ObservationCount
		{
            get { return _exchangeItems.Count; }
		}

		public ITime[] Times
		{
			get { throw new NotImplementedException(); }
		}
	}
}