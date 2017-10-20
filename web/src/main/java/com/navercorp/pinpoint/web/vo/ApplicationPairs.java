/*
 * Copyright 2017 NAVER Corp.
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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
public class ApplicationPairs {

    @JsonProperty("from")
    private List<ApplicationPair> fromApplications;

    @JsonProperty("to")
    private List<ApplicationPair> toApplications;

    public List<ApplicationPair> getFromApplications() {
        return fromApplications;
    }

    public void setFromApplications(List<ApplicationPair> fromApplications) {
        this.fromApplications = fromApplications;
    }

    public List<ApplicationPair> getToApplications() {
        return toApplications;
    }

    public void setToApplications(List<ApplicationPair> toApplications) {
        this.toApplications = toApplications;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApplicationPairs{");
        sb.append("fromApplications=").append(fromApplications);
        sb.append(", toApplications=").append(toApplications);
        sb.append('}');
        return sb.toString();
    }
}
