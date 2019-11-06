import { Directive, ElementRef, OnInit, OnDestroy, Renderer2, Input, HostListener } from '@angular/core';

import { WindowRefService, WebAppSettingDataService } from 'app/shared/services';

@Directive({
    selector: '[ppResizeTop]'
})
export class ResizeTopDirective implements OnInit, OnDestroy {
    @Input() minHeight: number;
    @Input() maxHeightPadding: number;
    private resizeElement: HTMLElement;
    private maxHeight: number;
    private dragging = false;
    constructor(
        private elementRef: ElementRef,
        private renderer: Renderer2,
        private webAppSettingDataService: WebAppSettingDataService,
        private windowRefService: WindowRefService
    ) {
        this.resizeElement = this.elementRef.nativeElement.parentElement;
        this.maxHeight = this.windowRefService.nativeWindow.innerHeight;
        this.windowRefService.nativeWindow.addEventListener('mouseup', this.onWindowMouseUp.bind(this));
        this.windowRefService.nativeWindow.addEventListener('mousemove', this.onWindowMouseMove.bind(this));
    }
    ngOnInit() {
        this.maxHeight = this.windowRefService.nativeWindow.innerHeight - this.maxHeightPadding;
    }
    ngOnDestroy() {
        this.windowRefService.nativeWindow.removeEventListener('mouseup', this.onWindowMouseUp);
        this.windowRefService.nativeWindow.removeEventListener('mousemove', this.onWindowMouseMove);
    }
    onWindowMouseUp($event: MouseEvent): void {
        this.dragging = false;
    }
    onWindowMouseMove($event: MouseEvent): void {
        if (this.dragging) {
            this.resizeOn(-$event.movementY);
        }
    }
    @HostListener('mousedown', ['$event']) onMouseDown($event) {
        this.dragging = true;
    }
    @HostListener('mousemove', ['$event']) onMouseMove($event: MouseEvent) {
        if (this.dragging) {
            this.resizeOn(-$event.movementY);
        }
    }
    @HostListener('mouseup') onMouseUp() {
        this.dragging = false;
    }
    resizeOn(y: number): void {
        if (y !== 0) {
            const nextHeight = (Number.parseInt(this.resizeElement.style.height, 10) || this.minHeight) + y;
            if (nextHeight >= this.minHeight && nextHeight <= this.maxHeight) {
                this.webAppSettingDataService.setLayerHeight(nextHeight);
                this.renderer.setStyle(this.resizeElement, 'height', nextHeight + 'px');

            }
        }
    }
}
