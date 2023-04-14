import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';

import { isSameArray } from 'app/core/utils/util';

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
    ngOnChanges(changes: SimpleChanges) {
        // TODO: If selected target in servermap has changed, re-initialize the selectedAppName as ''
        const targetListChange = changes['targetList'];

        if (targetListChange && targetListChange.currentValue) {
            const prevTargetList = targetListChange.previousValue ? targetListChange.previousValue : [];
            const currTargetList = targetListChange.currentValue;

            // * When the target node is not a merged one but it has multiple input links
            const shouldSelectApp = currTargetList.length === 1 && !!currTargetList[0].fromList && (
                    prevTargetList.length !== currTargetList.length ||
                    prevTargetList[0].applicationName !== currTargetList[0].applicationName ||
                    !isSameArray(prevTargetList[0].fromList.map(({key}: any) => key), currTargetList[0].fromList.map(({key}: any) => key))
                );

            if (shouldSelectApp) {
                this.selectedAppName = this.getSelectedAppName(this.targetList[0]);
            }
        }
    }

    onSelectTarget(target: any): void {
        const targetAppName = this.getSelectedAppName(target);

        if (this.selectedAppName === targetAppName) {
            return;
        }

        this.selectedAppName = targetAppName;
        this.outSelectTarget.emit(target);
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
        if (target.applicationName) {
            return this.selectedAppName === target.applicationName;
        } else {
            return this.selectedAppName === (`${target.sourceInfo.applicationName}-${target.targetInfo.applicationName}`);
        }
    }

    getApplicationName(target: any): string {
        if (this.isLink) {
            return target.targetInfo.applicationName;
        } else {
            return target.applicationName;
        }
    }

    private getSelectedAppName(target: any): string {
        const {applicationName, sourceInfo, targetInfo} = target;

        return applicationName ? applicationName : `${sourceInfo.applicationName}-${targetInfo.applicationName}`;
    }
}
