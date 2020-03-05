import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject, Observable, forkJoin, merge, of } from 'rxjs';
import { filter, tap, switchMap, catchError, map } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { AnalyticsService, TRACKED_EVENT_LIST, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { RemovableAgentDataService } from './removable-agent-data.service';

enum REMOVE_TYPE {
    APP = 'APP',
    EACH = 'EACH',
    NONE = 'NONE'
}

@Component({
    selector: 'pp-removable-agent-list-container',
    templateUrl: './removable-agent-list-container.component.html',
    styleUrls: ['./removable-agent-list-container.component.css'],
})
export class RemovableAgentListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private _removeType: REMOVE_TYPE = REMOVE_TYPE.NONE;
    private outAgentRemove = new Subject<void>();
    private onAgentRemove$ = this.outAgentRemove.asObservable();

    isOnRemovePhase = false;
    useDisable = false;
    showLoading = false;
    errorMessage: string;
    agentList$: Observable<{[key: string]: any}>;
    selectedApplication: IApplication = null;
    removeTarget: {appName: string, agentId: string};
    i18nText: {[key: string]: string} = {
        select: '',
        cancelButton: '',
        removeButton: '',
        removeAllAgents: '',
        removeAgent: ''
    };

    constructor(
        private translateService: TranslateService,
        private messageQueueService: MessageQueueService,
        private removableAgentDataService: RemovableAgentDataService,
        private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        this.getI18NText();
        this.connectApplicationList();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private connectApplicationList(): void {
        this.agentList$ = merge(
            this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
                filter((app: IApplication) => app !== null),
                tap((app: IApplication) => {
                    this.removeType = REMOVE_TYPE.NONE;
                    this.selectedApplication = app;
                    this.errorMessage = '';
                }),
            ),
            this.onAgentRemove$
        ).pipe(
            tap(() => this.showProcessing()),
            switchMap(() => this.removableAgentDataService.getAgentList(this.selectedApplication.getApplicationName()).pipe(
                map((data: IAgentList) => {
                    return Object.entries(data).reduce((acc: {[key: string]: any}[], [key, value]: [string, IAgent[]]) => {
                        return [...acc, ...value.map((agent: IAgent) => {
                            const {applicationName, hostName, agentId, agentVersion, startTimestamp, ip} = agent;

                            return {applicationName, hostName, agentId, agentVersion, startTimestamp, ip};
                        })];
                    }, []);
                }),
                catchError((error: IServerErrorFormat) => {
                    this.errorMessage = error.exception.message;
                    return of(null);
                }),
                tap(() => this.hideProcessing())
            )),
        );
    }

    private getI18NText(): void {
        forkJoin(
            this.translateService.get('COMMON.SELECT_YOUR_APP'),
            this.translateService.get('COMMON.REMOVE'),
            this.translateService.get('COMMON.CANCEL'),
            this.translateService.get('CONFIGURATION.AGENT_MANAGEMENT.REMOVE_APPLICATION'),
            this.translateService.get('CONFIGURATION.AGENT_MANAGEMENT.REMOVE_AGENT'),
        ).subscribe(([selectApp, removeBtnLabel, cancelBtnLabel, removeApp, removeAgent]: string[]) => {
            this.i18nText.select = selectApp;
            this.i18nText.removeButton = removeBtnLabel;
            this.i18nText.cancelButton = cancelBtnLabel;
            this.i18nText.removeApplication = removeApp;
            this.i18nText.removeAgent = removeAgent;
        });
    }

    private set removeType(type: REMOVE_TYPE) {
        this._removeType = type;
        this.isOnRemovePhase = type !== REMOVE_TYPE.NONE;
    }

    private get removeType(): REMOVE_TYPE {
        return this._removeType;
    }

    onRemoveSelectAgent(agentInfo: {appName: string, agentId: string}): void {
        this.removeTarget = agentInfo;
        this.removeType = REMOVE_TYPE.EACH;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ONE_AGENT_REMOVE_CONFIRM_VIEW);
    }

    onRemoveApplication(): void {
        this.removeType = REMOVE_TYPE.APP;
        // this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_ALL_INACTIVE_AGENTS_REMOVE_CONFIRM_VIEW);
    }

    onRemoveCancel(): void {
        this.removeType = REMOVE_TYPE.NONE;
    }

    onRemoveConfirm(): void {
        this.showProcessing();

        if (this.isAppRemove()) {
            this.removableAgentDataService.removeApplication(this.selectedApplication.getApplicationName()).pipe(
                filter((response: string) => response === 'OK')
            ).subscribe(() => {
                this.messageQueueService.sendMessage({
                    to: MESSAGE_TO.APPLICATION_REMOVED,
                    param: {
                        appName: this.selectedApplication.getApplicationName(),
                        appServiceType: this.selectedApplication.getServiceType(),
                    }
                });
                this.selectedApplication = null;
                this.removeType = REMOVE_TYPE.NONE;
                this.hideProcessing();
            }, (error: IServerErrorFormat) => {
                this.errorMessage = error.exception.message;
                this.hideProcessing();
            });
        } else {
            this.removableAgentDataService.removeAgentId({
                applicationName: this.removeTarget.appName,
                agentId: this.removeTarget.agentId
            }).pipe(
                tap(() => this.analyticsService.trackEvent(TRACKED_EVENT_LIST.REMOVE_ONE_AGENT)),
                filter((response: string) => response === 'OK'),
            ).subscribe(() => {
                this.removeType = REMOVE_TYPE.NONE;
                this.outAgentRemove.next();
            }, (error: IServerErrorFormat) => {
                this.errorMessage = error.exception.message;
                this.hideProcessing();
            });
        }

    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
        this.removeType = REMOVE_TYPE.NONE;
    }

    isAppRemove(): boolean {
        return this.removeType === REMOVE_TYPE.APP;
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
