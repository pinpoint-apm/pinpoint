import {
    Component,
    ChangeDetectionStrategy,
    OnInit,
    Output,
    EventEmitter,
    AfterViewInit,
    Input,
    ElementRef,
    Renderer2
} from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import {
    UrlRouteManagerService,
    DynamicPopup,
    PopupConstant,
    NewUrlStateNotificationService,
    AnalyticsService,
    TRACKED_EVENT_LIST
} from 'app/shared/services';
import { UrlPath, UrlPathId } from 'app/shared/models';

@Component({
    selector: 'pp-transaction-id-search-container',
    templateUrl: './transaction-id-search-container.component.html',
    styleUrls: ['./transaction-id-search-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionIdSearchContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    // * Display it as popup if the screen is not wide enough.
    @Input() coord: ICoordinate;
    @Output() outClose = new EventEmitter<void>();
    @Output() outCreated = new EventEmitter<ICoordinate>();
    @Output() outSearchId = new EventEmitter<{txId: string}>();

    private posX: number;

    searchUseEnter = true;
    isAppSelected$: Observable<boolean>;

    constructor(
        private urlRouteManagerService: UrlRouteManagerService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private analyticsService: AnalyticsService,
        private el: ElementRef,
        private renderer: Renderer2
    ) {}

    ngOnInit() {
        this.isAppSelected$ = this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            map((urlService: NewUrlStateNotificationService) => urlService.hasValue(UrlPathId.APPLICATION))
        );

        if (this.coord) {
            this.posX = this.coord.coordX - PopupConstant.SPACE_FROM_LEFT + this.el.nativeElement.offsetWidth <= window.innerWidth
                ? this.coord.coordX - PopupConstant.SPACE_FROM_LEFT
                : window.innerWidth - this.el.nativeElement.offsetWidth;
        }
    }

    ngAfterViewInit() {
        if (this.coord) {
            this.renderer.setStyle(this.el.nativeElement, 'position', 'absolute');
            this.renderer.setStyle(this.el.nativeElement.querySelector('.l-search-input'), 'border-top', 0);
            this.outCreated.emit({
                coordX: this.posX,
                coordY: this.coord.coordY + PopupConstant.SPACE_FROM_BUTTON + PopupConstant.TOOLTIP_TRIANGLE_HEIGHT
            });
        }
    }

    calculateTooltipCaretLeft(tooltipCaret: HTMLElement): string {
        const {coordX} = this.coord;

        return `${coordX - this.posX - (tooltipCaret.offsetWidth / 2)}px`;
    }

    onSearchId(txId: string): void {
        if (txId === '') {
            return;
        }

        const spanId = '-1';
        const collectorAcceptTime = '0';
        const txInfo = txId.split('^');

        if (txInfo.length !== 3) {
            return;
        }

        const agentId = txInfo[0];

        this.urlRouteManagerService.openPage({
            path: [
                UrlPath.TRANSACTION_DETAIL,
                txId,
                collectorAcceptTime,
                agentId,
                spanId
            ]
        });
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SEARCH_TRANSACTION_ID);
    }

    onInputChange(): void {
        this.outClose.emit();
    }

    onClickOutside(): void {
        this.outClose.emit();
    }
}
