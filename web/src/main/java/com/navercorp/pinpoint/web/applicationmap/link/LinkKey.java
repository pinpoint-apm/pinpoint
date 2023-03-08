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

package com.navercorp.pinpoint.web.applicationmap.link;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.Objects;

/**
 * @author emeroad
 */
public final class LinkKey {
    private final Application from;
    private final Application to;
    
    private int hash;

    public LinkKey(Application from, Application to) {
        this.from = Objects.requireNonNull(from, "from");
        this.to = Objects.requireNonNull(to, "to");
    }

    public static LinkKey of(String fromApplication, ServiceType fromServiceType, String toApplication, ServiceType toServiceType) {
        Application from = new Application(fromApplication, fromServiceType);
        Application to = new Application(toApplication, toServiceType);
        return new LinkKey(from, to);
    }


    public Application getFrom() {
        return from;
    }

    public Application getTo() {
        return to;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkKey linkKey = (LinkKey) o;

        if (!from.equals(linkKey.from)) return false;
        return to.equals(linkKey.to);
    }

    @Override
    public int hashCode() {
        final int hash = this.hash;
        if (hash != 0) {
            return hash;
        }
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        this.hash = result;
        return result;
    }

    @Override
    public String toString() {
        return "LinkKey{"
                    + from + " -> " + to +
                '}';
    }
}
