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

package com.navercorp.pinpoint.bootstrap;

import junit.framework.Assert;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.PinpointURLClassLoader;

import java.net.URL;

/**
 * @author emeroad
 */
public class PinpointURLClassLoaderTest {

    @Test
    public void testOnLoadClass() throws Exception {

        PinpointURLClassLoader cl = new PinpointURLClassLoader(new URL[]{}, Thread.currentThread().getContextClassLoader());
        try {
            cl.loadClass("test");
            Assert.fail();
        } catch (ClassNotFoundException e) {
        }

//        try {
//            cl.loadClass("com.navercorp.pinpoint.profiler.DefaultAgent");
//        } catch (ClassNotFoundException e) {
//
//        }
//      사실 위에 코드로 정상 테스트가 가능해야 겠지만  bootstrap testcase에서는 jar라서 찾을수 없음
//      아래 코드로 로드 하는 클래스인지 체크 정도만 하자.
//      URL에 다가 pinpoint.jar를 걸면 되긴하겠지만. 관리가 힘들듯함.
        Assert.assertTrue(cl.onLoadClass("com.navercorp.pinpoint.profiler.DefaultAgent"));
    }
}
