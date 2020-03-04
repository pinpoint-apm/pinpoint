/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.navercorp.pinpoint.common.server.bo.event;

/**
 * @author jaehong.kim - Copy TThreadState
 */
public enum ThreadState {
    NEW(0),
    RUNNABLE(1),
    BLOCKED(2),
    WAITING(3),
    TIMED_WAITING(4),
    TERMINATED(5),
    UNKNOWN(6);

    private final int value;

    ThreadState(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static ThreadState findByValue(int value) {
        switch (value) {
            case 0:
                return NEW;
            case 1:
                return RUNNABLE;
            case 2:
                return BLOCKED;
            case 3:
                return WAITING;
            case 4:
                return TIMED_WAITING;
            case 5:
                return TERMINATED;
            case 6:
                return UNKNOWN;
            default:
                return null;
        }
    }
}