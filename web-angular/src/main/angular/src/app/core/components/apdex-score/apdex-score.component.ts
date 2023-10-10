import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';

import {getApdexScoreRank} from './apdex-score-utils';

@Component({
    selector: 'pp-apdex-score',
    templateUrl: './apdex-score.component.html',
    styleUrls: ['./apdex-score.component.css']
})
export class ApdexScoreComponent implements OnInit, OnChanges {
    @Input() isEmpty: boolean;
    @Input() score: number;

    fixedScore: string;
    scoreStyleClass: string

    constructor() {
    }

    ngOnInit() {
    }

    ngOnChanges(changes: SimpleChanges) {
        const score: number = changes.score.currentValue;

        if (!isNaN(Number(score))) {
            this.scoreStyleClass = getApdexScoreRank(score);
            this.fixedScore = (Math.floor(score * 100) / 100).toFixed(2);
        }
    }
}
