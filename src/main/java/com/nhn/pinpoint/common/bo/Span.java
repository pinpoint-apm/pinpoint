package com.nhn.pinpoint.common.bo;

import java.util.List;

import com.nhn.pinpoint.common.ServiceType;

/**
 * @author emeroad
 */
public interface Span {
	ServiceType getServiceType();

	String getRpc();

	String getEndPoint();
	
	List<AnnotationBo> getAnnotationBoList();
}
