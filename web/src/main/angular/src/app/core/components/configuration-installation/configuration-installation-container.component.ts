import { Component, ComponentFactoryResolver, Injector, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, combineLatest, of } from 'rxjs';
import { filter, catchError, pluck, tap } from 'rxjs/operators';

import { ApplicationNameDuplicationCheckInteractionService } from 'app/core/components/duplication-check/application-name-duplication-check-interaction.service';
import { AgentIdDuplicationCheckInteractionService } from 'app/core/components/duplication-check/agent-id-duplication-check-interaction.service';
import { ConfigurationInstallationDataService, IInstallationData } from './configuration-installation-data.service';
import { AnalyticsService, DynamicPopupService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';

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
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
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
                catchError((error: IServerError) => {
                    this.dynamicPopupService.openPopup({
                        data: {
                            title: 'Error',
                            contents: error
                        },
                        component: ServerErrorPopupContainerComponent
                    }, {
                        resolver: this.componentFactoryResolver,
                        injector: this.injector
                    });

                    return of({
                        downloadUrl: '',
                        installationArgument: ''
                    });
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

    onLinkClick(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_DOWNLOAD_LINK);
    }
}
