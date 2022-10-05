import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, AfterViewInit, HostBinding, Renderer2 } from '@angular/core';
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
    agentName?: string;
    application: string;
    isMethod: boolean;
    methodType: string;
    hasException: boolean;
    isAuthorized: boolean;
    isFocused: boolean;
    isSearchTarget: boolean;
    folder?: boolean;
    open?: boolean;
    children?: any[];
}

@Component({
    selector: 'pp-call-tree',
    templateUrl: './call-tree.component.html',
    styleUrls: ['./call-tree.component.css'],
})
export class CallTreeComponent implements OnInit, OnChanges, AfterViewInit {
    @HostBinding('class') hostClass = 'l-calltree';
    @Input() canSelectRow: boolean;
    @Input() rowSelection: string;
    @Input() selectedRowId: string;
    @Input()
    set callTreeData(callTreeData: ITransactionDetailData) {
        this.originalData = callTreeData;
        this.ratio = this.calcTimeRatio(callTreeData.callStack[0][callTreeData.callStackIndex.begin], callTreeData.callStack[0][callTreeData.callStackIndex.end]);
        this.rowData = this.makeGridData(callTreeData.callStack, callTreeData.callStackIndex);
    }

    @Input() timezone: string;
    @Input() dateFormat: string;
    @Output() outSelectFormatting = new EventEmitter<any>();
    @Output() outRowSelected = new EventEmitter<IGridData>();
    @Output() outCellDoubleClicked = new EventEmitter<string>();

    private originalData: ITransactionDetailData;
    private ratio: number;

    gridOptions: GridOptions;
    rowData: IGridData[];

    constructor(
        private windowRefService: WindowRefService,
        private renderer: Renderer2,
    ) {}

    ngAfterViewInit() {
        const rowIndex = this.originalData.callStack.find((cs: any[]) => cs[4] === this.originalData.applicationId)[6];

        this.moveRow(rowIndex);
    }

    ngOnInit() {
        this.initGridOptions();
    }

    ngOnChanges(changes: SimpleChanges) {
        Object.keys(changes)
            .filter((propName: string) => {
                return changes[propName].currentValue && !changes[propName].isFirstChange();
            })
            .forEach((propName: string) => {
                switch (propName) {
                    case 'timezone':
                    case 'dateFormat':
                        this.gridOptions.api.refreshCells({
                            columns: ['startTime'],
                            force: true
                        });
                        break;
                    case 'selectedRowId':
                        this.moveRow(changes[propName].currentValue);
                        break;
                }
            });
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
            // rowClassRules: {
            //     // 'ag-row-exception': ({data}: any) => data.hasException,
            //     // 'ag-row-focused': ({data}: any) => data.isFocused,
            //     // 'ag-row-search-target': ({data}: any) => data.isSearchTarget,
            //     'ag-row-exception': ({data}: any) => data.hasException,
            //     'ag-row-focused': (params: any) => {
            //         // console.log(params);
            //         // return false;
            //         // return params.node.state.isFocused;
            //         return params.node.state ? params.node.state.isFocused : false;
            //     },
            //     'ag-row-search-target': ({node}: any) => node.state ? node.state.isSearchTarget : false
            // },
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
            rowSelection: this.rowSelection,
            // processRowPostCreate: ({node}: any) => {
            //     node.state = {
            //         isFocused: node.state === undefined ? false : node.state.isFocused,
            //         isSearchTarget: node.state === undefined ? false : node.state.isSearchTarget
            //     }
            //     console.log(node.state);
            // }
        };
    }

    private calcColor({agent, index}: {[key: string]: any}): string {
        const agentKey = agent ? agent : this.getAgentKey(index);
        let hash = 0;
        let color = '#';

        for (let i = 0; i < agentKey.length; i++) {
            hash = agentKey.charCodeAt(i) + ((hash << 5) - hash);
        }
        for (let i = 0; i < 3; i++ ) {
            color += ('00' + ((hash >> i * 8) & 0xFF).toString(16)).slice(-2);
        }

        return color;
    }

    private getAgentKey(rowIndex: number): string {
        let agentKey = null;

        for (let i = rowIndex - 1; agentKey === null; i--) {
            agentKey = this.originalData.callStack[i][20]; // 20th index indicates agentKey
        }
        return agentKey;
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
                    return {backgroundColor: this.calcColor(params.data)};
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
                    innerRenderer: this.innerCellRenderer.bind(this),
                    suppressCount: true
                },
                tooltipField: 'method',
                cellStyle: (params: any) => {
                    const { level } = params.node;
                    const indent = 15;
                    const isRootRow = params.node.rowIndex === 0;

                    return {
                        paddingLeft: isRootRow ? `4px` : `${(level + 1) * indent}px`
                    };
                }
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
                },
                tooltipValueGetter: (params: any) => {
                    const dayFormat = 'YYYY-MM-DD';
                    return params.value === 0 ? '' : moment(params.value).tz(this.timezone).format(dayFormat);
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
                headerName: 'Agent Id',
                field: 'agent',
                width: 150,
                tooltipField: 'agent'
            },
            {
                headerName: 'Application',
                field: 'application',
                width: 150,
                tooltipField: 'application'
            },
            {
                headerName: 'Agent Name',
                field: 'agentName',
                width: 150,
                tooltipField: 'agentName'
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
            result += '<i class="fa fa-fire l-icon-without-btn" style="color:red"></i>';
        } else if (!params.data.isMethod) {
            if (params.data.method === 'SQL') {
                return '<button type="button" class="btn btn-blue l-btn-inside-method" style="padding:0px 2px;height:20px;"><i class="fa fa-database l-icon-inside-btn"></i>' + params.data.method + '</button>';
            } else if (params.data.method === 'MONGO-JSON') {
                return '<button type="button" class="btn btn-blue l-btn-inside-method" style="padding:0px 2px;height:20px;"><i class="fa fa-database l-icon-inside-btn"></i>JSON</button>';
            } else {
                result += '<i class="fa fa-info-circle l-icon-without-btn"></i>';
            }
        } else {
            const itemMethodType = +params.data.methodType;

            switch (itemMethodType) {
                case 100:
                    result += '<i class="fas fa-paper-plane l-icon-without-btn"></i>';
                    break;
                case 200:
                    result += '<i class="fas fa-exchange-alt l-icon-without-btn"></i>';
                    break;
                case 900:
                    result += '<i class="fas fa-exclamation-triangle l-icon-without-btn" style="color:#FF6600"></i>';
                    break;
            }
        }

        const span = this.renderer.createElement('span');
        const text = this.renderer.createText(params.data.method);

        this.renderer.setProperty(span, 'innerHTML', result);
        this.renderer.appendChild(span, text);

        return span;
    }

    onCellClick({colDef, value, data}: any): void {
        if (colDef.field === 'method') {
            if (value === 'SQL' || value === 'MONGO-JSON') {
                const type = value.split('-').pop();
                const nextRowData = this.originalData.callStack[data.index + 1];
                const nextValue = nextRowData[this.originalData.callStackIndex.title];
                let bindValue;

                if (nextRowData && (nextValue === 'SQL-BindValue' || nextValue === 'MONGO-JSON-BindValue')) {
                    bindValue = nextRowData[this.originalData.callStackIndex.arguments];
                }

                this.outSelectFormatting.emit({
                    type,
                    originalContents: data.argument,
                    bindValue
                });
            }
        }
    }

    onCellDoubleClicked(params: any): void {
        this.outCellDoubleClicked.emit(params.data[params.colDef.field]);
    }

    onRendered(): void {
        this.gridOptions.api.sizeColumnsToFit();
        this.gridOptions.api.forEachNode((row: any) => {
            row.state = {
                isFocused: false,
                isSearchTarget: false
            }
        })
    }

    onRowDataChanged(): void {
        if (this.gridOptions) {
            const rowIndex = this.originalData.callStack.find((cs: any[]) => cs[4] === this.originalData.applicationId)[6];

            this.moveRow(rowIndex);
        }
    }

    getQueryedRowCount({type, query, resultIndex}: {type: string, query: string, resultIndex: number}): number {
        let resultCount = 0;
        // let targetIndex = -1;

        this.gridOptions.api.forEachNode((rowNode: RowNode) => {
            if (this.hasValueOnType(type, rowNode.data, query)) {
                // if (resultCount === 0) {
                //     targetIndex = rowNode.data.index;
                // }

                resultCount++;
                rowNode.setSelected(true);
            } else {
                rowNode.setSelected(false);
            }
        });

        // if (resultCount > 0) {
        //     this.gridOptions.api.ensureIndexVisible(targetIndex, 'top');
        // }

        return resultCount;
    }

    getQueryedRowList({type, query}: {type: string, query: string}): RowNode[] {
        const rowList: RowNode[] = [];

        this.gridOptions.api.forEachNode((rowNode: RowNode) => {
        // this.gridOptions.api.forEachNode((rowNode: any) => {
            // console.log(rowNode);
            // rowNode.setDataValue('isFocused', false);
            // rowNode.state.isFocused = false
            if (this.hasValueOnType(type, rowNode.data, query)) {
                rowNode.setSelected(true);
                // rowNode.setDataValue('isSearchTarget', true);
                // rowNode.state.isSearchTarget = true;
                rowList.push(rowNode);
            } else {
                // rowNode.setDataValue('isSearchTarget', false);
                // rowNode.state.isSearchTarget = false;
                rowNode.setSelected(false);
            }
        });

        return rowList;
    }

    // focusTargetRow(rowIndex: number): void {
    focusTargetRow(row: RowNode): void {
    // focusTargetRow(row: any): void {
        this.gridOptions.api.ensureNodeVisible(row, 'middle');
        // this.gridOptions.api.ensureIndexVisible(rowIndex, 'middle');
        // this.gridOptions.api.ensureIndexVisible(rowIndex);
        // row.updateData({targetRow: true});
        // row.setDataValue('isFocused', true);
        // row.state.isFocused = true;
        // console.log(row);

        // ! dataValue 안 바뀌는이유가 col field로 사용되지않는 data value라 그럼
        // * Custom Attribute로는 뭔짓해도 안됨 ㅠ
        // * 아니면 setSelected를 그냥쓰고 이걸 searchTarget이라고 생각한다? 근데그럼 focus 이벤트에 대한 구분이 안됨..
        // * Row의 엘리먼트에 직접접근해서 addClass하기?
        /**
         * 문제정리
         * 1. searched와 target(focused)의 구분이 있었으면 좋겠음
         * 2. 근데 custom attribute에 대한 방법이 없는듯함
         * 3. 그나마 쓸수있는게 selected 이벤트인데, 필요한건 두가지(searched & focused) 경우의 구분임
         * 4. 어떻게 할 수 있을까
         */
    }

    private calcTimeRatio(begin: number, end: number): number {
        return 100 / (end - begin);
    }

    private makeGridData(callTreeData: any, oIndex: any): IGridData[] {
        const newData = [];
        const parentRef = {};

        for (let i = 0; i < callTreeData.length; i++) {
            const callTree = callTreeData[i];
            const oRow = <IGridData>{};

            parentRef[callTree[oIndex.id]] = oRow;
            this.makeRow(callTree, oIndex, oRow, i);
            if (callTree[oIndex.parentId]) {
                const oParentRow = parentRef[callTree[oIndex.parentId]];

                if (oParentRow.children instanceof Array === false) {
                    oParentRow['folder'] = true;
                    oParentRow['open'] = true;
                    oParentRow['children'] = [];
                }

                oParentRow.children.push(oRow);
            } else {
                newData.push(oRow);
            }
        }
        return newData;
    }

    private makeRow(callTree: any, oIndex: any, oRow: IGridData, index: number): void {
        oRow['index'] = index;
        oRow['id'] = callTree[oIndex.id];
        oRow['method'] = callTree[oIndex.title];
        oRow['argument'] = callTree[oIndex.arguments];
        oRow['startTime'] = callTree[oIndex.begin];
        oRow['gap'] = callTree[oIndex.gap];
        oRow['exec'] = callTree[oIndex.elapsedTime];
        oRow['execPer'] =  callTree[oIndex.elapsedTime] ? Math.ceil((callTree[oIndex.end] - callTree[oIndex.begin]) * this.ratio) : '';
        oRow['selp'] = callTree[oIndex.executionMilliseconds];
        oRow['selpPer'] = callTree[oIndex.elapsedTime] && callTree[oIndex.executionMilliseconds] ?
            ( Math.floor( callTree[oIndex.executionMilliseconds].replace(/,/gi, '') ) / Math.floor( callTree[oIndex.elapsedTime].replace(/,/gi, '') ) ) * 100
            : 0;
        oRow['clazz'] = callTree[oIndex.simpleClassName];
        oRow['api'] = callTree[oIndex.apiType];
        oRow['agent'] = callTree[oIndex.agent];
        oRow['agentName'] = callTree[oIndex.agentName];
        oRow['application'] = callTree[oIndex.applicationName];
        oRow['isMethod'] = callTree[oIndex.isMethod];
        oRow['methodType'] = callTree[oIndex.methodType];
        oRow['hasException'] = callTree[oIndex.hasException];
        oRow['isAuthorized'] = callTree[oIndex.isAuthorized];
        // oRow['isFocused'] = callTree[oIndex.isFocused];
        // oRow['isSearchTarget'] = false;
        if (callTree[oIndex.hasChild]) {
            oRow['folder'] = true;
            oRow['open'] = true;
            oRow['children'] = [];
        }
    }

    private hasValueOnType(type: string, data: any, query: string): boolean {
        const value = query.toLowerCase();

        switch (type) {
            case 'all':
                return (data.method && data.method.toLowerCase().includes(value)) ||
                    (data.argument && data.argument.toLowerCase().includes(value)) ||
                    (data.clazz && data.clazz.toLowerCase().includes(value)) ||
                    (data.api && data.api.toLowerCase().includes(value)) ||
                    (data.agent && data.agent.toLowerCase().includes(value)) ||
                    (data.application && data.application.toLowerCase().includes(value));
            case 'self':
                return +data.selp >= +value;
            case 'argument':
                return data.argument.toLowerCase().includes(value);
            case 'exception':
                return data.hasException && (value === '' ||
                    ((data.method && data.method.toLowerCase().includes(value)) ||
                    (data.argument && data.argument.toLowerCase().includes(value)))
                );
                // return data.hasException && (
                //     (data.method && data.method.indexOf(value) !== -1) ||
                //     (data.argument && data.argument.indexOf(value) !== -1)
                // )
        }
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

        this.gridOptions.api.ensureIndexVisible(targetIndex, 'middle');
    }
}

// TODO: Selected, Searched, Focused 구분 필요할듯?
/**
 * - initial -> Focused
 * - 검색 리스트 -> Searched
 * - 검색 리스트 중 타겟 -> Focused
 * 
 * Default Row 구분: 
 * 
 * 검색 대상들에 대해 setSelected가 아니라 setDataValue로 Searched 데이터밸류를 업데이트해야할듯
 * 이때 검색 리스트 중 타겟 Row에 대해서는 isFocused 데이터밸류 업데이트(true)
 * 고민) 
 */