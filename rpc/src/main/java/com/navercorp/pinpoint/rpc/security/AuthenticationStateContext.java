/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.rpc.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author Taejin Koo
 */
class AuthenticationStateContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private static final AtomicIntegerFieldUpdater<AuthenticationStateContext> AUTHENTICATION_STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(AuthenticationStateContext.class, "authenticationState");

    static final int INIT = 0;
    static final int IN_PROGRESS = 1;
    static final int SUCCESS = 2;
    static final int FAIL = 3;

    private static Map<Integer, String> STATE_DEBUG_MAP;
    static {
        Map<Integer, String> tempMap = new HashMap<Integer, String>(4);
        tempMap.put(INIT, "INIT");
        tempMap.put(IN_PROGRESS, "IN_PROGRESS");
        tempMap.put(SUCCESS, "SUCCESS");
        tempMap.put(FAIL, "FAIL");

        STATE_DEBUG_MAP = Collections.unmodifiableMap(tempMap);
    }

    private volatile int authenticationState = INIT;

    boolean changeStateProgress() {
        return change0(INIT, IN_PROGRESS);
    }

    boolean changeStateSuccess() {
        return change0(IN_PROGRESS, SUCCESS);
    }

    boolean changeStateFail() {
        return change0(IN_PROGRESS, FAIL);
    }

    private boolean change0(int currentState, int updateState) {

        if (logger.isDebugEnabled()) {
            logger.debug("changeState() started. expect:{} -> update:{}", STATE_DEBUG_MAP.get(currentState), STATE_DEBUG_MAP.get(updateState));
        }
        boolean set = AUTHENTICATION_STATE_UPDATER.compareAndSet(this, currentState, updateState);
        if (!set) {
            if (logger.isDebugEnabled()) {
                logger.debug("failed to change state" +
                        ". (expect:{} -> update:{}. maybe actual:{})", STATE_DEBUG_MAP.get(currentState), STATE_DEBUG_MAP.get(updateState), STATE_DEBUG_MAP.get(authenticationState));
            }
        }
        return set;
    }

    int geState() {
        return authenticationState;
    }

    boolean isInProgress() {
        return authenticationState == IN_PROGRESS;
    }

    boolean isSucceeded() {
        return authenticationState == SUCCESS;
    }

    boolean isFailed() {
        return authenticationState == FAIL;
    }

    // complete state no longer changes state
    boolean isCompleted() {
        final int state = authenticationState;

        if (state == SUCCESS || state == FAIL) {
            return true;
        } else {
            return false;
        }
    }

}
