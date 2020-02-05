import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-target-list',
    templateUrl: './target-list.component.html',
    styleUrls: ['./target-list.component.css']
})
export class TargetListComponent implements OnInit, OnChanges {
    @Input() isLink: boolean;
    @Input() targetList: any[];
    @Output() outSelectTarget = new EventEmitter<any>();
    @Output() outOpenFilter = new EventEmitter<any>();
    @Output() outOpenFilterWizard = new EventEmitter<any>();

    selectedAppName = '';

    constructor() {}
    ngOnInit() {}
    ngOnChanges(_: SimpleChanges) {
        // * Only when the target has multi input
        if (this.targetList && this.targetList.length === 1 && this.targetList[0][0].fromList) {
            if (!this.isLink) {
                this.selectedAppName = this.getSelectedAppName(this.targetList[0][0]);
            }
        }
    }

    onSelectTarget(target: any): void {
        const targetAppName = this.getSelectedAppName(target[0]);

        if (this.selectedAppName === targetAppName) {
            return;
        }

        this.selectedAppName = targetAppName;
        this.outSelectTarget.emit(target[0]);
    }

    onOpenFilter($event: MouseEvent, target: any): void {
        $event.stopPropagation();
        this.outOpenFilter.emit(target);
    }

    onOpenFilterWizard($event: MouseEvent, target: any): void {
        $event.stopPropagation();
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

    private getSelectedAppName(target: any): string {
        const {applicationName, sourceInfo, targetInfo} = target;

        return applicationName ? applicationName : `${sourceInfo.applicationName}-${targetInfo.applicationName}`;
    }
}
