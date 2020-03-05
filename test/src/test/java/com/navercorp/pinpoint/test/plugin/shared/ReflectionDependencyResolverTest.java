/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.test.plugin.shared;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.List;


/**
 * TODO : Test fails in Travis
 * @author Woonduk Kang(emeroad)
 */
@Ignore
public class ReflectionDependencyResolverTest {

    @Test
    public void get() throws Exception {
        List<File> files = ReflectionDependencyResolver.get("org.slf4j:slf4j-api:1.7.21");
        Assert.assertEquals(files.size(), 1);
    }
}