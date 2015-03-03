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
public class SocketStateChangeResult {

    private final boolean isChange;
    
    private final SocketStateCode beforeState;
    private final SocketStateCode currentState;
    private final SocketStateCode updateWantedState;
    
    public SocketStateChangeResult(boolean isChange, SocketStateCode beforeState, SocketStateCode currentState, SocketStateCode updateWantedState) {
        this.isChange = isChange;
        this.beforeState = beforeState;
        this.currentState = currentState;
        this.updateWantedState = updateWantedState;
    }

    public boolean isChange() {
        return isChange;
    }

    public SocketStateCode getBeforeState() {
        return beforeState;
    }

    public SocketStateCode getCurrentState() {
        return currentState;
    }

    public SocketStateCode getUpdateWantedState() {
        return updateWantedState;
    }
    
    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder();
        toString.append("Socket state change ");
        
        if (isChange) {
            toString.append("success");
        } else {
            toString.append("fail");
        }
        
        toString.append("(updateWanted:");
        toString.append(updateWantedState);

        toString.append(" ,before:");
        toString.append(beforeState);
        
        toString.append(" ,current:");
        toString.append(currentState);
        
        toString.append(").");
        
        return toString.toString();
    }

}