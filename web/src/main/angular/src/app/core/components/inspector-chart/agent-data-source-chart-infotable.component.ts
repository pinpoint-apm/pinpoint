import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'pp-agent-data-source-chart-infotable',
    templateUrl: './agent-data-source-chart-infotable.component.html',
    styleUrls: ['./agent-data-source-chart-infotable.component.css']
})
export class AgentDataSourceChartInfotableComponent implements OnInit {
    @Input() infoTableObj: {[key: string]: any};

    constructor() {}
    ngOnInit() {}
}
