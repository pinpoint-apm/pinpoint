/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.web.calltree.span;

import java.util.List;
import java.util.Stack;

/**
 * @author jaehong.kim
 */
public class CallTreeFactory {

    private final Stack<CallStackMock> callStacks = new Stack<CallStackMock>();
    private int nextAsyncId = 0;

    public CallTree get(List<String> events) {
        int prevDepth = 0;
        for (String event : events) {
            int depth = event.length() - 1;
            EventType type = EventType.INTERNAL;
            if (event.startsWith(EventType.REMOTE.getEventCode())) {
                type = EventType.REMOTE;
            } else if (event.startsWith(EventType.ASYNC.getEventCode())) {
                type = EventType.ASYNC;
            } else if (event.startsWith(EventType.END.getEventCode())) {
                type = EventType.END;
            }

            if (callStacks.empty() && type == EventType.REMOTE) {
                callStacks.push(new CallStackMock());
                continue;
            }

            if (prevDepth < depth) {
                push(type);
            } else {
                for (int i = depth; i <= prevDepth; i++) {
                    pop();
                }
                push(type);
            }
            prevDepth = depth;
        }

        while(callStacks.size() > 1) {
            pop();
        }
        final CallStackMock callStack = callStacks.pop();
        while (!callStack.empty()) {
            callStack.pop();
        }
        callStacks.clear();
        return callStack.close();
    }

    private void push(EventType type) {
        if (type == EventType.REMOTE) {
            callStacks.push(new CallStackMock());
            CallStackMock callStack = callStacks.peek();
        } else if (type == EventType.ASYNC) {
            final int asyncId = nextAsyncId++;
            CallStackMock callStack = callStacks.peek();
            callStack.peek().setNextAsyncId(asyncId);
            // add async span
            CallStackMock asyncCallStack = new CallStackMock(true, asyncId);
            callStacks.push(asyncCallStack);
            asyncCallStack.push();
        } else {
            CallStackMock callStack = callStacks.peek();
            callStack.push();
        }
    }

    private void pop() {
        CallStackMock callStack = callStacks.peek();
        if (callStack.empty()) {
            if (callStacks.size() > 1) {
                callStacks.pop();
                callStacks.peek().append(callStack.close());
            }
        } else {
            callStack.pop();
        }
    }
}