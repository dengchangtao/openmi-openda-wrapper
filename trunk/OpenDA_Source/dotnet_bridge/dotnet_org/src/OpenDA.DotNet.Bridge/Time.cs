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
using OpenDA.DotNet.Interfaces;

namespace OpenDA.DotNet.Bridge
{
	public class Time : ITime
	{
		private const double _epsilon = 1.0e-9;
		private readonly double _beginTimeAsMJD;
		private readonly double _endTimeAsMJD;
		private readonly bool _isSpan;

		public Time(double beginTimeAsMJD, double endTimeAsMJD)
		{
			_beginTimeAsMJD = beginTimeAsMJD;
			_endTimeAsMJD = endTimeAsMJD;
			if (beginTimeAsMJD + _epsilon < endTimeAsMJD)
			{
				_isSpan = true;
			}
		}

		public Time(double beginTime, double endTime, double stepMJD, bool isSpan) : this(beginTime, endTime)
		{
			StepMJD = stepMJD;
			_isSpan = isSpan;
		}

		public Time(double timeStampAsMJD)
		{
			_beginTimeAsMJD = _endTimeAsMJD = timeStampAsMJD;
		}

		public double MJD
		{
			get { return (_beginTimeAsMJD + _endTimeAsMJD)/2.0d; }
		}

		public double StepMJD { get; set; }

		public ITime BeginTime
		{
			get { return new Time(_beginTimeAsMJD); }
		}

		public double BeginTimeAsMJD
		{
			get { return _beginTimeAsMJD; }
		}

		public ITime EndTime
		{
			get { return new Time(_endTimeAsMJD); }
		}

		public double EndTimeAsMJD
		{
			get { return _endTimeAsMJD; }
		}

		public bool IsStamp()
		{
			return !_isSpan;
		}

		public bool IsSpan()
		{
			return _isSpan;
		}

		public bool InSpan(double otherTimeBeginAsMJD, double otherTimeEndAsMJD)
		{
			if (!(otherTimeEndAsMJD > otherTimeBeginAsMJD + _epsilon))
			{
				throw new Exception("Argument is timeSTAMP where a timeSPAN was expected.");
			}
			return (_beginTimeAsMJD > otherTimeBeginAsMJD)
			       & (_endTimeAsMJD <= otherTimeEndAsMJD);
		}

		public bool After(double otherTimeAsMJD)
		{
			return (BeginTime.MJD > otherTimeAsMJD);
		}

		public bool BeforeEquals(double otherTimeAsMJD)
		{
			return (EndTime.MJD <= otherTimeAsMJD);
		}

		public void Finish()
		{
			// nothing to do
		}
	}
}