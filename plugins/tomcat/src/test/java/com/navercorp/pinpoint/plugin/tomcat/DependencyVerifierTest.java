/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.plugin.jboss.JbossConfig;
import com.navercorp.pinpoint.plugin.jboss.JbossConstants;
import com.navercorp.pinpoint.plugin.jboss.JbossDetector;

import org.junit.Test;

/**
 * @author Taejin Koo
 */
public class DependencyVerifierTest {

//    If the location or content of the classes that are located in JBoss plugin used during the test changes,
//    must check the shade setting of tomcat-plugin/pom.xml.
    @Test
    public void jbossDependencyTest() {
        Class<JbossConfig> jbossConfigClazz = JbossConfig.class;
        Class<JbossConstants> jbossConstantsClazz = JbossConstants.class;
        Class<JbossDetector> jbossDetectorClazz = JbossDetector.class;
    }

}
