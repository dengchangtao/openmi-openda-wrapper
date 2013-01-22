
Introduction

This model implement a simple oscillator model. Because of its small size
(2 state variables) and linear behaviour, this model is ideal as a first
test for assimilation methods. Al should converge quickly to the right 
values.

simple linear oscilator (e.g. mass-spring system with friction)
 d(x)/d(t) = u
 d(u)/d(t) = - omega^2 * x - (2/t_damp) u

Calibration experiment

In this 'simultaneous calibration' experiment. There are two versions of the 
oscillator model. Each has its own set of observations. Other than this 
difference the models are identical.

Observarions are generated with the 'true' values for the parameters, i.e.
model1 t_damp=9.0, omega=1.7
model2 t_damp=8.5, omega=1.8 
There is no noise added to initial condition,
system forcing or observations. All calibration experiments start with 
t_damp=8.0 and omega=1.5708. The following values are found with the
present settings:

experiment			t_damp		omega	#evaluations	cost
--------------------------------------------------------------------------------
true1				9.0		1.7
true2				8.5		1.8
initial				8.0		1.5708
--------------------------------------------------------------------------------
dud only model 1 		9.004		1.700	 6		3.81E-6
dud only model 2 		8.482		1.800	 10		8.07E-5
--------------------------------------------------------------------------------
dud no constraint		8.174		1.750	 8		5.887
simplex no constraint		8.305		1.747	34		5.914
powell no constraint		8.155		1.750	55		5.887
full gridded search             8.250           1.750 1200              5.889
--------------------------------------------------------------------------------
dud constraint			8.106		1.747	 7		6.899
simplex constraint		8.109		1.750	37		6.911
powell constraint		8.095		1.748	52		9.899

Kalman filtering experiment

not working yet.
