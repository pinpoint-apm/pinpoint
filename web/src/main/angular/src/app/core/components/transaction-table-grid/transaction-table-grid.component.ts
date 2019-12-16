import { Component, OnInit, OnChanges, SimpleChanges, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
import * as moment from 'moment-timezone';
import { GridOptions } from 'ag-grid-community';

export interface IGridData {
    id: number;
    startTime: number;
    path: string;
    responseTime: number;
    exception: number;
    agentId: string;
    clientIp: string;
    traceId: string;
    spanId: string;
    collectorAcceptTime: number;
}

@Component({
    selector: 'pp-transaction-table-grid',
    templateUrl: './transaction-table-grid.component.html',
    styleUrls: ['./transaction-table-grid.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class TransactionTableGridComponent implements OnInit, OnChanges {
    @Input() rowData: IGridData[];
    @Input() addData: IGridData[];
    @Input() resized: any;
    @Input() currentTraceId: string;
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Output() outSelectTransaction = new EventEmitter<{[key: string]: any}>();
    @Output() outSelectTransactionView = new EventEmitter<{[key: string]: any}>();

    gridOptions: GridOptions;

    constructor() {}
    ngOnInit() {
        this.initGridOptions();
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['addData'] && changes['addData']['currentValue']) {
            this.gridOptions.api.updateRowData({
                add: this.addData
            });
        }
        if (changes['resized'] && !changes['resized']['firstChange'] && changes['resized']['currentValue']) {
            this.gridOptions.api.doLayout();
        }
        if (changes['timezone'] && changes['timezone'].firstChange === false) {
            this.gridOptions.api.refreshCells({
                columns: ['startTime'],
                force: true
            });
        }
        if (changes['dateFormat'] && changes['dateFormat'].firstChange === false) {
            this.gridOptions.api.refreshCells({
                columns: ['startTime'],
                force: true
            });
        }
    }

    private initGridOptions() {
        this.gridOptions = <GridOptions>{
            defaultColDef: {
                resizable: true,
                sortable: true
            },
            rowHeight: 30,
            columnDefs: this.makeColumnDefs(),
            animateRows: true,
            rowSelection: 'single',
            headerHeight: 34,
            enableCellTextSelection: true,
            getRowClass: (params: any) => {
                return params.data.exception === 1 ? 'ag-row-exception' : '';
            },
            onCellClicked: (params: any) => {
                if ( params.colDef.field === 'path' ) {
                    const tag = params.event.target.tagName.toUpperCase();
                    if (tag === 'I' || tag === 'BUTTON' ) {
                        this.outSelectTransactionView.next({
                            agentId: params.data.agentId,
                            traceId: params.data.traceId,
                            collectorAcceptTime: params.data.collectorAcceptTime,
                            spanId: params.data.spanId
                        });
                        return;
                    }
                }
                if ( this.currentTraceId === params.data.traceId ) {
                    return;
                }
                this.currentTraceId = params.data.traceId;
                this.outSelectTransaction.next({
                    traceId: params.data.traceId,
                    collectorAcceptTime: params.data.collectorAcceptTime,
                    elapsed: params.data.responseTime
                });
            }
        };
    }

    onGridReady(params: GridOptions): void {
        this.gridOptions.api.forEachNode((node) => {
            if (this.currentTraceId === node.data.traceId) {
                node.setSelected(true);
            }
        });
    }

    onGridSizeChanged(params: GridOptions): void {
        this.gridOptions.api.sizeColumnsToFit();
    }

    onRendered(): void {
        this.gridOptions.api.sizeColumnsToFit();
    }

    private makeColumnDefs(): any {
        return [
            {
                headerName: '#',
                field: 'id',
                width: 40,
                headerClass: 'id-header',
                cellStyle: () => {
                    return {'text-align': 'center'};
                },
                suppressSizeToFit: true
            },
            {
                headerName: 'StartTime',
                field: 'startTime',
                width: 170,
                valueFormatter: (params: any) => {
                    return params.value === 0 ? '' : moment(params.value).tz(this.timezone).format(this.dateFormat);
                },
                suppressSizeToFit: true
            },
            {
                headerName: 'Path',
                field: 'path',
                width: 370,
                cellRenderer: (params: any) => {
                    return '<button style="margin-right:3px"><i class="fa fa-list-alt" aria-hidden="true"></i></button>' + params.value;
                },
                tooltipField: 'path'
            },
            {
                headerName: 'Res(ms)',
                field: 'responseTime',
                width: 85,
                cellStyle: this.alignRightCellStyle,
                sort: 'desc',
                valueFormatter: (params: any) => {
                    return params.value === '' ? '' : new Intl.NumberFormat().format(params.value);
                },
                suppressSizeToFit: true
            },
            {
                headerName: 'Exception',
                field: 'exception',
                width: 85,
                cellStyle: () => {
                    return {'text-align': 'center'};
                },
                cellRenderer: (params: any) => {
                    if ( params.value === 1 ) {
                        return '<i class="fa fa-fire" style="color:red"></i>';
                    } else {
                        return '';
                    }
                },
                suppressSizeToFit: true
            },
            {
                headerName: 'Agent',
                field: 'agentId',
                width: 200,
                tooltipField: 'agentId'
            },
            {
                headerName: 'Client IP',
                field: 'clientIp',
                width: 150
            },
            {
                headerName: 'Transaction',
                field: 'traceId',
                width: 270,
                // suppressSizeToFit: true,
                tooltipField: 'traceId'
            }
        ];
    }

    argumentCellStyle(): any {
        return {'text-align': 'left'};
    }

    alignRightCellStyle(): any {
        return {'text-align': 'right'};
    }
}
