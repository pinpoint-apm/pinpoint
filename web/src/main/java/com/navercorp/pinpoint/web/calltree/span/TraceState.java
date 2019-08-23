/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.web.calltree.span;

/**
 * @author Woonduk Kang(emeroad)
 */
public class TraceState {

    public enum State {
        INIT(-1, "Init"),
        // not matched
        ERROR(0, "Error"),
        // transaction completed successfully
        COMPLETE(1,"Complete"),
        // transaction in-flight or missing data
        PROGRESS(2, "Progress");

        private int code;
        private String message;

        State(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    private State state = State.INIT;

    private void update(final State matchType) {
        if (this.state == State.INIT) {
            this.state = matchType;
        }
    }

    public State getState() {
        return state;
    }

    public void progress() {
        update(State.PROGRESS);
    }

    public void complete() {
        update(State.COMPLETE);
    }

}
