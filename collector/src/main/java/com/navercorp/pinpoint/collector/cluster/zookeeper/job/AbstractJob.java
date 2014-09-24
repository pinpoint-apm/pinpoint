package com.nhn.pinpoint.collector.cluster.zookeeper.job;

import java.util.concurrent.atomic.AtomicInteger;

import com.nhn.pinpoint.rpc.server.ChannelContext;

public class AbstractJob implements Job {

	private final ChannelContext channelContext;
	
	private final int maxCount;
	private final AtomicInteger currentCount;

	public AbstractJob(ChannelContext channelContext) {
		this(channelContext, 3);
	}

	public AbstractJob(ChannelContext channelContext, int maxCount) {
		this.channelContext = channelContext;
		
		this.maxCount = maxCount;
		this.currentCount = new AtomicInteger(0);
	}
	
	@Override
	public ChannelContext getChannelContext() {
		return channelContext;
	}
	
	@Override
	public int getMaxRetryCount() {
		return maxCount;
	}

	@Override
	public int getCurrentRetryCount() {
		return currentCount.get();
	}
	
	@Override
	public void incrementCurrentRetryCount() {
		currentCount.incrementAndGet();
	}
	
	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append(this.getClass().getSimpleName());
		toString.append(", ChannelContext=" + channelContext);
		toString.append(", Retry=" + currentCount.get() + "/" + maxCount);
		
		return toString.toString();
	}

}
