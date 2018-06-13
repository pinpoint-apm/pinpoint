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

package com.navercorp.pinpoint.common;

import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.common.service.AnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.DefaultAnnotationKeyRegistryService;
import com.navercorp.pinpoint.common.service.DefaultTraceMetadataLoaderService;
import com.navercorp.pinpoint.common.service.TraceMetadataLoaderService;
import com.navercorp.pinpoint.common.trace.AnnotationKey;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;
import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.common.util.logger.CommonLoggerFactory;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * @author emeroad
 */
public class AnnotationKeyTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void getCode() {
        CommonLoggerFactory loggerFactory = StdoutCommonLoggerFactory.INSTANCE;
        TraceMetadataLoaderService typeLoaderService = new DefaultTraceMetadataLoaderService(Collections.<TraceMetadataProvider>emptyList(), loggerFactory);
        AnnotationKeyRegistryService annotationKeyRegistryService = new DefaultAnnotationKeyRegistryService(typeLoaderService, loggerFactory);

        AnnotationKey annotationKey = annotationKeyRegistryService.findAnnotationKey(AnnotationKey.API.getCode());
        Assert.assertEquals(annotationKey, AnnotationKey.API);
    }

//    @Test
    public void intSize() {
//        2147483647
        logger.debug("{}", Integer.MAX_VALUE);
//        -2147483648
        logger.debug("{}", Integer.MIN_VALUE);
    }

    @Test
    public void isArgsKey() {
        Assert.assertTrue(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGS0.getCode()));
        Assert.assertTrue(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGSN.getCode()));
        Assert.assertTrue(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGS5.getCode()));

        Assert.assertFalse(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGS0.getCode() +1));
        Assert.assertFalse(AnnotationKeyUtils.isArgsKey(AnnotationKey.ARGSN.getCode() -1));
        Assert.assertFalse(AnnotationKeyUtils.isArgsKey(Integer.MAX_VALUE));
        Assert.assertFalse(AnnotationKeyUtils.isArgsKey(Integer.MIN_VALUE));

    }

    @Test
    public void isCachedArgsToArgs() {
        int i = AnnotationKeyUtils.cachedArgsToArgs(AnnotationKey.CACHE_ARGS0.getCode());
        Assert.assertEquals(i, AnnotationKey.ARGS0.getCode());
    }
    
    @Test
    public void testValueOf() {
        CommonLoggerFactory loggerFactory = StdoutCommonLoggerFactory.INSTANCE;
        TraceMetadataLoaderService typeLoaderService = new DefaultTraceMetadataLoaderService(Collections.<TraceMetadataProvider>emptyList(), loggerFactory);
        AnnotationKeyRegistryService annotationKeyRegistryService = new DefaultAnnotationKeyRegistryService(typeLoaderService, loggerFactory);
        annotationKeyRegistryService.findAnnotationKeyByName(AnnotationKey.ARGS0.getName());

        AnnotationKey valueof = annotationKeyRegistryService.findAnnotationKeyByName(AnnotationKey.ARGS0.getName());
        Assert.assertSame(AnnotationKey.ARGS0, valueof);
    }
}
