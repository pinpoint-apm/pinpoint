package com.nhn.pinpoint.web.batch;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class JobFailListener implements JobExecutionListener {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String mexServerUrl;
	private final List<String> cellPhoneNumbers;
	private final String serviceID;
	
	public JobFailListener(String mexServerUrl, String serviceID, List<String> cellPhoneNumbers) {
		this.mexServerUrl = mexServerUrl;
		this.serviceID = serviceID;
		this.cellPhoneNumbers = cellPhoneNumbers;
	}
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (!jobExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
			sendSMS(jobExecution.getJobInstance().getJobName(), jobExecution.getStartTime(), jobExecution.getEndTime());
		}
	}

	private void sendSMS(String jobName, Date start, Date end) {
		String encodeMsg = encodeMessage("[PINPOINT]batch job fail\n jobName : " + jobName + "\n start : " + start + "\n end : NOW");
		
		for (String number : cellPhoneNumbers) {
			String url = mexServerUrl + "?serviceId=\"" + serviceID + "\""
						+ "&sendMdn=\"" + number + "\""
						+ "&receiveMdnList=[\"" + number + "\"]"
						+ "&content=\"" + encodeMsg + "\"";
			
			try {
				URL submitURL = new URL(url);
				HttpURLConnection connection = (HttpURLConnection)submitURL.openConnection();
				connection.setConnectTimeout(3000);
				connection.setReadTimeout(3000);
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Content-Language", "utf-8");
				connection.connect();
	
				if (connection != null && connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					logger.error("fail send sms message for batch fail.");
				}
			} catch (Exception e) {
				logger.error("fail send sms message for batch fail.", e);
			}
		}
	}

	private String encodeMessage(String message) {
		message = message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r\n", "\\n").replace("\r", "\\n").replace("\n", "\\n");
		try {
			return URLEncoder.encode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Can't encoding sms message.");
			return "batch job fail";
		}
	}
}
