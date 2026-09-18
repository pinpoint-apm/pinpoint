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

import com.navercorp.pinpoint.alarm.vo.AlarmDataSource;

import java.util.List;

/**
 * Contributes the data sources one module brings.
 *
 * <p>One bean per contributing module; {@link AlarmDataSourceRegistry} injects them
 * all and flattens the result, so a deployable that has two modules on its
 * classpath sees both sets without wiring anything itself. Enum constants cannot be
 * beans, which is why this exists rather than injecting
 * {@code List<AlarmDataSource>} directly.
 */
public interface AlarmDataSourceProvider {

    List<AlarmDataSource> dataSources();
}
