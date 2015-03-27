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

package com.navercorp.pinpoint.rpc.common;

/**
 * @author Taejin Koo
 */
public class CyclicStateChecker {

    private final byte conditionValue;

    private final int capacity;

    private byte data = 0;

    private int index = 0;

    // no guarantee of synchronization
    public CyclicStateChecker(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + capacity + ". Available 1 ~ 8.");
        }

        if (capacity > 8) {
            throw new IllegalArgumentException("Illegal Capacity: " + capacity + ". Available 1 ~ 8.");
        }

        byte conditionValue = 0;
        for (int i = 0; i < capacity; i++) {
            conditionValue |= 1 << i;
        }

        this.capacity = capacity;
        this.conditionValue = conditionValue;
    }

    public boolean markAndCheckCondition() {
        index++;
        index %= capacity;

        // 0000 1000 or operation
        data |= 1 << index;
        if (data == conditionValue) {
            return true;
        }
        return false;
    }

    public void unmark() {
        index++;
        index %= capacity;

        // 1111 0111 and operation
        data &= conditionValue - (1 << index);
    }

    public boolean checkCondition() {
        if (data == conditionValue) {
            return true;
        }

        return false;
    }

}