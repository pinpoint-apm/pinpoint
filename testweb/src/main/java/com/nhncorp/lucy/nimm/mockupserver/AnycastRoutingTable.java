package com.nhncorp.lucy.nimm.mockupserver;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: emeroad
 * Date: 2010. 3. 24
 * Time: 오전 11:52:07
 *
 * @author Middleware Platform Dev. Team
 */
public class AnycastRoutingTable {
	private Logger logger = Logger.getLogger(getClass().getName());

	private final ConcurrentMap<AnycastKey, List<IoSessionPair>> routingTable = new ConcurrentHashMap<AnycastKey, List<IoSessionPair>>();
	private final AtomicInteger rrKey = new AtomicInteger(0);

	public List<IoSessionPair> register(AnycastKey anycastKey, IoSessionPair pair) {
		List<IoSessionPair> list = this.routingTable.get(anycastKey);
		if (list == null) {
			// 약간의 동시성 문제가 있나? 생각좀 해봐야 할듯.
			list = new CopyOnWriteArrayList<IoSessionPair>();
		}
		list.add(pair);
		return this.routingTable.put(anycastKey, list);
	}

	public List<IoSessionPair> select(AnycastKey key) {
		return this.routingTable.get(key);
	}

	public IoSessionPair roundRobinSelect(AnycastKey key, boolean routingCheck) {
		List<IoSessionPair> ioSessionPairList = select(key);
		if(ioSessionPairList == null) {
			return null;
		}
		int size = ioSessionPairList.size();
		if (size == 0) {
			// anycast로 roundRobin할 키가 없음.
			return null;
		}
		for (int i = 0; i < size; i++) {
			try {
				IoSessionPair sessionPair = ioSessionPairList.get(rrKey.incrementAndGet() % size);
				if(routingCheck) {
					RoutingState routingState = RoutingState.getRoutingState(sessionPair.getIoSession());
					if(routingState.isAnycastRouting(key.getDomainId())) {
						return sessionPair;
					} else {
						continue;
					}
				} else {
					return sessionPair;
				}
			} catch (IndexOutOfBoundsException e) {
				logger.log(Level.WARNING, "index select error", e);
			}
		}
		return null;
	}

}
