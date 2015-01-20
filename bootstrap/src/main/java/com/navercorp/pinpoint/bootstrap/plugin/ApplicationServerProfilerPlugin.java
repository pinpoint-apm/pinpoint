/**
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

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * @author Jongho Moon <jongho.moon@navercorp.com>
 *
 */

public interface ApplicationServerProfilerPlugin extends ProfilerPlugin {
    /**
     * If true, current process is an instance of the application server profiled by this plugin.
     * 
     * FIXME better name?
     */
    public boolean isInstance();

    public ServiceType getServerType();
    
    public String[] getClassPath();
    
    /**
     * If true, this plugin must handle agent lifecycle. 
     * In other words, it have to invoke {@link Agent#start()} and {@link Agent#stop()} 
     */
    public boolean isPinpointAgentLifeCycleController();
}
