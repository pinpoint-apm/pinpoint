import { Component, OnInit, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, forkJoin, Observable } from 'rxjs';
import * as moment from 'moment-timezone';

import { StoreHelperService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { AgentStatisticDataService } from './agent-statistic-data.service';
import { Actions } from 'app/shared/store';
import { map, tap } from 'rxjs/operators';

@Component({
    selector: 'pp-configuration-agent-statistic-container',
    templateUrl: './configuration-agent-statistic-container.component.html',
    styleUrls: ['./configuration-agent-statistic-container.component.css']
})
export class ConfigurationAgentStatisticContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private timezone: string;
    private dateFormat: string;

    loadingData = false;
    useDisable = false;
    showLoading = false;
    hasData$: Observable<boolean>;
    i18nText: {[key: string]: string};
    lastRequestTime: string;

    constructor(
        private translateService: TranslateService,
        private storeHelperService: StoreHelperService,
        private agentStatisticDataService: AgentStatisticDataService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.initI18NText();
        this.connectStore();
        this.hasData$ = this.storeHelperService.getAgentList(this.unsubscribe).pipe(
            tap(() => {
                this.lastRequestTime = moment(this.agentStatisticDataService.getLastRequestTime()).tz(this.timezone).format(this.dateFormat);
            }),
            map((data: IAgentList) => !!data)
        );
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initI18NText(): void {
        forkJoin(
            this.translateService.get('CONFIGURATION.AGENT_STATISTIC.ZOOM_GUIDE'),
            this.translateService.get('CONFIGURATION.AGENT_STATISTIC.LOAD_GUIDE'),
            this.translateService.get('CONFIGURATION.AGENT_STATISTIC.LOADING'),
            this.translateService.get('CONFIGURATION.AGENT_STATISTIC.RELOAD'),
        ).subscribe(([zoomGuide, loadGuide, loadBtnText, reloadBtnText]: string[]) => {
            this.i18nText = {zoomGuide, loadGuide, loadBtnText, reloadBtnText};
        });
    }

    private connectStore(): void {
        this.storeHelperService.getTimezone(this.unsubscribe).subscribe((timezone: string) => {
            this.timezone = timezone;
        });
        this.storeHelperService.getDateFormat(this.unsubscribe, 0).subscribe((dateFormat: string) => {
            this.dateFormat = dateFormat;
        });
    }

    onLoadStart(throughReload = false): void {
        if (!throughReload) {
            this.analyticsService.trackEvent(TRACKED_EVENT_LIST.FETCH_AGENT_STATISTIC_DATA);
        }

        this.showProcessing();
        this.agentStatisticDataService.getData().subscribe((agentList: IAgentList) => {
            this.storeHelperService.dispatch(new Actions.UpdateAdminAgentList(agentList));
            this.hideProcessing();
        }, (error: any) => {
            this.hideProcessing();
        });
    }

    onReload(): void {
        this.onLoadStart(true);
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.RELOAD_AGENT_STATISTIC_DATA);
    }

    private showProcessing(): void {
        this.useDisable = true;
        this.showLoading = true;
    }

    private hideProcessing(): void {
        this.useDisable = false;
        this.showLoading = false;
    }
}
