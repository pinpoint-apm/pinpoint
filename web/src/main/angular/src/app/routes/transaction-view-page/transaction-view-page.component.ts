import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector, ViewChild, AfterViewInit } from '@angular/core';
import { Subject, Observable, of } from 'rxjs';
import { take, takeUntil, switchMap } from 'rxjs/operators';
import { SplitComponent } from 'angular-split';

import {
    StoreHelperService,
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    TransactionDetailDataService,
    DynamicPopupService,
    GutterEventService
} from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { UrlPathId } from 'app/shared/models';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { IOutputData } from 'angular-split/lib/interface';

@Component({
    selector: 'pp-transaction-view-page',
    templateUrl: './transaction-view-page.component.html',
    styleUrls: ['./transaction-view-page.component.css'],
})
export class TransactionViewPageComponent implements OnInit, OnDestroy, AfterViewInit {
    @ViewChild('splitElem', {static: false}) splitElem: SplitComponent;
    private unsubscribe = new Subject<void>();

    splitSize: number[];

    constructor(
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private transactionDetailDataService: TransactionDetailDataService,
        private gutterEventService: GutterEventService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
    ) { }

    ngOnInit() {
        this.initSplitRatio();
        this.initTransactionViewInfo();
    }

    ngAfterViewInit() {
        this.splitElem.dragProgress$.subscribe(({sizes}: IOutputData) => {
            this.gutterEventService.resizedGutter(sizes as number[]);
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initSplitRatio(): void {
        this.gutterEventService.onGutterResized$.pipe(
            take(1)
        ).subscribe((splitSize: number[]) => this.splitSize = splitSize);
    }

    private initTransactionViewInfo(): void {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
            switchMap((urlService: NewUrlStateNotificationService) => {
                return this.transactionDetailDataService.getData(
                    urlService.getPathValue(UrlPathId.AGENT_ID),
                    urlService.getPathValue(UrlPathId.SPAN_ID),
                    urlService.getPathValue(UrlPathId.TRACE_ID),
                    urlService.getPathValue(UrlPathId.FOCUS_TIMESTAMP)
                );
            })
        ).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.storeHelperService.dispatch(new Actions.UpdateTransactionDetailData(transactionDetailInfo));
        }, (error: IServerErrorFormat) => {
            this.dynamicPopupService.openPopup({
                data: {
                    title: 'Error',
                    contents: error
                },
                component: ServerErrorPopupContainerComponent,
                onCloseCallback: () => {
                    this.urlRouteManagerService.reload();
                }
            }, {
                resolver: this.componentFactoryResolver,
                injector: this.injector
            });
        });
    }

    onAjaxError(err: Error): Observable<any> {
        // TODO: Error발생시 띄워줄 팝업 컴포넌트 Call - issue#170
        return of({});
    }

    onGutterResized({sizes}: {sizes: number[]}): void {
        this.gutterEventService.resizedGutter(sizes);
    }
}
