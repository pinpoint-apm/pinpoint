/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.batch;

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

	private final Logger logger = LoggerFactory.getLogger(getClas       ());
	
    @Autowired
	private JobLo    ator loc    tor;
	@Autowired
	private J       bLauncher launcher;
	
	priv    te String batchServerIp;

	public void setBatchSer       erIp(String batchServerIp) {
		        is.batchServerIp = batchServerIp;
	}

	public JobExecution ru       (String jobName, JobPara          eters                          arams) {
		if(!decisionBa          chServer()) {
			return nul       ;
		}
		try {
			Jo          	job = locator.getJob(jobName             ;
			return launcher.run(job, params       ;
		} catch (Exception e) {
			throw                       ew IllegalStateException(e);
		}
	}

	private        oolean decisionBatchServe          () {
		Enumeration<NetworkInterface> int          rfaces;
		try {
			interfaces = Networ          Interface.getNetworkInterfaces();
		} catch (S          cketException e) {
			logger.error("not found network                    terface", e);
			retur              false;
		}

		while (interface                         .hasMoreElements())
			NetworkInterface network = interface                                                                                   nextElement();
			Enumeration<InetAddress> in       ts = network.getInetAddresses();
			
			while (inets.has       oreElements()) {
				InetAddress next = inets.nextElement(    ;
				
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
