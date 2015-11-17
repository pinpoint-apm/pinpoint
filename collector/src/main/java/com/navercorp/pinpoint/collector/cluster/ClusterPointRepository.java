/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterPointRepository<T extends ClusterPoint> implements ClusterPointLocator<T> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CopyOnWriteArrayList<T> clusterPointRepository = new CopyOnWriteArrayList<>();

    public boolean addClusterPoint(T clusterPoint) {
        boolean isAdd = clusterPointRepository.addIfAbsent(clusterPoint);

        if (!isAdd) {
            logger.warn("Already registered ClusterPoint({}).", clusterPoint);
        }

        return isAdd;
    }

    public boolean removeClusterPoint(T clusterPoint) {
        boolean isRemove = clusterPointRepository.remove(clusterPoint);

        if (!isRemove) {
            logger.warn("Already unregistered or not registered ClusterPoint({}).", clusterPoint);
        }

        return isRemove;
    }

    public List<T> getClusterPointList() {
        return new ArrayList<>(clusterPointRepository);
    }

    public void clear() {

    }

}
