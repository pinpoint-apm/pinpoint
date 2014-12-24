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

package com.navercorp.pinpoint.profiler.context;

import java.util.Collections;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.context.ServiceInfo;

/**
 * @author hyungil.jeong
 */
public class DefaultServiceInfo implements ServiceInfo {

    private final String serviceName;
    private final List<String> serviceLibs;

    public DefaultServiceInfo(String serviceName, List<String> serviceLibs) {
        if (serviceName == null) {
            this.serviceName = "";
        } else {
            this.serviceName = serviceName;
        }
        if (serviceLibs == null) {
            this.serviceLibs = Collections.emptyList();
        } else {
            this.serviceLibs = serviceLibs;
        }
    }

    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public List<String> getServiceLibs() {
        return Collections.unmodifiableList(this.serviceLibs);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultServiceInfo{");
        sb.append("serviceName='").append(serviceName).append('\'');
        sb.append(", serviceLibs=").append(serviceLibs).append("}");
        return sb.toString();
    }

}
