import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import * as dot from 'dot-object';

export enum ExperimentalConfigurationLocalStorageKey {
    enableServerSideScanForScatter = 'scatterScan',
    useStatisticsAgentState = 'statisticsAgentState',
    enableServerMapRealTime = 'serverMapRealTime',
    sampleScatter = 'scatterSampling',
    useScatterChartV2 = 'useScatterChartV2',
    updateServerMapLayoutManually = 'updateServerMapLayoutManually',
}

type ExperimentalConfigMetaItem = {
    value: boolean;
    description: string;
}

enum ClientExperimentalConfig {
    useScatterChartV2 = 'useScatterChartV2',
    updateServerMapLayoutManually = 'updateServerMapLayoutManually'
}

export type ExperimentalConfigurationKeyType = keyof typeof ExperimentalConfigurationLocalStorageKey
export type ExperimentalConfiguration = Record<ExperimentalConfigurationKeyType, boolean>;
export type ExperimentalConfigurationMeta = Record<ExperimentalConfigurationKeyType, ExperimentalConfigMetaItem>;

interface ISystemConfigurationWithExperimental extends ISystemConfiguration {
    experimental?: ExperimentalConfigurationMeta
}

@Injectable()
export class SystemConfigurationDataService {
    private url = 'configuration.pinpoint';
    private defaultConfiguration: ISystemConfigurationWithExperimental = {
        editUserInfo: false,
        enableServerMapRealTime: false,
        openSource: true,
        sendUsage: true,
        showActiveThread: false,
        showActiveThreadDump: false,
        showApplicationStat: false,
        webhookEnable: false,
        version: '',
        userId: '',
        userName: '',
        userDepartment: '',
        showSystemMetric: false,
        showUrlStat: false
    };

    constructor(
        private http: HttpClient
    ) {}

    getClientExperimentalConfiguration(): Record<ClientExperimentalConfig, ExperimentalConfigMetaItem> {
        return {
            useScatterChartV2: {
                description: 'Use ScatterChart v2.',
                value: false
            },
            updateServerMapLayoutManually: {
                description: 'Update server-map layout manually.',
                value: false
            }
        };
    }

    getConfiguration(): Observable<ISystemConfigurationWithExperimental> {
        return this.http.get<ISystemConfigurationWithExperimental>(this.url).pipe(
            map(res => {
                if (res) {
                    const clientExperimantalConfigure = this.getClientExperimentalConfiguration();
                    const generatedConfiguration = dot.object(res) as ISystemConfigurationWithExperimental;

                    return { ...generatedConfiguration, experimental: { ...generatedConfiguration.experimental, ...clientExperimantalConfigure }};
                } else {
                    return this.defaultConfiguration;
                }
            })
        );
    }
}
