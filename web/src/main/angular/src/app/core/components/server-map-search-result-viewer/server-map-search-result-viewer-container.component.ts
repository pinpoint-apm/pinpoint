import { Component, OnInit, ChangeDetectionStrategy, ViewChild, ElementRef, Renderer2, ChangeDetectorRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, forkJoin } from 'rxjs';
import { tap } from 'rxjs/operators';

import { NewUrlStateNotificationService, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { ServerMapInteractionService } from 'app/core/components/server-map/server-map-interaction.service';

@Component({
    selector: 'pp-server-map-search-result-viewer-container',
    templateUrl: './server-map-search-result-viewer-container.component.html',
    styleUrls: ['./server-map-search-result-viewer-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerMapSearchResultViewerContainerComponent implements OnInit {
    @ViewChild('searchInput', {static: true}) searchInput: ElementRef;
    private minLength = 3;

    i18nText: {[key: string]: string};
    searchResultList$: Observable<IApplication[]>;
    listDisplay = 'none';
    isEmpty: boolean;
    selectedApp: IApplication;
    searchUseEnter = false;

    constructor(
        private translateService: TranslateService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private serverMapInteractionService: ServerMapInteractionService,
        private renderer: Renderer2,
        private analyticsService: AnalyticsService,
        private cd: ChangeDetectorRef
    ) {}

    ngOnInit() {
        this.initI18NText();
        this.listenToEmitter();
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
        this.newUrlStateNotificationService.onUrlStateChange$.subscribe(() => {
            this.resetView();
            this.cd.markForCheck();
        });
        this.searchResultList$ = this.serverMapInteractionService.onSearchResult$.pipe(
            tap(() => this.listDisplay = 'block'),
            tap((list: IApplication[]) => this.isEmpty = list.length === 0)
        );
    }

    private resetView(): void {
        this.renderer.setProperty(this.searchInput.nativeElement, 'value', '');
        this.listDisplay = 'none';
    }

    onSearch(query: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_NODE);
        if (query.length < this.minLength) {
            return;
        }

        this.serverMapInteractionService.setSearchWord(query);
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
