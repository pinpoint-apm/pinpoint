package com.nhncorp.lucy.nimm.mockupserver;

import external.org.apache.mina.common.IoSession;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: emeroad
 * Date: 2010. 3. 25
 * Time: 오후 4:08:32
 *
 * @author Middleware Platform Dev. Team
 */
public class RoutingState {

	private ConcurrentMap<Integer, AtomicBoolean> routingState;
	private static final String ROUTING_STATE = "_ROUTING_STATE";

	public static RoutingState getRoutingState(IoSession ioSession) {
		return (RoutingState) ioSession.getAttribute(ROUTING_STATE);
	}

	public static void createRoutingState(IoSession ioSession) {
		ioSession.setAttribute(ROUTING_STATE, new RoutingState());
	}

	private RoutingState() {
		this.routingState = new ConcurrentHashMap<Integer, AtomicBoolean>();
	}

	public boolean register(int domainId) {
		AtomicBoolean before = this.routingState.putIfAbsent(domainId, new AtomicBoolean());
		return before == null;
	}

	public void routingEnable(int domainId, boolean enable) {
		AtomicBoolean routing = this.routingState.get(domainId);
		if(routing == null) {
			throw new RuntimeException("domainId not found:" + domainId);
		}
		boolean success = routing.compareAndSet(!enable, enable);
		if(!success) {
			throw new RuntimeException("Invalid RoutingState:" + domainId + " enable:" + enable); 
		}
	}

	public boolean isAnycastRouting(int domainId) {
		AtomicBoolean routing = this.routingState.get(domainId);
		if(routing == null) {
			throw new RuntimeException("domainId not found:" + domainId);
		}
		return routing.get();
	}
}
