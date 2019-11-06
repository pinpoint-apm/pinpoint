/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.common.hbase;

import com.navercorp.pinpoint.common.PinpointConstants;

/**
 * @author Taejin Koo
 */
public final class HbaseTableConstatns {

    public static final int APPLICATION_NAME_MAX_LEN = PinpointConstants.APPLICATION_NAME_MAX_LEN;
    public static final int AGENT_NAME_MAX_LEN = PinpointConstants.AGENT_NAME_MAX_LEN;

    // Time delta (in milliseconds) we can store in each row of AgentStatV2
    public static final int AGENT_STAT_TIMESPAN_MS = 5 * 60 * 1000;

    public static final int APPLICATION_TRACE_INDEX_ROW_DISTRIBUTE_SIZE = 1; // applicationIndex hash size

    public static final int APPLICATION_STAT_TIMESPAN_MS = 5 * 60 * 1000;

}
