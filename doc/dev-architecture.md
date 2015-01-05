# Architecture

Pinpoint is comprised of 3 main components (Agent, Collector, Web UI), and a HBase storage.

![Pinpoint Architecture](img/pinpoint-architecture.png)

## Components

### Pinpoint Agent
Pinpoint Agent attaches itself to a host application (such as Tomcat) as a java agent to instrument various classes for tracing. When a class marked for tracing is loaded into the JVM, the Agent injects code around pre-defined methods to collect and send trace data to the Collector.

In addition to trace data, the agent also collects various information about the host application such as JVM arguments, loaded libraries, CPU usage, Memory Usage and Garbage Collection.

For a more detailed information, please take a look [here](dev-profiler.md).

### Pinpoint Collector
The Collector listens for data sent by the Agents and writes them into the HBase storage.

Click [here](dev-collector.md) for more information.

### Pinpoint Web
The Web provides users with various information collected by the Agents. These include an automatically generated server map, call stacks on distributed transactions, and various information on the host applications.

Click [here](dev-web.md) for more information.
