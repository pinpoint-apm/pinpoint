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
