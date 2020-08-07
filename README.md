# CorRelator
CorRelator provides easy TEM and FLM correlation for On-The-Fly processing.


## About CorRelator
CorRelator supports both on-the-fly and post-acquisition two dimensional (2D) cryo-correlation. The on-the-fly correlation integrates image registration, transformation and correlation between EM and FM.

The output is ready for SerialEM automated data collection at transformed fluorescent targets of interest (TOIs) on registered EM images. 

![Map View](images/mapview.png)

## CorRelator Features

1. Import and Export of .nav Autodoc format provided by SerialEM.
2. Import and Viewing of PNG/TIF/JPG image maps.
3. Import of user-provided pixel positions (.csv).
4. Manual annotation of pixel positions on maps.
5. Affine alignment of pixel positions to align cryo-EM and (cryo)FLM images.
6. Image overlay and image export.

## Installation and Requirements
CorRelator is a cross-platform Java application which is built on JavaFX.

It requires an installation of the [Java SE Runtime Environment 8](https://www.oracle.com/java/technologies/javase-jre8-downloads.html), to run the application.

The application is distributed as a single Java ARchive (JAR) file which can be launched from the command line with `java -jar CorRelator.jar` or on Mac/Windows by double-clicking on the Correlator.jar from programs like File Explorer or Finder.

Note: OpenJDK does not contain the Java standard library JavaFX and cannot be used for running Correlator. The official Oracle JRE is required for JavaFX.

## References

CorRelator: An interactive and flexible toolkit for high-precision cryo-correlative light and electron microscopy.

Jie E. Yang1, Matthew R. Larson (1,2), Bryan S. Sibert (1,2), Samantha Shrum (3), and Elizabeth R. Wright (1,2,3,4) 

1. Department of Biochemistry, University of Wisconsin, Madison, WI 53706
2. Cryo-Electron Microscopy Research Center, Department of Biochemistry, University of Wisconsin, Madison, WI 53706
3. Biophysics Graduate Program, University of Wisconsin, Madison, WI 53706
4. Morgridge Institute for Research, Madison, WI, 53715


