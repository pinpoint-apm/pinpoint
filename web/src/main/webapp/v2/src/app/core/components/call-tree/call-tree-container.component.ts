import { Component, Input, OnInit, AfterViewInit, OnDestroy, ViewChild, ChangeDetectorRef, ChangeDetectionStrategy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { takeUntil, filter } from 'rxjs/operators';

import {
    StoreHelperService,
    NewUrlStateNotificationService,
    TransactionViewTypeService,
    VIEW_TYPE,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    DynamicPopupService,
    MessageQueueService,
    MESSAGE_TO
} from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { TransactionSearchInteractionService, ISearchParam } from 'app/core/components/transaction-search/transaction-search-interaction.service';
import { IGridData } from './call-tree.component';
import { CallTreeComponent } from './call-tree.component';
import { MessagePopupContainerComponent } from 'app/core/components/message-popup/message-popup-container.component';
import { SyntaxHighlightPopupContainerComponent } from 'app/core/components/syntax-highlight-popup/syntax-highlight-popup-container.component';

@Component({
    selector: 'pp-call-tree-container',
    templateUrl: './call-tree-container.component.html',
    styleUrls: ['./call-tree-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class CallTreeContainerComponent implements OnInit, OnDestroy, AfterViewInit {
    @ViewChild(CallTreeComponent) private callTreeComponent: CallTreeComponent;
    @Input() canSelectRow = false;
    @Input() rowSelection = 'multiple';
    private unsubscribe: Subject<null> = new Subject();
    timezone$: Observable<string>;
    dateFormat$: Observable<string>;
    ratio: number;
    searchSelfTime: number;
    hiddenComponent = true;
    transactionInfo: ITransactionMetaData;
    callTreeOriginalData: ITransactionDetailData;
    callTreeData: IGridData[];
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private transactionSearchInteractionService: TransactionSearchInteractionService,
        private transactionViewTypeService: TransactionViewTypeService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private messageQueueService: MessageQueueService,
    ) {}
    ngOnInit() {
        this.transactionViewTypeService.onChangeViewType$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((viewType: string) => {
            if ( viewType === VIEW_TYPE.CALL_TREE ) {
                this.hiddenComponent = false;
            } else {
                this.hiddenComponent = true;
            }
            this.changeDetectorRef.detectChanges();
        });
        this.connectStore();
        this.transactionSearchInteractionService.onSearch$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((params: ISearchParam) => {
            if (this.hiddenComponent === true) {
                return;
            }
            this.transactionSearchInteractionService.setSearchResult({
                type: params.type,
                query: params.query,
                result: this.callTreeComponent.searchRow(params)
            });
        });
    }
    ngAfterViewInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            if (urlService.hasValue(UrlPathId.SEARCH_ID)) {
                const searchId = urlService.getPathValue(UrlPathId.SEARCH_ID);
                if (searchId !== '' && this.hiddenComponent === false) {
                    this.callTreeComponent.moveRow(searchId);
                }
            }
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    private connectStore(): void {
        this.timezone$ = this.storeHelperService.getTimezone();
        this.dateFormat$ = this.storeHelperService.getDateFormat(this.unsubscribe, 3);
        this.storeHelperService.getTransactionDetailData(this.unsubscribe).pipe(
            filter((transactionDetailInfo: ITransactionDetailData) => {
                return transactionDetailInfo && transactionDetailInfo.transactionId ? true : false;
            })
        ).subscribe((transactionDetailInfo: ITransactionDetailData) => {
            this.ratio = this.calcTimeRatio(transactionDetailInfo.callStack[0][transactionDetailInfo.callStackIndex.begin], transactionDetailInfo.callStack[0][transactionDetailInfo.callStackIndex.end]);
            this.callTreeOriginalData = transactionDetailInfo;
            this.callTreeData  = this.makeGridData(transactionDetailInfo.callStack, transactionDetailInfo.callStackIndex);
            this.changeDetectorRef.detectChanges();
        });
    }
    private calcTimeRatio(begin: number, end: number): number {
        return 100 / (end - begin);
    }
    private makeGridData(callTreeData: any, oIndex: any): IGridData[] {
        const newData = [];
        const parentRef = {};
        for ( let i = 0 ; i < callTreeData.length ; i++ ) {
            const callTree = callTreeData[i];
            const oRow = <IGridData>{};
            parentRef[callTree[oIndex.id]] = oRow;
            this.makeRow(callTree, oIndex, oRow, i);
            if ( callTree[oIndex.parentId] ) {
                const oParentRow = parentRef[callTree[oIndex.parentId]];
                if ( oParentRow.children instanceof Array === false ) {
                    oParentRow['folder'] = true;
                    oParentRow['open'] = true;
                    oParentRow['children'] = [];
                }
                oParentRow.children.push(oRow);
            } else {
                newData.push(oRow);
            }
        }
        return newData;
    }
    private makeRow(callTree: any, oIndex: any, oRow: IGridData, index: number): void {
        oRow['index'] = index;
        oRow['id'] = callTree[oIndex.id];
        oRow['method'] = callTree[oIndex.title];
        oRow['argument'] = callTree[oIndex.arguments];
        oRow['startTime'] = callTree[oIndex.begin];
        oRow['gap'] = callTree[oIndex.gap];
        oRow['exec'] = callTree[oIndex.elapsedTime];
        oRow['execPer'] =  callTree[oIndex.elapsedTime] ? Math.ceil((callTree[oIndex.end] - callTree[oIndex.begin]) * this.ratio) : '';
        oRow['selp'] = callTree[oIndex.executionMilliseconds];
        oRow['selpPer'] = callTree[oIndex.elapsedTime] && callTree[oIndex.executionMilliseconds] ?
            ( Math.floor( callTree[oIndex.executionMilliseconds].replace(/,/gi, '') ) / Math.floor( callTree[oIndex.elapsedTime].replace(/,/gi, '') ) ) * 100
            : 0;
        oRow['clazz'] = callTree[oIndex.simpleClassName];
        oRow['api'] = callTree[oIndex.apiType];
        oRow['agent'] = callTree[oIndex.agent];
        oRow['application'] = callTree[oIndex.applicationName];
        oRow['isMethod'] = callTree[oIndex.isMethod];
        oRow['methodType'] = callTree[oIndex.methodType];
        oRow['hasException'] = callTree[oIndex.hasException];
        oRow['isAuthorized'] = callTree[oIndex.isAuthorized];
        oRow['isFocused'] = callTree[oIndex.isFocused];
        if ( callTree[oIndex.hasChild] === true ) {
            oRow['folder'] = true;
            oRow['open'] = true;
            oRow['children'] = [];
        }
    }
    outSelectFormatting(info: any): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_SQL);
        const nextRowData = this.callTreeOriginalData.callStack[info.index + 1];
        const nextValue = nextRowData[this.callTreeOriginalData.callStackIndex.title];
        let bindValue;

        if (nextRowData && (nextValue === 'SQL-BindValue' || nextValue === 'MONGO-JSON-BindValue')) {
            bindValue = nextRowData[this.callTreeOriginalData.callStackIndex.arguments];
        }

        this.dynamicPopupService.openPopup({
            data: {
                type: info.type,
                originalContents: info.formatText,
                bindValue
            },
            component: SyntaxHighlightPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
    onRowSelected({startTime, application, agent}: IGridData): void {
        if (startTime === 0) {
            return;
        }

        this.messageQueueService.sendMessage({
            to: MESSAGE_TO.CALL_TREE_ROW_SELECT,
            param: [{
                time: startTime,
                applicationId: application,
                agentId: agent
            }]
        });
    }
    onCellDoubleClicked(contents: string): void {
        this.dynamicPopupService.openPopup({
            data: {
                title: 'Contents',
                contents
            },
            component: MessagePopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
