package com.nhn.pinpoint.web.filter;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author netspider
 * 
 */
public class FilterDescriptor {

	/**
	 * from application name
	 */
	private String fa = null;
	
	/**
	 * from service type
	 */
	private String fst = null;
	
	/**
	 * to application name
	 */
	private String ta = null;
	
	/**
	 * to service type
	 */
	private String tst = null;
	
	/**
	 * response time from
	 */
	private Long rf = null;
	
	/**
	 * response time to
	 */
	private String rt = null;
	
	/**
	 * include exception
	 */
	private Boolean ie = null;
	
	/**
	 * requested url
	 */
	private String url = null;
	
	/**
	 * from agent name
	 */
	private String fan = null;
	
	/**
	 * to agent name
	 */
	private String tan = null;

	public boolean isValid() {
		return isValidFromToInfo() && isValidFromToResponseTime();
	}

	public boolean isValidFromToInfo() {
		return !(StringUtils.isEmpty(fa) || StringUtils.isEmpty(fst) || StringUtils.isEmpty(ta) || StringUtils.isEmpty(tst));
	}

	public boolean isValidFromToResponseTime() {
		return !((rf == null && !StringUtils.isEmpty(rt)) || (rf != null && StringUtils.isEmpty(rt)));
	}

	public boolean isSetUrl() {
		return !StringUtils.isEmpty(url);
	}

	public String getFromApplicationName() {
		return fa;
	}

	public String getFromServiceType() {
		return fst;
	}

	public String getToApplicationName() {
		return ta;
	}

	public String getToServiceType() {
		return tst;
	}

	public Long getResponseFrom() {
		return rf;
	}

	public Long getResponseTo() {
		if (rt == null) {
			return null;
		} else if ("max".equals(rt)) {
			return Long.MAX_VALUE;
		} else {
			return Long.valueOf(rt);
		}
	}

	public Boolean getIncludeException() {
		return ie;
	}

	public String getUrlPattern() {
		return url;
	}

	public String getFa() {
		return fa;
	}

	public void setFa(String fa) {
		this.fa = fa;
	}

	public String getFst() {
		return fst;
	}

	public void setFst(String fst) {
		this.fst = fst;
	}

	public String getTa() {
		return ta;
	}

	public void setTa(String ta) {
		this.ta = ta;
	}

	public String getTst() {
		return tst;
	}

	public void setTst(String tst) {
		this.tst = tst;
	}

	public Long getRf() {
		return rf;
	}

	public void setRf(Long rf) {
		this.rf = rf;
	}

	public String getRt() {
		return rt;
	}

	public void setRt(String rt) {
		this.rt = rt;
	}

	public Boolean getIe() {
		return ie;
	}

	public void setIe(Boolean ie) {
		this.ie = ie;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getFan() {
		return fan;
	}
	
	public String getFromAgentName() {
		return fan;
	}

	public void setFan(String fan) {
		this.fan = fan;
	}

	public String getTan() {
		return tan;
	}
	
	public String getToAgentName() {
		return tan;
	}

	public void setTan(String tan) {
		this.tan = tan;
	}

	
	@Override
	public String toString() {
		return "FilterDescriptor [fa=" + fa + ", fst=" + fst + ", ta=" + ta + ", tst=" + tst + ", rf=" + rf + ", rt=" + rt + ", ie=" + ie + ", url=" + url + ", fan=" + fan + ", tan=" + tan + "]";
	}
}
