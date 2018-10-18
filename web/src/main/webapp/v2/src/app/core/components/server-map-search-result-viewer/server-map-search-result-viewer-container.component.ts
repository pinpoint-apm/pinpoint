import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, combineLatest } from 'rxjs';
import { distinctUntilChanged, filter, takeUntil } from 'rxjs/operators';

import { UrlPathId } from 'app/shared/models';
import { NewUrlStateNotificationService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { ServerMapInteractionService } from 'app/core/components/server-map/server-map-interaction.service';
import { ServerMapSearchResultViewerComponent } from './server-map-search-result-viewer.component';

@Component({
    selector: 'pp-server-map-search-result-viewer-container',
    templateUrl: './server-map-search-result-viewer-container.component.html',
    styleUrls: ['./server-map-search-result-viewer-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerMapSearchResultViewerContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<null> = new Subject();
    private minLength = 3;
    i18nText: { [key: string]: string } = {};
    hiddenComponent = true;
    searchResultList: IApplication[] = [];
    userInput: Subject<string> = new Subject();

    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private translateService: TranslateService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private serverMapInteractionService: ServerMapInteractionService,
        private analyticsService: AnalyticsService
    ) {}

    ngOnInit() {
        this.getI18NText();
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil(this.unsubscribe),
        ).subscribe((urlService: NewUrlStateNotificationService) => {
            this.hiddenComponent = urlService.hasValue(UrlPathId.PERIOD, UrlPathId.END_TIME) ? false : true;
            this.changeDetectorRef.detectChanges();
        });
        this.userInput.pipe(
            distinctUntilChanged(),
            filter((query: string) => {
                return query.length >= this.minLength;
            })
        ).subscribe((query: string) => {
            this.serverMapInteractionService.setSearchWord(query);
        });
        this.serverMapInteractionService.onSearchResult$.subscribe((resultList: IApplication[]) => {
            this.searchResultList = resultList;
            this.changeDetectorRef.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    private getI18NText() {
        combineLatest(
            this.translateService.get('MAIN.SEARCH_SERVER_MAP_PLACE_HOLDER'),
            this.translateService.get('MAIN.EMPTY_RESULT')
        ).subscribe((i18n: string[]) => {
            this.i18nText = {
                [ServerMapSearchResultViewerComponent.I18NTEXT.PLACE_HOLDER]: i18n[0],
                [ServerMapSearchResultViewerComponent.I18NTEXT.EMPTY_RESULT]: i18n[1]
            };
        });
    }
    onSearch($event: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_NODE);
        this.userInput.next($event);
    }
    onSelectApplication(app: IApplication): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SELECT_APPLICATION_IN_SEARCH_RESULT);
        this.serverMapInteractionService.setSelectedApplication(app.getKeyStr());
    }
}
