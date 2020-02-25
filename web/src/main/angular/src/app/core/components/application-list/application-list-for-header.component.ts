import { Component, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter, ViewChild, ElementRef, ChangeDetectionStrategy, AfterViewInit } from '@angular/core';

export enum FOCUS_TYPE {
    KEYBOARD,
    MOUSE
}

@Component({
    selector: 'pp-application-list-for-header',
    templateUrl: './application-list-for-header.component.html',
    styleUrls: ['./application-list-for-header.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ApplicationListForHeaderComponent implements OnInit, OnChanges, AfterViewInit {
    @ViewChild('appList', {static: true}) ele: ElementRef;
    @Input() showTitle: boolean;
    @Input() title: string;
    @Input() restCount: number; // to distinguish the focusIndex between favAppList and entire appList
    @Input() focusIndex: number;
    @Input() focusType: FOCUS_TYPE;
    @Input() applicationList: IApplication[];
    @Input() selectedApplication: IApplication;
    @Input() emptyText: string;
    @Input() funcImagePath: Function;
    @Output() outSelected = new EventEmitter<IApplication>();
    @Output() outFocused = new EventEmitter<number>();

    private previousFocusIndex = -1;

    isEmpty: boolean;

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        const {applicationList, focusIndex} = changes;

        if (applicationList) {
            this.isEmpty = (applicationList.currentValue as IApplication[]).length === 0;
        }

        if (focusIndex) {
            this.scrollIntoView();
        }
    }

    ngAfterViewInit() {
        this.scrollIntoView();
    }

    private scrollIntoView(): void {
        const eleIndex = this.focusIndex - this.restCount;
        const targetElem = this.ele.nativeElement.querySelectorAll('dd')[eleIndex];

        if (!!targetElem && (this.focusType === FOCUS_TYPE.KEYBOARD)) {
            targetElem.scrollIntoView({
                block: 'nearest',
                inline: 'nearest',
                behavior: 'instant'
            });
        }
    }

    private isSelectedApplication(app: IApplication): boolean {
        return this.selectedApplication && this.selectedApplication.equals(app) ? true : false;
    }

    getIconPath(serviceType: string): string {
        return this.funcImagePath(serviceType);
    }

    makeClass(index: number): { [key: string]: boolean } {
        const app = this.applicationList[index - this.restCount];

        return {
            active: this.isSelectedApplication(app),
            focus: this.focusIndex === index
        };
    }

    onFocus(index: number): void {
        if (this.previousFocusIndex === index) {
            return;
        }

        this.outFocused.emit(index);
        this.previousFocusIndex = index;
    }

    onSelectApplication(app: IApplication): void {
        this.outSelected.emit(app);
    }
}
