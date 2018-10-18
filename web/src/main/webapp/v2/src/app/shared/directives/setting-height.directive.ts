import { Directive, ElementRef, OnInit, OnChanges, SimpleChanges, Renderer2, Input } from '@angular/core';
import { WindowRefService } from 'app/shared/services';

@Directive({
    selector: '[ppSettingHeight]'
})
export class SettingHeightDirective implements OnInit, OnChanges {
    // tslint:disable-next-line:no-input-rename
    @Input('ppSettingHeight') heightConfig: {[key: string]: any};

    constructor(
        private elementRef: ElementRef,
        private renderer: Renderer2,
        private windowRefService: WindowRefService
    ) {	}

    ngOnChanges(changes: SimpleChanges) {
        this.setHeight(this.getHeightValue());
    }

    ngOnInit() {
    }

    private getHeightValue(): string {
        return this.heightConfig.height || (this.heightConfig.setHeightAuto ? 'auto' : this.getComputedHeight());
    }

    private getComputedHeight(): string {
        const width = this.windowRefService.nativeWindow.getComputedStyle(this.elementRef.nativeElement).getPropertyValue('width');
        const height = Number(width.replace(/px/, '')) / this.heightConfig.ratio;

        return height + 'px';
    }

    private setHeight(height: string): void {
        this.renderer.setStyle(this.elementRef.nativeElement, 'height', height);
    }
}
