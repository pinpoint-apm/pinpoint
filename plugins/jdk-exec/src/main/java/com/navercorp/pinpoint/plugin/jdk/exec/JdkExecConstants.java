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
 *
 */
package com.navercorp.pinpoint.plugin.jdk.exec;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;

/**
 * @author hamlet-lee
 */
public class JdkExecConstants {
    private JdkExecConstants() {
    }
    public static final String ASYNC_ID_MAP = "AsyncIdMap";
    public static final ServiceType SERVICE_TYPE = ServiceTypeFactory.of(7500, "JDK_EXEC", "JDK_EXEC");
}
