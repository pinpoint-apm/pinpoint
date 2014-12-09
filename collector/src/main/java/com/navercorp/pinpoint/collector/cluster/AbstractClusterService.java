package com.navercorp.pinpoint.collector.cluster;

import com.navercorp.pinpoint.collector.config.CollectorConfiguration;

/**
 * @author koo.taejin <kr14910>
 */
public abstract class AbstractClusterService implements ClusterService {

	protected final CollectorConfiguration config;
	protected final ClusterPointRouter clusterPointRouter;

	public AbstractClusterService(CollectorConfiguration config, ClusterPointRouter clusterPointRouter) {
		this.config = config;
		this.clusterPointRouter = clusterPointRouter;
	}

}
