import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { filter } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService } from 'app/shared/services/new-url-state-notification.service';

export interface IViewType {
    key: string;
    display: string;
}
export const VIEW_TYPE = {
    CALL_TREE: 'callTree',
    SERVER_MAP: 'serverMap',
    TIMELINE: 'timeline'
};

@Injectable()
export class TransactionViewTypeService {
    private outChangeViewType: BehaviorSubject<string> = new BehaviorSubject(VIEW_TYPE.CALL_TREE);
    private viewTypeList = [
        {
            key: VIEW_TYPE.CALL_TREE,
            display: 'Call Tree'
        }, {
            key: VIEW_TYPE.SERVER_MAP,
            display: 'Server Map'
        }, {
            key: VIEW_TYPE.TIMELINE,
            display: 'Timeline'
        }
    ];
    private currentViewType = '';
    private defaultViewType = this.viewTypeList[0].key;
    onChangeViewType$: Observable<string>;
    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {
        this.onChangeViewType$ = this.outChangeViewType.asObservable();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            filter((urlService: NewUrlStateNotificationService) => !!urlService)
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            if (urlService.hasValue(UrlPathId.VIEW_TYPE)) {
                this.setCurrentViewType(urlService.getPathValue(UrlPathId.VIEW_TYPE));
            } else {
                this.setCurrentViewType('');
            }
        });
    }
    private setCurrentViewType(viewType: string): void {
        if ( viewType === '' ) {
            this.currentViewType = this.defaultViewType;
        } else {
            if ( this.currentViewType === viewType ) {
                return;
            }
            let hasMatchedKey = false;
            this.viewTypeList.forEach((obj: IViewType) => {
                if ( obj.key === viewType ) {
                    hasMatchedKey = true;
                }
            });
            this.currentViewType = hasMatchedKey ? viewType : this.defaultViewType;
        }
        this.outChangeViewType.next(this.currentViewType);
    }
    getViewTypeList(): IViewType[] {
        return this.viewTypeList;
    }
}
