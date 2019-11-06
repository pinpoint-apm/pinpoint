import { Component, OnInit, OnChanges, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-agent-data-source-chart-select-source',
    templateUrl: './agent-data-source-chart-select-source.component.html',
    styleUrls: ['./agent-data-source-chart-select-source.component.css']
})
export class AgentDataSourceChartSelectSourceComponent implements OnInit, OnChanges {
    @Input() data: {databaseName: string, id: number}[];
    @Output() outCheckedIdChange: EventEmitter<Set<number>> = new EventEmitter();

    showSourceSelectModal = false;
    checkedIdSet = new Set<number>();

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        Object.keys(changes).map((propName: string) => {
            switch (propName) {
                case 'data':
                    this.initCheckedIdSet();
                    break;
            }
        });
    }

    onSourceSelectClick(): void {
        this.showSourceSelectModal = !this.showSourceSelectModal;
    }

    onCheckAllBtnClick(): void {
        this.initCheckedIdSet();
        this.outCheckedIdChange.emit(this.checkedIdSet);
    }

    onSourceCheckboxChange(id: number): void {
        this.toggleCheckedId(id);
    }

    private initCheckedIdSet(): void {
        this.data.map(({id}: {databaseName: string, id: number}) => this.checkedIdSet.add(id));
    }

    private toggleCheckedId(id: number): void {
        this.checkedIdSet.has(id) ? this.checkedIdSet.delete(id) : this.checkedIdSet.add(id);
        this.outCheckedIdChange.emit(this.checkedIdSet);
    }
}
