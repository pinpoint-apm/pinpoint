/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
class WaitingJobListFactory {

    private final List<WaitingJob> waitingJobList = new ArrayList<WaitingJob>();


    public List<WaitingJob> createList(int size, long timeout) {
        Assert.state(waitingJobList.isEmpty(), "WaitingJob not close");

        for (int i = 0; i < size; i++) {
            WaitingJob latchJob = new WaitingJob(timeout);
            waitingJobList.add(latchJob);
        }
        return waitingJobList;
    }


    public void close() {
        if (this.waitingJobList.isEmpty()) {
            return;
        }

        for (WaitingJob waitingJob : waitingJobList) {
            waitingJob.close();
        }
        waitingJobList.clear();
    }
}
