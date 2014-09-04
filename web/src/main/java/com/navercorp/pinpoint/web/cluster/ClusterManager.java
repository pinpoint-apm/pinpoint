package com.nhn.pinpoint.web.cluster;

/**
 * @author koo.taejin <kr14910>
 */
public interface ClusterManager {

	boolean registerWebCluster(String zNodeName, byte[] contents);

	void close();

}
