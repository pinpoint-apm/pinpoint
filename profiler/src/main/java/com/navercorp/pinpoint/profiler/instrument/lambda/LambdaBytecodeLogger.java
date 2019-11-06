/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.instrument.lambda;

import com.navercorp.pinpoint.bootstrap.instrument.lambda.LambdaBytecodeHandler;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LambdaBytecodeLogger implements LambdaBytecodeHandler {
    private final Logger logger;
    private final LambdaBytecodeHandler delegate;

    public LambdaBytecodeLogger(LambdaBytecodeHandler delegate) {
        this.delegate = Assert.requireNonNull(delegate, "delegate");
        this.logger = LoggerFactory.getLogger(delegate.getClass());
    }

    @Override
    public byte[] handleLambdaBytecode(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        if (logger.isDebugEnabled()) {
            logger.debug("handleLambdaBytecode {} {}", hostClass, Arrays.toString(cpPatches));
        }
        return delegate.handleLambdaBytecode(hostClass, data, cpPatches);
    }
}
