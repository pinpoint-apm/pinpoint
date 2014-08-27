package com.nhn.pinpoint.web.vo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.nhn.pinpoint.common.bo.SpanBo;

/**
 * @author netspider
 * @author emeroad
 */
public class BusinessTransactions {

	private final Map<String, BusinessTransaction> transactions = new HashMap<String, BusinessTransaction>();

	private int totalCallCount;
	
	public void add(SpanBo span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        totalCallCount++;
		
		String rpc = span.getRpc();
		if (transactions.containsKey(rpc)) {
			transactions.get(rpc).add(span);
		} else {
			transactions.put(rpc, new BusinessTransaction(span));
		}
	}

	public Collection<BusinessTransaction> getBusinessTransaction() {
        return transactions.values();
	}
	
	public int getTotalCallCount() {
		return totalCallCount;
	}
	
	public int getURLCount() {
		return transactions.size();
	}
}
