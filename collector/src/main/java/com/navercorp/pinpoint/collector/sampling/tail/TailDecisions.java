/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.sampling.tail;

public final class TailDecisions {

    private TailDecisions() {
    }

    /**
     * Deterministic keep decision based on the transactionId.
     * @param ratePercent 0..100
     */
    public static boolean keep(String txid, int ratePercent) {
        if (ratePercent >= 100) {
            return true;
        }
        if (ratePercent <= 0) {
            return false;
        }
        int bucket = Math.floorMod(txid.hashCode(), 100);
        return bucket < ratePercent;
    }
}
