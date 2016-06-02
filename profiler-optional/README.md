# pinpoint-profiler-optional

Modules under **pinpoint-profiler-optional** contain optional packages for pinpoint-profiler, containing features and codes that must be compiled against specific versions of JDK.

Additionally, these optional modules may contain vendor-specific stub classes compile against.
These classes are not included in in the final jar packaging, and the vendor-specific implementations that are compiled against these stubs must be loaded with vendor-supplied implementations.

## Requirements
In order to build pinpoint-profiler-optional and it's child modules, the following requirements must be met:

* JDK 7 installed
* `JAVA_7_HOME` environment variable set to JDK 7 home directory
* JDK 8 installed
* `JAVA_8_HOME` environment variable set to JDK 8 home directory
