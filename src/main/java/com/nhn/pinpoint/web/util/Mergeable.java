package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.service.NodeId;

/**
 * 
 * @author netspider
 * 
 * @param <T>
 */
public interface Mergeable<I, T> {
	public I getId();
	public T mergeWith(T o);
}
