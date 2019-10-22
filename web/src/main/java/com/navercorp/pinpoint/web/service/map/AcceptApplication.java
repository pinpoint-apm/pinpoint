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

package com.navercorp.pinpoint.web.service.map;

import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * @author emeroad
 */
public class AcceptApplication {
    private final String host;
    private final Application application;


    public AcceptApplication(String host, Application application) {
        this.host = Objects.requireNonNull(host, "host");
        this.application = Objects.requireNonNull(application, "application");
    }

    public String getHost() {
        return host;
    }

    public Application getApplication() {
        return application;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AcceptApplication that = (AcceptApplication) o;

        if (!application.equals(that.application)) return false;
        if (!host.equals(that.host)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + application.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AcceptApplication{");
        sb.append("host='").append(host).append('\'');
        sb.append(", application=").append(application);
        sb.append('}');
        return sb.toString();
    }
}
