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
package com.navercorp.pinpoint.plugin.jboss;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

/**
 * The Class JbossConstants.
 *
 * @author <a href="mailto:suraj.raturi89@gmail.com">Suraj Raturi</a>
 * @author jaehong.kim
 */
public final class JbossConstants {
    private JbossConstants() {
    }

    /** The Constant JBOSS. */
    public static final ServiceType JBOSS = ServiceTypeFactory.of(1040, "JBOSS", RECORD_STATISTICS);

    /** The Constant JBOSS_METHOD. */
    public static final ServiceType JBOSS_METHOD = ServiceTypeFactory.of(1041, "JBOSS_METHOD");
}
