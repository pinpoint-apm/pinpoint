export const removeSpaces = (input: string) => {
  return `${input}`.replace(/\s/g, '');
};

export const capitalize = (word: string) => {
  return word.charAt(0).toUpperCase() + word.slice(1);
};
