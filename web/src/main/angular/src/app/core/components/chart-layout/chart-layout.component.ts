import { Component, OnInit, AfterViewInit, Input, Output, ViewChild, EventEmitter } from '@angular/core';
import { CdkDropListGroup, CdkDrag, CdkDragMove, CdkDropList, moveItemInArray} from '@angular/cdk/drag-drop';
import { ViewportRuler } from '@angular/cdk/overlay';

// https://stackblitz.com/edit/angular-dyz1eb

@Component({
    selector: 'pp-chart-layout',
    templateUrl: './chart-layout.component.html',
    styleUrls: ['./chart-layout.component.css']
})
export class ChartLayoutComponent implements OnInit, AfterViewInit {
    @ViewChild(CdkDropListGroup, { static: false }) listGroup: CdkDropListGroup<CdkDropList>;
    @ViewChild(CdkDropList, { static: false }) placeholder: CdkDropList;
    @Input() emptyText: string;
    @Input() column: number;
    @Input() chartList: string[];
    @Output() outUpdateChartOrder: EventEmitter<string[]> = new EventEmitter();
    @Output() outRemoveChart: EventEmitter<string> = new EventEmitter();

    public target: CdkDropList;
    public targetIndex: number;
    public source: CdkDropList;
    public sourceIndex: number;
    public dragIndex: number;
    public activeContainer: CdkDropList;

    constructor(private viewportRuler: ViewportRuler) {
        this.target = null;
        this.source = null;
    }
    ngOnInit() {}
    ngAfterViewInit() {
        if (this.isEmpty() === false) {
            const phElement = this.placeholder.element.nativeElement;
            phElement.style.display = 'none';
            phElement.parentElement.removeChild(phElement);
        }
    }
    dragMoved(e: CdkDragMove) {
        const point = this.getPointerPositionOnPage(e.event);

        this.listGroup._items.forEach((dropList: CdkDropList) => {
            if (__isInsideDropListClientRect(dropList, point.x, point.y)) {
                this.activeContainer = dropList;
                return;
            }
        });
    }
    isEmpty(): boolean {
        if (this.chartList) {
            return this.chartList.length === 0;
        }
        return false;
    }
    removeChart(chartName: string): void {
        this.outRemoveChart.next(chartName);
    }
    dropListDropped() {
        if (!this.target) {
            return;
        }

        const phElement = this.placeholder.element.nativeElement;
        const parent = phElement.parentElement;

        phElement.style.display = 'none';

        parent.removeChild(phElement);
        parent.appendChild(phElement);
        parent.insertBefore(this.source.element.nativeElement, parent.children[this.sourceIndex]);

        this.target = null;
        this.source = null;

        if (this.sourceIndex !== this.targetIndex) {
            moveItemInArray(this.chartList, this.sourceIndex, this.targetIndex);
            this.outUpdateChartOrder.next(this.chartList.concat([]));
        }
    }

    dropListEnterPredicate = (drag: CdkDrag, drop: CdkDropList) => {
        if (drop === this.placeholder) {
            return true;
        }
        if (drop !== this.activeContainer) {
            return false;
        }
        const phElement = this.placeholder.element.nativeElement;
        const sourceElement = drag.dropContainer.element.nativeElement;
        const dropElement = drop.element.nativeElement;

        const dragIndex = __indexOf(dropElement.parentElement.children, (this.source ? phElement : sourceElement));
        const dropIndex = __indexOf(dropElement.parentElement.children, dropElement);

        if (!this.source) {
            this.sourceIndex = dragIndex;
            this.source = drag.dropContainer;

            phElement.style.width = sourceElement.clientWidth + 'px';
            phElement.style.height = sourceElement.clientHeight + 'px';
            sourceElement.parentElement.removeChild(sourceElement);
        }
        this.targetIndex = dropIndex;
        this.target = drop;

        phElement.style.display = '';
        dropElement.parentElement.insertBefore(phElement, (dropIndex > dragIndex ? dropElement.nextSibling : dropElement));

        this.placeholder.enter(drag, drag.element.nativeElement.offsetLeft, drag.element.nativeElement.offsetTop);
        return false;
    }
    /** Determines the point of the page that was touched by the user. */
    getPointerPositionOnPage(event: MouseEvent | TouchEvent) {
      // `touches` will be empty for start/end events so we have to fall back to `changedTouches`.
        const point = __isTouchEvent(event) ? (event.touches[0] || event.changedTouches[0]) : event;
        const scrollPosition = this.viewportRuler.getViewportScrollPosition();
        return {
            x: point.pageX - scrollPosition.left,
            y: point.pageY - scrollPosition.top
        };
    }
}
function __indexOf(collection: HTMLCollection, node: HTMLElement) {
    return Array.prototype.indexOf.call(collection, node);
}
/** Determines whether an event is a touch event. */
function __isTouchEvent(event: MouseEvent | TouchEvent): event is TouchEvent {
    return event.type.startsWith('touch');
}
function __isInsideDropListClientRect(dropList: CdkDropList, x: number, y: number) {
    const {top, bottom, left, right} = dropList.element.nativeElement.getBoundingClientRect();
    return y >= top && y <= bottom && x >= left && x <= right;
}