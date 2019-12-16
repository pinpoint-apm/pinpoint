import { Component, ChangeDetectorRef, Input, Output, HostBinding, ChangeDetectionStrategy, EventEmitter, Renderer2, OnDestroy, ElementRef, AfterViewInit, NgZone } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { IArea } from './../interface/IArea';
import { IPoint } from './../interface/IPoint';
import { SplitAreaDirective } from './splitArea.directive';

@Component({
    selector: 'split',
    changeDetection: ChangeDetectionStrategy.OnPush,
    styles: [`
        :host {
            display: flex;
            flex-wrap: nowrap;
            justify-content: flex-start;
            align-items: stretch;
            overflow: hidden;
            /*
                Important to keep following rules even if overrided later by 'HostBinding'
                because if [width] & [height] not provided, when build() is executed,
                'HostBinding' hasn't been applied yet so code:
                this.elRef.nativeElement["offsetHeight"] gives wrong value!
             */
            width: 100%;
            height: 100%;
        }
        split-gutter {
            flex-grow: 0;
            flex-shrink: 0;
            background-position: center center;
            background-repeat: no-repeat;
        }
    `],
    template: `
        <ng-content></ng-content>
        <ng-template ngFor let-area [ngForOf]="displayedAreas" let-index="index" let-last="last">
            <split-gutter *ngIf="last === false"
                            [order]="index*2+1"
                            [direction]="direction"
                            [useTransition]="useTransition"
                            [size]="gutterSize"
                            [color]="gutterColor"
                            [imageH]="gutterImageH"
                            [imageV]="gutterImageV"
                            [disabled]="disabled"
                            (mousedown)="startDragging($event, index*2+1, index+1)"
                            (touchstart)="startDragging($event, index*2+1, index+1)"></split-gutter>
        </ng-template>`,
})
export class SplitComponent implements AfterViewInit, OnDestroy {

    private _direction: 'horizontal' | 'vertical' = 'horizontal';

    @Input() set direction(v: 'horizontal' | 'vertical') {
        v = (v === 'vertical') ? 'vertical' : 'horizontal';
        this._direction = v;

        [...this.displayedAreas, ...this.hidedAreas].forEach(area => {
            area.comp.setStyleVisibleAndDir(area.comp.visible, this.isDragging, this.direction);
        });

        this.build(false, false);
    }

    get direction(): 'horizontal' | 'vertical' {
        return this._direction;
    }

    ////
    private _useTransition: boolean = false;

    @Input() set useTransition(v: boolean) {
        v = (typeof(v) === 'boolean') ? v : (v === 'false' ? false : true);
        this._useTransition = v;
    }

    get useTransition(): boolean {
        return this._useTransition;
    }

    ////
    private _disabled: boolean = false;

    @Input() set disabled(v: boolean) {
        v = (typeof(v) === 'boolean') ? v : (v === 'false' ? false : true);
        this._disabled = v;

        // Force repaint if modified from TS class (instead of the template)
        this.cdRef.markForCheck();
    }

    get disabled(): boolean {
        return this._disabled;
    }

    ////
    private _width: number | null = null;

    @Input() set width(v: number | null) {
        v = Number(v);
        this._width = (!isNaN(v) && v > 0) ? v : null;

        this.build(false, false);
    }

    get width(): number | null {
        return this._width;
    }

    ////
    private _height: number | null = null;

    @Input() set height(v: number | null) {
        v = Number(v);
        this._height = (!isNaN(v) && v > 0) ? v : null;

        this.build(false, false);
    }

    get height(): number | null {
        return this._height;
    }

    ////
    private _gutterSize: number = 11;

    @Input() set gutterSize(v: number) {
        v = Number(v);
        this._gutterSize = (!isNaN(v) && v > 0) ? v : 11;

        this.build(false, false);
    }

    get gutterSize(): number {
        return this._gutterSize;
    }

    ////
    private _gutterColor: string = '';

    @Input() set gutterColor(v: string) {
        this._gutterColor = (typeof v === 'string' && v !== '') ? v : '';

        // Force repaint if modified from TS class (instead of the template)
        this.cdRef.markForCheck();
    }

    get gutterColor(): string {
        return this._gutterColor;
    }

    ////
    private _gutterImageH: string = '';

    @Input() set gutterImageH(v: string) {
        this._gutterImageH = (typeof v === 'string' && v !== '') ? v : '';

        // Force repaint if modified from TS class (instead of the template)
        this.cdRef.markForCheck();
    }

    get gutterImageH(): string {
        return this._gutterImageH;
    }

    ////
    private _gutterImageV: string = '';

    @Input() set gutterImageV(v: string) {
        this._gutterImageV = (typeof v === 'string' && v !== '') ? v : '';

        // Force repaint if modified from TS class (instead of the template)
        this.cdRef.markForCheck();
    }

    get gutterImageV(): string {
        return this._gutterImageV;
    }

    ////
    private _dir: 'ltr' | 'rtl' = 'ltr';

    @Input() set dir(v: 'ltr' | 'rtl') {
        v = (v === 'rtl') ? 'rtl' : 'ltr';
        this._dir = v;
    }

    get dir(): 'ltr' | 'rtl' {
        return this._dir;
    }

    ////
    @Output() dragStart = new EventEmitter<{gutterNum: number, sizes: Array<number>}>(false);
    @Output() dragProgress = new EventEmitter<{gutterNum: number, sizes: Array<number>}>(false);
    @Output() dragEnd = new EventEmitter<{gutterNum: number, sizes: Array<number>}>(false);
    @Output() gutterClick = new EventEmitter<{gutterNum: number, sizes: Array<number>}>(false);

    private transitionEndInternal = new Subject<Array<number>>();
    @Output() transitionEnd = (<Observable<Array<number>>> this.transitionEndInternal.asObservable()).pipe(
        debounceTime(20)
    );

    @HostBinding('style.flex-direction') get cssFlexdirection() {
        return (this.direction === 'horizontal') ? 'row' : 'column';
    }

    @HostBinding('style.width') get cssWidth() {
        return this.width ? `${ this.width }px` : '100%';
    }

    @HostBinding('style.height') get cssHeight() {
        return this.height ? `${ this.height }px` : '100%';
    }

    @HostBinding('style.min-width') get cssMinwidth() {
        return (this.direction === 'horizontal') ? `${ this.getNbGutters() * this.gutterSize }px` : null;
    }

    @HostBinding('style.min-height') get cssMinheight() {
        return (this.direction === 'vertical') ? `${ this.getNbGutters() * this.gutterSize }px` : null;
    }

    public isViewInitialized: boolean = false;
    private isDragging: boolean = false;
    private draggingWithoutMove: boolean = false;
    private currentGutterNum: number = 0;

    public readonly displayedAreas: Array<IArea> = [];
    private readonly hidedAreas: Array<IArea> = [];

    private readonly dragListeners: Array<Function> = [];
    private readonly dragStartValues = {
        sizePixelContainer: 0,
        sizePixelA: 0,
        sizePixelB: 0,
        sizePercentA: 0,
        sizePercentB: 0,
    };

    constructor(private ngZone: NgZone,
                private elRef: ElementRef,
                private cdRef: ChangeDetectorRef,
                private renderer: Renderer2) {}

    public ngAfterViewInit() {
        this.isViewInitialized = true;
    }

    private getNbGutters(): number {
        return this.displayedAreas.length - 1;
    }

    public addArea(comp: SplitAreaDirective): void {
        const newArea: IArea = {
            comp,
            order: 0,
            size: 0,
        };

        if (comp.visible === true) {
            this.displayedAreas.push(newArea);
        } else {
            this.hidedAreas.push(newArea);
        }

        comp.setStyleVisibleAndDir(comp.visible, this.isDragging, this.direction);

        this.build(true, true);
    }

    public removeArea(comp: SplitAreaDirective): void {
        if (this.displayedAreas.some(a => a.comp === comp)) {
            const area = <IArea> this.displayedAreas.find(a => a.comp === comp)
            this.displayedAreas.splice(this.displayedAreas.indexOf(area), 1);

            this.build(true, true);
        } else if(this.hidedAreas.some(a => a.comp === comp)) {
            const area = <IArea> this.hidedAreas.find(a => a.comp === comp)
            this.hidedAreas.splice(this.hidedAreas.indexOf(area), 1);
        }
    }

    public updateArea(comp: SplitAreaDirective, resetOrders: boolean, resetSizes: boolean): void {
        // Only refresh if area is displayed (No need to check inside 'hidedAreas')
        const item = this.displayedAreas.find(a => a.comp === comp);

        if (item) {
            this.build(resetOrders, resetSizes);
        }
    }

    public showArea(comp: SplitAreaDirective): void {
        const area = this.hidedAreas.find(a => a.comp === comp);

        if (area) {
            comp.setStyleVisibleAndDir(comp.visible, this.isDragging, this.direction);

            const areas = this.hidedAreas.splice(this.hidedAreas.indexOf(area), 1);
            this.displayedAreas.push(...areas);

            this.build(true, true);
        }
    }

    public hideArea(comp: SplitAreaDirective): void {
        const area = this.displayedAreas.find(a => a.comp === comp);

        if (area) {
            comp.setStyleVisibleAndDir(comp.visible, this.isDragging, this.direction);

            const areas = this.displayedAreas.splice(this.displayedAreas.indexOf(area), 1);
            areas.forEach(area => {
                area.order = 0;
                area.size = 0;
            })
            this.hidedAreas.push(...areas);

            this.build(true, true);
        }
    }

    private build(resetOrders: boolean, resetSizes: boolean): void {
        this.stopDragging();

        // ¤ AREAS ORDER

        if (resetOrders === true) {

            // If user provided 'order' for each area, use it to sort them.
            if (this.displayedAreas.every(a => a.comp.order !== null)) {
                this.displayedAreas.sort((a, b) => (<number> a.comp.order) - (<number> b.comp.order));
            }

            // Then set real order with multiples of 2, numbers between will be used by gutters.
            this.displayedAreas.forEach((area, i) => {
                area.order = i * 2;
                area.comp.setStyleOrder(area.order);
            });

        }

        // ¤ AREAS SIZE PERCENT

        if (resetSizes === true) {

            const totalUserSize = <number> this.displayedAreas.reduce((total: number, s: IArea) => s.comp.size ? total + s.comp.size : total, 0);

            // If user provided 'size' for each area and total == 1, use it.
            if (this.displayedAreas.every(a => a.comp.size !== null) && totalUserSize > .999 && totalUserSize < 1.001 ) {

                this.displayedAreas.forEach(area => {
                    area.size = <number> area.comp.size;
                });
            } else {
                const size = 1 / this.displayedAreas.length;

                this.displayedAreas.forEach(area => {
                    area.size = size;
                });
            }
        }

        //
        // If some real area sizes are less than gutterSize,
        // set them to zero and dispatch size to others.
        let percentToDispatch = 0;

        // Get container pixel size
        let containerSizePixel = this.getNbGutters() * this.gutterSize;
        if (this.direction === 'horizontal') {
            containerSizePixel = this.width ? this.width : this.elRef.nativeElement['offsetWidth'];
        } else {
            containerSizePixel = this.height ? this.height : this.elRef.nativeElement['offsetHeight'];
        }

        this.displayedAreas.forEach(area => {
            if (area.size * containerSizePixel < this.gutterSize) {
                percentToDispatch += area.size;
                area.size = 0;
            }
        });

        if (percentToDispatch > 0 && this.displayedAreas.length > 0) {
            const nbAreasNotZero = this.displayedAreas.filter(a => a.size !== 0).length;

            if (nbAreasNotZero > 0) {
                const percentToAdd = percentToDispatch / nbAreasNotZero;

                this.displayedAreas.filter(a => a.size !== 0).forEach(area => {
                    area.size += percentToAdd;
                });
            } else {
                this.displayedAreas[this.displayedAreas.length - 1].size = 1;
            }
        }


        this.refreshStyleSizes();
        this.cdRef.markForCheck();
    }

    private refreshStyleSizes(): void {
        const sumGutterSize = this.getNbGutters() * this.gutterSize;

        this.displayedAreas.forEach(area => {
            area.comp.setStyleFlexbasis(`calc( ${ area.size * 100 }% - ${ area.size * sumGutterSize }px )`, this.isDragging);
        });
    }

    public startDragging(startEvent: MouseEvent | TouchEvent, gutterOrder: number, gutterNum: number): void {
        startEvent.preventDefault();

        // Place code here to allow '(gutterClick)' event even if '[disabled]="true"'.
        this.currentGutterNum = gutterNum;
        this.draggingWithoutMove = true;
        this.ngZone.runOutsideAngular(() => {
            this.dragListeners.push( this.renderer.listen('document', 'mouseup', (e: MouseEvent) => this.stopDragging()) );
            this.dragListeners.push( this.renderer.listen('document', 'touchend', (e: TouchEvent) => this.stopDragging()) );
            this.dragListeners.push( this.renderer.listen('document', 'touchcancel', (e: TouchEvent) => this.stopDragging()) );
        });

        if(this.disabled) {
            return;
        }

        const areaA = this.displayedAreas.find(a => a.order === gutterOrder - 1);
        const areaB = this.displayedAreas.find(a => a.order === gutterOrder + 1);

        if (!areaA || !areaB) {
            return;
        }

        const prop = (this.direction === 'horizontal') ? 'offsetWidth' : 'offsetHeight';
        this.dragStartValues.sizePixelContainer = this.elRef.nativeElement[prop];
        this.dragStartValues.sizePixelA = areaA.comp.getSizePixel(prop);
        this.dragStartValues.sizePixelB = areaB.comp.getSizePixel(prop);
        this.dragStartValues.sizePercentA = areaA.size;
        this.dragStartValues.sizePercentB = areaB.size;

        let start: IPoint;
        if (startEvent instanceof MouseEvent) {
            start = {
                x: startEvent.screenX,
                y: startEvent.screenY,
            };
        } else if (startEvent instanceof TouchEvent) {
            start = {
                x: startEvent.touches[0].screenX,
                y: startEvent.touches[0].screenY,
            };
        } else {
            return;
        }

        this.ngZone.runOutsideAngular(() => {
            this.dragListeners.push( this.renderer.listen('document', 'mousemove', (e: MouseEvent) => this.dragEvent(e, start, areaA, areaB)) );
            this.dragListeners.push( this.renderer.listen('document', 'touchmove', (e: TouchEvent) => this.dragEvent(e, start, areaA, areaB)) );
        });

        areaA.comp.lockEvents();
        areaB.comp.lockEvents();

        this.isDragging = true;

        this.notify('start');
    }

    private dragEvent(event: MouseEvent | TouchEvent, start: IPoint, areaA: IArea, areaB: IArea): void {
        if (!this.isDragging) {
            return;
        }

        let end: IPoint;
        if (event instanceof MouseEvent) {
            end = {
                x: event.screenX,
                y: event.screenY,
            };
        } else if (event instanceof TouchEvent) {
            end = {
                x: event.touches[0].screenX,
                y: event.touches[0].screenY,
            };
        } else {
            return;
        }

        this.draggingWithoutMove = false;
        this.drag(start, end, areaA, areaB);
    }

    private drag(start: IPoint, end: IPoint, areaA: IArea, areaB: IArea): void {

        // ¤ AREAS SIZE PIXEL
        const devicePixelRatio = window.devicePixelRatio || 1;
        let offsetPixel = (this.direction === 'horizontal') ? (start.x - end.x) : (start.y - end.y);
        offsetPixel = offsetPixel / devicePixelRatio;

        if(this.dir === 'rtl') {
            offsetPixel = -offsetPixel;
        }

        let newSizePixelA = this.dragStartValues.sizePixelA - offsetPixel;
        let newSizePixelB = this.dragStartValues.sizePixelB + offsetPixel;

        if (newSizePixelA < this.gutterSize && newSizePixelB < this.gutterSize) {
            // WTF.. get out of here!
            return;
        } else if (newSizePixelA < this.gutterSize) {
            newSizePixelB += newSizePixelA;
            newSizePixelA = 0;
        } else if (newSizePixelB < this.gutterSize) {
            newSizePixelA += newSizePixelB;
            newSizePixelB = 0;
        }

        // ¤ AREAS SIZE PERCENT
        if (newSizePixelA === 0) {
            areaB.size += areaA.size;
            areaA.size = 0;
        } else if (newSizePixelB === 0) {
            areaA.size += areaB.size;
            areaB.size = 0;
        } else {
            // NEW_PERCENT = START_PERCENT / START_PIXEL * NEW_PIXEL;
            if (this.dragStartValues.sizePercentA === 0) {
                areaB.size = this.dragStartValues.sizePercentB / this.dragStartValues.sizePixelB * newSizePixelB;
                areaA.size = this.dragStartValues.sizePercentB - areaB.size;
            } else if (this.dragStartValues.sizePercentB === 0) {
                areaA.size = this.dragStartValues.sizePercentA / this.dragStartValues.sizePixelA * newSizePixelA;
                areaB.size = this.dragStartValues.sizePercentA - areaA.size;
            } else {
                areaA.size = this.dragStartValues.sizePercentA / this.dragStartValues.sizePixelA * newSizePixelA;
                areaB.size = (this.dragStartValues.sizePercentA + this.dragStartValues.sizePercentB) - areaA.size;
            }
        }

        this.refreshStyleSizes();
        this.notify('progress');
    }

    private stopDragging(): void {
        if (this.isDragging === false && this.draggingWithoutMove === false) {
            return;
        }

        this.displayedAreas.forEach(area => {
            area.comp.unlockEvents();
        });

        while (this.dragListeners.length > 0) {
            const fct = this.dragListeners.pop();
            if (fct) {
                fct();
            }
        }

        if (this.draggingWithoutMove === true) {
            this.notify('click');
        } else {
            this.notify('end');
        }

        this.isDragging = false;
        this.draggingWithoutMove = false;
    }


    public notify(type: 'start' | 'progress' | 'end' | 'click' | 'transitionEnd'): void {
        const areasSize: Array<number> = this.displayedAreas.map(a => a.size * 100);

        switch (type) {
            case 'start':
                return this.dragStart.emit({gutterNum: this.currentGutterNum, sizes: areasSize});

            case 'progress':
                return this.dragProgress.emit({gutterNum: this.currentGutterNum, sizes: areasSize});

            case 'end':
                return this.dragEnd.emit({gutterNum: this.currentGutterNum, sizes: areasSize});

            case 'click':
                return this.gutterClick.emit({gutterNum: this.currentGutterNum, sizes: areasSize});

            case 'transitionEnd':
                return this.transitionEndInternal.next(areasSize);
        }
    }

    public ngOnDestroy(): void {
        this.stopDragging();
    }
}
