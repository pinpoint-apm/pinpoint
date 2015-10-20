package com.navercorp.pinpoint.web.batch;

import org.springframework.batch.core.JobExecution;

public class EmptyJobFailMessageSender implements JobFailMessageSender {

	@Override
	public void sendSMS(JobExecution jobExecution) {
	}

	@Override
	public void sendEmail(JobExecution jobExecution) {
	}

}
