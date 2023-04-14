export enum APDEX_SCORE_RANK {
    EXCELLENT = 'excellent',
    GOOD = 'good',
    FAIR = 'fair',
    POOR = 'poor',
    UNACCEPTABLE = 'unacceptable',
}

export function getApdexScoreRank(score: number): string {
    return score < 0.5 ? APDEX_SCORE_RANK.UNACCEPTABLE
        : score < 0.7 ? APDEX_SCORE_RANK.POOR
            : score < 0.85 ? APDEX_SCORE_RANK.FAIR
                : score < 0.94 ? APDEX_SCORE_RANK.GOOD
                    : APDEX_SCORE_RANK.EXCELLENT;
}
