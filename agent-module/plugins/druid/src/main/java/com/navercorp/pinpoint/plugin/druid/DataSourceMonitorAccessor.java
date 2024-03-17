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
package com.navercorp.pinpoint.plugin.druid;

/**
 * The interface Data source monitor accessor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/07/21
 */
public interface DataSourceMonitorAccessor {

    /**
     * Pinpoint set data source monitor.
     *
     * @param dataSourceMonitor the data source monitor
     */
    void _$PINPOINT$_setDataSourceMonitor(DruidDataSourceMonitor dataSourceMonitor);

    /**
     * Pinpoint get data source monitor druid data source monitor.
     *
     * @return the druid data source monitor
     */
    DruidDataSourceMonitor _$PINPOINT$_getDataSourceMonitor();
}