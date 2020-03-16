/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.fastjson;

import com.alibaba.fastjson.JSON;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * The type Fastjson it.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/07/17
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({"com.alibaba:fastjson:[1.2.10],[1.2.20],[1.2.30],[1.2.40,)"})
@ImportPlugin("com.navercorp.pinpoint:pinpoint-fastjson-plugin")
@PinpointConfig("fastjson/pinpoint-fastjson-test.config")
public class FastjsonIT {

    private static final String serviceType = "FASTJSON";
    private static final String annotationKeyName = "fastjson.json.length";

    /**
     * Test.
     *
     * @throws Exception the exception
     */
    @Test
    public void test() throws Exception {

        TestBean testBean1 = new TestBean();
        testBean1.setId(123);
        testBean1.setName("abc");

        String json = JSON.toJSONString(testBean1);

        Method toJSONString = JSON.class.getDeclaredMethod("toJSONString", Object.class);

        TestBean testBean2 = JSON.parseObject(json, TestBean.class);

        Method parseObject = JSON.class.getDeclaredMethod("parseObject", String.class, Class.class);

        Assert.assertEquals(testBean1.getId(), testBean2.getId());
        Assert.assertEquals(testBean1.getName(), testBean2.getName());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event(serviceType, toJSONString, annotation(annotationKeyName, json.length())));

        verifier.verifyTrace(event(serviceType, parseObject, annotation(annotationKeyName, json.length())));

        // No more traces
        verifier.verifyTraceCount(0);
    }

    /**
     * The type Test bean.
     */
    static class TestBean {

        private int id;
        private String name;

        /**
         * Gets id.
         *
         * @return the id
         */
        public int getId() {
            return id;
        }

        /**
         * Sets id.
         *
         * @param id the id
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * Gets name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets name.
         *
         * @param name the name
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}
