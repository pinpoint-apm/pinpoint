package com.nhn.hippo.web.vo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.profiler.common.bo.SpanBo;

public class BusinessTransactions {

	private final Map<String, BusinessTransaction> transactions = new HashMap<String, BusinessTransaction>();

	public void add(SpanBo span) {
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
}
