---
title: Per Request Features
tags:
keywords: history
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: perrequestfeatureguide.html
disqus: true
---
# ENGLISH GUIDE

## Per-request logging

### 1. Description 
Pinpoint saves additional information(transactionId, spanId) in log messages to classify them by request.

When tomcat processes multiple requests concurrently, we can see log files printed in chronological order.
But we can not classify them by each request.
For example when an exception message is logged, we can not easily identify all the logs related to the request that threw the exception.

Pinpoint is able to classify logs by requests by storing additional information(transactionId, spanId) in MDC of each request.
The transactionId printed in the log message is the same as the transactionId in Pinpoint Web’s Transaction List view.
 
Let’s take a look at a more specific example.
The log below is from an exception that occurred without using Pinpoint. 
As you can see, it is hard to identify the logs related to the request that threw the exception.
ex) Without Pinpoint
```
2015-04-04 14:35:20 [INFO](ContentInfoCollector:76 ) get content name : TECH
2015-04-04 14:35:20 [INFO](ContentInfoCollector:123 ) get content name : OPINION
2015-04-04 14:35:20 [INFO](ContentInfoCollector:12) get content name : SPORTS
2015-04-04 14:35:20 [INFO](ContentInfoCollector:25 ) get content name : TECH
2015-04-04 14:35:20 [INFO](ContentInfoCollector:56 ) get content name : NATIONAL
2015-04-04 14:35:20 [INFO](ContentInfoCollector:34 ) get content name : OPINION
2015-04-04 14:35:20 [INFO](ContentInfoService:55 ) check authorization of user
2015-04-04 14:35:20 [INFO](ContentInfoService:14 ) get title of content
2015-04-04 14:35:21 [INFO](ContentDAOImpl:14 ) execute query ...
2015-04-04 14:35:21 [INFO](ContentDAOImpl:114 ) execute query ...    
2015-04-04 14:35:20 [INFO](ContentInfoService:74 ) get top linking for content
2015-04-04 14:35:21 [INFO](ContentDAOImpl:14 ) execute query ...
2015-04-04 14:35:21 [INFO](ContentDAOImpl:114 ) execute query ...
2015-04-04 14:35:22 [INFO](ContentDAOImpl:186 ) execute query ...
2015-04-04 14:35:22 [ERROR]ContentDAOImpl:158 )
     com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure
          at example.ContentDAO.executequery(ContentDAOImpl.java:152)
          ...
          at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
          at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
          at com.mysql.jdbc.ConnectionImpl.<init>(ConnectionImpl.java:787)
          ...
     Caused by: com.mysql.jdbc.exceptions.jdbc4.CommunicationsException:
     Communications link failure The last packet sent successfully to the server was 0 milliseconds ago.
     The driver has not received any packets from the server.    
          at com.mysql.jdbc.ConnectionImpl.createNewIO(ConnectionImpl.java:2181)
          ... 12 more
     Caused by: java.net.ConnectException: Connection refused
          at java.net.PlainSocketImpl.socketConnect(Native Method)
          at java.net.PlainSocketImpl.doConnect(PlainSocketImpl.java:333)
          at java.net.PlainSocketImpl.connectToAddress(PlainSocketImpl.java:195)   
          at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:182)   
          at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:432)   
          at java.net.Socket.connect(Socket.java:529)    
          ...13 more
2015-04-04 14:35:22 [INFO](ContentDAO:145 ) execute query ...
2015-04-04 14:35:20 [INFO](ContentInfoService:38 ) update hits for content
2015-04-04 14:35:20 [INFO](ContentInfoService:89 ) check of user
2015-04-04 14:35:24 [INFO](ContentDAO:146 ) execute query ...
2015-04-04 14:35:25 [INFO](ContentDAO:123 ) execute query ...
```

Pinpoint classifies logs by requests by storing additional information(transactionId, spanId) in MDC of each request.
ex) With Pinpoint
```
2015-04-04 14:35:20 [INFO](ContentInfoCollector:76) [txId : agent^14252^17 spanId : 1224] get content name : TECH
2015-04-04 14:35:20 [INFO](ContentInfoCollector:123) [txId : agent^142533^18 spanId : 1231] get content name : OPINION
2015-04-04 14:35:20 [INFO](ContentInfoCollector:12) [txId : agent^142533^19 spanId : 1246] get content name : SPORTS
2015-04-04 14:35:20 [INFO](ContentInfoCollector:25) [txId : agent^142533^20 spanId : 1263] get content name : TECH
2015-04-04 14:35:20 [INFO](ContentInfoCollector:56) [txId : agent^142533^21 spanId : 1265] get content name : NATIONAL
2015-04-04 14:35:20 [INFO](ContentInfoCollector:34) [txId : agent^142533^22 spanId : 1278] get content name : OPINION
2015-04-04 14:35:20 [INFO](ContentInfoService:55) [txId : agent^14252^18 spanId : 1231] check authorization of user
2015-04-04 14:35:20 [INFO](ContentInfoService:14) [txId : agent^14252^17 spanId : 1224] get title of content
2015-04-04 14:35:21 [INFO](ContentDAOImpl:14) [txId : agent^14252^17 spanId : 1224] execute query ...
2015-04-04 14:35:21 [INFO](ContentDAOImpl:114) [txId : agent^142533^19 spanId : 1246] execute query ...    
2015-04-04 14:35:20 [INFO](ContentInfoService:74) [txId : agent^14252^17 spanId : 1224] get top linking for content
2015-04-04 14:35:21 [INFO](ContentDAOImpl:14) [txId : agent^142533^18 spanId : 1231] execute query ...
2015-04-04 14:35:21 [INFO](ContentDAOImpl:114) [txId : agent^142533^21 spanId : 1265] execute query ...
2015-04-04 14:35:22 [INFO](ContentDAOImpl:186) [txId : agent^142533^22 spanId : 1278] execute query ...
2015-04-04 14:35:22 [ERROR](ContentDAOImpl:158) [txId : agent^142533^18 spanId : 1231]
     com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure
          at com.pinpoint.example.dao.ContentDAO.executequery(ContentDAOImpl.java:152)
          ...
          at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
          at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
          at com.mysql.jdbc.ConnectionImpl.<init>(ConnectionImpl.java:787)   
          ...
     Caused by: com.mysql.jdbc.exceptions.jdbc4.CommunicationsException:
     Communications link failure The last packet sent successfully to the server was 0 milliseconds ago.
     The driver has not received any packets from the server.
          ...
          at com.mysql.jdbc.ConnectionImpl.createNewIO(ConnectionImpl.java:2181)
          ... 12 more
     Caused by: java.net.ConnectException: Connection refused
          at java.net.PlainSocketImpl.socketConnect(Native Method)   
          at java.net.PlainSocketImpl.doConnect(PlainSocketImpl.java:333)
          at java.net.PlainSocketImpl.connectToAddress(PlainSocketImpl.java:195)
          at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:182)
          at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:432)
          at java.net.Socket.connect(Socket.java:529)
          ... 13 more
2015-04-04 14:35:22 [INFO](ContentDAO:145) [txId : agent^14252^17 spanId : 1224] execute query ...
2015-04-04 14:35:20 [INFO](ContentInfoService:38) [txId : agent^142533^19 spanId : 1246] update hits for content
2015-04-04 14:35:20 [INFO](ContentInfoService:89) [txId : agent^142533^21 spanId : 1265] check of user
2015-04-04 14:35:24 [INFO](ContentDAO:146) [txId : agent^142533^22 spanId : 1278] execute query ...
2015-04-04 14:35:25 [INFO](ContentDAO:123) [txId : agent^14252^17 spanId : 1224] execute query ...
```

The transactionId printed in the log message is the same as the transactionId in Pinpoint Web’s Transaction List view.
![per-request_feature_1.jpg](images/per-request_feature_1.jpg)

### 2. How to configure

**2-1 Pinpoint agent configuration**

To enable this feature, set the logging property corresponding to the logging library in use to true in *pinpoint.config*.
For example,

ex) pinpoint.config when using log4j
```
###########################################################
# log4j
###########################################################
profiler.log4j.logging.transactioninfo=true

###########################################################
# logback
###########################################################
profiler.logback.logging.transactioninfo=false
```

ex) pinpoint.config when using logback
```
###########################################################
# log4j
###########################################################
profiler.log4j.logging.transactioninfo=false

###########################################################
# logback
###########################################################
profiler.logback.logging.transactioninfo=true
```

**2-2 log4j, logback configuration**

Change the log message format to print the transactionId, and spanId saved in MDC.

ex) log4j : log4j.xml
```xml
Before
<appender name = "console" class= "org.apache.log4j.ConsoleAppender" >
     <layout class = "org.apache.log4j.EnhancedPatternLayout">
          <param name = "ConversionPattern" value= "%d{yyyy-MM-dd HH:mm:ss} [%-5p](%-30c{1}) %m%n" />
     </layout >
</appender >

After
<appender name = "console" class= "org.apache.log4j.ConsoleAppender" >
     <layout class = "org.apache.log4j.EnhancedPatternLayout">
          <param name = "ConversionPattern" value= "%d{yyyy-MM-dd HH:mm:ss} [%-5p](%-30c{1}) [TxId : %X{PtxId} , SpanId : %X{PspanId}] %m%n" />
        </layout >
</appender >
```

ex) logback : logback.xml
```xml
Before
<appender name = "STDOUT" class= "ch.qos.logback.core.ConsoleAppender" >
     <layout class = "ch.qos.logback.classic.PatternLayout">
          <Pattern >%d{HH:mm} %-5level %logger{36} - %msg%n</Pattern >
     </layout >
</appender >

After
<appender name = "STDOUT" class= "ch.qos.logback.core.ConsoleAppender" >
     <layout class = "ch.qos.logback.classic.PatternLayout">
          <Pattern >%d{HH:mm} %-5level %logger{36} - [TxId : %X{PtxId} , SpanId : %X{PspanId}] %msg%n</Pattern >
     </layout >
</appender >
```

**2-3 Checking log message**

If the per-request logging is correctly configured, the transactionId, and spanId are printed in the log file.

```  
2015-04-04 14:35:20 [INFO](ContentInfoCollector:76 ) [txId : agent^14252^17 spanId : 1224] get content name : TECH
2015-04-04 14:35:20 [INFO](ContentInfoCollector:123 ) [txId : agent^142533^18 spanId : 1231] get content name : OPINION
2015-04-04 14:35:20 [INFO](ContentInfoCollector:12) [txId : agent^142533^19 spanId : 1246] get content name : SPORTS
```

### 3. expose log in Pinpoint Web

If you want to add links to the logs in the transaction list view, you should configure and implement the logic as below.
Pinpoint Web only adds link buttons - you should implement the logic to retrieve the log message.

If you want to expose your agent’s log messages, please follow the steps below.

**step 1**
You should implement a controller that receives transactionId, spanId, transaction_start_time as parameters and retrieve the logs yourself. 
We do not yet provide a way to retrieve the logs.

example)
```java
@Controller
public class Nelo2LogController {
  
    @RequestMapping(value = "/????")
    public String NeloLogForTransactionId(@RequestParam (value= "transactionId", required=true) String transactionId,
                                            @RequestParam(value= "spanId" , required=false) String spanId,
                                            @RequestParam(value="time" , required=true) long time ) {

          // you should implement the logic to retrieve your agent’s logs.
    }
```

**step 2**
In *pinpoint-web.properties* file, set `log.enable` to true, and `log.page.url` to the url of the controller above.
The value set in `log.button.name` will show up as the button text in the Web UI.
```properties
log.enable= true
log.page.url=XXXX.pinpoint
log.button.name= log
```

**step 3**
Pinpoint 1.5.0 or later, we improve button to decided enable/disable depending on whether or not being logged.
You should implement interceptor for using logging appender to add logic whether or not being logged. you also should create plugin for logging appender internally.
Please refer to Pinpoint Profiler Plugin Sample([Link](https://github.com/naver/pinpoint-plugin-sample)).
Location added logic of interceptor is method to log for data of LoggingEvent in appender class. you should review your appender class and find method.
This is interceptor example.

```
public class AppenderInterceptor implements AroundInterceptor0 {

    private final TraceContext traceContext;

    public AppenderInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target) {
        Trace trace = traceContext.currentTraceObject();

        if (trace != null) {
            SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordLogging(LoggingInfo.LOGGED);
        }
    }

    @IgnoreMethod
    @Override
    public void after(Object target, Object result, Throwable throwable) {

    }
}
```

If those are correctly configured, the buttons are added in the transaction list view.
![per-request_feature_2.jpg](images/per-request_feature_2.jpg)

For details in how the log buttons are generated, please refer to Pinpoint Web’s BusinessTransactionController and ScatterChartController.
