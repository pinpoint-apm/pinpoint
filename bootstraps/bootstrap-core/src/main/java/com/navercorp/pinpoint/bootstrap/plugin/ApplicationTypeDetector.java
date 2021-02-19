/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.resolver.ConditionProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;


/**
 * @author Jongho Moon
 *
 * @deprecated As of 1.9.0, application type detection timing has been changed.
 *             Set the application's {@code ServiceType} directly during each plugin's setup time.
 */
@Deprecated
public interface ApplicationTypeDetector {
    
    /**
     * Returns the {@link ServiceType} representing the current plugin, 
     * with code in a range corresponding to {@link com.navercorp.pinpoint.common.trace.ServiceTypeCategory#SERVER}
     * 
     * @return the {@link ServiceType} representing the current plugin
     * @see ServiceType#isWas()
     * @see com.navercorp.pinpoint.common.trace.ServiceTypeCategory#SERVER
     */
    ServiceType getApplicationType();
    
    /**
     * Checks whether the provided conditions satisfy the requirements given by the plugins implementing this class.
     * 
     * <p>This method allows the agent to go through each of the registered plugins with classes implementing this interface,
     * checking whether the execution environment satisfies the requirements specified in them, returning <tt>true</tt> if the
     * requirements are satisfied.
     * 
     * @param provider conditions provided by the current application
     * @return <tt>true</tt> if the provided conditions satisfy the requirements, <tt>false</tt> if otherwise
     * @see ConditionProvider
     */
    boolean detect(ConditionProvider provider);
}
