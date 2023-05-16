import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { combineLatest, Observable, of, Subject } from 'rxjs';
import { catchError, startWith, switchMap, takeUntil, tap } from 'rxjs/operators';
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
    private changeCount = new Subject<number>();
    private clickSort = new Subject<{orderby: string, isDesc: boolean}>();
    private onChangeCount$ = this.changeCount.asObservable();
    private onClickSort$ = this.clickSort.asObservable();

    data$: Observable<IUrlStatInfoData[]>;
    countList = [50, 100, 150, 200];
    selectedCount: number = this.countList[0];
    sortStatus = {
        orderby: 'totalCount',
        isDesc: true
    };

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
    ) {}

    ngOnInit() {
        this.emptyMessage$ = this.translateService.get('COMMON.NO_DATA');

        this.data$ = combineLatest([
            this.newUrlStateNotificationService.onUrlStateChange$,
            this.onChangeCount$.pipe(
                startWith(this.selectedCount)
            ),
            this.onClickSort$.pipe(
                startWith(this.sortStatus)
            )
        ]).pipe(
            takeUntil(this.unsubscribe),
            tap(() => {
                this.useDisable = true;
                this.showLoading = true;
                this.cd.detectChanges();
            }),
            switchMap(([urlService, count, {orderby, isDesc}]: [NewUrlStateNotificationService, number, {orderby: string, isDesc: boolean}]) => {
                const from = urlService.getStartTimeToNumber();
                const to = urlService.getEndTimeToNumber();
                const applicationName = urlService.getPathValue(UrlPathId.APPLICATION).getApplicationName();
                const agentId = urlService.getPathValue(UrlPathId.AGENT_ID) || '';
                const params = {from, to, applicationName, agentId, count, orderby, isDesc};

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

    onSelectionChange(count: number): void {
        this.changeCount.next(count);
    }

    onClickSort(sortStatus: {orderby: string, isDesc: boolean}): void {
        this.sortStatus = sortStatus;
        this.clickSort.next(sortStatus);
    }

    onCloseErrorMessage(): void {
        this.errorMessage = '';
    }
}
