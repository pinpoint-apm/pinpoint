import { Component, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { Actions } from 'app/shared/store';
import { StoreHelperService } from 'app/shared/services';
import { InspectorChartListDataService, SOURCE_TYPE } from '../inspector-chart-list/inspector-chart-list-data.service';

@Component({
    selector: 'pp-configuration-inspector-chart-manager-container',
    templateUrl: './configuration-inspector-chart-manager-container.component.html',
    styleUrls: ['./configuration-inspector-chart-manager-container.component.css']
})
export class ConfigurationInspectorChartManagerContainerComponent implements OnInit {
    private unsubscribe: Subject<null> = new Subject();
    constructor(
        private storeHelperService: StoreHelperService,
        private inspectorChartListDataService: InspectorChartListDataService
    ) {}
    ngOnInit() {
        this.inspectorChartListDataService.getChartLayoutInfo(SOURCE_TYPE.APPLICATION_INSPECTOR).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((applicationData: {[key: string]: IChartLayoutInfo[]}) => {
            this.storeHelperService.dispatch(new Actions.UpdateApplicationInspectorChartLayout(applicationData));
        }, (error: IServerErrorFormat) => {
            this.storeHelperService.dispatch(new Actions.UpdateApplicationInspectorChartLayout({
                applicationInspectorChart: []
            }));
        });
        this.inspectorChartListDataService.getChartLayoutInfo(SOURCE_TYPE.AGENT_INSPECTOR).pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((agentData: {[key: string]: IChartLayoutInfo[]}) => {
            this.storeHelperService.dispatch(new Actions.UpdateAgentInspectorChartLayout(agentData));
        }, (error: IServerErrorFormat) => {
            this.storeHelperService.dispatch(new Actions.UpdateAgentInspectorChartLayout({
                applicationInspectorChart: []
            }));
        });
    }
}
