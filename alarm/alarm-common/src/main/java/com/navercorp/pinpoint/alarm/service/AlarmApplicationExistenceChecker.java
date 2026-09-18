/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.alarm.service;

import com.navercorp.pinpoint.alarm.vo.AlarmApplication;

import java.util.Set;

/**
 * Answers whether the application a rule targets still exists.
 *
 * <p>An alarm rule names its target by service, application name and application
 * type rather than by an id, and the registry that owns the application differs per
 * type -- the application index for agent-reported applications, and whatever else a
 * deployment registers for the types it adds. Each registry supplies its own
 * implementation, the same way {@code SmsDispatcher} and {@code EmailDispatcher} are
 * supplied per deployment, so that neither the shared alarm mapper nor any caller has
 * to know which store backs a given type.
 */
public interface AlarmApplicationExistenceChecker {

    /**
     * The application types this checker owns the registry for.
     *
     * <p>Declared rather than answered per type so that two checkers claiming one type
     * is a startup failure, the way two modules claiming one data source code is.
     */
    Set<String> supportedTypes();

    /**
     * Whether the application exists in this checker's registry. Only called for an
     * application whose type is in {@link #supportedTypes()}.
     */
    boolean exists(AlarmApplication application);
}
