import { Injectable } from '@angular/core';
import { Store, Action, select } from '@ngrx/store';
import { Observable, Subject, iif, pipe } from 'rxjs';
import { takeUntil, map, debounceTime, distinctUntilChanged, filter, pluck } from 'rxjs/operators';

import {
    AppState,
    STORE_KEY,
    selectInfoPerServerVisibleState,
    selectTimelineRange,
    selectTimelineSelectedTime,
    selectTimelineSelectionRange
} from 'app/shared/store/reducers';
import { WebAppSettingDataService } from './web-app-setting-data.service';
import { isEmpty } from 'app/core/utils/util';
import { applicationList, applicationListError } from 'app/shared/store/selectors/application-list.selector';
import { favoriteApplicationList, favoriteApplicationListError } from 'app/shared/store/selectors/favorite-application-list.selector';
import { ErrorType } from 'app/shared/store/reducers/favorite-application-list.reducer';
import { hostGroupList, hostGroupListError } from 'app/shared/store/selectors/host-group-list.selector';

@Injectable()
export class StoreHelperService {
    private dateFormatList: string[][];
    constructor(
        private store: Store<AppState>,
        private webAppSettingDataService: WebAppSettingDataService,
    ) {
        this.dateFormatList = this.webAppSettingDataService.getDateFormatList();
    }

    getApplicationList(unsubscribe?: Subject<void>): Observable<IApplication[]> {
        return this.store.pipe(select(applicationList));
    }
    getApplicationListError(): Observable<IServerErrorFormat> {
        const selectError = pipe(
            select(applicationListError),
            filter((error: IServerErrorFormat) => error && !isEmpty(error))
        );

        return this.store.pipe(selectError);
    }
    getFavoriteApplicationList(unsubscribe?: Subject<void>): Observable<IApplication[]> {
        return this.store.pipe(select(favoriteApplicationList));
    }
    getFavoriteApplicationListError(): Observable<IServerErrorFormat> {
        const selectError = pipe(
            select(favoriteApplicationListError),
            filter((error) => error && !isEmpty(error)),
            filter(({errorType}) => errorType === ErrorType.GET),
            pluck('error')
        );

        return this.store.pipe(selectError);
    }
    getFavoriteApplicationAddError(): Observable<IServerErrorFormat> {
        const selectError = pipe(
            select(favoriteApplicationListError),
            filter((error) => error && !isEmpty(error)),
            filter(({errorType}) => errorType === ErrorType.ADD),
            pluck('error')
        );

        return this.store.pipe(selectError);
    }
    getFavoriteApplicationRemoveError(): Observable<IServerErrorFormat> {
        const selectError = pipe(
            select(favoriteApplicationListError),
            filter((error) => error && !isEmpty(error)),
            filter(({errorType}) => errorType === ErrorType.REMOVE),
            pluck('error')
        );

        return this.store.pipe(selectError);
    }
    getHostGroupList(): Observable<string[]> {
        return this.store.pipe(select(hostGroupList));
    }
    getHostGroupListError(): Observable<IServerErrorFormat> {
        const selectError = pipe(
            select(hostGroupListError),
            filter((error: IServerErrorFormat) => error && !isEmpty(error))
        );

        return this.store.pipe(selectError);
    }
    getTimezone(unsubscribe?: Subject<void>): Observable<string> {
        return this.getObservable(STORE_KEY.TIMEZONE, unsubscribe);
    }
    getLanguage(unsubscribe?: Subject<void>): Observable<string> {
        return this.getObservable(STORE_KEY.LANGUAGE, unsubscribe);
    }
    getDateFormatIndex(unsubscribe: Subject<void>): Observable<number> {
        return this.getObservable(STORE_KEY.DATE_FORMAT, unsubscribe);
    }
    getDateFormat(unsubscribe: Subject<void>, index: number): Observable<string> {
        return this.getObservable(STORE_KEY.DATE_FORMAT, unsubscribe).pipe(
            map((format: number) => {
                return this.dateFormatList[format][index];
            })
        );
    }
    getDateFormatArray(unsubscribe: Subject<void>, ...index: number[]): Observable<string[]> {
        return this.getObservable(STORE_KEY.DATE_FORMAT, unsubscribe).pipe(
            map((format: number) => {
                return index.map((i: number) => {
                    return this.dateFormatList[format][i];
                });
            })
        );
    }
    getAgentList(unsubscribe: Subject<void>): Observable<IAgentList> {
        return this.getObservable(STORE_KEY.ADMIN_AGENT_LIST, unsubscribe);
    }
    getServerAndAgentQuery(unsubscribe: Subject<void>): Observable<string> {
        return this.getObservable(STORE_KEY.SERVER_AND_AGENT, unsubscribe).pipe(
            debounceTime(100),
            distinctUntilChanged()
        );
    }
    getAgentSelection(unsubscribe: Subject<void>): Observable<string> {
        return this.getObservable(STORE_KEY.AGENT_SELECTION, unsubscribe);
    }
    getScatterChartData<T>(unsubscribe: Subject<void>): Observable<T> {
        return this.getObservable(STORE_KEY.SCATTER_CHART, unsubscribe);
    }
    getServerMapLoadingState(unsubscribe: Subject<void>): Observable<string> {
        return this.getObservable(STORE_KEY.SERVER_MAP_LOADING_STATE, unsubscribe);
    }
    getTransactionData(unsubscribe: Subject<void>): Observable<ITransactionMetaData> {
        return this.getObservable(STORE_KEY.TRANSACTION_DATA, unsubscribe);
    }
    getTransactionDetailData(unsubscribe: Subject<void>): Observable<ITransactionDetailData> {
        return this.getObservable(STORE_KEY.TRANSACTION_DETAIL_DATA, unsubscribe);
    }
    getTransactionTimelineData(unsubscribe: Subject<void>): Observable<ITransactionTimelineData> {
        return this.getObservable(STORE_KEY.TRANSACTION_TIMELINE_DATA, unsubscribe);
    }
    getServerListData(unsubscribe: Subject<void>): Observable<any> {
        return this.getObservable(STORE_KEY.SERVER_LIST, unsubscribe);
    }
    getResponseSummaryChartYMax(unsubscribe: Subject<void>): Observable<number> {
        return this.getObservable(STORE_KEY.RESPONSE_SUMMARY_CHART_Y_MAX, unsubscribe);
    }
    getResponseAvgMaxChartYMax(unsubscribe: Subject<void>): Observable<number> {
        return this.getObservable(STORE_KEY.RESPONSE_AVG_MAX_CHART_Y_MAX, unsubscribe);
    }
    getLoadChartYMax(unsubscribe: Subject<void>): Observable<number> {
        return this.getObservable(STORE_KEY.LOAD_CHART_Y_MAX, unsubscribe);
    }
    getLoadAvgMaxChartYMax(unsubscribe: Subject<void>): Observable<number> {
        return this.getObservable(STORE_KEY.LOAD_AVG_MAX_CHART_Y_MAX, unsubscribe);
    }
    getInfoPerServerState(unsubscribe: Subject<void>): Observable<boolean> {
        return this.store.pipe(
            select(selectInfoPerServerVisibleState),
            takeUntil(unsubscribe)
        );
    }
    getInspectorTimelineData(unsubscribe: Subject<void>): Observable<ITimelineInfo> {
        return this.getObservable(STORE_KEY.TIMELINE, unsubscribe);
    }
    getInspectorTimelineSelectionRange(unsubscribe: Subject<void>): Observable<number[]> {
        return this.store.pipe(
            takeUntil(unsubscribe),
            select(selectTimelineSelectionRange),
        );
    }
    getInspectorTimelineSelectedTime(unsubscribe: Subject<void>): Observable<number> {
        return this.store.pipe(
            takeUntil(unsubscribe),
            select(selectTimelineSelectedTime),
        );
    }
    getRange(unsubscribe: Subject<void>): Observable<number[]> {
        return this.getObservable(STORE_KEY.RANGE, unsubscribe);
    }
    getApplicationInspectorChartLayoutInfo(unsubscribe: Subject<void>): Observable<IChartLayoutInfoResponse> {
        return this.getObservable(STORE_KEY.APPLICATION_INSPECTOR_CHART_LAYOUT, unsubscribe);
    }
    getAgentInspectorChartLayoutInfo(unsubscribe: Subject<void>): Observable<IChartLayoutInfoResponse> {
        return this.getObservable(STORE_KEY.AGENT_INSPECTOR_CHART_LAYOUT, unsubscribe);
    }
    getTransactionViewType(unsubscribe: Subject<void>): Observable<string> {
        return this.getObservable(STORE_KEY.TRANSACTION_VIEW_TYPE, unsubscribe);
    }
    getObservable(key: keyof AppState, unsubscribe?: Subject<void>): Observable<any> {
        return iif(
            () => !!unsubscribe,
            this.store.pipe(
                select(key),
                takeUntil(unsubscribe)
            ),
            this.store.pipe(select(key))
        );
    }
    dispatch(action: Action): void {
        this.store.dispatch(action);
    }
}
