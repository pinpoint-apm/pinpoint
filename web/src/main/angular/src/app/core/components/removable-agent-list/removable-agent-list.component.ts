import { Component, OnInit, ViewEncapsulation, EventEmitter, Input, Output } from '@angular/core';
import { GridOptions } from 'ag-grid-community';

@Component({
    selector: 'pp-removable-agent-list',
    templateUrl: './removable-agent-list.component.html',
    styleUrls: ['./removable-agent-list.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class RemovableAgentListComponent implements OnInit {
    @Input() rowData: any[];
    @Output() outSelectAgent = new EventEmitter<{[key: string]: string}>();

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
            columnDefs : this.makeColumnDefs(),
            headerHeight: 32,
            animateRows: true,
            rowHeight: 30,
            suppressRowClickSelection: false,
            suppressLoadingOverlay: true,
            suppressCellSelection: true,
            localeText: {noRowsToShow: 'No Agent'}
        };
    }

    private makeColumnDefs(): {[key: string]: any} {
        return [
            {
                headerName: 'Removable Agent List',
                children: [
                    {
                        headerName: 'REMOVE',
                        field: 'agentId',
                        width: 110,
                        cellRenderer: (param: any) => {
                            return '<i style="color:red" class="far fa-trash-alt"></i>';
                        },
                        cellStyle: this.alignCenterPointCellStyle
                    },
                    {
                        headerName: 'Host Name',
                        field: 'hostName',
                        width: 400,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Agent Id',
                        field: 'agentId',
                        width: 250,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Agent Version',
                        field: 'agentVersion',
                        width: 160,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'IP',
                        field: 'ip',
                        width: 150,
                        cellStyle: this.alignCenterCellStyle
                    }
                ]
            }
        ];
    }

    alignCenterCellStyle(): {[key: string]: any} {
        return {
            'text-align': 'center'
        };
    }

    alignCenterPointCellStyle(): {[key: string]: any} {
        return {
            'cursor': 'pointer',
            'text-align': 'center'
        };
    }

    onCellClick(row: any): void {
        if (row.colDef.headerName !== 'REMOVE') {
            return;
        }

        this.outSelectAgent.next({
            appName: row.data.applicationName,
            agentId: row.data.agentId
        });
    }

    onGridReady(params: any): void {
        params.api.sizeColumnsToFit();
    }

    onGridSizeChanged(): void {
        this.gridOptions.api.sizeColumnsToFit();
    }
}
