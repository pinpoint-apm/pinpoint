import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { filter, map } from 'rxjs/operators';

import {
    WindowRefService,
    WebAppSettingDataService,
    UrlRouteManagerService,
    NewUrlStateNotificationService,
    DynamicPopup
} from 'app/shared/services';
import { UrlPathId } from 'app/shared/models';
import { Filter } from 'app/core/models';

@Component({
    selector: 'pp-filter-transaction-wizard-popup-container',
    templateUrl: './filter-transaction-wizard-popup-container.component.html',
    styleUrls: ['./filter-transaction-wizard-popup-container.component.css']
})
export class FilterTransactionWizardPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() data: any;
    @Output() outCreated = new EventEmitter<ICoordinate>();
    @Output() outClose = new EventEmitter<void>();

    filterInfo$: Observable<Filter>;
    funcImagePath: Function;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private urlRouteManagerService: UrlRouteManagerService,
        private windowRefService: WindowRefService,
    ) {}

    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
        this.filterInfo$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            filter((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.FILTER);
            }),
            map((urlService) => {
                const filterInfo = Filter.instanceFromString(urlService.getPathValue(UrlPathId.FILTER));
                const { from, to } = this.data;

                return filterInfo.find((f: Filter) => {
                    return (`${f.fromApplication}^${f.fromServiceType}` === from) && (`${f.toApplication}^${f.toServiceType}` === to);
                });
            }),
            filter((f: Filter) => !!f)
        );
    }

    ngAfterViewInit() {
        this.outCreated.emit({ coordX: 0, coordY: 0 });
    }

    openFilterMapPage(param: any): void {
        const f = new Filter(
            param.from.applicationName,
            param.from.serviceType,
            param.to.applicationName,
            param.to.serviceType,
            param.transactionResult
        );
        f.setResponseFrom(param.responseFrom);
        f.setResponseTo(param.responseTo);
        if (param.urlPattern) {
            f.setUrlPattern(this.windowRefService.nativeWindow.btoa(param.urlPattern));
        }
        if (param.from.agent) {
            f.setFromAgentName(param.from.agent);
        }
        if (param.to.agent) {
            f.setToAgentName(param.to.agent);
        }
        const isBothWas = param.from.isWas && param.to.isWas;
        this.urlRouteManagerService.openPage(
            this.urlRouteManagerService.makeFilterMapUrl({
                applicationName: this.data.filterApplicationName,
                serviceType: this.data.filterApplicationServiceTypeName,
                periodStr: this.newUrlStateNotificationService.getPathValue(UrlPathId.PERIOD).getValueWithAddedWords(),
                timeStr: this.newUrlStateNotificationService.getPathValue(UrlPathId.END_TIME).getEndTime(),
                filterStr: this.newUrlStateNotificationService.hasValue(UrlPathId.FILTER) ? this.newUrlStateNotificationService.getPathValue(UrlPathId.FILTER) : '',
                hintStr: this.newUrlStateNotificationService.hasValue(UrlPathId.HINT) ? this.newUrlStateNotificationService.getPathValue(UrlPathId.HINT) : '',
                addedFilter: f,
                addedHint: (isBothWas ? {
                    [param.to.applicationName]: param.filterTargetRpcList
                } : null)
            })
        );
    }

    onClosePopup(): void {
        this.outClose.emit();
    }
}
