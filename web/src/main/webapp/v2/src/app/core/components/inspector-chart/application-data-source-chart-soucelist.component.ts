import { Component, OnInit, OnChanges, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-application-data-source-chart-sourcelist',
    templateUrl: './application-data-source-chart-soucelist.component.html',
    styleUrls: ['./application-data-source-chart-soucelist.component.css']
})
export class ApplicationDataSourceChartSourcelistComponent implements OnInit, OnChanges {
    @Input() data: {serviceType: string, jdbcUrl: string}[];
    @Output() outSourceDataSelected = new EventEmitter<number>();

    private selectedIndex = 0;

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        Object.keys(changes).map((propName: string) => {
            switch (propName) {
                case 'data':
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
