/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.cluster;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Objects;

public class ClusterServiceFactory implements FactoryBean<ClusterService>, InitializingBean, DisposableBean {

    private ClusterPointRouter clusterPointRouter;
    private ClusterService clusterService;

    @Override
    public ClusterService getObject() {
        return this.clusterService;
    }

    public void setClusterPointRouter(ClusterPointRouter clusterPointRouter) {
        this.clusterPointRouter = Objects.requireNonNull(clusterPointRouter, "clusterPointRouter");
    }


    @Override
    public void destroy() throws Exception {
        this.clusterService.tearDown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.clusterService = newClusterService();
        this.clusterService.setup();
    }

    private ClusterService newClusterService() {
        return new ClusterServiceImpl(clusterPointRouter);
    }

    @Override
    public Class<?> getObjectType() {
        return ClusterService.class;
    }

}
