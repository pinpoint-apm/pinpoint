import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-target-list',
    templateUrl: './target-list.component.html',
    styleUrls: ['./target-list.component.css']
})
export class TargetListComponent implements OnInit, OnChanges {
    selectedAppName = '';
    @Input() isLink: boolean;
    @Input() targetList: any[];
    @Output() outSelectTarget: EventEmitter<any> = new EventEmitter();
    @Output() outOpenFilter: EventEmitter<any> = new EventEmitter();
    @Output() outOpenFilterWizard: EventEmitter<any> = new EventEmitter();
    constructor() { }
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (this.targetList && this.targetList.length === 1) {
            if (this.isLink === false) {
                this.onSelectTarget(this.targetList[0]);
            }
        }
    }
    onSelectTarget(target: any): void {
        const sendTarget = target[0];
        if (sendTarget.applicationName) {
            this.selectedAppName = sendTarget.applicationName;
        } else {
            this.selectedAppName = sendTarget.sourceInfo.applicationName + '-' + sendTarget.targetInfo.applicationName;
        }
        this.outSelectTarget.emit(sendTarget);
    }
    onOpenFilter($event: any, target: any): void {
        this.outOpenFilter.emit(target);
    }
    onOpenFilterWizard($event: any, target: any): void {
        this.outOpenFilterWizard.emit(target);
    }
    isSelected(target: any): boolean {
        if (target[0].applicationName) {
            return this.selectedAppName === target[0].applicationName;
        } else {
            return this.selectedAppName === (target[0].sourceInfo.applicationName + '-' + target[0].targetInfo.applicationName);
        }
    }
    getApplicationName(target: any): string {
        if (this.isLink) {
            return target[0].targetInfo.applicationName;
        } else {
            return target[0].applicationName;
        }
    }
}
