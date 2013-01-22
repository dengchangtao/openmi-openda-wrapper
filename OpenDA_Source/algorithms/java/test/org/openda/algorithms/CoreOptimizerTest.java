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
package org.openda.algorithms;
import junit.framework.TestCase;
import org.openda.interfaces.*;
import org.openda.utils.Vector;
//import org.openda.algorithms.*;

public class CoreOptimizerTest extends TestCase {

	public static void testSimplexCoreOptimizer_1() {
		System.out.println("========================================================");
		System.out.println("Basic test of a cost function");
		System.out.println("========================================================");
		// getting and setting
		ICostFunction f1 = new SimpleCostFunction();
		IVector p1 = new Vector("[0.0,0.0]");
		//public SimplexCoreOptimizer(SimpleCostFunction f, Vector pInit, double width){
		SimplexCoreOptimizer s1 = new SimplexCoreOptimizer(f1);
        s1.initialize(p1,1.5);
		//public Vector[] getCurrentValues(){
		IVector[] p1b = s1.getCurrentValues();
		System.out.println("s1.getCurrentValues()[1] = "+p1b[1].toString());
		System.out.println("Should be s1.getCurrentValues()[1] = [1.5,0.0]");
		assertEquals("s1.getCurrentValues()",p1b[1].toString(), "[1.5,0.0]");
		//public double[] getCurrentCosts(){
		double[] f1b = s1.getCurrentCosts();
		System.out.println("s1.getCurrentCosts()[1] = "+f1b[1]);
		System.out.println("Should be s1.getCurrentCosts()[1] = 0.5");
		assertEquals("s1.getCurrentCosts()",""+f1b[1], "0.5");
	}

	public static void testSimplexCoreOptimizer_2() {
		System.out.println("========================================================");
		System.out.println("Simplex minimization of kwadratic cost");
		System.out.println("========================================================");
		// Simplex test on simple kwadratic cost
		SimpleCostFunction f2 = new SimpleCostFunction();
		IVector p2 = new Vector("[0.0,0.0]");
		//public SimplexCoreOptimizer(SimpleCostFunction f, Vector pInit, double width){
		SimplexCoreOptimizer s2 = new SimplexCoreOptimizer(f2);
        s2.initialize(p2,1.5);
		s2.absTolSimplex = 0.001;
		s2.relTolSimplex = 0.00001;
		//public void optimize(int maxIter, double tol){
		s2.optimize();
		//public Vector getOptimalValue(){
		IVector p2b = s2.getOptimalValue();
		System.out.println("s2.getOptimalValue() = "+p2b);
		System.out.println("Should be s2.getOptimalValue() = [0.96148681640625,0.9224853515625]");
		assertEquals("s2.getOptimalValue()",""+p2b.toString(), "[0.96148681640625,0.9224853515625]");
		//public double getOptimalCost(){
		double f2b = s2.getOptimalCost();
		System.out.println("s2.getOptimalCost() = "+f2b);
		System.out.println("Should be s2.getOpimalCosts()[1] = 0.0029853954911231995");
		assertEquals("s2.getOptimalCost()",""+f2b, "0.0029853954911231995");
		IVector[] p2c = s2.getCurrentValues();
		double[] f2c = s2.getCurrentCosts();
		for(int i=0;i<p2c.length;i++){
			System.out.println("par["+i+"]="+p2c[i]);
			System.out.println("cost["+i+"]="+f2c[i]);
		}
	}

	public static void testRosenbrock_1() {
		System.out.println("========================================================");
		System.out.println("Basic test of the Rosenbrock costfunction");
		System.out.println("========================================================");
		// test Rosenbrock function itself (no optimization)
		ICostFunction fRb2 = new RosenbrockCostFunction();
		IVector p2a = new Vector("[0.0,0.0]");
		IVector p2b = new Vector("[1.0,1.0]"); //optimal with value 0.0
		double r2Vala = fRb2.evaluate(p2a);
		double r2Valb = fRb2.evaluate(p2b);
		System.out.println("Rosenbrock_2("+p2a+") = "+r2Vala);
		System.out.println("Should be Rosenbrock_2("+p2a+") = 1.0");
		assertEquals("Rosenbrock_2("+p2a+") ",r2Vala, 1.0);
		System.out.println("Rosenbrock_2("+p2b+") = "+r2Valb);
		System.out.println("Should be Rosenbrock_2("+p2b+") = 0.0");
		assertEquals("Rosenbrock_2("+p2b+") ",r2Valb, 0.0);

		ICostFunction fRb4 = new RosenbrockCostFunction();
		IVector p4a = new Vector("[0.0,0.0,0.0,0.0]");
		IVector p4b = new Vector("[1.0,1.0,1.0,1.0]"); //optimal with value 0.0
		double r4Vala = fRb4.evaluate(p4a);
		double r4Valb = fRb4.evaluate(p4b);
		System.out.println("Rosenbrock_4("+p4a+") = "+r4Vala);
		System.out.println("Should be Rosenbrock_4("+p4a+") = 2.0");
		assertEquals("Rosenbrock_4("+p4a+") ",r4Vala, 2.0);
		System.out.println("Rosenbrock_4("+p4b+") = "+r4Valb);
		System.out.println("Should be Rosenbrock_2("+p2b+") = 0.0");
		assertEquals("Rosenbrock_4("+p4b+") ",r4Valb, 0.0);
	}

	public static void testSimplexRosenbrock_1() {
		System.out.println("========================================================");
		System.out.println("Simplex minimization of the Rosenbrock function");
		System.out.println("========================================================");
		// test simplex method on rosenbrock
		ICostFunction fRb2 = new RosenbrockCostFunction();
		IVector p2 = new Vector("[0.0,0.0]");
		SimplexCoreOptimizer sRb2 = new SimplexCoreOptimizer(fRb2);
        sRb2.initialize(p2,1.5);
		sRb2.absTolSimplex = 0.001;
		sRb2.relTolSimplex = 0.0001;
		sRb2.optimize();
		//public Vector getOptimalValue(){
		IVector p2b = sRb2.getOptimalValue();
		System.out.println("s2.getOptimalValue() = "+p2b);
		System.out.println("Should be close to s2.getOptimalValue() = [1.0,1.0]");
		assertEquals("sRb2.getOptimalValue()",""+p2b.toString(), "[0.9961825689188117,0.9919014011138643]");
		//public double getOptimalCost(){
		double f2b = sRb2.getOptimalCost();
		System.out.println("s2.getOptimalCost() = "+f2b);
		System.out.println("Should be close to s2.getOpimalCosts() = 0.0");
		assertEquals("sRb2.getOptimalCost()",f2b, 0.0,0.0001);

	}

	public static void testPowelKwadratic_1() {
		System.out.println("========================================================");
		System.out.println("Powell minimization of a simple kwadratic function");
		System.out.println("========================================================");
		SimpleCostFunction f2 = new SimpleCostFunction();
		IVector p2 = new Vector("[0.1,0.1]");
		PowellCoreOptimizer p = new PowellCoreOptimizer(f2);
        p.initialize(p2,0.5);
		p.absTolPowell = 0.001;
		p.relTolPowell = 0.0001;
		p.optimize();
		//public Vector getOptimalValue(){
		IVector pOpt = p.getOptimalValue();
		System.out.println("p.getOptimalValue() = "+pOpt);
		System.out.println("Should be close to p.getOptimalValue() = [1.0,1.0]");
		assertEquals("p.getOptimalValue()",""+pOpt.toString(), "[0.9999999999999999,1.0]");
		//public double getOptimalCost(){
		double fOpt = p.getOptimalCost();
		System.out.println("p.getOptimalCost() = "+fOpt);
		System.out.println("Should be close to p.getOpimalCosts() = 0.0");
		assertEquals("p.getOptimalCost()",fOpt, 0.0,0.0001);

	}

	
	public static void testPowelRosenbrock_1() {
		System.out.println("========================================================");
		System.out.println("Powell minimization of the Rosenbrock function");
		System.out.println("========================================================");
		ICostFunction fRb2 = new RosenbrockCostFunction();
		IVector p2 = new Vector("[0.0,0.0]");
		PowellCoreOptimizer pRb = new PowellCoreOptimizer(fRb2);
        pRb.initialize(p2,0.5);
		pRb.absTolPowell = 0.001;
		pRb.relTolPowell = 0.0001;
		pRb.optimize();
		//public Vector getOptimalValue(){
		IVector pOpt = pRb.getOptimalValue();
		System.out.println("pRb.getOptimalValue() = "+pOpt);
		System.out.println("Should be close to pRb.getOptimalValue() = [1.0,1.0]");
		assertEquals("pRb.getOptimalValue()",""+pOpt.toString(), "[0.9313307487691697,0.8666303581882956]");
		//public double getOptimalCost(){
		double fOpt = pRb.getOptimalCost();
		System.out.println("pRb.getOptimalCost() = "+fOpt);
		System.out.println("Should be close to pRb.getOpimalCosts() = 0.0");
		assertEquals("pRb.getOptimalCost()",fOpt, 0.0,0.01);

	}

	public static void testPowellWithInit() {
		System.out.println("========================================================");
		System.out.println("Powell minimization of kwadratic cost with initialization");
		System.out.println("========================================================");
		// Powell test on simple kwadratic cost with inialization
		SimpleCostFunction f2 = new SimpleCostFunction();
		IVector p2 = new Vector("[0.0,0.0]");
		// minimum is at [1.0,1.0]
		IVector[] searchInit = new Vector[2];
		searchInit[0] = new Vector("[2.0,0.0]");
		searchInit[1] = new Vector("[0.0,1.0]");
		//public PowellCoreOptimizer(SimpleCostFunction f, Vector pInit, double width){
		PowellCoreOptimizer s2 = new PowellCoreOptimizer(f2);
        s2.initialize(p2,searchInit);
		//public void optimize()
		s2.optimize();
		//public Vector getOptimalValue(){
		IVector p2b = s2.getOptimalValue();
		System.out.println("s2.getOptimalValue() = "+p2b);
		System.out.println("Should be s2.getOptimalValue() = [0.9999999999999998,1.0]");
		assertEquals("s2.getOptimalValue()",""+p2b.toString(), "[0.9999999999999998,1.0]");
		//public double getOptimalCost(){
		double f2b = s2.getOptimalCost();
		System.out.println("s2.getOptimalCost() = "+f2b);
		System.out.println("Should be s2.getOpimalCosts()[1] = 4.930380657631324E-32");
		assertEquals("s2.getOptimalCost()",""+f2b, "4.930380657631324E-32");
		IVector[] p2c = s2.getCurrentSearchDirections();
		for(int i=0;i<p2c.length;i++){
			System.out.println("searchDir["+i+"]="+p2c[i]);
		}
	}

	public static void testSimplexWithInit() {
		System.out.println("========================================================");
		System.out.println("Simplex minimization of kwadratic cost with initialization");
		System.out.println("========================================================");
		// Simplex test on simple kwadratic cost with inialization
		SimpleCostFunction f2 = new SimpleCostFunction();
		//Vector p2 = new Vector("[0.0,0.0]");
		// minimum is at [1.0,1.0]
		IVector[] simplexInit = new Vector[3];
		simplexInit[0] = new Vector("[2.0,0.0]");
		simplexInit[1] = new Vector("[0.0,1.0]");
		simplexInit[2] = new Vector("[0.0,0.0]");
		//public SimplexCoreOptimizer(SimpleCostFunction f, Vector[] initialSimplex){
		SimplexCoreOptimizer s2 = new SimplexCoreOptimizer(f2);
        s2.initialize(simplexInit);
		s2.absTolSimplex = 0.001;
		s2.relTolSimplex = 0.00001;
		//public void optimize()
		s2.optimize();
		//public Vector getOptimalValue(){
		IVector p2b = s2.getOptimalValue();
		System.out.println("s2.getOptimalValue() = "+p2b);
		System.out.println("Should be s2.getOptimalValue() = [0.9871273040771484,0.9974803924560547]");
		assertEquals("s2.getOptimalValue()",""+p2b.toString(), "[0.9871273040771484,0.9974803924560547]");
		//public double getOptimalCost(){
		double f2b = s2.getOptimalCost();
		System.out.println("s2.getOptimalCost() = "+f2b);
		System.out.println("Should be s2.getOpimalCosts()[1] = 1.6729340586607577E-4");
		assertEquals("s2.getOptimalCost()",""+f2b, "1.6729340586607577E-4");
		IVector[] p2c = s2.getCurrentValues();
		for(int i=0;i<p2c.length;i++){
			System.out.println("searchDir["+i+"]="+p2c[i]);
		}
	}
	
	public static void testLeastSquaresCost_1() {
		System.out.println("========================================================");
		System.out.println("Simplex minimization of kwadratic LEAST-SQUARES cost");
		System.out.println("========================================================");
		// Simplex test on simple kwadratic cost
		ICostFunction f2 = new SimpleLeastSquaresCostFunction();
		IVector p2 = new Vector("[0.0,0.0]");
		//public SimplexCoreOptimizer(SimpleCostFunction f, Vector pInit, double width){
		SimplexCoreOptimizer s2 = new SimplexCoreOptimizer(f2);
        s2.initialize(p2,1.5);
		s2.absTolSimplex = 0.001;
		s2.relTolSimplex = 0.00001;
		//public void optimize(int maxIter, double tol){
		s2.optimize();
		//public Vector getOptimalValue(){
		IVector p2b = s2.getOptimalValue();
		System.out.println("s2.getOptimalValue() = "+p2b);
		System.out.println("Should be s2.getOptimalValue() = [0.96148681640625,0.9224853515625]");
		assertEquals("s2.getOptimalValue()",""+p2b.toString(), "[0.96148681640625,0.9224853515625]");
		//public double getOptimalCost(){
		double f2b = s2.getOptimalCost();
		System.out.println("s2.getOptimalCost() = "+f2b);
		System.out.println("Should be s2.getOpimalCosts()[1] = 1.9291848002467304E-4");
		assertEquals("s2.getOptimalCost()",f2b,0.0,0.01);
		IVector[] p2c = s2.getCurrentValues();
		double[] f2c = s2.getCurrentCosts();
		for(int i=0;i<p2c.length;i++){
			System.out.println("par["+i+"]="+p2c[i]);
			System.out.println("cost["+i+"]="+f2c[i]);
		}
	}

	public static void testDudWithoutConstraint() {
		System.out.println("========================================================");
		System.out.println("Dud minimization of kwadratic least-squares cost");
		System.out.println("without constraint");
		System.out.println("========================================================");
		// Dud test on simple kwadratic cost
		LeastSquaresCostFunction f1 = new SimpleLeastSquaresCostFunction();
		IVector p1 = new Vector("[0.0,0.0]");
		//public DudCoreOptimizer(SimpleLeastSquaresCostFunction f, Vector pInit){
		DudCoreOptimizer d1 = new DudCoreOptimizer(f1);
        d1.initialize(p1);
		d1.absTol = 0.001;
		d1.relTol = 0.001;
		//public void optimize(int maxIter, double tol){
		d1.optimize();
		//public Vector getOptimalValue(){
		IVector p1b = d1.getOptimalValue();
		System.out.println("d1.getOptimalValue() = "+p1b);
		System.out.println("Should be d1.getOptimalValue() = [1.0,1.0]");
		assertEquals("s2.getOptimalValue()",""+p1b.toString(), "[1.0,1.0]");
		double f1b = d1.getOptimalCost();
		System.out.println("d1.getOptimalCost() = "+f1b);
		System.out.println("Should be s2.getOpimalCosts()[1] = 0.0");
		assertEquals("s2.getOptimalCost()",""+f1b, "0.0");
		IVector[] p1c = d1.getCurrentValues();
		double[] f1c = d1.getCurrentCosts();
		for(int i=0;i<p1c.length;i++){
			System.out.println("par["+i+"]="+p1c[i]);
			System.out.println("cost["+i+"]="+f1c[i]);
		}
	}


	public static void testDudWithConstraint() {
		System.out.println("========================================================");
		System.out.println("Dud minimization of kwadratic least-squares cost ");
		System.out.println("with constraint");
		System.out.println("========================================================");
		// Dud test on simple kwadratic cost
		LeastSquaresCostFunction f1 = new SimpleLeastSquaresCostFunction();
		f1.setBackgroundTerm(true);
		IVector p1 = new Vector("[0.0,0.0]");
		//public DudCoreOptimizer(SimpleLeastSquaresCostFunction f, Vector pInit){
		DudCoreOptimizer d1 = new DudCoreOptimizer(f1);
        d1.initialize(p1);
		d1.absTol = 0.001;
		d1.relTol = 0.001;
		
		//public void optimize(int maxIter, double tol){
		d1.optimize();
		//public Vector getOptimalValue(){
		IVector p1b = d1.getOptimalValue();
		System.out.println("d1.getOptimalValue() = "+p1b);
		System.out.println("Should be d1.getOptimalValue() = [0.5,0.5000000000000002]");
		assertEquals("s2.getOptimalValue()",""+p1b.toString(), "[0.5,0.5000000000000002]");
		double f1b = d1.getOptimalCost();
		System.out.println("d1.getOptimalCost() = "+f1b);
		System.out.println("Should be s2.getOpimalCosts()[1] = 0.625");
		assertEquals("s2.getOptimalCost()",""+f1b, "0.625");
		IVector[] p1c = d1.getCurrentValues();
		double[] f1c = d1.getCurrentCosts();
		for(int i=0;i<p1c.length;i++){
			System.out.println("par["+i+"]="+p1c[i]);
			System.out.println("cost["+i+"]="+f1c[i]);
		}
		
	}

	public static void testSimplexWithConstraint() {
		System.out.println("========================================================");
		System.out.println("Simplex minimization of kwadratic least-squares cost ");
		System.out.println("with constraint");
		System.out.println("========================================================");
		// Simplex test on simple kwadratic cost
		LeastSquaresCostFunction f1 = new SimpleLeastSquaresCostFunction();
		f1.setBackgroundTerm(true);
		IVector p1 = new Vector("[0.0,0.0]");
		//public DudCoreOptimizer(SimpleLeastSquaresCostFunction f, Vector pInit){
		SimplexCoreOptimizer s1 = new SimplexCoreOptimizer(f1);
        s1.initialize(p1,2.0);
		s1.absTolSimplex = 0.0001;
		s1.relTolSimplex = 0.0001;
		
		//public void optimize(int maxIter, double tol){
		s1.optimize();
		//public Vector getOptimalValue(){
		IVector p1b = s1.getOptimalValue();
		System.out.println("d1.getOptimalValue() = "+p1b);
		System.out.println("Should be d1.getOptimalValue() = [0.5033355318009853,0.4968565031886101]");
		assertEquals("s2.getOptimalValue()",""+p1b.toString(), "[0.5033355318009853,0.4968565031886101]");
		double f1b = s1.getOptimalCost();
		System.out.println("d1.getOptimalCost() = "+f1b);
		System.out.println("Should be s2.getOpimalCosts()[1] = 0.6250271923308923");
		assertEquals("s2.getOptimalCost()",""+f1b, "0.6250271923308923");
		IVector[] p1c = s1.getCurrentValues();
		double[] f1c = s1.getCurrentCosts();
		for(int i=0;i<p1c.length;i++){
			System.out.println("par["+i+"]="+p1c[i]);
			System.out.println("cost["+i+"]="+f1c[i]);
		}
	}

	public static void testGriddedFullSearch() {
		System.out.println("========================================================");
		System.out.println("gridded full search minimization of kwadratic least-squares cost ");
		System.out.println("without constraint");
		System.out.println("========================================================");
		// Simplex test on simple kwadratic cost
		LeastSquaresCostFunction f1 = new SimpleLeastSquaresCostFunction();
		f1.setBackgroundTerm(false);
        IVector pStart = new Vector("[0.0,0.0]");
		IVector pStop  = new Vector("[2.0,2.0]");
		IVector pStep  = new Vector("[0.2,0.2]");
		GriddedFullSearchCoreOptimizer s1 = new GriddedFullSearchCoreOptimizer(f1,pStart,pStop,pStep);
		
		s1.optimize();
		//public Vector getOptimalValue(){
		IVector p1b = s1.getOptimalValue();
		System.out.println("s1.getOptimalValue() = "+p1b);
		System.out.println("Should be s1.getOptimalValue() = [1.0,1.0]");
		assertEquals("s2.getOptimalValue()",""+p1b.toString(), "[1.0,1.0]");
		//public double getOptimalCost(){
		double f1b = s1.getOptimalCost();
		System.out.println("s1.getOptimalCost() = "+f1b);
		System.out.println("Should be s2.getOpimalCosts()[1] = 0.0");
		assertEquals("s2.getOptimalCost()",f1b,0.0,0.01);
	}


	//TODO restart on stored iteration
	//public void setCurrentValues(Vector[] pars){
	//public void setCurrentValues(Vector pars[], double values[]){

}
