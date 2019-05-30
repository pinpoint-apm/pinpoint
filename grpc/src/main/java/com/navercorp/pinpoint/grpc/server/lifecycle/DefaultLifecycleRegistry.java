/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.grpc.server.lifecycle;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultLifecycleRegistry implements LifecycleRegistry {

    private final ConcurrentMap<Long, Lifecycle> map = new ConcurrentHashMap<Long, Lifecycle>();


    @Override
    public Lifecycle add(Long transportId, Lifecycle lifecycle) {
        Assert.requireNonNull(transportId, "transportId must not be null");
        return map.put(transportId, lifecycle);
    }

    @Override
    public Lifecycle get(Long transportId) {
        Assert.requireNonNull(transportId, "transportId must not be null");
        return map.get(transportId);
    }

    @Override
    public Lifecycle remove(Long transportId) {
        Assert.requireNonNull(transportId, "transportId must not be null");
        return map.remove(transportId);
    }

    @Override
    public Collection<Lifecycle> values() {
        return map.values();
    }
}
