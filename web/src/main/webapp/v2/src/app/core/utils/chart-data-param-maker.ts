export function getParamForAgentChartData(agentId: string, [from, to]: number[]): object {
    return {
        params: {
            agentId,
            from,
            to,
            sampleRate: 1
        }
    };
}

export function getParamForApplicationChartData(applicationId: string, [from, to]: number[]): object {
    return {
        params: {
            applicationId,
            from,
            to,
            sampleRate: 1
        }
    };
}
