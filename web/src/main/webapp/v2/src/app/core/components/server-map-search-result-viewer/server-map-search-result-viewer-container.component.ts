import { Component, OnInit, ChangeDetectionStrategy, ViewChild, AfterViewInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Subject, Observable, forkJoin } from 'rxjs';
import { filter, map, tap } from 'rxjs/operators';

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
export class ServerMapSearchResultViewerContainerComponent implements OnInit, AfterViewInit {
    @ViewChild(ServerMapSearchResultViewerComponent, {static: false}) comp: ServerMapSearchResultViewerComponent;
    private minLength = 3;

    i18nText: { [key: string]: string } = {};
    hiddenComponent$: Observable<boolean>;
    searchResultList$: Observable<IApplication[]>;
    userInput = new Subject<string>();

    constructor(
        private translateService: TranslateService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private serverMapInteractionService: ServerMapInteractionService,
        private analyticsService: AnalyticsService
    ) {}

    ngOnInit() {
        this.getI18NText();
        this.searchResultList$ = this.serverMapInteractionService.onSearchResult$;
        this.userInput.pipe(
            filter((query: string) => {
                return query.length >= this.minLength;
            })
        ).subscribe((query: string) => {
            this.serverMapInteractionService.setSearchWord(query);
        });
    }

    ngAfterViewInit() {
        /**
         *  TODO: Refactor the structure entirely
         *  1. Locate the template on the container level so that closing the result area and initializing the query becomes easier.
         *  2. Subscribing the server map data directly so that this weird userInput thing becomes unnecessary.
         */
        this.hiddenComponent$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            tap(() => this.comp.onCloseResult()),
            map((urlService: NewUrlStateNotificationService) => {
                return urlService.hasValue(UrlPathId.PERIOD, UrlPathId.END_TIME) ? false : true;
            }),
        );
    }

    private getI18NText() {
        forkJoin(
            this.translateService.get('COMMON.SEARCH_INPUT'),
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
