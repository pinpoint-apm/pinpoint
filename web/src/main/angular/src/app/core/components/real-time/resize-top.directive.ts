import { Directive, ElementRef, OnInit, OnDestroy, Renderer2, Input, HostListener } from '@angular/core';

import { WebAppSettingDataService } from 'app/shared/services';

@Directive({
    selector: '[ppResizeTop]'
})
export class ResizeTopDirective implements OnInit, OnDestroy {
    @Input() minHeight: number;
    @Input() maxHeightPadding: number;

    private maxHeight: number;
    private dragging = false;
    private resizeElement: HTMLElement;

    constructor(
        private elementRef: ElementRef,
        private renderer: Renderer2,
        private webAppSettingDataService: WebAppSettingDataService,
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
    }

    resize(dy: number): void {
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
