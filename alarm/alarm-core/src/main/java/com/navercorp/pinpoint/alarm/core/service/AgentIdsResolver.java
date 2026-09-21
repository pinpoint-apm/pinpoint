/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.core.service;

import com.navercorp.pinpoint.common.server.bo.Application;
import com.navercorp.pinpoint.common.timeseries.time.Range;

import java.util.List;

/**
 * The agents of an application.
 *
 * <p>A rule names an application, but some of what it can measure is stored per agent under a
 * key that cannot be scanned by application, so the agents have to be listed before they can be
 * read. Which registry answers that differs by deployment, so it is passed in rather than read
 * here.
 *
 * <p>The window is the rule's own, so an implementation may answer with the agents that were
 * alive during it rather than with every agent ever registered. An implementation is free to
 * ignore it -- and one that measures events rather than usage should, because an agent that
 * went quiet is not the same as an agent with nothing wrong.
 */
public interface AgentIdsResolver {

    List<String> resolve(Application application, Range window);
}
