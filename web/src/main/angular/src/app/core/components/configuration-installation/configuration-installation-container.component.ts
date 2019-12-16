import { Component, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, combineLatest, of } from 'rxjs';
import { filter, catchError, pluck, tap } from 'rxjs/operators';

import { ApplicationNameDuplicationCheckInteractionService } from 'app/core/components/duplication-check/application-name-duplication-check-interaction.service';
import { AgentIdDuplicationCheckInteractionService } from 'app/core/components/duplication-check/agent-id-duplication-check-interaction.service';
import { ConfigurationInstallationDataService, IInstallationData } from './configuration-installation-data.service';
import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-installation-container',
    templateUrl: './configuration-installation-container.component.html',
    styleUrls: ['./configuration-installation-container.component.css'],
})
export class ConfigurationInstallationContainerComponent implements OnInit {
    desc$: Observable<string>;
    installationInfo$: Observable<object>;
    jvmArgument$: Observable<string[]>;
    showLoading = true;

    constructor(
        private translateService: TranslateService,
        private configurationInstallationDataService: ConfigurationInstallationDataService,
        private applicationNameDuplicationCheckInteractionService: ApplicationNameDuplicationCheckInteractionService,
        private agentIdDuplicationCheckInteractionService: AgentIdDuplicationCheckInteractionService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.initDescText();
        this.initInstallationInfo();
        this.initJVMArgument();
    }

    private initDescText(): void {
        this.desc$ = this.translateService.get('CONFIGURATION.INSTALLATION.DESC');
    }

    private initInstallationInfo(): void {
        this.installationInfo$ = this.configurationInstallationDataService.getData()
            .pipe(
                filter((data: IInstallationData) => {
                    return data.code === 0;
                }),
                pluck('message'),
                catchError((err) => {
                    return this.onAjaxError(err);
                }),
                tap(() => this.showLoading = false)
            );
    }

    private initJVMArgument(): void {
        this.jvmArgument$ = combineLatest(
            this.applicationNameDuplicationCheckInteractionService.onCheckSuccess$,
            this.agentIdDuplicationCheckInteractionService.onCheckSuccess$,
        );
    }

    private onAjaxError(err: Error): Observable<any> {
        // TODO: Error발생시 띄워줄 팝업 컴포넌트 Call - issue#170
        return of({
            downloadUrl: '',
            installationArgument: ''
        });
    }

    onLinkClick(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_DOWNLOAD_LINK);
    }
}
