package com.nhn.pinpoint.testweb.controller;

import com.nhn.pinpoint.testweb.connector.apachehttp4.HttpConnectorOptions;
import com.nhn.pinpoint.testweb.connector.apachehttp4.ApacheHttpClient4;
import com.nhn.pinpoint.testweb.util.Description;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;

@Controller
public class ExceptionalCaseController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 루트의 완성이 지연될 경우 먼저 끝난 rpc콜을 정상적으로 읽을수 있는지 테스트
	 */
	@Description("root의 완료가 지연될경우 parent가 완료된 데이터를 정상적으로 확인가능지.")
	@RequestMapping(value = "/exceptionalcase/rootslow")
	public void rootSlow() {
		ApacheHttpClient4 client2 = new ApacheHttpClient4(new HttpConnectorOptions());
		client2.execute("http://localhost:8080/donothing.pinpoint", new HashMap<String, Object>());

		try {
			final int sleep = 1000 * 30;
			logger.info("sleep:{}", sleep);
			Thread.sleep(sleep);
		} catch (InterruptedException e) {

		}
	}
}
