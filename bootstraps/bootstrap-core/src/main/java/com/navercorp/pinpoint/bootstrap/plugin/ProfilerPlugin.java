/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin;


/**
 * Pinpoint profiler plugin should implement this interface.
 * 
 * When Pinpoint agent initialize, plugins are loaded by the agent, and then their {@link #setup(ProfilerPluginSetupContext)} methods are invoked.
 * 
 * @author Jongho Moon
 *
 */
public interface ProfilerPlugin {
    void setup(ProfilerPluginSetupContext context);
}
