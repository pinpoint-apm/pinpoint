package com.nhn.pinpoint.web.alarm.filter;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.alarm.AlarmEvent;
import com.nhn.pinpoint.web.alarm.resource.MailResource;
import com.nhn.pinpoint.web.vo.Application;
import com.nhncorp.lucy.net.call.Fault;
import com.nhncorp.lucy.net.call.Reply;
import com.nhncorp.lucy.net.call.ReturnValue;
import com.nhncorp.lucy.net.invoker.InvocationFuture;
import com.nhncorp.lucy.npc.connector.NpcConnectionFactory;
import com.nhncorp.lucy.npc.connector.NpcHessianConnector;

public class AlarmMailSendFilter extends AlarmSendFilter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final String ADDR_SEPARATOR = ";";

	private final Application application;
	private final MailResource mailResource;
	private final List<String> receiverList;
	
	public AlarmMailSendFilter(Application application, MailResource mailResource, List<String> receiverList) {
		this.application = application;
		this.mailResource = mailResource;
		this.receiverList = receiverList;
	}
	
	@Override
	protected boolean send(AlarmEvent event) {
		NpcConnectionFactory factory = new NpcConnectionFactory();
		factory.setBoxDirectoryServiceHostName(mailResource.getUrl());
		factory.setCharset(Charset.forName("UTF-8"));
		factory.setLightWeight(true);
		
		NpcHessianConnector connector = null;
		try {
			connector = factory.create();
			Object[] params = createSendMailParams();
			
			InvocationFuture future = connector.invoke(null, "send", params);
			future.await();
			Reply reply = (Reply) future.getReturnValue();
			if (reply instanceof ReturnValue) {
				Object result = ((ReturnValue) reply).get();
				logger.debug("MessageId:{}", result);
			} else if (reply instanceof Fault) {
				String code = ((Fault) reply).getCode();
				String message = ((Fault) reply).getMessage();
				logger.warn("Unexpected result:code={}, message={}", code, message);
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
		
		// TODO Auto-generated method stub
		return false;
	}

	private Object[] createSendMailParams() {
		return new Object[] {
				mailResource.getServiceId(),
				mailResource.getOption(),
				mailResource.getSenderEmailAddress(),					/* 보낸이 */
				"",														/* 답장 받을 주소 */
				joinAddresses(receiverList),							/* 받는이 */
				String.format(mailResource.getSubject(), application.getName()),
				application.getName() + " error"
		};
	}
	
	private String joinAddresses(List<String> addresses) {
		return StringUtils.join(addresses, ADDR_SEPARATOR);
	}

}
