package com.nhn.pinpoint.collector.cluster.zookeeper.job;

/**
 * @author koo.taejin
 */
public class UpdateJob implements Job {

	private final byte[] contents;
	
	public UpdateJob(byte[] contents) {
		this.contents = contents;
	}

	public byte[] getContents() {
		return contents;
	}

}
