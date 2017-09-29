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

import org.apache.commons.lang3.StringUtils;

/**
 * @author Taejin Koo
 */
public class ZookeeperJob {

    private final Type type;
    private final String key;

    public ZookeeperJob(Type type) {
        this(type, StringUtils.EMPTY);
    }

    public ZookeeperJob(Type type, String key) {
        if (type == null) {
            throw new NullPointerException("type must not be null");
        }

        this.type = type;
        this.key = key;
    }

    public Type getType() {
        return type;
    }

    public String getKey() {
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
