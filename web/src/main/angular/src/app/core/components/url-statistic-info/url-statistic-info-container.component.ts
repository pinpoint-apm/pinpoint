import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Observable, of, Subject } from 'rxjs';
import { catchError, switchMap, takeUntil, tap } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import { MessageQueueService, MESSAGE_TO, NewUrlStateNotificationService } from 'app/shared/services';
import { UrlStatisticInfoDataService } from './url-statistic-info-data.service';
import { UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-url-statistic-info-container',
    templateUrl: './url-statistic-info-container.component.html',
    styleUrls: ['./url-statistic-info-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class UrlStatisticInfoContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    data$: Observable<IUrlStatInfoData[]>;
    emptyMessage$: Observable<string>;
    errorMessage: string;
    showLoading: boolean;
    useDisable: boolean;

    constructor(
        private urlStatisticInfoDataService: UrlStatisticInfoDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private messageQueueService: MessageQueueService,
        private translateService: TranslateService,
        private cd: ChangeDetectorRef
    ) { }

    ngOnInit() {
        this.emptyMessage$ = this.translateService.get('COMMON.NO_DATA');
        this.data$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            tap((urlService: NewUrlStateNotificationService) => {
                // if (!urlService.isValueChanged(UrlPathId.APPLICATION)) {
                    this.useDisable = true;
                // }

                this.showLoading = true;
                this.cd.detectChanges();
            }),
            switchMap((urlService: NewUrlStateNotificationService) => {
                const from = urlService.getStartTimeToNumber();
                const to = urlService.getEndTimeToNumber();
                const applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
                const agentId = urlService.getPathValue(UrlPathId.AGENT_ID) || '';
                const params = {from, to, applicationName, agentId};

                return this.urlStatisticInfoDataService.getData(params).pipe(
                    tap(() => {
                        this.showLoading = false;
                        this.useDisable = false;
                    }),
                    catchError((error: IServerError) => {
                        this.errorMessage = error.message;
                        this.showLoading = false;
                        this.useDisable = false;
                        return of([])
                    }),
                );
            }),
        );
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onSelectUrlInfo(url: string): void {
        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.SELECT_URL_INFO,
            param: url
        })
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
}
