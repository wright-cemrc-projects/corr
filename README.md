# CorRelator
CorRelator provides easy TEM and FLM correlation for On-The-Fly processing.

## About CorRelator
CorRelator supports both on-the-fly and post-acquisition two dimensional (2D) cryo-correlation. The on-the-fly correlation integrates image registration, transformation and correlation between EM and FM.

The output is ready for SerialEM automated data collection at transformed fluorescent targets of interest (TOIs) on registered EM images. To get started and gain supports, visit Wiki for more details.

![Map View](images/mapview.png)

## Updates

- Version 1.40 Release (9/17/2023) :
 - Updates of all dependency libraries (Apache Commons Math, Java XML, and OpenJFX).
 - Conversion to the Maven build system.
 - Target Java version is now OpenJDK 19.

- Version 1.30 Release (6/30/2021) :
 - Added a bounding box of the viewport within the Image and Free Align views.
 - Improvements to histogram behaviors in hole-finding view.
 - Switch to a tile-based representation of image data for all views.

- Version 1.25 Release (3/31/2021) : 
 - Added Circle Hough Transform, hole centroid finding Analysis GUI.
 - Added Free Alignment GUI to dynamically define registrations between images.
 - Improvements to label size in images.

## Documentation

### Tutorials

1. [Tutorial 01: On-the-fly Rough Correlation with Navigator files](documentation/Tutorial_01_OnTheFly_GridSquare.md)
2. [Tutorial 02: On-the-fly Rough Correlation with raw images](documentation/Tutorial_02_OneTheFly_GridSquare2.md)	
3. [Tutorial 03: Fine Correlation with Navigator files](documentation/Tutorial_03_OnTheFly_FineAlignment.md) 

## CorRelator Features 

1. Import and Export of .nav Autodoc format provided by SerialEM.
2. Import and Perform correlation of MRC/PNG/TIF(8-bit greyscale and 32/8-bit RBG)/JPG image maps.
3. Import of user-provided pixel positions (.csv).
4. Manual annotation of pixel positions on maps.
5. Affine alignment of pixel positions to align cryo-EM and (cryo)FLM images.
6. Image overlay and image export.
7. Identify hole center coordinates algorithmically. 

## Installation and Requirements (1.40.0 and newer)
CorRelator is a cross-platform Java application which is built on JavaFX for the OpenJDK platform.

Install a version of OpenJDK 19 available from the [Adoptium Temurin](https://adoptium.net/temurin/releases/?version=19) that matches your system Operating System. After you have installed the OpenJDK Java, you can verify that this is the current working version by typing `java -version` on a command-line terminal. 

```
% java -version
openjdk version "19.0.2" 2023-01-17
OpenJDK Runtime Environment (build 19.0.2+7-44)
OpenJDK 64-Bit Server VM (build 19.0.2+7-44, mixed mode, sharing)
```

You can then run CorRelator by typing `java -jar Correlator-1.4.0-OpenJDK.jar`.

### Older versions (1.30.0 and earlier)
Older versions require an installation of the [Java SE Runtime Environment 8](https://www.oracle.com/java/technologies/javase-jre8-downloads.html), to run the application. Note, licensing changes by Oracle may now require purchasing a license for Oracle Java to be able to download and use.

The application is distributed as a single Java ARchive (JAR) file which can be launched from the command line with `java -jar CorRelator.jar` or on Mac/Windows by double-clicking on the Correlator.jar from programs like File Explorer or Finder.

Note: OpenJDK does not contain the Java standard library JavaFX and cannot be used for running Correlator. The official Oracle JRE is required for JavaFX.

## References

Jie E. Yang, Matthew R. Larson, Bryan S. Sibert, Samantha Shrum, Elizabeth R. Wright,
**CorRelator: Interactive software for real-time high precision cryo-correlative light and electron microscopy**,
*Journal of Structural Biology*,
2021,
107709,
ISSN 1047-8477,
https://doi.org/10.1016/j.jsb.2021.107709.

[Link to preprint](https://www.sciencedirect.com/science/article/pii/S1047847721000149)

## Eclipse and OpenJDK, JavaFX notes.

CorRelator should be built in an Eclipse IDE with Eciplse-RCP support.

JavaFX20 is now the available library version as of March 2023.  An OS-specific version of this library should be downloaded and will need to be setup in the 
IDE with these steps:

1. Download JavaFX20 from [Gluon](https://gluonhq.com/products/javafx/).
2. Create a user library in Eclipse that points to the local copy of the JavaFX  libraries.
3. Include that user library "JavaFX20" in the CorRelatorGUI classpath.
4. Create a Java Application "Run Configuration" for Correlator, org.cemrc.correlator.Correlator as the Main class. There are both required Program arguments and required VM arguments.
 
  - Program `--module-path ${project_classpath:CorrelatorGUI} --add-modules=javafx.controls,javafx.fxml`
  - VM `--module-path ${project_classpath:CorrelatorGUI} --add-modules=javafx.controls,javafx.fxml`

 Also uncheck the "Run Configuration" VM arguments of '-XstartOnFirstThread' when launching with SWT.  Uncheck the '-XX:+ShowCodeDetailsInExceptionMessages' argument when launching.

 After making these changes, it should now be possible to launch CorRelator as a Java Application with JavaFX GUI, from the Eclipse IDE and with OpenJDK as the runtime Java library.

## Build changes

CorRelator with Oracle JDK1.8 was previously built with a two-step Ant build system that built a base library of `CorRelator` and a separate GUI project of `CorRelatorGUI`. 

CorRelator has now been upated to build with Maven, targeting OpenJDK 19 and providing all dependencies via the Maven build system.

### Developer Guide for building CorRelator

Running from the commandline with Maven:

1. `mvn clean javafx:run`

Building a distribution release with Maven:

2. `mvn clean package shade:shade`
