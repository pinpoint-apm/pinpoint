import { Component, OnInit, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
import { GridOptions } from 'ag-grid-community';

export interface IGridData {
    index: number;
    application: string;
    serviceType: string;
    agent: string;
    agentVersion: string;
    jvmVersion: string;
    folder?: boolean;
    open?: boolean;
    children?: IGridData[];
}

@Component({
    selector: 'pp-agent-statistic-list',
    templateUrl: './agent-statistic-list.component.html',
    styleUrls: ['./agent-statistic-list.component.css'],
})
export class AgentStatisticListComponent implements OnInit  {
    @Input() gridData: IGridData[];
    @Output() outCellClick = new EventEmitter<any>();

    gridOptions: GridOptions;

    constructor() {}
    ngOnInit() {
        this.initGridOptions();
    }

    private initGridOptions() {
        this.gridOptions = <GridOptions>{
            defaultColDef: {
                resizable: true,
                sortable: false,
                filter: true
            },
            columnDefs : this.makeColumnDefs(),
            headerHeight: 34,
            floatingFilter: true,
            animateRows: true,
            rowHeight: 30,
            getNodeChildDetails: (file) => {
                if (file && file.folder) {
                    return {
                        group: true,
                        children: file.children,
                        expanded: file.open
                    };
                } else {
                    return null;
                }
            },
            suppressRowClickSelection: true,
            rowSelection: 'multiple'
        };
    }

    private makeColumnDefs(): any {
        return [
            {
                headerName: '#',
                field: 'index',
                width: 60,
                filter: false
            },
            {
                headerName: `Application`,
                field: 'application',
                width: 550,
                cellRenderer: 'agGroupCellRenderer',
                cellRendererParams: {
                    innerRenderer: (params: any) => {
                        return '&nbsp;' + params.data.application;
                    },
                    suppressCount: true
                },
                cellStyle: {
                    color: 'rgb(54, 162, 235)',
                    'font-weight': 600
                },
                filter: 'agTextColumnFilter',
                tooltipField: 'application'
            },
            {
                headerName: `Agent`,
                field: 'agent',
                width: 300,
                filter: 'agTextColumnFilter',
                tooltipField: 'agent'
            },
            {
                headerName: 'Agent Version',
                field: 'agentVersion',
                width: 150,
                filter: 'agTextColumnFilter',
                tooltipField: 'agentVersion'
            },
            {
                headerName: 'JVM Version',
                field: 'jvmVersion',
                width: 150,
                filter: 'agTextColumnFilter',
                tooltipField: 'jvmVersion'
            },
        ];
    }

    onCellClick(params: any): void {
        if (params.colDef.field !== 'application') {
            return;
        }

        this.outCellClick.next({
            application: params.data.application,
            serviceType: params.data.serviceType
        });
    }

    onRendered(): void {
        this.gridOptions.api.sizeColumnsToFit();
    }

    onGridSizeChanged(_: GridOptions): void {
        this.gridOptions.api.sizeColumnsToFit();
    }
}
