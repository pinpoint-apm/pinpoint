package com.nhn.pinpoint.testweb.vo;

public class RequestMappingInfo {
	private String url;
	private String description;

	public RequestMappingInfo(String url, String description) {
		this.url = url;
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "RequestMappingInfo [url=" + url + ", description=" + description + "]";
	}
}
