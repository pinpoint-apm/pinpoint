import { Component, OnInit, OnDestroy, ComponentFactoryResolver, Injector } from '@angular/core';
import { Subject, Observable, EMPTY } from 'rxjs';
import { tap, filter, switchMap, catchError, map } from 'rxjs/operators';
import { TranslateService } from '@ngx-translate/core';

import {
    NewUrlStateNotificationService,
    UrlRouteManagerService,
    AnalyticsService,
    DynamicPopupService,
    TRACKED_EVENT_LIST,
    TranslateReplaceService,
} from 'app/shared/services';
import { ServerErrorPopupContainerComponent } from 'app/core/components/server-error-popup/server-error-popup-container.component';
import { UrlPath, UrlPathId } from 'app/shared/models';
import { isEmpty } from 'app/core/utils/util';
import { HostGroupAndHostListDataService } from './host-group-and-host-list-data.service';

@Component({
    selector: 'pp-host-group-and-host-list-container',
    templateUrl: './host-group-and-host-list-container.component.html',
    styleUrls: ['./host-group-and-host-list-container.component.css'],
})
export class HostGroupAndHostListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();
    private hostList: string[];
    private _query = '';

    selectedHostGroup: string;
    selectedHost: string;
    filteredHostList: string[];
    isEmpty: boolean;
    emptyText$: Observable<string>;
    inputPlaceholder$: Observable<string>;
    searchUseEnter = false;
    SEARCH_MIN_LENGTH = 2;

    constructor(
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private dynamicPopupService: DynamicPopupService,
        private analyticsService: AnalyticsService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector,
        private translateService: TranslateService,
        private hostGroupAndHostListDataService: HostGroupAndHostListDataService,
        private translateReplaceService: TranslateReplaceService,
    ) {}

    // TODO: Add search input box
    ngOnInit() {
        this.initI18nText();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            filter((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.HOST_GROUP) && urlService.isValueChanged(UrlPathId.HOST_GROUP)),
            map((urlService: NewUrlStateNotificationService) => urlService.getPathValue(UrlPathId.HOST_GROUP)),
            tap((hostGroup: string) => this.selectedHostGroup = hostGroup),
            switchMap((hostGroup: string) => this.hostGroupAndHostListDataService.getHostList(hostGroup).pipe(
                catchError((error: IServerErrorFormat) => {
                    this.dynamicPopupService.openPopup({
                        data: {
                            title: 'Server Error',
                            contents: error
                        },
                        component: ServerErrorPopupContainerComponent,
                    }, {
                        resolver: this.componentFactoryResolver,
                        injector: this.injector
                    });
                    return EMPTY;
                })
            )),
        ).subscribe((hostList: string[]) => {
            this.hostList = hostList;
            this.filteredHostList = this.filterHostList(hostList);
            this.isEmpty = isEmpty(this.filteredHostList);

            const selectedHost = this.newUrlStateNotificationService.hasValue(UrlPathId.HOST) ? this.newUrlStateNotificationService.getPathValue(UrlPathId.HOST)
                : hostList[0];

            this.onSelectHost(selectedHost);
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initI18nText(): void {
        this.emptyText$ = this.translateService.get('COMMON.EMPTY_ON_SEARCH');
        this.inputPlaceholder$ = this.translateService.get('COMMON.MIN_LENGTH').pipe(
            map((text: string) => this.translateReplaceService.replace(text, this.SEARCH_MIN_LENGTH))
        );
    }

    onSelectHost(host: string) {
        const url = [
            UrlPath.METRIC,
            this.newUrlStateNotificationService.getPathValue(UrlPathId.HOST_GROUP),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithTime(),
            this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
            host
        ];

        this.selectedHost = host;
        this.urlRouteManagerService.moveOnPage({url});
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_HOST);
    }

    private filterHostList(hostList: string[]): string[] {
        return this.query === '' ? hostList
            : hostList.filter((host: string) => host.toLowerCase().includes(this.query.toLowerCase()));
    }

    private set query(query: string) {
        this._query = query;
        this.filteredHostList = this.filterHostList(this.hostList);
        this.isEmpty = isEmpty(this.filteredHostList);
    }

    private get query(): string {
        return this._query;
    }

    onSearch(query: string): void {
        if (this.query === query) {
            return;
        }

        this.query = query;
    }

    onCancel(): void {
        this.query = '';
    }
}
