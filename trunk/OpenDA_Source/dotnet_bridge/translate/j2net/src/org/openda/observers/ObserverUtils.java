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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import org.openda.interfaces.IPrevExchangeItem;
import org.openda.interfaces.IStochObserver;

public class ObserverUtils {
	private int numberOfObsValues=0;
	private Vector<String> obsIds = new Vector<String>();
	private Vector<Integer> indexFirst = new Vector<Integer>();
	private Vector<Integer> indexLast = new Vector<Integer>();
	private double[] obsTimes = null;

	public ObserverUtils(IStochObserver observer){
		this.numberOfObsValues = observer.getCount();
		this.obsTimes = new double[numberOfObsValues];
		List<IPrevExchangeItem> items = null;
		try{
			items = observer.getObservationDescriptions().getExchangeItems();
		}catch (Exception e) {
			items = new ArrayList<IPrevExchangeItem>(); //Empty list
		}
		if(items!=null){
			int indFirst = 0;
			int indLast = 0;
			for(IPrevExchangeItem item : items){ // assume the exchangeItems are in the
				String id = item.getId();
				int n = 0;
				double times[] = item.getTimes();
				if (times != null) {
					n = times.length;
				}
				indLast = indFirst + n -1;
				this.indexFirst.add(indFirst);
				this.indexLast.add(indLast);
				this.obsIds.add(id);
				if(n>0){
					System.arraycopy(times, 0, obsTimes, indFirst, n);
				}
				indFirst = indLast+1;
			}
		}else{ //if there is no metadata then use the index
			for(int i=0;i<numberOfObsValues;i++){
				this.indexFirst.add(i);
				this.indexLast.add(i);
				this.obsIds.add("index_"+i);
				this.obsTimes[i]=0.0;
			}
		}
	}

	public String[] getObsIds(){
		String[] results = new String[this.numberOfObsValues];
		for(int i=0;i<this.obsIds.size();i++){
			for(int j=this.indexFirst.get(i);j<=indexLast.get(i);j++){
				results[j] = this.obsIds.get(i);
			}
		}
		return results;
	}

	public double[] getObsTimeOffsets(double refTime){
		double[] result = new double[this.numberOfObsValues];
		for(int i=0;i<this.numberOfObsValues;i++){
			result[i] = this.obsTimes[i] - refTime;
		}
		return result;
	}
}
