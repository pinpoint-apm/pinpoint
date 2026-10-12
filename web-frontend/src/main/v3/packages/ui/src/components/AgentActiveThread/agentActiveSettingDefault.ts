// AgentActiveSetting 은 눌러야 열리는 팝업이라 지연 로딩한다. 기본값은 AgentActiveThreadFetcher 도
// 쓰므로, zod·react-hook-form 이 딸려 오지 않도록 따로 둔다.
export const DefaultValue = { yMax: 100, isSplit: true, inactivityThreshold: 5 };
