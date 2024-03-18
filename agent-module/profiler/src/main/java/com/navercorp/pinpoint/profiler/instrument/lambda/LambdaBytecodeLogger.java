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

import com.navercorp.pinpoint.bootstrap.BootLogger;
import com.navercorp.pinpoint.bootstrap.instrument.lambda.LambdaBytecodeHandler;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LambdaBytecodeLogger implements LambdaBytecodeHandler {
    private final BootLogger logger;
    private final LambdaBytecodeHandler delegate;

    public LambdaBytecodeLogger(LambdaBytecodeHandler delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.logger = BootLogger.getLogger(this.getClass());
    }

    @Override
    public byte[] handleLambdaBytecode(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        if (logger.isTraceEnabled()) {
            logger.trace("handleLambdaBytecode "
                    + hostClass.getName() + " "
                    + Arrays.toString(cpPatches)
            );
        }
        return delegate.handleLambdaBytecode(hostClass, data, cpPatches);
    }
}
