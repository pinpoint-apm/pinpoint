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
package com.navercorp.pinpoint.bootstrap.plugin.test;

import java.io.PrintStream;
import java.util.List;

import com.navercorp.pinpoint.common.trace.LoggingInfo;



/**
 * @author Jongho Moon
 *
 */
public interface PluginTestVerifier {
    void verifyServerType(String expected);
    void verifyServerInfo(String expected);
    void verifyConnector(String protocol, int port);
    void verifyService(String context, List<String> libs);
    void verifyTraceCount(int expected);
    void verifyTrace(ExpectedTrace... expectations);
    void verifyDiscreteTrace(ExpectedTrace... expectations);
    void ignoreServiceType(String... serviceTypes);
    void printCache(PrintStream out);
    void printCache();
    void initialize(boolean initializeTraceObject);
    void cleanUp(boolean detachTraceObject);
    void verifyIsLoggingTransactionInfo(LoggingInfo loggingInfo);
}
