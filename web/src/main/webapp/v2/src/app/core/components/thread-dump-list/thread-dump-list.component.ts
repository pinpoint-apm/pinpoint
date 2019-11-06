import { Component, OnInit, OnDestroy, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
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
export class ThreadDumpListComponent implements OnInit, OnDestroy {
    @Input() rowData: IThreadDumpData[];
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Output() outSelectThread: EventEmitter<any> = new EventEmitter();
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
            onCellClicked: (params: any) => {
                if ( params.colDef.field === 'localTraceId' ) {
                    const tag = params.event.target.tagName.toUpperCase();
                    if (tag === 'I' || tag === 'BUTTON' ) {
                        this.outSelectThread.next({
                            threadName: params.data.name,
                            localTraceId: params.data.localTraceId
                        });
                        return;
                    }
                }
            }
        };
    }
    private makeColumnDefs(): any {
        return [
            {
                headerName: '#',
                field: 'index',
                width: 30,
                cellStyle: () => {
                    return {'text-align': 'center'};
                },
                suppressSizeToFit: true
            },
            {
                headerName: 'id',
                field: 'id',
                width: 60,
                cellStyle: () => {
                    return {'text-align': 'center'};
                },
                suppressSizeToFit: true
            },
            {
                headerName: 'name',
                field: 'name',
                width: 150,
                tooltipField: 'name'
            },
            {
                headerName: 'state',
                field: 'state',
                width: 120,
                suppressSizeToFit: true
            },
            {
                headerName: 'start time',
                field: 'startTime',
                width: 140,
                valueFormatter: (params: any) => {
                    return moment(params.value).tz(this.timezone).format(this.dateFormat);
                },
                suppressSizeToFit: true,
                tooltipField: 'startTime'
            },
            {
                headerName: 'exec(ms)',
                field: 'exec',
                width: 120,
                suppressSizeToFit: true
            },
            {
                headerName: 'sampled',
                field: 'sampled',
                width: 90,
                suppressSizeToFit: true
            },
            {
                headerName: 'path',
                field: 'path',
                width: 200,
                tooltipField: 'path'
            },
            {
                headerName: 'transaction id',
                field: 'transactionId',
                width: 220,
                suppressSizeToFit: true,
                tooltipField: 'transactionId'
            },
            {
                headerName: '',
                field: 'localTraceId',
                width: 40,
                cellStyle: () => {
                    return {'text-align': 'center'};
                },
                cellRenderer: () => {
                    return '<button><i class="fa fa-search"></i></button>';
                },
                suppressSizeToFit: true
            }
        ];
    }
    ngOnDestroy() {
    }
    onGridReady(params: GridOptions): void {
        this.gridOptions.api.sizeColumnsToFit();
    }
    onGridSizeChanged(params: GridOptions): void {
        this.gridOptions.api.sizeColumnsToFit();
    }

}
