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

package com.navercorp.pinpoint.collector.receiver.udp;

import com.codahale.metrics.Timer;

/**
 * @author emeroad
 */
public class TimingWrap implements Runnable {
    private final Timer timer;
    private final Runnable child;

    public TimingWrap(Timer timer, Runnable child) {
        if (timer == null) {
            throw new NullPointerException("timer must not be null");
        }
        if (child == null) {
            throw new NullPointerException("child must not be null");
        }
        this.timer = timer;
        this.child = child;
    }

    @Override
    public void run() {
        final Timer.Context time = timer.time();
        try {
            child.run();
        } finally {
            time.stop();
        }

    }
}
