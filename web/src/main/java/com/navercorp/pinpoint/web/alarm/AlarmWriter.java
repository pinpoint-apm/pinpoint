package com.nhn.pinpoint.web.alarm;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;
import com.nhn.pinpoint.web.alarm.vo.Rule;
import com.nhn.pinpoint.web.dao.mysql.MySqlAlarmResourceDao;
import com.nhncorp.lucy.net.call.Fault;
import com.nhncorp.lucy.net.call.Reply;
import com.nhncorp.lucy.net.call.ReturnValue;
import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.connector.NpcConnectionFactory;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;

public class AlarmWriter implements ItemWriter<AlarmCheckFilter> {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Value("#{dataProps['pinpoint.url']}")
    private String pinpointUrl;
    
    @Autowired
    private MySqlAlarmResourceDao dao;
    
    // Email config
    @Value("#{dataProps['alarm.mail.url']}")
    private String emailServerUrl;
    private static final String QUOTATATION = "\"";
    private static final String ADDR_SEPARATOR = ";";
    private static final String SENDER_EMAIL_ADDRESS = "<dl_labs_p_pinpoint@navercorp.com>";
    private static final String EMAIL_SERVICE_ID = "pinpoint";
    private static final String OPTION = "version=1;mimeCharset=utf-8;debugMode=false";
    
    // SMS config
    @Value("#{dataProps['alarm.sms.url']}")
    private String smsServerUrl;
    private static final String SENDER_NUMBER = "1588820";
    private static final String SMS_SERVICE_ID = "EMG00058";
    
    @Override
    public void write(List<? extends AlarmCheckFilter> checkers) throws Exception {
        for(AlarmCheckFilter checker : checkers) {
            send(checker);
        }
    }
    
    private void send(AlarmCheckFilter checker) {
        if (!checker.isDetected()) {
            return;
        }
        if (checker.isSMSSend()) {
            sendSms(checker);
        }
        if (checker.isEmailSend()) {
            sendEmail(checker);
        }
    }
    
    private void sendSms(AlarmCheckFilter checker) {
        List<String> receivers = dao.selectEmpGroupPhoneNumber(checker.getEmpGroup());

        if (receivers.size() == 0) {
            return;
        }
        
        CloseableHttpClient client = HttpClients.createDefault();
        
        try { 
            for(String message : checker.getSmsMessage()) {
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair("serviceId", SMS_SERVICE_ID));
                nvps.add(new BasicNameValuePair("sendMdn", QUOTATATION + SENDER_NUMBER + QUOTATATION));
                nvps.add(new BasicNameValuePair("receiveMdnList",convertToReceiverFormat(receivers)));
                nvps.add(new BasicNameValuePair("content", QUOTATATION + message + QUOTATATION));
            
                HttpGet get = new HttpGet(smsServerUrl + "?" + URLEncodedUtils.format(nvps, "UTF-8"));
                logger.debug("SMSServer url : {}", get.getURI());
                HttpResponse response = client.execute(get);
                logger.debug("SMSServer call result ={}", EntityUtils.toString(response.getEntity()));
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                logger.error("Error while HttpClient closed", e);
            }
        }
    }

    private String convertToReceiverFormat(List<String> receivers) {
        List<String> result = new ArrayList<String>();
        
        for (String receiver : receivers) {
            result.add(QUOTATATION + receiver + QUOTATATION);
        }
        
        return result.toString();
    }
    
    private void sendEmail(AlarmCheckFilter checker) {
        NpcConnectionFactory factory = new NpcConnectionFactory();
        factory.setBoxDirectoryServiceHostName(emailServerUrl);
        factory.setCharset(Charset.forName("UTF-8"));
        factory.setLightWeight(true);

        NpcHessianConnector connector = null;

        try {
            connector = factory.create();
            Object[] params = createSendMailParams(checker);
            InvocationFuture future = connector.invoke(null, "send", params);
            future.await();
            Reply reply = (Reply) future.getReturnValue();
            
            if (reply instanceof ReturnValue) {
                Object result = ((ReturnValue) reply).get();
                logger.debug("MessageId:{}", result);
            } else if (reply instanceof Fault) {
                Fault fault = (Fault) reply;
                logger.warn("Unexpected result:code={}, message={}", fault.getCode(), fault.getMessage());
            } else {
                logger.warn("Unexpected clazz({}).", reply.getClass());
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        } finally {
            if (connector != null) {
                connector.dispose();
            }
        }
    }

    private Object[] createSendMailParams(AlarmCheckFilter checker) {
        AlarmMailTemplate mailTemplate = new AlarmMailTemplate(checker, pinpointUrl);
        List<String> receivers = dao.selectEmpGroupEmail(checker.getEmpGroup());
        return new Object[] { EMAIL_SERVICE_ID, OPTION, SENDER_EMAIL_ADDRESS, "", joinAddresses(receivers), mailTemplate.createSubject(), mailTemplate.createBody()};
    }

    private String joinAddresses(List<String> addresses) {
        return StringUtils.join(addresses, ADDR_SEPARATOR);
    }
}
