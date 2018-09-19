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
    @Output() outSelectTarget: EventEmitter<string> = new EventEmitter();
    @Output() outOpenFilter: EventEmitter<string> = new EventEmitter();
    @Output() outOpenFilterWizard: EventEmitter<string> = new EventEmitter();
    constructor() { }
    ngOnInit() {
    }
    onSelectTarget(target): void {
        this.selectedAppName = target[0].applicationName;
        this.outSelectTarget.emit(target);
    }
    onOpenFilter($event, target): void {
        this.outOpenFilter.emit(target);
    }
    onOpenFilterWizard($event, target): void {
        this.outOpenFilterWizard.emit(target);
    }
}
