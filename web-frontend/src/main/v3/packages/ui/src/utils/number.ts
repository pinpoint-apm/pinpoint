export const addCommas = (str: string | number): string => {
  return `${str}`.replace(/(\d)(?=(?:\d{3})+(?!\d))/g, '$1,');
};

export const abbreviateNumber = (value: number, unitList: string[]): string => {
  return value === null
    ? ''
    : [...unitList].reduce((acc: string, curr: string, i: number, arr: string[]) => {
        const v = Number(acc);

        return v >= 1000
          ? (v / 1000).toString()
          : (arr.splice(i + 1), Number.isInteger(v) ? `${v}${curr}` : `${v?.toFixed(2)}${curr}`);
      }, value.toString());
};

export const numberInDecimal = (v: number, decimalPlace: number): string => {
  return (Math.floor(v * 100) / 100).toFixed(decimalPlace);
};

export const numberInInteger = (v: number): number => {
  return Math.round(v);
};
