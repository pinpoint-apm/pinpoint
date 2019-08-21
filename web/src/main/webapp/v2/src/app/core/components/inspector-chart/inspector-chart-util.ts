export function getAgentId(columnId: string, i: number, minAgentIdList: string[], maxAgentIdList: string[]): string {
    return columnId === 'avg' ? '' : `(${columnId === 'min' ? minAgentIdList[i] : maxAgentIdList[i]})`;
}
