import { Component, OnInit, ChangeDetectionStrategy, ViewChild, ElementRef, Renderer2, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { forkJoin, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { NewUrlStateNotificationService, AnalyticsService, TRACKED_EVENT_LIST, MessageQueueService, MESSAGE_TO } from 'app/shared/services';
import { ServerMapInteractionService } from 'app/core/components/server-map/server-map-interaction.service';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';
import { NodeGroup } from 'app/core/components/server-map/class';
import { Application } from 'app/core/models';

@Component({
    selector: 'pp-server-map-search-result-viewer-container',
    templateUrl: './server-map-search-result-viewer-container.component.html',
    styleUrls: ['./server-map-search-result-viewer-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerMapSearchResultViewerContainerComponent implements OnInit, OnDestroy {
    @ViewChild('searchInput', {static: true}) searchInput: ElementRef;
    private minLength = 3;
    private unsubscribe = new Subject<void>();
    private serverMapData: ServerMapData;

    i18nText: {[key: string]: string};
    searchResultList: IApplication[];
    listDisplay = 'none';
    isEmpty: boolean;
    selectedApp: IApplication;
    searchUseEnter = false;

    constructor(
        private translateService: TranslateService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private serverMapInteractionService: ServerMapInteractionService,
        private messageQueueService: MessageQueueService,
        private renderer: Renderer2,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.initI18NText();
        this.listenToEmitter();
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private initI18NText() {
        forkJoin(
            this.translateService.get('COMMON.SEARCH_INPUT'),
            this.translateService.get('COMMON.EMPTY_ON_SEARCH')
        ).subscribe((i18n: string[]) => {
            this.i18nText = {
                'PLACE_HOLDER': i18n[0],
                'EMPTY_RESULT': i18n[1]
            };
        });
    }

    private listenToEmitter(): void {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe)
        ).subscribe(() => {
            this.resetView();
            this.cd.markForCheck();
        });

        this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DATA_UPDATE).subscribe((data: ServerMapData) => {
            this.serverMapData = data;
        });
    }

    private resetView(): void {
        this.renderer.setProperty(this.searchInput.nativeElement, 'value', '');
        this.listDisplay = 'none';
    }

    onSearch(query: string): void {
        if (query.length < this.minLength) {
            return;
        }

        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_NODE);
        this.listDisplay = 'block';
        this.searchResultList = this.serverMapData.getNodeList()
            .reduce((acc: {[key: string]: any}[], curr: {[key: string]: any}) => {
                const {key, mergedNodes} = curr;

                return NodeGroup.isGroupKey(key) ? [...acc, ...mergedNodes] : [...acc, curr];
            }, [])
            .filter(({applicationName}: {applicationName: string}) => {
                const regCheckQuery = new RegExp(query, 'i');

                return regCheckQuery.test(applicationName);
            })
            .map(({key, applicationName, serviceType}: {key: string, applicationName: string, serviceType: string}) => {
                return new Application(applicationName, serviceType, 0, key);
            });
        this.isEmpty = this.searchResultList.length === 0;
    }

    onSelectApp(app: IApplication): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_APPLICATION_IN_SEARCH_RESULT);
        this.selectedApp = app;
        this.serverMapInteractionService.setSelectedApplication(app.getKeyStr());
    }

    onClose() {
        this.listDisplay = 'none';
    }
}
