import { Directive, Input, ElementRef, Renderer2 } from '@angular/core';

@Directive({
    selector: 'split-gutter'
})
export class SplitGutterDirective {

    @Input() set order(v: number) {
        this.renderer.setStyle(this.elRef.nativeElement, 'order', v);
    }

    ////
    private _direction: 'vertical' | 'horizontal';

    @Input() set direction(v: 'vertical' | 'horizontal') {
        this._direction = v;
        this.refreshStyle();
    }

    get direction(): 'vertical' | 'horizontal' {
        return this._direction;
    }

    ////
    @Input() set useTransition(v: boolean) {
        if (v) {
            this.renderer.setStyle(this.elRef.nativeElement, 'transition', `flex-basis 0.3s`);
        } else {
            this.renderer.removeStyle(this.elRef.nativeElement, 'transition');
        }
    }

    ////
    private _size: number;

    @Input() set size(v: number) {
        this._size = v;
        this.refreshStyle();
    }

    get size(): number {
        return this._size;
    }

    ////
    private _color: string;

    @Input() set color(v: string) {
        this._color = v;
        this.refreshStyle();
    }

    get color(): string {
        return this._color;
    }

    ////
    private _imageH: string;

    @Input() set imageH(v: string) {
        this._imageH = v;
        this.refreshStyle();
    }

    get imageH(): string {
        return this._imageH;
    }

    ////
    private _imageV: string;

    @Input() set imageV(v: string) {
        this._imageV = v;
        this.refreshStyle();
    }

    get imageV(): string {
        return this._imageV;
    }

    ////
    private _disabled: boolean = false;

    @Input() set disabled(v: boolean) {
        this._disabled = v;
        this.refreshStyle();
    }

    get disabled(): boolean {
        return this._disabled;
    }

    ////
    constructor(private elRef: ElementRef,
                private renderer: Renderer2) {}

    private refreshStyle(): void {
        this.renderer.setStyle(this.elRef.nativeElement, 'flex-basis', `${ this.size }px`);

        // fix safari bug about gutter height when direction is horizontal
        this.renderer.setStyle(this.elRef.nativeElement, 'height', (this.direction === 'vertical') ? `${ this.size }px` : `100%`);

        this.renderer.setStyle(this.elRef.nativeElement, 'background-color', (this.color !== '') ? this.color : `#eeeeee`);

        const state: 'disabled' | 'vertical' | 'horizontal' = (this.disabled === true) ? 'disabled' : this.direction;
        this.renderer.setStyle(this.elRef.nativeElement, 'background-image', this.getImage(state));
        this.renderer.setStyle(this.elRef.nativeElement, 'cursor', this.getCursor(state));
    }

    private getCursor(state: 'disabled' | 'vertical' | 'horizontal'): string {
        switch (state) {
            case 'horizontal':
                return 'col-resize';

            case 'vertical':
                return 'row-resize';

            case 'disabled':
                return 'default';
        }
    }

    private getImage(state: 'disabled' | 'vertical' | 'horizontal'): string {
        switch (state) {
            case 'horizontal':
                return (this.imageH !== '') ? this.imageH : defaultImageH;

            case 'vertical':
                return (this.imageV !== '') ? this.imageV : defaultImageV;

            case 'disabled':
                return '';
        }
    }
}


const defaultImageH = 'url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAeCAYAAADkftS9AAAAIklEQVQoU2M4c+bMfxAGAgYYmwGrIIiDjrELjpo5aiZeMwF+yNnOs5KSvgAAAABJRU5ErkJggg==")';
const defaultImageV = 'url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAFCAMAAABl/6zIAAAABlBMVEUAAADMzMzIT8AyAAAAAXRSTlMAQObYZgAAABRJREFUeAFjYGRkwIMJSeMHlBkOABP7AEGzSuPKAAAAAElFTkSuQmCC")';
