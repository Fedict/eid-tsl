README for FedICT eID TSL Project
=================================

=== 1. Introduction

This project contains the source code tree of the FedICT eID TSL project.
The source code is hosted at: http://code.google.com/p/eid-tsl/


=== 2. Requirements

The following is required for compiling the eID TSL software:
* Sun/Oracle Java 1.6.0_18+ !!! Required for RSA-SHA256 XML signatures !!!
* Apache Maven 3.0.3.


=== 3. Build

The project can be build via:
	mvn clean install


=== 4. Eclipse IDE

The Eclipse project files can be created via:
	mvn eclipse:eclipse

Afterwards simply import the projects in Eclipse via:
	File -> Import... -> General:Existing Projects into Workspace

First time you use an Eclipse workspace you might need to add the maven 
repository location. Do this via:
    mvn eclipse:add-maven-repo -Declipse.workspace=<location of your workspace>


=== 5. License

The license conditions can be found in the file: LICENSE.txt

