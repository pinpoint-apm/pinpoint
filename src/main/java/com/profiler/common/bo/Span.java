package com.profiler.common.bo;

import java.util.List;

import com.profiler.common.ServiceType;

public interface Span {
	ServiceType getServiceType();

	String getRpc();

	String getEndPoint();
	
	List<AnnotationBo> getAnnotationBoList();
}
