/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.common.trace;

/**
 * @author HyunGil Jeong
 */
public class ServiceTypeBuilder {

    private final short code;
    private final String name;
    private final String desc;
    private boolean terminal;
    private boolean queue;
    private boolean recordStatistics;
    private boolean includeDestinationId;
    private boolean alias;

    public ServiceTypeBuilder(short code, String name) {
        this(code, name, name);
    }

    public ServiceTypeBuilder(short code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public ServiceTypeBuilder terminal(boolean terminal) {
        this.terminal = terminal;
        return this;
    }

    public ServiceTypeBuilder queue(boolean queue) {
        this.queue = queue;
        return this;
    }

    public ServiceTypeBuilder recordStatistics(boolean recordStatistics) {
        this.recordStatistics = recordStatistics;
        return this;
    }

    public ServiceTypeBuilder includeDestinationId(boolean includeDestinationId) {
        this.includeDestinationId = includeDestinationId;
        return this;
    }

    public ServiceTypeBuilder alias(boolean alias) {
        this.alias = alias;
        return this;
    }


    short code() {
        return code;
    }

    String name() {
        return name;
    }

    String desc() {
        return desc;
    }

    boolean terminal() {
        return terminal;
    }

    boolean queue() {
        return queue;
    }

    boolean recordStatistics() {
        return recordStatistics;
    }

    boolean includeDestinationId() {
        return includeDestinationId;
    }

    boolean alias() {
        return alias;
    }

    public ServiceType build() {
        return new DefaultServiceType(this);
    }
}
