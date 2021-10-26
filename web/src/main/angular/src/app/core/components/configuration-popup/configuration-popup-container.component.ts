import { Component, OnInit, Output, EventEmitter, AfterViewInit, Input, ElementRef } from '@angular/core';

import {
    DynamicPopup,
    WindowRefService,
    PopupConstant,
    UrlRouteManagerService,
    AnalyticsService,
    TRACKED_EVENT_LIST,
    ThemeService,
    WebAppSettingDataService
} from 'app/shared/services';

@Component({
    selector: 'pp-configuration-popup-container',
    templateUrl: './configuration-popup-container.component.html',
    styleUrls: ['./configuration-popup-container.component.css'],
})
export class ConfigurationPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() coord: ICoordinate;
    @Output() outClose = new EventEmitter<void>();
    @Output() outCreated = new EventEmitter<ICoordinate>();

    currentTheme: string;
    webhookEnable: boolean;

    private posX: number;

    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private windowRefService: WindowRefService,
        private analyticsService: AnalyticsService,
        private webAppSettingDataService: WebAppSettingDataService,
        private el: ElementRef,
        private themeService: ThemeService,
    ) {}

    ngOnInit() {
        this.posX = this.coord.coordX - PopupConstant.SPACE_FROM_LEFT + this.el.nativeElement.offsetWidth <= this.windowRefService.nativeWindow.innerWidth
            ? this.coord.coordX - PopupConstant.SPACE_FROM_LEFT
            : this.windowRefService.nativeWindow.innerWidth - this.el.nativeElement.offsetWidth;
        this.currentTheme = this.webAppSettingDataService.getTheme();
        this.initWebhookConfig();
    }

    ngAfterViewInit() {
        this.outCreated.emit({
            coordX: this.posX,
            coordY: this.coord.coordY + PopupConstant.SPACE_FROM_BUTTON + PopupConstant.TOOLTIP_TRIANGLE_HEIGHT
        });
    }

    private initWebhookConfig(): void {
        this.webAppSettingDataService.isWebhookEnable().subscribe((webhookEnable: boolean) => {
            this.webhookEnable = webhookEnable
        });
    }

    calculateTooltipCaretLeft(tooltipCaret: HTMLElement): string {
        const {coordX} = this.coord;

        return `${coordX - this.posX - (tooltipCaret.offsetWidth / 2)}px`;
    }

    onInputChange(): void {
        this.outClose.emit();
    }

    onMenuClick(type: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_CONFIGURATION_MENU, type);
        this.urlRouteManagerService.moveToConfigPage(type);
        this.outClose.emit();
    }

    onOpenLink(): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_GITHUB_LINK);
        this.windowRefService.nativeWindow.open('http://github.com/naver/pinpoint');
        this.outClose.emit();
    }

    onClickOutside(): void {
        this.outClose.emit();
    }

    onChangeTheme(theme: string): void {
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CHANGE_THEME, theme);
        this.themeService.changeTheme(theme);
    }
}
