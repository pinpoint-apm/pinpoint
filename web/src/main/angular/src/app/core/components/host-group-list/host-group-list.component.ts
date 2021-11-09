import { Component, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter, ViewChild, ElementRef, AfterViewInit } from '@angular/core';

export enum FOCUS_TYPE {
    KEYBOARD,
    MOUSE
}

@Component({
    selector: 'pp-host-group-list',
    templateUrl: './host-group-list.component.html',
    styleUrls: ['./host-group-list.component.css'],
})
export class HostGroupListComponent implements OnInit, OnChanges, AfterViewInit {
    @ViewChild('listWrapper', {static: true}) ele: ElementRef;
    @Input() showTitle: boolean;
    @Input() title: string;
    @Input() restCount: number; // to distinguish the focusIndex between favAppList and entire appList
    @Input() focusIndex: number;
    @Input() focusType: FOCUS_TYPE;
    @Input() hostGroupList: string[];
    @Input() selectedHostGroup: string;
    @Input() emptyText: string;
    @Output() outSelected = new EventEmitter<string>();
    @Output() outFocused = new EventEmitter<number>();

    private previousFocusIndex = -1;

    isEmpty: boolean;

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        const {hostGroupList, focusIndex} = changes;

        if (hostGroupList && hostGroupList.currentValue) {
            this.isEmpty = (hostGroupList.currentValue as string[]).length === 0;
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

    private isSelectedHostGroup(hostGroup: string): boolean {
        return this.selectedHostGroup && this.selectedHostGroup === hostGroup;
    }

    makeClass(index: number): { [key: string]: boolean } {
        const app = this.hostGroupList[index - this.restCount];

        return {
            active: this.isSelectedHostGroup(app),
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

    onSelectHostGroup(hostGroup: string): void {
        this.outSelected.emit(hostGroup);
    }
}
