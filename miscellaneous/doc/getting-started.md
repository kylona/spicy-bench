## Using BYU-HJ Standalone ##

Using the simplified BYU version of hj-lib is as easy as downloading it from the [download page](./downloads.html) and adding it to the classpath of any Habanero program. BYU-Habanero should serve in lieu of HJ-lib without the need to make changes to existing code.

## Installing JPF ##

The JPF-HJ data race verifier requires JPF to be installed. In order to use JPF, you need to install a current version of the Java Development Kit (JDK). You can find an installer on [Oracle’s website](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

Once the JDK is installed, we can install JPF (skip this step if JPF is already installed). Only the "core" module is required to use our verification library. To install JPF on your machine: 

*   With the JDK in place, installing the JPF VM is as simple as downloading the [binary package](http://jpf.byu.edu/files/jpf-core/downloads/jpf-core-binary-v1202.zip) and       
    extracting the contents into a folder of your choice. 

More details can be found on the [BYU JPF Homepage](http://jpf.byu.edu). Troubleshooting information can also be found in these locations.

## Installing the Verifier ##
 
*	Once JPF is installed, download the current build of the BYU-HJ library, the JPF-Extension, and the config.jpf file from the [downloads page](./downloads.html).
*   Customize the config.jpf file for your application
    *   Edit "target" to list the fully qualified name of your main class (e.g.
        package.subpackage.classname)
    *   Edit "classpath" to include any relevant classes for your application. It
        is required that "classpath" contain at least the location of the class
        file under test and all its dependencies, the location of
        hj-lib-byu.jar, and jpf-hj.jar
    *   Edit "native_classpath" to point to location of jpf-hj.jar

## Running the Verifier ##

The quickest way to run the verifier is to do so from the command line. Using
the following command will execute JPF using config.jpf to customize its runtime
behavior.

*java -jar <path_to_JPF_HOME>/build/RunJPF.jar <path_to_config.jpf>/config.jpf*
