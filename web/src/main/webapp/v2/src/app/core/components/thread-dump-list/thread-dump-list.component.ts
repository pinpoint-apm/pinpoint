import { Component, OnInit, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
import * as moment from 'moment-timezone';
import { GridOptions } from 'ag-grid-community';

export interface IThreadDumpData {
    index: number;
    id: string;
    name: string;
    state: string;
    startTime: number;
    exec: number;
    sampled: boolean;
    path: string;
    transactionId: string;
    localTraceId: number;
}

@Component({
    selector: 'pp-thread-dump-list',
    templateUrl: './thread-dump-list.component.html',
    styleUrls: ['./thread-dump-list.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class ThreadDumpListComponent implements OnInit {
    @Input() rowData: IThreadDumpData[];
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Output() outSelectThread = new EventEmitter<any>();

    gridOptions: GridOptions;

    constructor() {}
    ngOnInit() {
        this.initGridOptions();
    }

    private initGridOptions(): void {
        this.gridOptions = <GridOptions>{
            defaultColDef: {
                resizable: true,
                sortable: false
            },
            rowHeight: 30,
            columnDefs: this.makeColumnDefs(),
            animateRows: true,
            rowSelection: 'single',
            headerHeight: 34,
            enableCellTextSelection: true,
            onCellClicked: ({data}: any) => {
                this.outSelectThread.next({
                    threadName: data.name,
                    localTraceId: data.localTraceId
                });
            }
        };
    }
    private makeColumnDefs(): any {
        return [
            {
                headerName: '#',
                headerClass: 'order-header',
                field: 'index',
                width: 30,
                cellStyle: () => {
                    return {'text-align': 'center'};
                },
                suppressSizeToFit: true
            },
            {
                headerName: 'ID',
                headerClass: 'id-header',
                field: 'id',
                width: 60,
                cellStyle: () => {
                    return {'text-align': 'center'};
                },
                suppressSizeToFit: true
            },
            {
                headerName: 'Name',
                field: 'name',
                width: 150,
                tooltipField: 'name'
            },
            {
                headerName: 'State',
                field: 'state',
                width: 120,
                suppressSizeToFit: true
            },
            {
                headerName: 'StartTime',
                field: 'startTime',
                width: 140,
                valueFormatter: (params: any) => {
                    return moment(params.value).tz(this.timezone).format(this.dateFormat);
                },
                suppressSizeToFit: true,
                tooltipField: 'startTime'
            },
            {
                headerName: 'Exec(ms)',
                field: 'exec',
                width: 120,
                suppressSizeToFit: true
            },
            {
                headerName: 'Sampled',
                field: 'sampled',
                width: 90,
                suppressSizeToFit: true
            },
            {
                headerName: 'Path',
                field: 'path',
                width: 200,
                tooltipField: 'path'
            },
            {
                headerName: 'Transaction ID',
                field: 'transactionId',
                width: 220,
                suppressSizeToFit: true,
                tooltipField: 'transactionId'
            }
        ];
    }

    onGridReady(params: GridOptions): void {
        this.gridOptions.api.sizeColumnsToFit();
    }

    onGridSizeChanged(params: GridOptions): void {
        this.gridOptions.api.sizeColumnsToFit();
    }
}
