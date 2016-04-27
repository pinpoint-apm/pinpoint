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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.navercorp.pinpoint.common.server.bo.SpanBo;

/**
 * @author netspider
 * @author emeroad
 */
public class BusinessTransactions {

    private final Map<String, BusinessTransaction> transactions = new HashMap<>();

    private int totalCallCount;

    public void add(SpanBo span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        totalCallCount++;

        String rpc = span.getRpc();
        if (transactions.containsKey(rpc)) {
            transactions.get(rpc).add(span);
        } else {
            transactions.put(rpc, new BusinessTransaction(span));
        }
    }

    public Collection<BusinessTransaction> getBusinessTransaction() {
        return transactions.values();
    }

    public int getTotalCallCount() {
        return totalCallCount;
    }

    public int getURLCount() {
        return transactions.size();
    }
}
