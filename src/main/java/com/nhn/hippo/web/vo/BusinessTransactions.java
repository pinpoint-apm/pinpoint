package com.nhn.hippo.web.vo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.profiler.common.dto.thrift.Span;

public final class BusinessTransactions implements Iterable<BusinessTransaction> {

	private final Map<String, BusinessTransaction> transactions = new HashMap<String, BusinessTransaction>();
	private Iterator<Entry<String, BusinessTransaction>> iterator;

	public void add(Span span) {
		String name = span.getName();
		if (transactions.containsKey(name)) {
			transactions.get(name).add(span);
		} else {
			transactions.put(name, new BusinessTransaction(span));
		}
	}

	@Override
	public Iterator<BusinessTransaction> iterator() {
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
}
