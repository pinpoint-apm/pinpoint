package com.nhn.pinpoint.web.alarm.filter;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.alarm.AlarmEvent;
import com.nhn.pinpoint.web.alarm.resource.SmsResource;
import com.nhn.pinpoint.web.vo.Application;

/**
 * 
 * @author koo.taejin
 */
public class AlarmSmsSendFilter extends AlarmSendFilter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String QUOTATATION = "\"";

	private final Application application;
	private final SmsResource smsResource;
	private final List<String> receiverList;

	public AlarmSmsSendFilter(Application application, SmsResource smsResource, List<String> receiverList) {
		this.application = application;
		this.smsResource = smsResource;
		this.receiverList = receiverList;
	}

	@Override
	public boolean send(AlarmEvent event) {
		DefaultHttpClient client = new DefaultHttpClient();

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("serviceId", smsResource.getServiceId()));
		nvps.add(new BasicNameValuePair("sendMdn", QUOTATATION + smsResource.getSenderPhoneNumber() + QUOTATATION));
		nvps.add(new BasicNameValuePair("receiveMdnList", convertToReceiverFormat(receiverList)));
		nvps.add(new BasicNameValuePair("content", QUOTATATION + null + QUOTATATION));

		HttpGet get = new HttpGet(smsResource.getUrl() + "?" + URLEncodedUtils.format(nvps, "UTF-8"));

		logger.debug("{}", get.getURI());
		
		try {
			HttpResponse response = client.execute(get);
			if (response != null) {
				HttpEntity entity = response.getEntity();
				// 이작업 자체가 닫는거랑 같음
				logger.debug("result={}", EntityUtils.toString(entity));
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		} finally {
			// 이건 일단 이렇게 하고 나중에 성능을 위해서 바꾸장
			client.getConnectionManager().shutdown();
		}
		return false;
	}

	private String convertToReceiverFormat(List<String> receivers) {
		List<String> result = new ArrayList<String>();
		for (String receiver : receivers) {
			result.add(QUOTATATION + receiver + QUOTATATION);
		}
		return result.toString();
	}

}
