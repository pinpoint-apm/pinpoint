import { Component, Input, Output, ViewEncapsulation, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import * as moment from 'moment-timezone';
import { GridOptions, RowNode } from 'ag-grid-community';
import { WindowRefService } from 'app/shared/services';

export interface IGridData {
    id: string;
    index: number;
    method: string;
    argument: string;
    startTime: number;
    gap: number;
    exec: number;
    execPer: number | string;
    selp: number;
    selpPer: number;
    clazz: string;
    api: string;
    agent: string;
    application: string;
    isMethod: boolean;
    methodType: string;
    hasException: boolean;
    isAuthorized: boolean;
    isFocused: boolean;
    folder?: boolean;
    open?: boolean;
    children?: any[];
}

@Component({
    selector: 'pp-call-tree',
    templateUrl: './call-tree.component.html',
    styleUrls: ['./call-tree.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class CallTreeComponent implements OnInit, OnChanges {
    gridOptions: GridOptions;
    previousColor: string;
    @Input() canSelectRow: boolean;
    @Input() rowSelection: string;
    @Input() rowData: IGridData[];
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Output() outSelectFormatting: EventEmitter<any> = new EventEmitter();
    @Output() outRowSelected: EventEmitter<IGridData> = new EventEmitter();
    @Output() outCellDoubleClicked: EventEmitter<string> = new EventEmitter();

    constructor(private windowRefService: WindowRefService) {}
    ngOnInit() {
        this.initGridOptions();
    }
    ngOnChanges(changes: SimpleChanges) {
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
                sortable: false
            },
            columnDefs : this.makeColumnDefs(),
            headerHeight: 34,
            animateRows: true,
            enableCellTextSelection: true,
            rowHeight: 30,
            getRowClass: (params: any) => {
                if ( params.data.isFocused ) {
                    return 'ag-row-focused';
                } else if ( params.data.hasException ) {
                    return 'ag-row-exception';
                } else {
                    return '';
                }
            },
            getNodeChildDetails: (file) => {
                if (file.folder) {
                    return {
                        group: true,
                        children: file.children,
                        expanded: file.open
                    };
                } else {
                    return null;
                }
            },
            onRowClicked: (params: any) => {
                if (this.canSelectRow) {
                    params.node.setSelected(true);
                    this.outRowSelected.emit(params.data);
                }
            },
            suppressRowClickSelection: !this.canSelectRow,
            rowSelection: this.rowSelection
        };
    }
    private calcColor(str: string): string {
        if ( str ) {
            let hash = 0;
            let colour = '#';
            for ( let i = 0 ; i < str.length ; i++ ) {
                hash = str.charCodeAt(i) + ((hash << 5) - hash);
            }
            for ( let i = 0 ; i < 3 ; i++ ) {
                colour += ('00' + ((hash >> i * 8) & 0xFF).toString(16)).slice(-2);
            }
            this.previousColor = colour;
        }
        return this.previousColor;
    }
    private makeColumnDefs(): any {
        return [
            {
                headerName: '',
                field: 'agent',
                width: 10,
                minWidth: 10,
                maxWidth: 10,
                cellStyle: (params: any) => {
                    return { backgroundColor: this.calcColor(params.value) };
                },
                cellRenderer: (params: any) => {
                    return '';
                }
            },
            {
                headerName: 'Method',
                field: 'method',
                width: 420,
                cellRenderer: 'agGroupCellRenderer',
                cellRendererParams: {
                    innerRenderer: this.innerCellRenderer,
                    suppressCount: true
                },
                tooltipField: 'method'
            },
            {
                headerName: 'Argument',
                field: 'argument',
                width: 250,
                cellStyle: this.argumentCellStyle,
                tooltipField: 'argument'
            },
            {
                headerName: 'StartTime',
                field: 'startTime',
                width: 100,
                suppressSizeToFit: true,
                valueFormatter: (params: any) => {
                    return params.value === 0 ? '' : moment(params.value).tz(this.timezone).format(this.dateFormat);
                }
            },
            {
                headerName: 'Gap(ms)',
                field: 'gap',
                width: 75,
                suppressSizeToFit: true,
                cellStyle: this.alignRightCellStyle,
                valueFormatter: (params: any) => {
                    return params.value === '' ? '' : new Intl.NumberFormat().format(params.value);
                }
            },
            {
                headerName: 'Exec(ms)',
                field: 'exec',
                width: 78,
                suppressSizeToFit: true,
                cellStyle: this.alignRightCellStyle,
                valueFormatter: (params: any) => {
                    return params.value === '' ? '' : new Intl.NumberFormat().format(params.value);
                }
            },
            {
                headerName: 'Exec(%)',
                field: 'execPer',
                width: 100,
                minWidth: 100,
                maxWidth: 100,
                cellRenderer: (params: any) => {
                    if ( params.value === '' ) {
                        return '';
                    }
                    const adjustRatio = 0.92;
                    const value = params.value;
                    const eDivPercentBar = this.windowRefService.nativeWindow.document.createElement('div');
                    eDivPercentBar.className = 'div-percent-bar';
                    eDivPercentBar.style.width = (value * adjustRatio) + '%';
                    eDivPercentBar.style.top = '10px';
                    eDivPercentBar.style.backgroundColor = '#5bc0de';
                    const eDivSelfBar = this.windowRefService.nativeWindow.document.createElement('div');
                    eDivSelfBar.className = 'div-percent-bar';
                    eDivSelfBar.style.height = '18%';
                    eDivSelfBar.style.top = '12px';
                    if ( params.data.selpPer ) {
                        eDivSelfBar.style.width = (((value * params.data.selpPer) / 100) * adjustRatio) + '%';
                    } else {
                        eDivSelfBar.style.width = '0%';
                    }
                    eDivSelfBar.style.backgroundColor = '#4343C8';
                    const eOuterDiv = this.windowRefService.nativeWindow.document.createElement('div');
                    eOuterDiv.className = 'div-outer-div';
                    eOuterDiv.appendChild(eDivPercentBar);
                    eOuterDiv.appendChild(eDivSelfBar);
                    return eOuterDiv;
                }
            },
            {
                headerName: 'Self(ms)',
                field: 'selp',
                width: 78,
                suppressSizeToFit: true,
                cellStyle: this.alignRightCellStyle,
                valueFormatter: function(params: any) {
                    return params.value === '' ? '' : new Intl.NumberFormat().format(params.value);
                }
            },
            {
                headerName: 'Class',
                field: 'clazz',
                width: 150,
                tooltipField: 'clazz'
            },
            {
                headerName: 'API',
                field: 'api',
                width: 150,
                tooltipField: 'api'
            },
            {
                headerName: 'Agent',
                field: 'agent',
                width: 150,
                tooltipField: 'agent'
            },
            {
                headerName: 'Application',
                field: 'application',
                width: 150,
                tooltipField: 'application'
            }
        ];
    }
    argumentCellStyle(): any {
        return {'text-align': 'left'};
    }
    alignRightCellStyle(): any {
        return {'text-align': 'right'};
    }
    timeFormatter(params: any): string {
        return params.value === 0 ? '' : moment(params.value).tz(this.timezone).format(this.dateFormat);
    }
    numberFormatter(params: any): string {
        return params.value === '' ? '' : new Intl.NumberFormat().format(params.value);
    }
    innerCellRenderer(params: any) {
        let result = '';
        if (params.data.hasException) {
            result += '<i class="fa fa-fire" style="color:red"></i>&nbsp;';
        } else if (!params.data.isMethod) {
            if (params.data.method === 'SQL') {
                result += '<button type="button" class="btn btn-blue" style="padding: 0px 2px; height: 20px;"><i class="fa fa-database"></i> ' + params.data.method + '</button>&nbsp;';
                return '&nbsp;' + result;
            } else if (params.data.method === 'MONGO-JSON') {
                result += '<button type="button" class="btn btn-blue" style="padding: 0px 2px; height: 20px;"><i class="fa fa-database"></i> JSON</button>&nbsp;';
                return '&nbsp;' + result;
            } else {
                result += '<i class="fa fa-info-circle"></i>&nbsp;';
            }
        } else {
            const itemMethodType = +params.data.methodType;
            switch ( itemMethodType ) {
            case 100:
                result += '<i class="fa fa-paper-plane"></i>&nbsp;';
                break;
            case 200:
                result += '<i class="fa fa-exchange"></i>&nbsp;';
                break;
            case 900:
                result += '<i class="fa fa-eclamation-triangle" style="color:#FF6600"></i>&nbsp;';
                break;
            }
        }
        return '&nbsp;' + result + params.data.method;
    }
    onCellClick(params: any): void {
        if (params.colDef.field === 'method') {
            let paramValue;
            if (params.value === 'SQL' || params.value === 'MONGO-JSON') {
                paramValue = params.value.split('-').pop();
                this.outSelectFormatting.next({
                    type: paramValue,
                    formatText: params.data.argument,
                    index: params.data.index
                });
            }
        }
    }
    onCellDoubleClicked(params: any): void {
        this.outCellDoubleClicked.next(params.data[params.colDef.field]);
    }
    onRendered(): void {
        this.gridOptions.api.sizeColumnsToFit();
    }
    searchRow({type, query}: {type: string, query: string | number}): number {
        let resultCount = 0;
        let targetIndex = -1;
        const fnCompare: { [key: string]: Function } = {
            'all': (data: any, value: string): boolean => {
                return (data.method && data.method.indexOf(value) !== -1) ||
                    (data.argument && data.argument.indexOf(value) !== -1) ||
                    (data.clazz && data.clazz.indexOf(value) !== -1) ||
                    (data.api && data.api.indexOf(value) !== -1) ||
                    (data.agent && data.agent.indexOf(value) !== -1) ||
                    (data.application && data.application.indexOf(value) !== -1);
            },
            'self': (data: any, value: number): boolean => {
                return +data.selp >= +value;
            },
            'argument': (data: any, value: string): boolean => {
                return data.argument.indexOf(value) !== -1;
            }
        };
        this.gridOptions.api.forEachNode((rowNode: RowNode) => {
            if (fnCompare[type](rowNode.data, query)) {
                if (resultCount === 0) {
                    targetIndex = rowNode.data.index;
                }
                resultCount++;
                rowNode.setSelected(true);
            } else {
                rowNode.setSelected(false);
            }
        });
        if (resultCount > 0) {
            this.gridOptions.api.ensureIndexVisible(targetIndex, 'top');
        }
        return resultCount;
    }
    moveRow(id: string): void {
        let targetIndex = -1;
        this.gridOptions.api.forEachNode((rowNode: RowNode) => {
            if (rowNode.data.id === id) {
                targetIndex = rowNode.data.index;
                rowNode.setSelected(true);
            } else {
                rowNode.setSelected(false);
            }
        });
        this.gridOptions.api.ensureIndexVisible(targetIndex, 'top');
    }
}
