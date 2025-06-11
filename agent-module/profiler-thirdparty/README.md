## Profiler Third Party
It is a module for external libraries used in Pinpoint agents.

### Replace java.util.logging
Google guice/guava is using java.util.logging, pinpoint agent logging (log4j) is not compatible.

Use maven shade plugin to replace the java.util.logging package inside guava/guice with profiler.logging from pinpoint agent.

### Build
Make sure guava/guice is not included in the lib folder during the pinpoint agent build process.
The dependent library of guava/guice should be included.
