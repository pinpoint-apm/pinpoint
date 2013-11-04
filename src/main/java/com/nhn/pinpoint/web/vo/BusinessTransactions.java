package com.nhn.pinpoint.web.vo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.nhn.pinpoint.common.bo.SpanBo;

/**
 * @author emeroad
 */
public class BusinessTransactions {

	private final Map<String, BusinessTransaction> transactions = new HashMap<String, BusinessTransaction>();

	private int totalCallCount;
	
	public void add(SpanBo span) {
		totalCallCount++;
		
		String rpc = span.getRpc();
		if (transactions.containsKey(rpc)) {
			transactions.get(rpc).add(span);
		} else {
			transactions.put(rpc, new BusinessTransaction(span));
		}
	}

	public Iterator<BusinessTransaction> getBusinessTransactionIterator() {
		final Iterator<Entry<String, BusinessTransaction>> iterator = transactions.entrySet().iterator();

		return new Iterator<BusinessTransaction>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public BusinessTransaction next() {
				return iterator.next().getValue();
			}

			@Override
			public void remove() {

			}
		};
	}
	
	public int getTotalCallCount() {
		return totalCallCount;
	}
	
	public int getURLCount() {
		return transactions.size();
	}
}
