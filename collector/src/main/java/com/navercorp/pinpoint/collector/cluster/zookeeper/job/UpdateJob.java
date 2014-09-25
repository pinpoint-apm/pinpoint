package com.nhn.pinpoint.collector.cluster.zookeeper.job;

import com.nhn.pinpoint.rpc.server.ChannelContext;

/**
 * @author koo.taejin
 */
public class UpdateJob extends AbstractJob {

	private final byte[] contents;
	
	public UpdateJob(ChannelContext channelContext, byte[] contents) {
		super(channelContext);
		this.contents = contents;
	}
	
	public UpdateJob(ChannelContext channelContext, int maxRetryCount, byte[] contents) {
		super(channelContext, maxRetryCount);
		this.contents = contents;
	}

	public byte[] getContents() {
		return contents;
	}

}
