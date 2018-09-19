import { Directive, Input, ElementRef, Renderer2, OnInit, OnDestroy, NgZone } from '@angular/core';

import { SplitComponent } from './split.component';

@Directive({
    selector: 'split-area'
})
export class SplitAreaDirective implements OnInit, OnDestroy {

    private _order: number | null = null;

    @Input() set order(v: number | null) {
        v = Number(v);
        this._order = !isNaN(v) ? v : null;

        this.split.updateArea(this, true, false);
    }
    
    get order(): number | null {
        return this._order;
    }

    ////
    private _size: number | null = null;

    @Input() set size(v: number | null) {
        v = Number(v);
        this._size = (!isNaN(v) && v >= 0 && v <= 100) ? (v/100) : null;

        this.split.updateArea(this, false, true);
    }
    
    get size(): number | null {
        return this._size;
    }

    ////
    private _minSize: number = 0;

    @Input() set minSize(v: number) {
        v = Number(v);
        this._minSize = (!isNaN(v) && v > 0 && v < 100) ? v/100 : 0;

        this.split.updateArea(this, false, true);
    }
    
    get minSize(): number {
        return this._minSize;
    }

    ////
    private _visible: boolean = true;

    @Input() set visible(v: boolean) {
        v = (typeof(v) === 'boolean') ? v : (v === 'false' ? false : true);
        this._visible = v;

        if(this.visible) { 
            this.split.showArea(this);
        }
        else {
            this.split.hideArea(this);
        }
    }

    get visible(): boolean {
        return this._visible;
    }

    ////
    private transitionListener: Function;
    private readonly lockListeners: Array<Function> = [];

    constructor(private ngZone: NgZone,
                private elRef: ElementRef,
                private renderer: Renderer2,
                private split: SplitComponent) {}

    public ngOnInit(): void {
        this.split.addArea(this);

        this.renderer.setStyle(this.elRef.nativeElement, 'flex-grow', '0');
        this.renderer.setStyle(this.elRef.nativeElement, 'flex-shrink', '0');

        this.ngZone.runOutsideAngular(() => {
            this.transitionListener = this.renderer.listen(this.elRef.nativeElement, 'transitionend', (e: TransitionEvent) => this.onTransitionEnd(e));
        });
    }

    public getSizePixel(prop: 'offsetWidth' | 'offsetHeight'): number {
        return this.elRef.nativeElement[prop];
    }

    public setStyleVisibleAndDir(isVisible: boolean, isDragging: boolean, direction: 'horizontal' | 'vertical'): void {
        if(isVisible === false) {
            this.setStyleFlexbasis('0', isDragging);
            this.renderer.setStyle(this.elRef.nativeElement, 'overflow-x', 'hidden');
            this.renderer.setStyle(this.elRef.nativeElement, 'overflow-y', 'hidden');
            
            if(direction === 'vertical') {
                this.renderer.setStyle(this.elRef.nativeElement, 'max-width', '0');
            }
        }
        else {
            this.renderer.setStyle(this.elRef.nativeElement, 'overflow-x', 'hidden');
            this.renderer.setStyle(this.elRef.nativeElement, 'overflow-y', 'auto');
            this.renderer.removeStyle(this.elRef.nativeElement, 'max-width');
        }

        if(direction === 'horizontal') {
            this.renderer.setStyle(this.elRef.nativeElement, 'height', '100%');
            this.renderer.removeStyle(this.elRef.nativeElement, 'width');
        }
        else {
            this.renderer.setStyle(this.elRef.nativeElement, 'width', '100%');
            this.renderer.removeStyle(this.elRef.nativeElement, 'height');
        }
    }

    public setStyleOrder(value: number): void {
        this.renderer.setStyle(this.elRef.nativeElement, 'order', value);
    }
    
    public setStyleFlexbasis(value: string, isDragging: boolean): void {
        // If component not yet initialized or gutter being dragged, disable transition
        if(this.split.isViewInitialized === false || isDragging === true) {
            this.setStyleTransition(false);
        }
        // Or use 'useTransition' to know if transition.
        else {
            this.setStyleTransition(this.split.useTransition);
        }

        this.renderer.setStyle(this.elRef.nativeElement, 'flex-basis', value);
    }

    private setStyleTransition(useTransition: boolean): void {
        if (useTransition) {
            this.renderer.setStyle(this.elRef.nativeElement, 'transition', `flex-basis 0.3s`);
        } else {
            this.renderer.removeStyle(this.elRef.nativeElement, 'transition');
        }
    }
    private onTransitionEnd(event: TransitionEvent): void {
        // Limit only flex-basis transition to trigger the event
        if (event.propertyName === 'flex-basis') {
            this.split.notify('transitionEnd');
        }
    }

    public lockEvents(): void {
        this.ngZone.runOutsideAngular(() => {
            this.lockListeners.push( this.renderer.listen(this.elRef.nativeElement, 'selectstart', (e: Event) => false) );
            this.lockListeners.push( this.renderer.listen(this.elRef.nativeElement, 'dragstart', (e: Event) => false) );
        });
    }

    public unlockEvents(): void {
        while (this.lockListeners.length > 0) {
            const fct = this.lockListeners.pop();
            if (fct) {
                fct();
            }
        }
    }

    public ngOnDestroy(): void {
        this.unlockEvents();

        if (this.transitionListener) {
            this.transitionListener();
        }

        this.split.removeArea(this);
    }
}