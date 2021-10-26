import { Directive, ElementRef, OnInit, OnDestroy, Renderer2, Input, HostListener, Output, EventEmitter } from '@angular/core';

import { AnalyticsService, TRACKED_EVENT_LIST, WebAppSettingDataService } from 'app/shared/services';

@Directive({
    selector: '[ppResizeTop]'
})
export class ResizeTopDirective implements OnInit, OnDestroy {
    @Input() minHeight: number;
    @Input() maxHeightPadding: number;
    @Output() outResize = new EventEmitter<number>();

    private maxHeight: number;
    private dragging = false;
    private resizeElement: HTMLElement;

    constructor(
        private elementRef: ElementRef,
        private renderer: Renderer2,
        private webAppSettingDataService: WebAppSettingDataService,
        private analyticsService: AnalyticsService,
    ) {}

    ngOnInit() {
        window.addEventListener('mouseup', this.onWindowMouseUp.bind(this));
        window.addEventListener('mousemove', this.onWindowMouseMove.bind(this));

        this.maxHeight = window.innerHeight - this.maxHeightPadding;
        this.resizeElement = this.elementRef.nativeElement.parentElement;
    }

    ngOnDestroy() {
        window.removeEventListener('mouseup', this.onWindowMouseUp);
        window.removeEventListener('mousemove', this.onWindowMouseMove);
    }

    onWindowMouseUp(): void {
        this.dragging = false;
        this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_REAL_TIME_AREA_HEIGHT, `${Math.floor(this.resizeElement.offsetHeight / this.maxHeight * 100)}%`);
    }

    onWindowMouseMove({movementY}: MouseEvent): void {
        if (!this.dragging) {
            return;
        }

        this.resize(-movementY);
    }

    @HostListener('mousedown', ['$event']) onMouseDown() {
        this.dragging = true;
    }

    @HostListener('mousemove', ['$event']) onMouseMove({movementY}: MouseEvent) {
        if (!this.dragging) {
            return;
        }

        this.resize(-movementY);
    }

    @HostListener('mouseup') onMouseUp() {
        this.dragging = false;
        // this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SET_REAL_TIME_AREA_HEIGHT, `${this.resizeElement.offsetHeight}px`);
    }

    private resize(dy: number): void {
        if (dy === 0) {
            return;
        }

        const computedHeight = (Number(this.resizeElement.offsetHeight) || this.minHeight) + dy;
        const isValidHeight = computedHeight >= this.minHeight && computedHeight <= this.maxHeight;

        if (!isValidHeight) {
            return;
        }

        this.webAppSettingDataService.setLayerHeight(computedHeight);
        this.renderer.setStyle(this.resizeElement, 'height', `${computedHeight}px`);
    }
}
