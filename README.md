ParallaxBlur
============

Motion blur accounting for parallax motion.

Installation Requirements
-------------------------

Project is set up as an eclipse project, and should be able to be easily imported.

[Rootbeer](https://github.com/pcpratts/rootbeer1) is required for proper functioning.
To install rootbeer:

1.  Download the source code from the link above
2.  Navigate to the download location and run 'ant' to compile project
3.  Run pack-rootbeer in order to generate the JAR for your system
4.  Copy the generated Rootbeer.jar into the lib directory

Execution Instructions
----------------------

The program is to be run from command line.  The arguments are as follows:

    -a <arg>   Angle, in degrees
    -d <arg>   Depthmap image
    -g         Process images on GPU
    -help      Print this message
    -i <arg>   Input image
    -l <arg>   Blur length
    -m         Use linear motion blue only (no parallax)