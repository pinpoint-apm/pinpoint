/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.interaction.trace.decision;

/**
 * @author yjqg6666
 */
@SuppressWarnings("unused")
public class TraceDecisionMakerHolder {

    private static final TraceDecisionMaker DEFAULT_DECISION_MAKER = new DefaultDecisionMaker();

    private static TraceDecisionMaker maker = DEFAULT_DECISION_MAKER;

    private TraceDecisionMakerHolder(){
    }

    public static void setTraceDecisionMaker(TraceDecisionMaker decisionMaker) {
        maker = decisionMaker == null ? DEFAULT_DECISION_MAKER : decisionMaker;
    }

    public static TraceDecisionMaker getTraceDecisionMaker() {
        return maker;
    }

    public static void clearTraceDecisionMaker() {
        maker = DEFAULT_DECISION_MAKER;
    }

}
