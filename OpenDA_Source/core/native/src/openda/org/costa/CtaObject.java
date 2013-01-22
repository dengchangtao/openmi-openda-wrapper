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

package org.costa;

import org.openda.utils.Instance;

/**
 * Costa Object (stores Costa handle, initializes DLL)
 */


public class CtaObject extends Instance {

    protected final int CtaNULL = 0;
    protected final int CtaOK = 0;

    static {
        // load OpenDA/Costa bridging DLL once.
        System.loadLibrary("opendabridge");
        ctaInit();
    }
    
    protected int ctaHandle = CtaNULL; // Costa handle to tree vector
    protected int ctaLastError = CtaOK; // Result of last error call

    @Override
    public void finalize() throws Throwable {
        this.free();
        super.finalize();
    }

    public void free() {
        if ( ctaHandle != CtaNULL ) {
            this.ctaFree();
        }
    }

    private static native void ctaInit();

    public native void ctaFree();

}
