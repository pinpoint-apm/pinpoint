package com.navercorp.pinpoint.web.batch;

import org.springframework.batch.core.JobExecution;

public interface JobFailMessageSender {
	public void sendSMS(JobExecution jobExecution);
	public void sendEmail(JobExecution jobExecution);
}
