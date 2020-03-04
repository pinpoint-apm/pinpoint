import { Injectable } from '@angular/core';
import { Subject, BehaviorSubject, Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService, StoreHelperService } from 'app/shared/services';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';

export enum SOURCE_TYPE {
    MAIN = 'MAIN',
    FILTERED = 'FILTERED_MAP',
    TRANSACTION_VIEW = 'TRANSACTION_VIEW',
    TRANSACTION_LIST = 'TRANSACTION_LIST',
}

export interface IServerMapNotificationData {
    baseApplication: string;
    serverMapData: ServerMapData;
    hidden: boolean;
    error: boolean;
}

@Injectable()
export class ServerMapChangeNotificationService {
    private unsubscribe: Subject<null> = new Subject();
    private outServerMapDataInMain: Subject<IServerMapNotificationData> = new Subject();
    private outServerMapDataInFilteredMap: Subject<IServerMapNotificationData> = new Subject();
    private outServerMapDataInTransactionView: BehaviorSubject<IServerMapNotificationData> = new BehaviorSubject(null);
    private outServerMapDataInTransactionList: BehaviorSubject<IServerMapNotificationData> = new BehaviorSubject(null);
    currentPage: string;
    baseApplication = '';

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private storeHelperService: StoreHelperService
    ) {
        this.connectStore();
    }
    private connectStore(): void {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService !== null;
            }),
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.APPLICATION);
            })
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.currentPage = urlService.getStartPath();
            this.baseApplication = urlService.getPathValue(UrlPathId.APPLICATION).getKeyStr();
        });
        this.storeHelperService.getTransactionDetailData(this.unsubscribe).pipe(
            filter((data: ITransactionDetailData) => {
                return data !== undefined;
            })
        ).subscribe((data: ITransactionDetailData) => {
            if (this.baseApplication === '') {
                const applicationId = data.applicationId;
                this.baseApplication = (data.applicationMapData.nodeDataArray as INodeInfo[]).find(({ applicationName }: INodeInfo) => {
                    return applicationId === applicationName;
                }).key;
            }
            this.emitData({
                baseApplication: this.baseApplication,
                serverMapData: new ServerMapData(data.applicationMapData.nodeDataArray, data.applicationMapData.linkDataArray),
                error: false,
                hidden: false
            });
        });
    }
    getObservable(type: string): Observable<any> {
        switch (type) {
            case SOURCE_TYPE.MAIN:
                return this.outServerMapDataInMain.asObservable();
            case SOURCE_TYPE.FILTERED:
                return this.outServerMapDataInFilteredMap.asObservable();
            case SOURCE_TYPE.TRANSACTION_VIEW:
                return this.outServerMapDataInTransactionView.asObservable();
            case SOURCE_TYPE.TRANSACTION_LIST:
                return this.outServerMapDataInTransactionList.asObservable();
            default:
                return this.outServerMapDataInMain.asObservable();
        }
    }
    private emitData(data: IServerMapNotificationData): void {
        this.outServerMapDataInMain.next(data);
        this.outServerMapDataInFilteredMap.next(data);
        this.outServerMapDataInTransactionList.next(data);
        this.outServerMapDataInTransactionView.next(data);
    }
}
