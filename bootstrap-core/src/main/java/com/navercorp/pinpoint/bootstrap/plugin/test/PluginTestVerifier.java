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
package com.navercorp.pinpoint.bootstrap.plugin.test;

import java.io.PrintStream;
import java.util.List;

import com.navercorp.pinpoint.common.trace.LoggingInfo;



/**
 * @author Jongho Moon
 *
 */
public interface PluginTestVerifier {
    public void verifyServerType(String expected);
    public void verifyServerInfo(String expected);
    public void verifyConnector(String protocol, int port);
    public void verifyService(String context, List<String> libs);
    public void verifyTraceCount(int expected);
    public void verifyTrace(ExpectedTrace... expectations);
    public void verifyDiscreteTraceBlock(ExpectedTrace... expectations);
    public void ignoreServiceType(String... serviceTypes);
    public void printCache(PrintStream out);
    public void printCache();
    public void initialize(boolean initializeTraceObject);
    public void cleanUp(boolean detachTraceObject);
    public void verifyIsLoggingTransactionInfo(LoggingInfo loggingInfo);
}
