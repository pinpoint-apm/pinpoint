package com.nhn.hippo.web.controller;

import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author netspider
 */
public class BaseController {

	/**
	 * 항상 3초 전 데이터를 조회한다. 이것은 collector에서 지연되는 상황에 대한 처리.
	 * 
	 * @return
	 */
	protected long getQueryEndTime() {
		return System.currentTimeMillis() - 3000L;
	}
}