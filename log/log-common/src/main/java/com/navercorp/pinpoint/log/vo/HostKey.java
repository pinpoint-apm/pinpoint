/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.log.vo;

import java.util.Objects;

/**
 * @author youngjin.kim2
 */
public class HostKey {

    private final String hostGroupName;
    private final String hostName;

    private HostKey(String hostGroupName, String hostName) {
        this.hostGroupName = hostGroupName;
        this.hostName = hostName;
    }

    public static HostKey of(String hostGroupName, String hostName) {
        return new HostKey(hostGroupName, hostName);
    }

    public String getHostGroupName() {
        return hostGroupName;
    }

    public String getHostName() {
        return hostName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostKey hostKey = (HostKey) o;
        return Objects.equals(hostGroupName, hostKey.hostGroupName) && Objects.equals(hostName, hostKey.hostName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostGroupName, hostName);
    }

    @Override
    public String toString() {
        return hostGroupName + ':' + hostName;
    }

}
