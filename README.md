![Pinpoint](web/src/main/webapp/images/logo.png)

**Pinpoint** is an APM (Application Performance Management) tool for large-scale distributed systems written in Java. Pinpoint provides a solution to help analyze the overall structure of the system and how components within them are interconnected by tracing transactions across distributed applications.

* Install agents without changing a single line of code
* Minimal impact on performance (approximately 3% increase in resource usage)

## Overview
Services nowadays often consist of many different components, communicating amongst themselves as well as making API calls to external services. How each and every transaction gets executed is often left as a blackbox. Pinpoint traces transaction flows between these components and provides a clear view to identify problem areas and potential bottlenecks.

* **ServerMap** - Understand the topology of any distributed systems by visualizing how their components are interconnected. Clicking on a node reveals details about the component, such as its current status, and transaction count.
* **Request/Response Scatter Chart** - Visualize request count and response patterns over time to identify potential problems. Transactions can be selected for additional detail by dragging over the chart.
 
  ![Server Map](doc/img/ss_server-map.png)

* **CallStack** - Gain code-level visibility to every transaction in a distributed environment, identifying bottlenecks and points of failure in a single view.

  ![Call Stack](doc/img/ss_call-stack.png)
  
* **Inspector** - View additional details on the application such as CPU usage, Memory/Garbage Collection, and JVM arguments.

  ![Inspector](doc/img/ss_inspector.png)
  
## Architecture
To be included.

## Quick Start
You may run a sample Pinpoint instance in your own machine by running four simple scripts for each components: Collector, Web, Sample TestApp, HBase.

Once the components are running, you should be able to visit http://localhost:28080 to view the Pinpoint Web UI, and http://localhost:28081 to generate transactions on the Sample TestApp.

For details, please refer to the [quick-start guide](quickstart/README.md "Pinpoint quick-start guide").

## Get Involved
To be included.

## Issues
To be included.

## License
Pinpoint is licensed under the Apache License, Version 2.0.

See [LICENSE](LICENSE) for full license text.
