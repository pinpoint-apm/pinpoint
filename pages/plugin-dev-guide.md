---
title: Plugin Developer Guide
tags:
keywords: plugin, plug-in, plug
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: plugindevguide.html
disqus: true
---

You can write Pinpoint profiler plugins to extend profiling target coverage. It is highly advisable to look into the trace data recorded by pinpoint plugins before jumping into plugin development.

 * There is a [fast auto pinpoint agent plugin generate tool](https://github.com/bbossgroups/pinpoint-plugin-generate) from a 3rd party for creating a simple plug-in, if you'd like to check out.

## I. Trace Data
In Pinpoint, a transaction consists of a group of `Spans`. Each `Span` represents a trace of a single logical node where the transaction had gone through. 

To aid in visualization, let's suppose that there is a system like below. The *FrontEnd* server receives requests from users, then sends request to the *BackEnd* server, which queries a DB. Among these nodes, let's assume only the *FrontEnd* and *BackEnd* servers are profiled by the Pinpoint Agent.

![trace](https://cloud.githubusercontent.com/assets/8037461/13870778/0073df06-ed22-11e5-97a3-ebe116186947.jpg)


When a request arrives at the *FrontEnd* server, Pinpoint Agent generates a new transaction id and creates a `Span` with it. To handle the request, the *FrontEnd* server then invokes the *BackEnd* server. At this point, Pinpoint Agent injects the transaction id (plus a few other values for propagation) into the invocation message. When the *BackEnd* server receives this message, it extracts the transaction id (and the other values) from the message and creates a new `Span` with them. Resulting, all `Spans` in a single transaction share the same transaction id.

A `Span` records important method invocations and their related data(arguments, return value, etc) before encapsulating them as `SpanEvents` in a call stack like representation. The `Span` itself and each of its `SpanEvents` represents a method invocation.

`Span` and `SpanEvent` have many fields, but most of them are handled internally by Pinpoint Agent and most plugin developers doesn't need to concern about them. But For the fields, and information that must be handled by the plugin developers is as follow.


## II. Pinpoint Plugin Structure
Pinpoint plugin consists of `TraceMetadataProvider` and `ProfilerPlugin` implementations. `TraceMetadataProvider` implementation provides `ServiceType` and `AnnotationKey` to Pinpoint Agent, Web and Collector. `ProfilerPlugin` implementations are used by Pinpoint Agent to transform target classes to record trace data.

Plugins are deployed as jar files. Pinpoint Agent searches `TraceMetadataProvider` and `ProfilerPlugin` implementations using `ServiceLoader` from the `plugin` directory, while Web and Collector look into `WEB-INF/lib` directory. `ServiceLoader` requires provider configuration file located in `META-INF/services`, so you must put the following files in the plugin jar file.

* META-INF/services/com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin
* META-INF/services/com.navercorp.pinpoint.common.trace.TraceMetadataProvider

Here is a [template plugin project](https://github.com/naver/pinpoint-plugin-template). You can start creating your own plugin withgae0 this template.


### 1. TraceMetadataProvider
`TraceMetadataProvider` implementations provide `ServiceTypes` and `AnnotationKeys`.

#### 1.1 ServiceType

Every `Span` and `SpanEvent` contains a `ServiceType`. The `ServiceType` represents which library the traced method belongs to, as well as how the `Span` and `SpanEvent` should be handled.

Below table shows `ServiceType`'s properties.

property | description
--- | ---
name | name of the `ServiceType`. Must be unique
code | short type code value of the `ServiceType`. Must be unique
desc | description
properties | properties 

The `ServiceType` code must use a value from its appropriate category. Below table shows the categories and the ranges.

category | range
--- | ---
Internal Use | 0 ~ 999 
Server | 1000 ~ 1999 
DB Client | 2000 ~ 2999 
Cache Client | 8000 ~ 8999 
RPC Client | 9000 ~ 9999 
Others | 5000 ~ 7999 


`ServiceType` code must be unique. Therefore, if you are writing a plugin that will be shared publicly, **you must** contact the Pinpoint dev. team to get a `ServiceType` code assigned. If your plugin is for private use, you may freely pick a value for `ServiceType` code from the table below.

category | range
--- | ---
Server | 1900 ~ 1999 
DB client | 2900 ~ 2999 
Cache client | 8900 ~ 8999 
RPC client | 9900 ~ 9999 
Others | 7500 ~ 7999 


`ServiceTypes` can have the following properties.

property | description
--- | ---
TERMINAL | This `Span` or `SpanEvent` invokes a remote node but the target node is not traceable with Pinpoint
INCLUDE_DESTINATION_ID | This `Span` or `SpanEvent` records a `destination id` and remote server is not a traceable type.
RECORD_STATISTICS | Pinpoint Collector should collect execution time statistics of this `Span` or `SpanEvent`


#### 1.2 AnnotationKey
You can annotate Spans or SpanEvents with more information. An `Annotation` is a key-value pair where the key is an `AnnotationKey` type and the value is a primitive type, String or a byte[]. There are pre-defined `AnnotationKeys` for commonly used annotation types, but you can define your own keys in `TraceMetadataProvider` if these are not enough.


property | description
--- | ---
name | Name of the `AnnotationKey`
code | int type code value of the `AnnotationKey`. Must be unique. 
properties | properties

If your public plugin is going to add a new `AnnotationKey`, you must contact the Pinpoint dev. team to get an `AnnotationKey` code assigned. If your plugin is for private use, you may pick a value between 900 to 999 safely to use as `AnnotationKey` code.

The table below shows the `AnnotationKey` properties.

property | description
--- | ---
VIEW_IN_RECORD_SET | Show this annotation in transaction call tree.
ERROR_API_METADATA | This property is not for plugins.


#### Example
You can find `TraceMetadataProvider` sample [here](https://github.com/naver/pinpoint-plugin-sample/blob/master/plugin/src/main/java/com/navercorp/pinpoint/plugin/sample/SampleTraceMetadataProvider.java).

You may also pass `AnnotationKeyMatcher` with the `ServiceType` (`TraceMetadata.addServiceType(ServiceType, AnnotationKeyMatcher`) in the sample code). If you pass an `AnnotationKeyMatcher` this way, matching annotations will be displayed as representative annotation when the `ServiceType`'s `Span` or `SpanEvent` is displayed in the transaction call tree.



### 2. ProfilerPlugin
`ProfilerPlugin` modifies target library classes to collect trace data.

`ProfilerPlugin` works in the order of following steps:

1. Pinpoint Agent is started when the JVM starts.
2. Pinpoint Agent loads all plugins under plugin directory.
3. Pinpoint Agent invokes `ProfilerPlugin.setup(ProfilerPluginSetupContext)` for each loaded plugin.
4. In the `setup` method, the plugin defines classes that should be transformed and registers a `TransformerCallback`.
5. Target application start.
6. Every time a class is loaded, Pinpoint Agent looks up for the `TransformerCallback` registered for the class.
7. If a `TransformerCallback` is registered, the Agent invokes its `doInTransform` method.
8. `TransformerCallback` modifies the target class' byte code. (e.g. add interceptors, add fields, etc.)
9. The modified byte code is returned to the JVM, and the class is loaded with the returned byte code.
10. Application continues running.
11. When a modified method is invoked, the injected interceptor's `before` and `after` methods are invoked.
12. The interceptor records the trace data.

The most important points of consideration boils down to i) figuring out which methods are interesting enough to warrant tracing. ii) injecting interceptors to actually trace these methods. 
These interceptors are used to extract, store and pass trace data around before they are sent off to the Collector. Interceptors may even cooperate with each other, sharing context between them. Plugins may also aid in tracing by adding getters or even custom fields to the target class so that the interceptors may access them during execution. [Pinpoint plugin sample](https://github.com/naver/pinpoint-plugin-sample) shows you how the `TransformerCallback` modifies classes and what the injected interceptors do to trace methods.

We will now describe what interceptors must do to trace different kinds of methods.

#### 2.1 Plain method
*Plain method* refers to anything that is not a top-level method of a node, or is not related to remote or asynchronous invocation. [Sample 2](https://github.com/naver/pinpoint-plugin-sample/tree/master/plugin/src/main/java/com/navercorp/pinpoint/plugin/sample/_02_Injecting_Custom_Interceptor) shows you how to trace these plain methods.

#### 2.2 Top level method of a node
*Top level method of a node* is a method in which its interceptor begins a new trace in a node. These methods are typically acceptors for RPCs, and the trace is recorded as a `Span` with `ServiceType` categorized as a server.

How the `Span` is recorded depends on whether the transaction has already begun at any previous nodes.

##### 2.2.1 New transaction
If the current node is the first one that is recording the transaction, you must issue a new transaction id and record it. `TraceContext.newTraceObject()` will handle this task automatically, so you will simply need to invoke it.

##### 2.2.2 Continue Transaction
If the request came from another node traced by a Pinpoint Agent, then the transaction will already have a transaction id issued; and you will have to record the data below to the `Span`. (Most of these data are sent from the previous node, usually packed in the request message)

name | description
--- | ---
transactionId | Transaction ID
parentSpanId | Span ID of the previous node
parentApplicationName | Application name of the previous node
parentApplicationType | Application type of the previous node
rpc | Procedure name (Optional)
endPoint | Server(current node) address
remoteAddr | Client address
acceptorHost | Server address that the client used

Pinpoint finds caller-callee relation between nodes using *acceptorHost*. In most cases, *acceptorHost* is identical to *endPoint*. However, the address which client sent the request to, may sometimes be different from the address the server received the request (proxy). To handle such cases, you have to record the actual address the client used to send the request to as *acceptorHost*. Normally, the client plugin will have added this address into the request message along with the transaction data.

Moreover, you must also use the span id issued and sent by the previous node.

Sometimes, the previous node marks the transaction not to be traced. In this case, you must not trace the transaction.

As you can see, the client plugin have to pass many data to the server plugin. How to do it is protocol dependent.

You can find an example of top-level method server interceptor [here](https://github.com/naver/pinpoint-plugin-sample/tree/master/plugin/src/main/java/com/navercorp/pinpoint/plugin/sample/_14_RPC_Server).

#### 2.3 Methods invoking a remote node

An interceptor of a method that invokes a remote node has to record the following data:

name | description
--- | ---
endPoint | Target server address
destinationId | Logical name of the target
rpc | Invoking target procedure name (optional)
nextSpanId | Span id that will be used by next node's span (If next node is traceable by Pinpoint)


Whether or not the next node is traceable by Pinpoint affects how the interceptor is implemented. The term "traceable" here is about possibility. For example, a HTTP client's next node is a HTTP server. Pinpoint does not trace all HTTP servers, but it is possible to trace them (and there already are HTTP server plugins). In this case, the HTTP client's next node is traceable. On the other hand, MySQL JDBC's next node, a MySQL database server, is not traceable.

##### 2.3.1 If the next node is traceable
If the next node is traceable, the interceptor must propagate the following data to the next node. How to pass them is protocol dependent, and in worst cases may be impossible to pass them at all.

name | description
--- | ---
transactionId | Transaction ID
parentApplicationName | Application name of current node
parentApplicationType | Application type of current node
parentSpanId | Span id of trace at current node
nextSpanId | Span id that will be used by the next node's span (same value with nextSpanId of above table)

Pinpoint finds out caller-callee relation by matching *destinationId* of client trace and *acceptorHost* of server trace. Therefore the client plugin has to record *destinationId* and the server plugin has to record *acceptorHost* with the same value. If server cannot acquire the value by itself, client plugin has to pass it to server.

The interceptor's recorded `ServiceType` must be from the RPC client category.

You can find an example for these interceptors [here](https://github.com/naver/pinpoint-plugin-sample/tree/master/plugin/src/main/java/com/navercorp/pinpoint/plugin/sample/_13_RPC_Client).

##### 2.3.2 If the next node is not traceable
If the next node is not traceable, your `ServiceType` must have the `TERMINAL` property. 

If you want to record the *destinationId*, it must also have the `INCLUDE_DESTINATION_ID` property. If you record *destinationId*, server map will show a node per destinationId even if they have same *endPoint*.

Also, the `ServiceType` must be a DB client or Cache client category. Note that you do not need to concern yourself about the terms "DB" or "Cache", as any plugin tracing a client library with non-traceable target server may use them. The only difference between "DB" and "Cache" is the time range of the response time histogram ("Cache" having smaller intervals for the histogram).


#### 2.4 Asynchronous task

Trace objects are bound to the thread that first created them via **ThreadLocal** and whenever the execution crosses a thread boundary, trace objects are *lost* to the new thread. Therefore, in order to trace tasks across thread boundaries, you must take care of passing the current trace context over to the new thread. This is done by injecting an **AsyncContext** into an object shared by both the invocation thread and the execution thread.  
The invocation thread creates an **AsyncContext** from the current trace, and injects it into an object that will be passed over to the execution thread. The execution thread then retrieves the **AsyncContext** from the object, creates a new trace out of it and binds it to it's own **ThreadLocal**.  
You must therefore create interceptors for two methods : i) one that initiates the task (invocation thread), and ii) the other that actually handles the task (execution thread). 

The initiating method's interceptor has to issue an **AsyncContext** and pass it to the handling method. How to pass this value depends on the target library. In worst cases, you may not be able to pass it at all.

The handling method's interceptor must then continue the trace using the propagated **AsyncContext** and bind it to it's own thread. However, it is very strongly recommended that you simply extend the **AsyncContextSpanEventSimpleAroundInterceptor** so that you do not have to handle this manually.

Keep in mind that since the shared object must be able have **AsyncContext** injected into it, you have to add a field using `AsyncContextAccessor` during it's class transformation.
You can find an example for tracing asynchronous tasks [here](https://github.com/naver/pinpoint-plugin-sample/tree/master/plugin/src/main/java/com/navercorp/pinpoint/plugin/sample/_12_Asynchronous_Trace).

#### 2.5 Case Study: HTTP
HTTP client is an example of _a method invoking a remote node_ (client), and HTTP server is an example of a _top level method of a node_ (server). As mentioned before, client plugins must have a way to pass transaction data to server plugins to continue the trace. Note that the implementation is protocol dependent, and [HttpMethodBaseExecuteMethodInterceptor](https://github.com/naver/pinpoint/blob/master/plugins/httpclient3/src/main/java/com/navercorp/pinpoint/plugin/httpclient3/interceptor/HttpMethodBaseExecuteMethodInterceptor.java) of [HttpClient3 plugin](https://github.com/naver/pinpoint/tree/master/plugins/httpclient3) and [StandardHostValveInvokeInterceptor](https://github.com/naver/pinpoint/blob/master/plugins/tomcat/src/main/java/com/navercorp/pinpoint/plugin/tomcat/interceptor/StandardHostValveInvokeInterceptor.java) of [Tomcat plugin](https://github.com/naver/pinpoint/tree/master/plugins/tomcat) show a working example of this for HTTP:

1. Pass transaction data as HTTP headers. You can find header names [here](https://github.com/naver/pinpoint/blob/master/bootstrap-core/src/main/java/com/navercorp/pinpoint/bootstrap/context/Header.java)
2. Client plugin records `IP:PORT` of the server as `destinationId`.
3. Client plugin passes `destinationId` value to server as `Header.HTTP_HOST` header.
4. Server plugin records `Header.HTTP_HOST` header value as `acceptorHost`.

One more thing you have to remember is that all the clients and servers using the same protocol must pass the transaction data in the same way to ensure compatibility. So if you are writing a plugin of some other HTTP client or server, your plugin has to record and pass transaction data as described above.

### 3. Plugin Integration Test
You can run plugin integration tests (`mvn integration-test`) with [PinointPluginTestSuite](https://github.com/naver/pinpoint/blob/master/test/src/main/java/com/navercorp/pinpoint/test/plugin/PinpointPluginTestSuite.java), which is a *JUnit Runner*. It downloads all the required dependencies from maven repositories and launches a new JVM with the Pinpoint Agent and the aforementioned dependencies. The JUnit tests are executed in this JVM.

To run the plugin integration test, it needs a complete agent distribution - which is why integration tests are in the *plugin-sample-agent* module and why they are run in **integration-test phase**.

For the actual integration test, you will want to first invoke the method you are tracing, and then use [PluginTestVerifier](https://github.com/naver/pinpoint/blob/master/bootstrap-core/src/main/java/com/navercorp/pinpoint/bootstrap/plugin/test/PluginTestVerifier.java) to check if the trace data is correctly recorded.


#### 3.1 Test Dependency
`PinointPluginTestSuite` doesn't use the project's dependencies (configured in pom.xml). It uses the dependencies that are listed by `@Dependency` annotation. This way, you may test multiple versions of the target library using the same test class.

Dependencies are declared as following. You may specify versions or version ranges for a dependency library.
```
@Dependency({"some.group:some-artifact:1.0", "another.group:another-artifact:2.1-RELEASE"})
@Dependency({"some.group:some-artifact:[1.0,)"})
@Dependency({"some.group:some-artifact:[1.0,1.9]"})
@Dependency({"some.group:some-artifact:[1.0],[2.1],[3.2])"})
```
`PinointPluginTestSuite` by default searches the local repository and maven central repository. You may also add your own repositories by using the `@Repository` annotation.

#### 3.2 Jvm Version
You can specify the JVM version for a test using `@JvmVersion`. If `@JvmVersion` is not present, JVM at `java.home property` will be used.

#### 3.3 Application Test
`PinpointPluginTestSuite` is not for applications that has to be launched by its own main class. You can extend [AbstractPinpointPluginTestSuite](https://github.com/naver/pinpoint/blob/master/test/src/main/java/com/navercorp/pinpoint/test/plugin/AbstractPinpointPluginTestSuite.java) and related types to test such applications.


### 4. Adding Images

If you're developing a plugin for applications, you need to add images so the server map can render the corresponding node. The plugin jar itself cannot provide these image files and for now, you will have to add the image files to the web module manually.

First, put the PNG files to following directories:

* web/src/main/webapp/images/icons (25x25)
* web/src/main/webapp/images/servermap (80x40)

Then, add `ServiceType` name and the image file name to `htIcons` in *web/src/main/webapp/components/server-map2/jquery.ServerMap2.js*.
