# Synergy Between Touch and Gesture as Input for Head Worn Displays
This thesis is an attempt to develop novel interaction techniques for smart see through head worn displays (ST-HWD) using a smartwatch as input controller. To realize this, 2 applications were developed. One application runs on the See through display and the other on the smartwatch.

## Designing Interaction
<img src = "https://github.com/ksughosh/Thesis/blob/master/prototype.png" alt = "Interaction Working">

The interaction techniques that were designed and evaluated were (1) Purely touch interaction, (2) Purely gesture interaction (3) Combination of touch and gesture in a unique format.

### Basic Software Components of ST-HWD
The application components are divided into several programmable components that use a custom developed library to communicate between devices. The most important applications and sub components here are:

1. __FittsExperiment__: that runs on the STHWD
  * UDPServer: That keeps listening to data from the smartwatch and converts data into injected input.
  * FittsLayout: Designed for steroscopic viewing on ST-HWD, is a layout used for testing pointing accuracy and speed of pointing. It is an ISO standard evaluation for pointing.

2. __WatchController__: that is the driver for smartwatch
  * SensorFusion: that is used for gesture recognition.
  * TouchController: that is used for touch recognition.

