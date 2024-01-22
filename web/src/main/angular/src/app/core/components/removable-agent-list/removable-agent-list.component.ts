import { Component, OnInit, ViewEncapsulation, EventEmitter, Input, Output } from '@angular/core';
import { GridOptions } from 'ag-grid-community';
import { forkJoin } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'pp-removable-agent-list',
    templateUrl: './removable-agent-list.component.html',
    styleUrls: ['./removable-agent-list.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class RemovableAgentListComponent implements OnInit {
    @Input() rowData: any[];
    @Output() outSelectAgent = new EventEmitter<string>();
    i18nText: {[key: string]: string} = {
        removeButton: '',
        removableTitleLabel: '',
    };
    gridOptions: GridOptions;

    constructor(
        private translateService: TranslateService,
    ) {}
    ngOnInit() {
        this.initI18NText();
        this.initGridOptions();
    }

    private initI18NText(): void {
        forkJoin(
            this.translateService.get('COMMON.REMOVE'),
            this.translateService.get('CONFIGURATION.AGENT_MANAGEMENT.REMOVABLE_TITLE')
        ).subscribe(([removeBtnLabel, removableTitleLabel]: string[]) => {
            this.i18nText.removeButton = removeBtnLabel;
            this.i18nText.removableTitleLabel = removableTitleLabel;
        });
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
                headerName: this.i18nText.removableTitleLabel,
                children: [
                    {
                        headerName: this.i18nText.removeButton,
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
                        headerName: 'Agent Name',
                        field: 'agentNameText',
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

        this.outSelectAgent.emit(row.data.agentId);
    }

    onGridReady(params: any): void {
        params.api.sizeColumnsToFit();
    }

    onGridSizeChanged(): void {
        this.gridOptions.api.sizeColumnsToFit();
    }
}
