/*
 * Copyright 2025 NAVER Corp.
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

package org.junit.jupiter.engine.descriptor;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.engine.EngineExecutionListener;

public final class ExtensionContextFactory {

    public static ExtensionContext jupiterEngineContext(EngineExecutionListener engineExecutionListener,
                                                        JupiterEngineDescriptor testDescriptor,
                                                        JupiterConfiguration configuration,
                                                        ExtensionRegistry extensionRegistry) {
        return new JupiterEngineExtensionContext(engineExecutionListener, testDescriptor, configuration, extensionRegistry);
    }

}
