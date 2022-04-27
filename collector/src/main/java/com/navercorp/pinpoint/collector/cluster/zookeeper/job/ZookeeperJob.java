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

package com.navercorp.pinpoint.collector.cluster.zookeeper.job;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ZookeeperJob<K> {

    private final Type type;
    private final K key;

    public ZookeeperJob(Type type) {
        this(type, null);
    }

    public ZookeeperJob(Type type, K key) {
        this.type = Objects.requireNonNull(type, "type");
        this.key = key;
    }

    public Type getType() {
        return type;
    }

    public K getKey() {
        return key;
    }

    public enum Type {
        ADD,
        REMOVE,
        CLEAR
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ZookeeperJob{");
        sb.append("type=").append(type);
        sb.append(", key='").append(key).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
