## What is Habanero Java? ##

Habanero is a high-level parallel extension of the Java language, written as a library that can be included in any Java project using version 1.7 or above. Habanero-Java was developed at Rice University. It includes several lightweight constructs useful for parallel programming including async, finish, future, forall, forasync, phasers, and isolated (to name just a few). For more information about Habanero-Java or to download the library, visit [Rice's Habanero-Java Home Page](https://wiki.rice.edu/confluence/display/HABANERO/Habanero-Java).

## What is JPF-HJ? ##

Though programs written for the Habanero library contain numerous safety guarantees, they are still subject to data races under certain conditions. JPF-HJ was written as a systemic way to detect these data races using the tools presented by JPF such as lightweight thread creation. JPF-HJ includes a greatly simplified implementation of the HJ library compatible with any program written for the standard implementation of HJ.

For more information about the implementation of JPF-HJ, see the [publications section](./publications.html).