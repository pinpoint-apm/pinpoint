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

package com.navercorp.pinpoint.bootstrap.interceptor;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author emeroad
 */
public class AtomicMaxUpdater {

    private static final AtomicIntegerFieldUpdater<AtomicMaxUpdater> UPDATER = AtomicIntegerFieldUpdater.newUpdater(AtomicMaxUpdater.class, "maxIndex");

    @SuppressWarnings("unused")
	private volatile int maxIndex = 0;

    public boolean update(int max) {
        while (true) {
            final int currentMax = getIndex();
            if (currentMax >= max) {
                return false;
            }
            final boolean update = UPDATER.compareAndSet(this, currentMax, max);
            if (update) {
                return true;
            }
        }
    }

    public int getIndex() {
        return UPDATER.get(this);
    }
}
