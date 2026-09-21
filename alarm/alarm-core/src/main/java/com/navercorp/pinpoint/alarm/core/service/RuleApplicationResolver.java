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

import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.common.server.bo.Application;

/**
 * The application a rule is written against.
 *
 * <p>All three parts of it have to come off the rule. The map stores its histograms under a
 * service uid as well as an application name and type, so resolving only the type and letting
 * the service default reads a different application than the rule names -- one that answers
 * with an empty histogram rather than with an error.
 *
 * <p>Resolved rather than stored, because a rule names its service and type the way an
 * operator picked them, and what those names map to is decided by this installation: the
 * service registry it keeps and the plugins it loaded.
 */
public interface RuleApplicationResolver {

    Application resolve(AlarmRuleV2 rule);
}
