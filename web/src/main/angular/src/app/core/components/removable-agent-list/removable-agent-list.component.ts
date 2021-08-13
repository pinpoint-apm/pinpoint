import {Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation} from '@angular/core';
import {GridOptions, ValueFormatterParams, ValueSetterParams} from 'ag-grid-community';
import {RemovableAgentDataService} from "./removable-agent-data.service";
import {take} from "rxjs/operators";

@Component({
    selector: 'pp-removable-agent-list',
    templateUrl: './removable-agent-list.component.html',
    styleUrls: ['./removable-agent-list.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class RemovableAgentListComponent implements OnInit {
    @Input() rowData: any[];
    @Output() outSelectAgent = new EventEmitter<string>();

    gridOptions: GridOptions;

    constructor(
        private removableAgentDataService: RemovableAgentDataService,
    ) {}
    ngOnInit() {
        this.initGridOptions();
    }

    private initGridOptions(): void {
        this.gridOptions = <GridOptions>{
            defaultColDef: {
                resizable: true,
                sortable: false
            },
            enableCellChangeFlash: true,
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
                        width: 200,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Agent Id',
                        field: 'agentId',
                        width: 250,
                        resizable: true,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Agent Name',
                        field: 'agentNameText',
                        width: 250,
                        resizable: true,
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Rate',
                        field: 'samplingRate',
                        width: 150,
                        resizable: true,
                        valueSetter: (param: ValueSetterParams) => {
                            return this.updateSamplingRate(param);
                        },
                        valueFormatter: (param: ValueFormatterParams) => {
                            return this.getSamplingRate(param);
                        },
                        editable: true,
                        singleClickEdit: true,
                        headerTooltip: 'Click cell to update rate',
                        cellStyle: this.alignCenterCellStyle
                    },
                    {
                        headerName: 'Agent Version',
                        field: 'agentVersion',
                        width: 160,
                        resizable: true,
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

        this.outSelectAgent.emit(row.data.agentId);
    }

    onGridReady(params: any): void {
        params.api.sizeColumnsToFit();
    }

    getSamplingRate(param: any): any {
        if (param.value !== undefined) {
            return;
        }
        this.removableAgentDataService.getSamplingRate(param.data).pipe(take(1)).subscribe(res => {
            const samplingRate = res.code == 0 ? res.message.samplingRate : 'N/A';
            param.data.samplingRate = samplingRate;
            param.node.setDataValue('samplingRate', samplingRate);
        });
    }

    updateSamplingRate(param: any): any {
        if (param.oldValue == undefined || param.oldValue == param.newValue || isNaN(Number(param.newValue))) {
            return false;
        }
        const updateData = {
            applicationName: param.data.applicationName,
            agentId: param.data.agentId,
            samplingRate: param.newValue
        }
        this.removableAgentDataService.setSamplingRate(updateData).pipe(take(1)).subscribe(res => {
            const samplingRate = res.code == 0 ? res.message.samplingRate : 'N/A';
            param.data.samplingRate = samplingRate;
            param.node.setDataValue('samplingRate', samplingRate);
        });
    }

    onGridSizeChanged(): void {
        this.gridOptions.api.sizeColumnsToFit();
    }
}
