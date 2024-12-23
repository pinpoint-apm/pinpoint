export const isRegexString = (str: string) => {
  return /^\/.*\/[gi]*$/.test(str);
};
