       **BusinessLog** is for Application to record their Logs with mark to label which trace belong to.In our implementation,
we implement thread parts :
* BusinessLog Collector in Pinpoint's Agent started as thread with Application'progrecess
* A branch of BusinessLog at Pinpoint'UDP Receiver in Pinpoint's Collector
* Web displaying of BusinessLog in Pinpoint's Webui
next we will explain how to config and use our BusinessLog' function.
##Configuration of Pinpoint' Agent
As konwn,most application take log4j or logback to record their business log,here we take log4j
for an example.
   * **configuration of log4j.xml** - Currently,we have a firm constraint on log4j:*Firstly*,it
   must generate one log per day;*Secondly*,the pattern of logfile'name must match  the Pattern *"^BUSINESS_LOG_[A-Za-z0-9\_-]*.log$"* 
   and in the example, the filename is "BUSINESS_LOG_test.log";*Thirdly*,it must has **exactly** the same log pattern as illustrated
   as follows:
   ![log4j.xml](doc/img/businesslog/log4j.png)
   * **configuration of pinpoint.config** 
        1. profiler.log4j.logging.transactioninfo=true
        2. profiler.tomcatlog.dir=AppOneAgentId=E:/Test/log4j/;AppTwoAgentId=D:/Test/log4j
        3. profiler.businesslog.enable=true
     
     
     
     1. The value of *profiler.tomcatlog.dir* is composited of different Application' Info which use the same Pinpoint's agent,for example,
     AppOne and AppTwo are different applications but on the same machine, so they can share the same Pinpoint'agent.Different Application'
     Info must be seperated by **;** .One Application's Info is composited by AgentId and dir of business log which the same with configuration
     in the log4j,but must be a *dir*, and seperated by **=**
     2. Setting profiler.businesslog.enable=true will start the thread of businesslog collector ,or else disable.
##Operation in Webui
  * **select application** *
  ![application](doc/img/businesslog/application.png)
  * **select trace** *
  ![trace](doc/img/businesslog/trace.png)
  * **select log** *
  ![application](doc/img/businesslog/log.png) 
  This version is just an alpha version, we send bussiness log in batch. One batch consists of lists of businesslog 
  record.In order to keep the logging integrity,we send one log record util meet the next one, so if you check log in
  webui, you may find losing the last log entry.