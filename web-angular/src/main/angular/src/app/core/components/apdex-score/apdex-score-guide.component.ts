import {Component, OnInit} from '@angular/core';

import {IApdexFormulaData} from './apdex-score-data.service';
import {ApdexScoreInteractionService} from './apdex-score-interaction.service';
import {APDEX_SCORE_RANK} from './apdex-score-utils';

@Component({
    selector: 'pp-apdex-score-guide',
    templateUrl: './apdex-score-guide.component.html',
    styleUrls: ['./apdex-score-guide.component.css']
})
export class ApdexScoreGuideComponent implements OnInit {
    apdexScoreGuide: { range: string, rank: APDEX_SCORE_RANK }[];
    apdexFormulaData: IApdexFormulaData;

    constructor(
        private apdexScoreInteractionService: ApdexScoreInteractionService
    ) {
    }

    ngOnInit() {
        this.apdexScoreGuide = [
            {range: '0.94 ~ 1.00', rank: APDEX_SCORE_RANK.EXCELLENT},
            {range: '0.85 ~ 0.94', rank: APDEX_SCORE_RANK.GOOD},
            {range: '0.7 ~ 0.85', rank: APDEX_SCORE_RANK.FAIR},
            {range: '0.5 ~ 0.7', rank: APDEX_SCORE_RANK.POOR},
            {range: '< 0.5', rank: APDEX_SCORE_RANK.UNACCEPTABLE},
        ];

        this.apdexScoreInteractionService.onApdexFormulaData$.subscribe((data: IApdexFormulaData) => {
            this.apdexFormulaData = data;
        });
    }

    getIconClass(apdexScoreRank: APDEX_SCORE_RANK) {
        const iconClass = apdexScoreRank === APDEX_SCORE_RANK.EXCELLENT ? 'fa-smile-beam'
            : apdexScoreRank === APDEX_SCORE_RANK.GOOD ? 'fa-smile'
                : apdexScoreRank === APDEX_SCORE_RANK.FAIR ? 'fa-meh'
                    : apdexScoreRank === APDEX_SCORE_RANK.POOR ? 'fa-frown'
                        : 'fa-angry';

        return `${apdexScoreRank} ${iconClass}`;
    }
}
