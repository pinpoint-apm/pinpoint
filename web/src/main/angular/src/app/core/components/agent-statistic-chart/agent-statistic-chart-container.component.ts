import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, Observable } from 'rxjs';
import { PrimitiveArray } from 'billboard.js';
import { map, filter } from 'rxjs/operators';

import { StoreHelperService } from 'app/shared/services';
import { isEmpty } from 'app/core/utils/util';

@Component({
    selector: 'pp-agent-statistic-chart-container',
    templateUrl: './agent-statistic-chart-container.component.html',
    styleUrls: ['./agent-statistic-chart-container.component.css']
})
export class AgentStatisticChartContainerComponent implements OnInit {
    private unsubscribe = new Subject<void>();

    chartData$: Observable<{[key: string]: PrimitiveArray[]}>;
    emptyText$: Observable<string>;
    agentCount = 0;

    constructor(
        private translateService: TranslateService,
        private storeHelperService: StoreHelperService,
    ) {}

    ngOnInit() {
        this.emptyText$ = this.translateService.get('COMMON.NO_DATA');
        this.chartData$ = this.storeHelperService.getAgentList(this.unsubscribe).pipe(
            filter((data: IAgentList) => !!data),
            map((data: IAgentList) => this.makeChartData(data))
        );
    }

    private makeChartData(data: IAgentList): {[key: string]: PrimitiveArray[]} {
        if (isEmpty(data)) {
            return {
                jvm: [],
                agent: []
            };
        }
        /**
         * ex) dateObj의 형태:
         * {
         *    jvm = {
         *      v1: 3,
         *      v2: 2,
         *      v3: 4
         *    },
         *    agent = {
         *      v1: 4,
         *      v2: 5,
         *      v3: 6
         *    }
         * }
         */
        let count = 0;
        const dataObj = Object.keys(data).reduce((acc1: {[key: string]: any}, appKey: string) => {
            const app = data[appKey];

            return app.reduce((acc2: {[key: string]: any}, {agentVersion, jvmInfo}: IAgent) => {
                count++;
                if (agentVersion) {
                    acc2['agent'][agentVersion] = (acc2['agent'][agentVersion] || 0) + 1;
                }
                if (jvmInfo && jvmInfo.jvmVersion) {
                    acc2['jvm'][jvmInfo.jvmVersion] = (acc2['jvm'][jvmInfo.jvmVersion] || 0) + 1;
                } else {
                    acc2['jvm']['UNKNOWN'] = (acc2['jvm']['UNKNOWN'] || 0) + 1;
                }

                return acc2;
            }, acc1);
        }, {jvm: {}, agent: {}}) as {[key: string]: {[key: string]: number}};

        const sortedDataObj = Object.entries(dataObj).reduce((acc1: {[key: string]: Map<string, number>}, [key, valueObj]: [string, {[key: string]: number}]) => {
            const sortedChildMap = new Map<string, number>(Object.entries(valueObj).sort(([k1], [k2]) => k1 < k2 ? -1 : 1));

            return {...acc1, [key]: sortedChildMap};
        }, {});

        this.agentCount = count;

        return {
            jvm: [['x', ...sortedDataObj['jvm'].keys()], ['jvm', ...sortedDataObj['jvm'].values()]],
            agent: [['x', ...sortedDataObj['agent'].keys()], ['agent', ...sortedDataObj['agent'].values()]]
        };
    }
}
