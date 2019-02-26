import { Component, OnInit, Output, EventEmitter, AfterViewInit, Input, ElementRef } from '@angular/core';

import { DynamicPopup, WindowRefService, PopupConstant, UrlRouteManagerService, StoreHelperService } from 'app/shared/services';
import { Actions } from 'app/shared/store';

@Component({
    selector: 'pp-configuration-popup-container',
    templateUrl: './configuration-popup-container.component.html',
    styleUrls: ['./configuration-popup-container.component.css']
})
export class ConfigurationPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() coord: ICoordinate;
    @Output() outClose = new EventEmitter<void>();
    @Output() outCreated = new EventEmitter<ICoordinate>();

    private posX: number;

    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private windowRefService: WindowRefService,
        private storeHelperService: StoreHelperService,
        private el: ElementRef
    ) {}

    ngOnInit() {
        this.posX = this.windowRefService.nativeWindow.innerWidth - this.el.nativeElement.offsetWidth;
    }

    ngAfterViewInit() {
        this.outCreated.emit({
            coordX: this.posX,
            coordY: this.coord.coordY + PopupConstant.SPACE_FROM_BUTTON + PopupConstant.TOOLTIP_TRIANGLE_HEIGHT
        });
    }

    calculateTooltipCaretLeft(tooltipCaret: HTMLElement): string {
        const { coordX } = this.coord;

        return `${coordX - this.posX - (tooltipCaret.offsetWidth / 2)}px`;
    }

    onInputChange(): void {
        this.outClose.emit();
    }

    onMenuClick(type: string): void {
        this.updateURLPathState();
        this.urlRouteManagerService.moveToConfigPage(type);
        this.outClose.emit();
    }

    onOpenLink(): void {
        this.windowRefService.nativeWindow.open('http://github.com/naver/pinpoint');
        this.outClose.emit();
    }

    onClickOutside(): void {
        this.outClose.emit();
    }

    private updateURLPathState(): void {
        const pathName = (this.windowRefService.nativeWindow as Window).location.pathname;

        this.storeHelperService.dispatch(new Actions.UpdateURLPath(pathName));
    }
}
