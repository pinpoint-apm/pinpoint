package com.nhn.pinpoint.web.filter;

import org.apache.commons.lang.StringUtils;

public class FilterDescriptor {

	private String fa = null;
	private String fst = null;
	private String ta = null;
	private String tst = null;
	private Long rf = null;
	private Long rt = null;
	private Boolean ie = null;
	private String url = null;

	public boolean isValid() {
		return isValidFromToInfo() && isValidFromToResponseTime();
	}

	public boolean isValidFromToInfo() {
		return !(StringUtils.isEmpty(fa) || StringUtils.isEmpty(fst) || StringUtils.isEmpty(ta) || StringUtils.isEmpty(tst));
	}

	public boolean isValidFromToResponseTime() {
		return !((rf == null && rt != null) || (rf != null && rt == null));
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
		return rt;
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

	public Long getRt() {
		return rt;
	}

	public void setRt(Long rt) {
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

	@Override
	public String toString() {
		return "FilterDescriptor [fa=" + fa + ", fst=" + fst + ", ta=" + ta + ", tst=" + tst + ", rf=" + rf + ", rt=" + rt + ", ie=" + ie + ", url=" + url + "]";
	}
}
