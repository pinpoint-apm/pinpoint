import { Component, OnInit, ChangeDetectionStrategy, ComponentFactoryResolver, Injector } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

import { Actions } from 'app/shared/store';
import { TranslateReplaceService, AnalyticsService, TRACKED_EVENT_LIST, StoreHelperService, DynamicPopupService } from 'app/shared/services';
import { HELP_VIEWER_LIST, HelpViewerPopupContainerComponent } from 'app/core/components/help-viewer-popup/help-viewer-popup-container.component';

@Component({
    selector: 'pp-agent-search-input-container',
    templateUrl: './agent-search-input-container.component.html',
    styleUrls: ['./agent-search-input-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AgentSearchInputContainerComponent implements OnInit {
    i18nText: { [key: string]: string } = {
        MIN_LENGTH_MSG: ''
    };
    searchUseEnter = false;
    SEARCH_MIN_LENGTH = 2;

    constructor(
        private storeHelperService: StoreHelperService,
        private translateService: TranslateService,
        private translateReplaceService: TranslateReplaceService,
        private analyticsService: AnalyticsService,
        private dynamicPopupService: DynamicPopupService,
        private componentFactoryResolver: ComponentFactoryResolver,
        private injector: Injector
    ) {}

    ngOnInit() {
        this.getI18NText();
    }

    private getI18NText(): void {
        this.translateService.get('COMMON.MIN_LENGTH').subscribe((i18n: string) => {
            this.i18nText.MIN_LENGTH_MSG = this.translateReplaceService.replace(i18n, this.SEARCH_MIN_LENGTH);
        });
    }

    onSearchQuery(query: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_AGENT);
        this.storeHelperService.dispatch(new Actions.UpdateFilterOfServerAndAgentList(query));
    }

    onCancel(): void {
        this.storeHelperService.dispatch(new Actions.UpdateFilterOfServerAndAgentList(''));
    }

    onShowHelp($event: MouseEvent): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.TOGGLE_HELP_VIEWER, HELP_VIEWER_LIST.AGENT_LIST);
        const {left, top, width, height} = ($event.target as HTMLElement).getBoundingClientRect();

        this.dynamicPopupService.openPopup({
            data: HELP_VIEWER_LIST.AGENT_LIST,
            coord: {
                coordX: left + width / 2,
                coordY: top + height / 2
            },
            component: HelpViewerPopupContainerComponent
        }, {
            resolver: this.componentFactoryResolver,
            injector: this.injector
        });
    }
}
