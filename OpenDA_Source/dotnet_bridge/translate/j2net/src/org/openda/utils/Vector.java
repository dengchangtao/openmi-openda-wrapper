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
package org.openda.utils;

import org.openda.interfaces.IVector;
import org.openda.utils.performance.OdaGlobSettings;

import java.io.*;
import java.util.List;

/**
 * Vector
 */
public class Vector implements IVector, Externalizable {

    VectorDouble dVec=null;
	VectorFloat fVec=null;
	public int maxFullExpandLength = 20;
	private boolean doublePrecision=true;




    // simple constructor for derived simple-tree-vector
    public Vector() {
		doublePrecision=!OdaGlobSettings.getVectorPrecisionFloat();
    }

    /**
     * Create a new Vector
     * <p/>
     *
     * @param n Length of the vector
     */
    public Vector(int n) {
		doublePrecision=!OdaGlobSettings.getVectorPrecisionFloat();
		if (doublePrecision){
			dVec=new VectorDouble(n);
		}
		else {
			fVec=new VectorFloat(n);
		}
    }

    /**
     * Create a new Vector
     * <p/>
     *
     * @param values content of the vector as array of doubles
     */
    public Vector(double[] values) {
		doublePrecision=!OdaGlobSettings.getVectorPrecisionFloat();
		if (doublePrecision){
			dVec=new VectorDouble(values);
		}
		else {
			fVec=new VectorFloat(values);
		}
    }

    /**
     * Create a new Vector
     * <p/>
     *
     * @param values content of the vector as array of ints
     */
    public Vector(int[] values) {
		doublePrecision=!OdaGlobSettings.getVectorPrecisionFloat();
		if (doublePrecision){
			dVec=new VectorDouble(values);
		}
		else {
			fVec=new VectorFloat(values);
		}
    }

    /**
     * Create a new Vector
     * <p/>
     *
     * @param v content of the new vector, provide as source vector
     */
    public Vector(IVector v) {
		doublePrecision=!OdaGlobSettings.getVectorPrecisionFloat();
		if (doublePrecision){
			dVec=new VectorDouble(castVector(v));
		}
		else {
			fVec=new VectorFloat(castVector(v));
		}
    }

    /**
     * Create a new Vector
     * <p/>
     *
     * @param valuestring content of the vector as array string, eg. "[1.0,2.0]"
     */
    public Vector(String valuestring) {
		doublePrecision=!OdaGlobSettings.getVectorPrecisionFloat();
		if (doublePrecision){
			dVec=new VectorDouble(valuestring);
		}
		else {
			fVec=new VectorFloat(valuestring);
		}
    }

    public Vector(List<IVector> vectorList) {
		doublePrecision=!OdaGlobSettings.getVectorPrecisionFloat();
		if (doublePrecision){
			dVec=new VectorDouble(vectorList);
		}
		else {
			fVec=new VectorFloat(vectorList);
		}
    }

    /**
     * Write Vector to string
     * <p/>
     */
    public String toString() {
		if (doublePrecision){
			return dVec.toString();
		}
		else {
			return fVec.toString();
		}
    }

    public String printString(String indent) {
		if (doublePrecision){
			return dVec.printString(indent);
		}
		else {
			return fVec.printString(indent);
		}
    }

    /**
     * {@inheritDoc}
     */
    public void setConstant(double value) {
		if (doublePrecision){
			dVec.setConstant(value);
		}
		else {
			fVec.setConstant(value);
		}
    }

    /**
     * {@inheritDoc}
     */
    public void scale(double alpha) {
		if (doublePrecision){
		   dVec.scale(alpha);
		}
		else {
		   fVec.scale(alpha);
		}
    }

    /**
     * {@inheritDoc}
     */
    public void setValues(double[] values) {
		if (doublePrecision){
			dVec.setValues(values);
		}
		else {
			fVec.setValues(values);
		}
    }

    /**
     * {@inheritDoc}
     */
    public double[] getValues() {
		if (doublePrecision){
			return dVec.getValues();
		}
		else {
			return fVec.getValues();
		}
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(int index, double value) {
		if (doublePrecision){
			dVec.setValue(index,value);
		}
		else {
			fVec.setValue(index,value);
		}
    }

    /**
     * {@inheritDoc}
     */
    public double getValue(int index) {
		if (doublePrecision){
			return dVec.getValue(index);
		}
		else {
			return fVec.getValue(index);
		}
    }

    /**
     * {@inheritDoc}
     */
    public int getSize() {
		if (doublePrecision){
			return dVec.getSize();
		}
		else {
			return fVec.getSize();
		}
    }

    /**
     * {@inheritDoc}
     */
    public void axpy(double alpha, IVector x) {
		if (doublePrecision){
			dVec.axpy(alpha,castVector(x));
		}
		else {
			fVec.axpy(alpha,castVector(x));
		}
    }

    /**
     * {@inheritDoc}
     */
    public double dotProduct(IVector otherVector) {
		if (doublePrecision){
			return dVec.dotProduct(castVector(otherVector));
		}
		else {
			return fVec.dotProduct(castVector(otherVector));
		}
    }

    /**
     * {@inheritDoc}
     */
    public double norm2() {
		if (doublePrecision){
			return dVec.norm2();
		}
		else {
			return fVec.norm2();
		}
    }

    /**
     * {@inheritDoc}
     */
    public void pointwiseDivide(IVector otherVector) {
		if (doublePrecision){
			dVec.pointwiseDivide(castVector(otherVector));
		}
		else {
			fVec.pointwiseDivide(castVector(otherVector));
		}
    }

    /**
     * {@inheritDoc}
     */
    public void pointwiseMultiply(IVector otherVector) {
		if (doublePrecision){
			dVec.pointwiseMultiply(castVector(otherVector));
		}
		else {
			fVec.pointwiseMultiply(castVector(otherVector));
		}
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    public Vector clone() {
		Vector cloned=new Vector();
		if (doublePrecision){
			  cloned.dVec=this.dVec.clone();
		}
		else {
			cloned.fVec=this.fVec.clone();
		}
		return cloned;
    }

    /**
     * Free a Vector Instance.
     */
    public void free() {
    } //nothing to do, but required to match interface


    /**
     * Create a vector as a range of numbers
     *
     * @param xmin  first number
     * @param xmax  last number is less or equal this number
     * @param xstep increment between numbers
     * @return Vector containing the numbers defined by min,max,step.
     */
    public static IVector range(double xmin, double xmax, double xstep) {
        Vector newVec=new Vector();
		if (newVec.doublePrecision){
		   newVec.dVec= (VectorDouble) VectorDouble.range(xmin,xmax,xstep);
		}
		else {
			newVec.fVec= (VectorFloat) VectorFloat.range(xmin,xmax,xstep);
		}
		return newVec;
    }

    /**
     * Compute pointwise sqrt of a vector x(i) <= sqrt(x(i))
     */
    public void sqrt() {
		if (doublePrecision){
			dVec.sqrt();
		}
		else {
			fVec.sqrt();
		}
    }

    /**
     * Concatenate two vectors
     *
     * @param v1 first part
     * @param v2 second part
     * @return [v1,v2] concatenated
     */
    public static Vector concatenate(IVector v1, IVector v2) {
        Vector newVec=new Vector();
		if (newVec.doublePrecision){
		  	newVec.dVec=VectorDouble.concatenate(v1,v2);
		}
		else {
			newVec.fVec=VectorFloat.concatenate(v1,v2);
		}
		return newVec;
    }

    public void invert() {
		if (doublePrecision){
			dVec.invert();
		}
		else {
			fVec.invert();
		}
    }

	public void serialize(PrintStream outputStream) {
		if(doublePrecision){
			dVec.serialize(outputStream);
		}
		else {
			fVec.serialize(outputStream);
		}
 }

	public void writeExternal(ObjectOutput objectOutput) throws IOException {
		objectOutput.writeBoolean(doublePrecision);
		if(doublePrecision){
			objectOutput.writeObject(dVec);
		}
		else {
			objectOutput.writeObject(fVec);
		}
	}

	public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
		doublePrecision = objectInput.readBoolean();
		if(doublePrecision){
			dVec=(VectorDouble) objectInput.readObject();
		}
		else {
			fVec=(VectorFloat) objectInput.readObject();
		}
	}

	// Return handle to one of the sub-vector classes (used for argument in Vector-Vector operations
	// This will often results in operations between similar vector classes improving performance significantly
	private IVector castVector(IVector vecIn){
		Vector retVec=null;
		if (vecIn instanceof Vector){
			Vector vec=(Vector) vecIn;
			if (vec.dVec!=null){
				return vec.dVec;
			}
			else {
				return vec.fVec;
			}
		}
		else {
			return vecIn;
		}
	}
}

