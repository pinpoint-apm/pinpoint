/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.common.hbase.counter;

import java.util.concurrent.atomic.LongAdder;

public class HbaseBatchPerformanceCounter implements HBaseBatchPerformance {

    private final LongAdder opsCount = new LongAdder();
    private final LongAdder opsRejectCount = new LongAdder();
    private final LongAdder opsFailedCount = new LongAdder();

    private final LongAdder currentOpsCounter = new LongAdder();



    public void opsCount() {
        this.opsCount.increment();
        this.currentOpsCounter.increment();
    }

    public void opsCount(int counter) {
        this.opsCount.add(counter);
        this.currentOpsCounter.add(counter);
    }

    public void opsReject() {
        this.opsRejectCount.increment();
        this.currentOpsCounter.decrement();
    }

    public void opsReject(int size) {
        this.opsRejectCount.add(size);
        this.currentOpsCounter.add(-size);
    }

    public void success() {
        this.currentOpsCounter.decrement();
    }

    public void success(int size) {
        this.currentOpsCounter.add(-size);
    }


    public void opsFailed() {
        this.opsFailedCount.increment();
        this.currentOpsCounter.decrement();
    }



    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Long getOpsCount() {
        return opsCount.longValue();
    }

    @Override
    public Long getOpsRejectedCount() {
        return opsRejectCount.longValue();
    }

    @Override
    public Long getCurrentOpsCount() {
        return currentOpsCounter.longValue();
    }

    @Override
    public Long getOpsFailedCount() {
        return opsFailedCount.longValue();
    }



}
