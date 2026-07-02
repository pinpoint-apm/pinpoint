export const convertParamsToQueryString = (params: object): string => {
  const queryString = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined) {
      queryString.append(key, String(value));
    }
  });

  return queryString.toString();
};

export const extractStringAfterSubstring = (inputString = '', substring = '') => {
  const index = inputString.indexOf(substring);

  return index !== -1 ? inputString.substring(index + substring.length) : '';
};

export function decodeHTMLEntities(text: string): string {
  const entities: { [key: string]: string } = {
    '&lt;': '<',
    '&gt;': '>',
    '&amp;': '&',
    '&quot;': '"',
    '&#39;': "'",
  };

  return text.replace(/&lt;|&gt;|&amp;|&quot;|&#39;/g, (match) => entities[match]);
}

// HTML 문자열에 서버 제공 값을 삽입할 때 인젝션을 막기 위해 특수문자를 엔티티로 이스케이프한다.
export function escapeHTMLEntities(text: string): string {
  const entities: { [key: string]: string } = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  };

  return text.replace(/[&<>"']/g, (match) => entities[match]);
}
