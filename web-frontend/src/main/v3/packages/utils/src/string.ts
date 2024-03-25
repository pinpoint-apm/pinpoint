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
