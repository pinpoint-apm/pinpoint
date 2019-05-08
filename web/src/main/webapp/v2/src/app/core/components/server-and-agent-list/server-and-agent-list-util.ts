export function filterServerList(serverList: { [key: string]: IServerAndAgentData[] }, query: string, predi: any): { [key: string]: IServerAndAgentData[] } {
    return query === ''
        ? serverList
        : Object.keys(serverList).reduce((acc: { [key: string]: IServerAndAgentData[] }, key: string) => {
            const matchedList = serverList[key].filter(predi);

            return matchedList.length !== 0 ? { ...acc, [key]: matchedList } : acc;
        }, {} as { [key: string]: IServerAndAgentData[] });
}
