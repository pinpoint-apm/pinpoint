/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.applicationmap.model;

import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Write-side application map model collected from a single span or span chunk.
 * All rows share the same requestTime (collector accept time).
 * The row lists are lazily initialized; most spans populate only some of them.
 */
public class ApplicationMapModel {

    private final long requestTime;

    private List<OutLinkRow> outLinks;
    private List<InLinkRow> inLinks;
    private List<ResponseTimeRow> responseTimes;
    private List<AcceptorHostRow> acceptorHosts;

    public ApplicationMapModel(long requestTime) {
        this.requestTime = requestTime;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void addOutLink(OutLinkRow outLink) {
        Objects.requireNonNull(outLink, "outLink");
        if (this.outLinks == null) {
            this.outLinks = new ArrayList<>();
        }
        this.outLinks.add(outLink);
    }

    public void addInLink(InLinkRow inLink) {
        Objects.requireNonNull(inLink, "inLink");
        if (this.inLinks == null) {
            this.inLinks = new ArrayList<>();
        }
        this.inLinks.add(inLink);
    }

    public void addResponseTime(ResponseTimeRow responseTime) {
        Objects.requireNonNull(responseTime, "responseTime");
        if (this.responseTimes == null) {
            this.responseTimes = new ArrayList<>();
        }
        this.responseTimes.add(responseTime);
    }

    public void addAcceptorHost(AcceptorHostRow acceptorHost) {
        Objects.requireNonNull(acceptorHost, "acceptorHost");
        if (this.acceptorHosts == null) {
            this.acceptorHosts = new ArrayList<>();
        }
        this.acceptorHosts.add(acceptorHost);
    }

    public List<OutLinkRow> getOutLinks() {
        return nonNull(outLinks);
    }

    public List<InLinkRow> getInLinks() {
        return nonNull(inLinks);
    }

    public List<ResponseTimeRow> getResponseTimes() {
        return nonNull(responseTimes);
    }

    public List<AcceptorHostRow> getAcceptorHosts() {
        return nonNull(acceptorHosts);
    }

    private static <T> List<T> nonNull(List<T> list) {
        if (list == null) {
            return List.of();
        }
        return list;
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(outLinks) && CollectionUtils.isEmpty(inLinks)
                && CollectionUtils.isEmpty(responseTimes) && CollectionUtils.isEmpty(acceptorHosts);
    }

    @Override
    public String toString() {
        return "ApplicationMapModel{" +
                "requestTime=" + requestTime +
                ", outLinks=" + CollectionUtils.nullSafeSize(outLinks) +
                ", inLinks=" + CollectionUtils.nullSafeSize(inLinks) +
                ", responseTimes=" + CollectionUtils.nullSafeSize(responseTimes) +
                ", acceptorHosts=" + CollectionUtils.nullSafeSize(acceptorHosts) +
                '}';
    }

    /**
     * Dumps every collected row, unlike {@link #toString()} which only prints the list sizes.
     */
    public String dump() {
        return "ApplicationMapModel{" +
                "requestTime=" + requestTime +
                ", outLinks=" + nonNull(outLinks) +
                ", inLinks=" + nonNull(inLinks) +
                ", responseTimes=" + nonNull(responseTimes) +
                ", acceptorHosts=" + nonNull(acceptorHosts) +
                '}';
    }
}
