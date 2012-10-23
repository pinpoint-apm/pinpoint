package com.nhn.hippo.web.vo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.profiler.common.bo.SpanBo;

public final class BusinessTransactions {

	private final Map<String, BusinessTransaction> transactions = new HashMap<String, BusinessTransaction>();
	private Iterator<Entry<String, BusinessTransaction>> iterator;
	private Iterator<Entry<String, BusinessTransaction>> traceIterator;

	public void add(SpanBo span) {
		String name = span.getName();
		if (transactions.containsKey(name)) {
			transactions.get(name).add(span);
		} else {
			transactions.put(name, new BusinessTransaction(span));
		}
	}

	public Iterator<BusinessTransaction> getBusinessTransactionIterator() {
		iterator = transactions.entrySet().iterator();

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

	public Iterator<BusinessTransaction> getTracesIterator() {
		traceIterator = transactions.entrySet().iterator();

		return new Iterator<BusinessTransaction>() {
			@Override
			public boolean hasNext() {
				return traceIterator.hasNext();
			}

			@Override
			public BusinessTransaction next() {
				return traceIterator.next().getValue();
			}

			@Override
			public void remove() {

			}
		};
	}
}
