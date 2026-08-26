/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.service.web.controller.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.server.uid.Service;

import java.util.Objects;

/**
 * JSON view of a registered {@link Service}.
 * Keeps the {@code {"uid": .., "name": ..}} shape that the former ServiceEntity response exposed.
 */
public class ServiceView {

    private final int uid;
    private final String name;

    public static ServiceView of(Service service) {
        Objects.requireNonNull(service, "service");
        return new ServiceView(service.getServiceUid(), service.getServiceName());
    }

    public ServiceView(int uid, String name) {
        this.uid = uid;
        this.name = Objects.requireNonNull(name, "name");
    }

    @JsonProperty("uid")
    public int getUid() {
        return uid;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ServiceView{" + name + '(' + uid + ")}";
    }
}
