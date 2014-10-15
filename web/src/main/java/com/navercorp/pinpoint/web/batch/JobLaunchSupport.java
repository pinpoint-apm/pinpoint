package com.nhn.pinpoint.web.batch;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

public class JobLaunchSupport implements InitializingBean {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private JobLocator locator;
	@Autowired
	private JobLauncher launcher;
	
	private String batchServerIp;

	public void setBatchServerIp(String batchServerIp) {
		this.batchServerIp = batchServerIp;
	}

	public JobExecution run(String jobName, JobParameters params) {
		if(!decisionBatchServer()) {
			return null;
		}
		try {
			Job	job = locator.getJob(jobName);
			return launcher.run(job, params);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private boolean decisionBatchServer() {
		Enumeration<NetworkInterface> interfaces;
		
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			logger.error("not found network nterface", e);
			return false;
		}

		while (interfaces.hasMoreElements()) {
			NetworkInterface network = interfaces.nextElement();
			Enumeration<InetAddress> inets = network.getInetAddresses();
			
			while (inets.hasMoreElements()) {
				InetAddress next = inets.nextElement();
				
				if (next instanceof Inet4Address) {
					if (next.getHostAddress().equals(batchServerIp)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(locator, "jobLocator name must be provided");
		Assert.notNull(launcher, "jobLauncher name must be provided");
	}
}
