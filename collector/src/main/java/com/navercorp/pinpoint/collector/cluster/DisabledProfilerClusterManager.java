/*
 * Copyright 2019 NAVER Corp.
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

import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
public class DisabledProfilerClusterManager implements ProfilerClusterManager {

    @Override
    public void register(ClusterPoint targetClusterPoint) {
    }

    @Override
    public void unregister(ClusterPoint targetClusterPoint) {
    }

    @Override
    public void refresh() {
    }

    @Override
    public List<String> getClusterData() {
        return Collections.emptyList();
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isRunning() {
        return false;
    }

}
