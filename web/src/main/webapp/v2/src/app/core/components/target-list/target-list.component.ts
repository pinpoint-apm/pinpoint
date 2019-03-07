import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-target-list',
    templateUrl: './target-list.component.html',
    styleUrls: ['./target-list.component.css']
})
export class TargetListComponent implements OnInit {
    selectedAppName = '';
    @Input() isLink: boolean;
    @Input() targetList: any[];
    @Output() outSelectTarget: EventEmitter<any> = new EventEmitter();
    @Output() outOpenFilter: EventEmitter<any> = new EventEmitter();
    @Output() outOpenFilterWizard: EventEmitter<any> = new EventEmitter();
    constructor() { }
    ngOnInit() {}
    onSelectTarget(target: any): void {
        if (this.isLink) {
            this.selectedAppName = target[0].sourceInfo.applicationName + '-' + target[0].targetInfo.applicationName;
        } else {
            this.selectedAppName = target[0].applicationName;
        }
        this.outSelectTarget.emit(target);
    }
    onOpenFilter($event: any, target: any): void {
        this.outOpenFilter.emit(target);
    }
    onOpenFilterWizard($event: any, target: any): void {
        this.outOpenFilterWizard.emit(target);
    }
    isSelected(target: any): boolean {
        if (this.isLink) {
            return this.selectedAppName === (target[0].sourceInfo.applicationName + '-' + target[0].targetInfo.applicationName);
        } else {
            return this.selectedAppName === target[0].applicationName;
        }
    }
    getApplicationName(target: any): string {
        if (this.isLink) {
            return `${target[0].sourceInfo.applicationName} > ${target[0].targetInfo.applicationName}`;
        } else {
            return target[0].applicationName;
        }
    }
}
