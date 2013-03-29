package com.nhn.hippo.web.controller;

import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author netspider
 */
public class BaseController {

	// Ajax UI개발 테스트를 위해 추가함. crossdomain문제 해결용도.
	protected void addResponseHeader(final HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*.*");
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		response.setHeader("Expires", "0");
	}

	/**
	 * 항상 3초 전 데이터를 조회한다. 이것은 collector에서 지연되는 상황에 대한 처리.
	 * 
	 * @return
	 */
	protected long getQueryEndTime() {
		return System.currentTimeMillis() - 3000L;
	}
}