# CorRelator
CorRelator provides easy TEM and FLM correlation for On-The-Fly processing.

## About CorRelator
CorRelator supports both on-the-fly and post-acquisition two dimensional (2D) cryo-correlation. The on-the-fly correlation integrates image registration, transformation and correlation between EM and FM.

The output is ready for SerialEM automated data collection at transformed fluorescent targets of interest (TOIs) on registered EM images. To get started and gain supports, visit Wiki for more details.

![Map View](images/mapview.png)

## Updates

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

## Installation and Requirements
CorRelator is a cross-platform Java application which is built on JavaFX.

It requires an installation of the [Java SE Runtime Environment 8](https://www.oracle.com/java/technologies/javase-jre8-downloads.html), to run the application.

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



