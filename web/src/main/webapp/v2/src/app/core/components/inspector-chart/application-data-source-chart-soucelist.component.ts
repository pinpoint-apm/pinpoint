import { Component, OnInit, OnChanges, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-application-data-source-chart-sourcelist',
    templateUrl: './application-data-source-chart-soucelist.component.html',
    styleUrls: ['./application-data-source-chart-soucelist.component.css']
})
export class ApplicationDataSourceChartSourcelistComponent implements OnInit, OnChanges {
    @Input() isDataEmpty: boolean;
    @Input() sourceDataArr: { [key: string]: any }[];
    @Output() outSourceDataSelected: EventEmitter<number> = new EventEmitter();

    private selectedIndex = 0;

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        Object.keys(changes).map((propName: string) => {
            switch (propName) {
                case 'sourceDataArr':
                    this.initSelectedIndex();
                    break;
            }
        });
    }

    private initSelectedIndex(): void {
        this.selectedIndex = 0;
    }

    isItemSelected(index: number): boolean {
        return index === this.selectedIndex;
    }

    private selectSource(index: number): void {
        this.selectedIndex = index;
        this.outSourceDataSelected.emit(this.selectedIndex);
    }

    onClickSourceList(index: number): void {
        if (index !== this.selectedIndex) {
            this.selectSource(index);
        }
    }
}
