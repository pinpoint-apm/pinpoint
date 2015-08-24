# Alarm 

pinpoint-web 서버에서 application 상태를 주기적으로 체크하여 applicaiton 상태의 수치가 임계치를 초과할 경우 사용자에게 알람을 전송하는 기능을 제공한다. 

applicaiton 상태 값이 사용자가 설정한 임계치를 초과하는 지 판단하는 로직은 pinpoint-web 서버내에서 background batch로 동작이 된다.
alarm batch는 기본적으로 3분에 한번씩 동작이 된다. 최근 5분동안의 데이터를 수집해서 alarm 조건을 만족하면 user group의 담당자들에게 sms /email을 전송합니다.


## 1. Alarm 기능 사용 방법

1) user를 등록한다. 
	- 화면 챕쳐 추가해야함.
2) alarm 대상이 되는 userGroup을 생성한다.
	- 화면 챕쳐 추가해야함.
3) userGroup에 member를 등록한다.
	- 화면 챕쳐 추가해야함.
4) 상태를 체크하고 싶은 alarm rule을 등록한다. 
	- 화면 챕쳐 추가해야함.
	- alarm rule에 대한 설명은 아래를 참고하세요. 

```         
SLOW_COUNT
   application 내에서 외부서버를 호출한 요청 중 slow 호출의 개수가 임계치를 초과한 경우 알람이 전송된다.
    
SLOW_RATE
   application 내에서 외부서버를 호출한 요청 중 slow 호출의 비율(%)이 임계치를 초과한 경우 알람이 전송된다.

ERROR_COUNT
   application 내에서 외부서버를 호출한 요청 중 error 가 발생한 호출의 개수가 임계치를 초과한 경우 알람이 전송된다.

ERROR_RATE
   application 내에서 외부서버를 호출한 요청 중 error 가 발생한 호출의 비율이 임계치를 초과한 경우 알람이 전송된다.

TOTAL_COUNT
   application 내에서 외부서버를 호출한 요청의 개수가 임계치를 초과한 경우 알람이 전송된다.

SLOW_COUNT_TO_CALLE
   외부에서 application을 호출한 요청 중에 외부서버로 응답을 늦게 준 요청의 개수가 임계치를 초과한 경우 알람이 전송된다.

SLOW_RATE_TO_CALLE
   외부에서 application을 호출한 요청 중에 외부서버로 응답을 늦게 준 요청의 비율(%)이 임계치를 초과한 경우 알람이 전송된다.

ERROR_COUNT_TO_CALLE
   외부에서 application을 호출한 요청 중에 에러가 발생한 요청의 개수가 임계치를 초과한 경우 알람이 전송된다.

ERROR_RATE_TO_CALLE
   외부에서 application을 호출한 요청 중에 에러가 발생한 요청의 비율(%)이 임계치를 초과한 경우 알람이 전송된다.

TOTAL_COUNT_TO_CALLE
   외부에서 application을 호출한 요청 개수가 임계치를 초과한 경우 알람이 전송된다.

HEAP_USAGE_RATE
   heap의 사용률이 임계치를 초과한 경우 알람이 전송된다.

GC_COUNT
   GC가 수행된 개수가 임계치를 초과한 경우 알람이 전송된다.

JVM_CPU_USAGE_RATE
   applicaiton의 CPU 사용률이 임계치를 초과한 경우 알람이 전송된다.
```


## 2. 구현 및 설정 방법

알람 기능을 사용하기 위해서는 sms, email을 전송하는 로직을 직접 구현해야 한다.
pipoint-web에서 제공하는 com.navercorp.pinpoint.web.alarm.AlarmMessageSender interface를 구현하고 spring framework의 bean으로 등록하면 된다.
사용자가 등록한 alarm rule 중 임계치를 초과한 rule이 발생하면 AlarmMessageSender의 sendEmail, sendSms 함수가 호출이 된다.
구현 및 설정 방법은 아래와 같다.

### 1) AlarmMessageSender 구현
	
```
public class AlarmMessageSenderImple implements AlarmMessageSender {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
	@Override
    public void sendSms(AlarmChecker checker) {
        List<String> receivers = userGroupService.selectPhoneNumberOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
            return;
        }
        
        for(String message : checker.getSmsMessage()) {
        	logger.info("send SMS : {}", message);
            
			//TODO sms 전송 로직 구현
        }
        
    }

	@Override
    public void sendEmail(AlarmChecker checker) {
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
            return;
        }
        
        for(String message : checker.getEmailMessage()) {
        	logger.info("send email : {}", message);
            
			//TODO email 전송 로직 구현
        }
    }
}
```
 
### 2) AlarmMessageSender 구현체 bean 등록

```
<bean id="naverAlarmMessageSender" class="com.navercorp.pinpoint.web.alarm.NaverAlarmMessageSender"/>
```

### 3) batch.properties 파일에 alarm batch가 실행되는 서버의 IP를 설정한다.
   alarm 기능은 설정한 ip의 값과 pinpoint-web서버의 ip가 일치할 경우에만 동작이 된다.(127.0.0.1을 설정한 경우에도 alarm batch가 동작 된다.)
   alarm 로직은 pinpoint-web 웹서버 내에서 동작이 되기때문에 pinpoint-web 서버를 2대 이상 운영할 경우 여러대의 서버에서 동시에 alarm batch가 동작되는것을 막기위해서 한대의 서버에서만 alarm batch가 동작이 되도록 alarm batch 서버의 ip를 설정한다.  
```
batch.server.ip=X.X.X.X
``` 
    
### 3. 기타
**1) alarm batch를 별도 프로세스로 실행하는 것도 가능하다.**
pinpoint-web 프로젝트의 src/main/resources/batch/applicationContext-alarmJob.xml 파일을 이용해서 spring batch job을 실행하면 된다.
실행 방법은 대한 구체적인 방법은 spirng batch 메뉴얼을 참고하자.

**2) batch의 동작 주기를 조정하고 싶다면 applicationContext-batch.xml 파일의 cron excpression을 수정하면 된다.**
```
<task:scheduled-tasks scheduler="scheduler">
	<task:scheduled ref="batchJobLauncher" method="alarmJob" cron="0 0/2 * * * *" />
</task:scheduled-tasks>
```

**3) alarm batch 수행 성능을 높이는 방법은 다음과 같다.**
alarm batch 성능 튜닝을 위해서 병렬로 동작이 가능하도록 구현을 해놨다.
그래서 아래에서 언급된 조건에 해당하는 경우 설정값을 조정한다면 성능을 향상 시킬수 있다. 단 병렬성을 높이면 리소스의 사용률이 높아지는것은 감안해야한다.  
	
alarm이 등록된 application의 개수가 많다면 applicationContext-batch.xml 파일의 poolTaskExecutorForPartition의 pool size를 늘려주면 된다.
``` 
<task:executor id="poolTaskExecutorForPartition" pool-size="1" />
```

application 각각마다 등록된 alarm의 개수가 많다면 applicationContext-batch.xml 파일에 선언된 alarmStep이 병렬로 동작되도록 설정하면 된다.
```
<step id="alarmStep" xmlns="http://www.springframework.org/schema/batch">
    <tasklet task-executor="poolTaskExecutorForStep" throttle-limit="3">
        <chunk reader="reader" processor="processor" writer="writer" commit-interval="1"/>
    </tasklet>
</step>
<task:executor id="poolTaskExecutorForStep" pool-size="10" />
```	









