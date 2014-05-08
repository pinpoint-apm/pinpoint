package com.nhn.pinpoint.web.alarm;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.connector.NpcConnectionFactory;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;

public class AlarmMailFilter {

//	# owl email
//	alarm.mail.url=common.mail.mailer.class2.2nd/owl#dev
//	alarm.mail.serviceId=pinpoint
//	alarm.mail.option=version=1;mimeCharset=utf-8;debugMode=false
//	alarm.mail.sender.emailAddress=<dl_labs_p_pinpoint@navercorp.com>
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String ADDR_SEPARATOR = ";";

//	private final Application application;
	private final String owlUrl = "common.mail.mailer.class2.2nd/owl#dev";
	private final String serviceId = "pinpoint";
	private final String sender = "<dl_labs_p_pinpoint@navercorp.com>";
	private final String option = "version=1;mimeCharset=utf-8;debugMode=false";
	private final List<String> receiverList = Arrays.asList("koo.taejin@navercorp.com");

	@Test
	public void send() {

		NpcConnectionFactory factory = new NpcConnectionFactory();
		factory.setBoxDirectoryServiceHostName(owlUrl);
		factory.setCharset(Charset.forName("UTF-8"));
		 factory.setLightWeight(true);

		NpcHessianConnector connector = null;
		try {
			connector = factory.create();
			InvocationFuture future = connector.invoke(null, "send", createSendMailParams());
			future.await();
			System.out.println(future.getReturnValue());
		} catch (Exception e) {
			logger.warn(e.getMessage());
		} finally {
			if (connector != null) {
				connector.dispose();
			}
		}

	}

	private Object[] createSendMailParams() {
		return new Object[] { serviceId, option, sender, /* 보낸이 */
		"", /* 답장 받을 주소 */
		joinAddresses(receiverList), /* 받는이 */
		"test", " error" };
	}

	private String joinAddresses(List<String> addresses) {
		return StringUtils.join(addresses, ADDR_SEPARATOR);
	}

}
