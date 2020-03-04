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

package com.navercorp.pinpoint.web.vo;

import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.Objects;

/**
 * @author emeroad
 */
public final class LinkKey {

    private final String fromApplication;
    private final ServiceType fromServiceType;

    private final String toApplication;
    private final ServiceType toServiceType;

    private int hash;

    public LinkKey(Application fromApplication, Application toApplication) {
        Objects.requireNonNull(fromApplication, "fromApplication");
        this.fromApplication = fromApplication.getName();
        this.fromServiceType = fromApplication.getServiceType();

        Objects.requireNonNull(toApplication, "toApplication");
        this.toApplication = toApplication.getName();
        this.toServiceType = toApplication.getServiceType();
    }

    public LinkKey(String fromApplication, ServiceType fromServiceType, String toApplication, ServiceType toServiceType) {
        this.fromApplication = Objects.requireNonNull(fromApplication, "fromApplication");
        this.fromServiceType = Objects.requireNonNull(fromServiceType, "fromServiceType");

        this.toApplication = Objects.requireNonNull(toApplication, "toApplication");
        this.toServiceType = Objects.requireNonNull(toServiceType, "toServiceType");
    }

    public String getFromApplication() {
        return fromApplication;
    }

    public ServiceType getFromServiceType() {
        return fromServiceType;
    }

    public String getToApplication() {
        return toApplication;
    }

    public ServiceType getToServiceType() {
        return toServiceType;
    }




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkKey linkKey = (LinkKey) o;

        if (fromServiceType != linkKey.fromServiceType) return false;
        if (toServiceType != linkKey.toServiceType) return false;
        if (!fromApplication.equals(linkKey.fromApplication)) return false;
        if (!toApplication.equals(linkKey.toApplication)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int hash = this.hash;
        if (hash != 0) {
            return hash;
        }
        int result = fromApplication.hashCode();
        result = 31 * result + fromServiceType.hashCode();
        result = 31 * result + toApplication.hashCode();
        result = 31 * result + toServiceType.hashCode();
        this.hash = result;
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LinkKey{");
        sb.append("fromApplication='").append(fromApplication).append('\'');
        sb.append(", fromServiceType=").append(fromServiceType);
        sb.append(", toApplication='").append(toApplication).append('\'');
        sb.append(", toServiceType=").append(toServiceType);
        sb.append(", hash=").append(hash);
        sb.append('}');
        return sb.toString();
    }
}
