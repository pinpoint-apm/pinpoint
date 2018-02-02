---
title: Setting Alarm
tags:
keywords: alarm
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: alarm.html
disqus: true
---

# Alarm

Pinpoint Web periodically checks the applications' status and triggers an alarm if certain pre-configured conditions (rules) are met.

These conditions are (by default) checked every 3 minutes by a background batch process within the Web module, using the last 5 minutes of data. Once a condition is met, the batch process sends an sms/email to users registered to a user group.

## 1. User Guide

1) Configuration menu 
![alarm_figure01.gif](images/alarm_figure01.gif)  
  
2) Registering users  
![alarm_figure02.gif](images/alarm_figure02.gif)  
  
3) Creating user groups  
![alarm_figure03.gif](images/alarm_figure03.gif)  
  
4) Adding users to user group  
![alarm_figure04.gif](images/alarm_figure04.gif)  
  
5) Setting alarm rules  
![alarm_figure05.gif](images/alarm_figure05.gif)  
  
**Alarm Rules**
```
SLOW COUNT
   Triggered when the number of slow requests sent by the application exceeds the configured threshold.

SLOW RATE
   Triggered when the percentage(%) of slow requests sent by the application exceeds the configured threshold.

ERROR COUNT
   Triggered when the number of failed requests sent by the application exceeds the configured threshold.

ERROR RATE
   Triggered when the percentage(%) of failed requests sent by the application exceeds the configured threshold.

TOTAL COUNT
   Triggered when the number of all requests sent by the application exceeds the configured threshold.

SLOW COUNT TO CALLEE
   Triggered when the number of slow requests sent to the application exceeds the configured threshold.

SLOW RATE TO CALLEE
   Triggered when the percentage(%) of slow requests sent to the application exceeds the configured threshold.

ERROR COUNT TO CALLEE
   Triggered when the number of failed requests sent to the application exceeds the configured threshold.

ERROR RATE TO CALLEE
   Triggered when the percentage(%) of failed requests sent to the application exceeds the configured threshold.

TOTAL COUNT TO CALLEE
   Triggered when the number of all requests sent to the application exceeds the configured threshold.

HEAP USAGE RATE
   Triggered when the application's heap usage(%) exceeds the configured threshold.

JVM CPU USAGE RATE
   Triggered when the application's CPU usage(%) exceeds the configured threshold.

DATASOURCE CONNECTION USAGE RATE
   Triggered when the application's DataSource connection usage(%) exceeds the configured threshold.
```


## 2. Implementation & Configuration

In order to use the alarm function, you must implement your own logic to send sms and email by implementing `com.navercorp.pinpoint.web.alarm.AlarmMessageSender` and registering it as a Spring managed bean. When an alarm is triggered, `AlarmMessageSender#sendEmail`, and `AlarmMessageSender#sendSms` methods are called.

> If an email/sms is sent everytime when a threshold is exceeded, we felt that alarm message would be spammable.<br/>
> Therefore we decided to gradually increase the transmission frequency for alarms.<br/>
> ex) If an alarm occurs continuously, transmission frequency is increased by a factor of two. 3 min -> 6min -> 12min -> 24min

### 1) Implementing `AlarmMessageSender` and Spring bean registration
```
public class AlarmMessageSenderImple implements AlarmMessageSender {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserGroupService userGroupService;
    
    @Override
    public void sendSms(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getUserGroupId());

        if (receivers.size() == 0) {
            return;
        }

        for (String message : checker.getSmsMessage()) {
            logger.info("send SMS : {}", message);

            // TODO Implement logic for sending SMS
        }
    }

    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getUserGroupId());

        if (receivers.size() == 0) {
            return;
        }

        for (String message : checker.getEmailMessage()) {
            logger.info("send email : {}", message);

            // TODO Implement logic for sending email
        }
    }
}
```

```
<bean id="AlarmMessageSenderImple" class="com.navercorp.pinpoint.web.alarm.AlarmMessageSenderImple"/>
```

### 2) Configuring batch properties
Set `batch.enable` flag to **true** in *batch.properties*
```
batch.enable=true
```

`batch.server.ip` configuration is there to prevent concurrent batch operations when there are multiple pinpoint web servers. The batch is executed only if the server's IP address is identical to the value set in `batch.server.ip`. (Setting this to 127.0.0.1 will start the batch in all the web servers)
```
batch.server.ip=X.X.X.X
```

### 3) Configuring MYSQL
Set up a MYSQL server and configure connection information in *jdbc.properties* file.
```
jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:13306/pinpoint?characterEncoding=UTF-8
jdbc.username=admin
jdbc.password=admin
```
Create tables by running *[CreateTableStatement-mysql.sql](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/sql/CreateTableStatement-mysql.sql)*, and *[SpringBatchJobRepositorySchema-mysql.sql](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/sql/SpringBatchJobRepositorySchema-mysql.sql)*.

### 4) Others
**1) You may start the alarm batch in a separate process** - Simply start the spring batch job using the *[applicationContext-alarmJob.xml](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/batch/applicationContext-alarmJob.xml)* file inside the Pinpoint-web module.

**2) You may change the batch execution period by modifying the cron expression in *[applicationContext-batch-schedule.xml](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/batch/applicationContext-batch-schedule.xml)* file**
```
<task:scheduled-tasks scheduler="scheduler">
    <task:scheduled ref="batchJobLauncher" method="alarmJob" cron="0 0/3 * * * *" />
</task:scheduled-tasks>
```

**3) Ways to improve alarm batch performance** - The alarm batch was designed to run concurrently. If you have a lot of applications with alarms registered, you may increase the size of the executor's thread pool by modifying `pool-size` in *[applicationContext-batch.xml](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/batch/applicationContext-batch.xml)* file.

Note that increasing this value will result in higher resource usage.
```
<task:executor id="poolTaskExecutorForPartition" pool-size="1" />
```

If there are a lot of alarms registered to applications, you may set the `alarmStep` registered in *[applicationContext-batch.xml](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/batch/applicationContext-batch.xml)* file to run concurrently.
```
<step id="alarmStep" xmlns="http://www.springframework.org/schema/batch">
    <tasklet task-executor="poolTaskExecutorForStep" throttle-limit="3">
        <chunk reader="reader" processor="processor" writer="writer" commit-interval="1"/>
    </tasklet>
</step>
<task:executor id="poolTaskExecutorForStep" pool-size="10" />
```

**4) use quickstart's web** - 
Pinpoint Web uses Mysql to persist users, user groups, and alarm configurations.<br/>
However Quickstart uses MockDAO to reduce memory usage.<br/>
Therefore if you want to use Mysql for Quickstart, please refer to Pinpoint Web's [applicationContext-dao-config.xml](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/applicationContext-dao-config.xml
), [jdbc.properties](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/jdbc.properties).  
