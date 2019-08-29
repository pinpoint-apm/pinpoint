import { Component, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';

export enum FOCUS_TYPE {
    KEYBOARD,
    MOUSE
}

@Component({
    selector: 'pp-application-list-for-header',
    templateUrl: './application-list-for-header.component.html',
    styleUrls: ['./application-list-for-header.component.css']
})
export class ApplicationListForHeaderComponent implements OnInit, OnChanges {
    @ViewChild('appList', { static: true }) ele: ElementRef;
    @Input() showTitle: boolean;
    @Input() title: string;
    @Input() restCount: number;
    @Input() focusIndex: number;
    @Input() focusType: FOCUS_TYPE;
    @Input() applicationList: IApplication[];
    @Input() selectedApplication: IApplication;
    @Input() emptyText: string;
    @Input() funcImagePath: Function;
    @Output() outSelected: EventEmitter<IApplication> = new EventEmitter();
    @Output() outFocused: EventEmitter<number> = new EventEmitter();
    private previousFocusIndex = -1;

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes['focusIndex']) {
            const eleIndex = this.focusIndex - this.restCount;
            if (eleIndex >= 0 && eleIndex < this.applicationList.length && this.focusType === FOCUS_TYPE.KEYBOARD) {
                this.ele.nativeElement.querySelectorAll('dd')[eleIndex].scrollIntoView({
                    block: 'nearest',
                    inline: 'nearest',
                    behavior: 'instant'
                });
            }
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
        if (this.previousFocusIndex !== index) {
            this.outFocused.emit(index);
            this.previousFocusIndex = index;
        }
    }

    onSelectApplication(app: IApplication): void {
        this.outSelected.emit(app);
    }

    isListEmpty(): boolean {
        return this.applicationList.length === 0;
    }
}
