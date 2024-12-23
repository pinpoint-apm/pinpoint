import { abbreviateNumber, addCommas, numberInDecimal } from '../number';

export const getMaxTickValue = (data: number[][], startIndex = 0, endIndex?: number): number => {
  const maxData = Math.max(
    ...data
      .slice(startIndex, endIndex)
      .map((d: number[]) => d.slice(startIndex))
      .flat(),
  );
  const adjustedMax = maxData + maxData / 4;
  const baseUnitNumber = 1000;
  const maxTick = Array(4)
    .fill(0)
    .reduce((acc: number, _: number, i: number, arr: number[]) => {
      const unitNumber = Math.pow(baseUnitNumber, i);

      return acc / unitNumber >= baseUnitNumber
        ? acc
        : (arr.splice(i + 1), getNearestNiceNumber(acc / unitNumber) * unitNumber);
    }, adjustedMax);

  return maxTick;
};

const getNearestNiceNumber = (v: number) => {
  /**
   * ex: v = 10.2345
   * 정수부 자릿수 switch/case
   * 1자리면 4의배수
   * 2자리면 20의배수
   * 3자리면 100의배수
   * v보다 큰 가장가까운 수 리턴해주기.
   */
  const integerPartLength = v.toString().split('.')[0].length;

  switch (integerPartLength) {
    case 1:
      return Math.ceil(v / 4) * 4;
    case 2:
      return Math.ceil(v / 20) * 20;
    case 3:
      return Math.ceil(v / 100) * 100;
  }
  return 0;
};

export const hasWindow = () => {
  return typeof window !== 'undefined';
};

export const getEllipsisText = ({
  text,
  maxTextLength,
}: {
  text: string;
  maxTextLength: number;
}): string => {
  return text.length >= maxTextLength ? `${text.substring(0, maxTextLength - 3)}...` : text;
};

const enum DATA_UNIT {
  BYTES = 'bytes',
  BYTE = 'byte',
  COUNT = 'count',
  PERCENT = 'percent',
  TIME = 'time',
}

export const getFormat = (dataUnit: string) => {
  switch (dataUnit) {
    case DATA_UNIT.BYTES:
    case DATA_UNIT.BYTE:
      return (v: number) => abbreviateNumber(v, ['', 'K', 'M', 'G']);
    case DATA_UNIT.COUNT:
      return (v: number) => addCommas(v);
    case DATA_UNIT.PERCENT:
      return (v: number) => (Number.isInteger(v) ? `${v}%` : `${numberInDecimal(v, 2)}%`);
    case DATA_UNIT.TIME:
      return (v: number) => abbreviateNumber(v, ['ms', 'sec']);
    default:
      return (v: number) => `${v}`;
  }
};

export const getTooltipStr = (title: string, contentsData: unknown[]) => {
  const header = `<tr><th colspan="2">${title}</th></tr>`;
  const body = contentsData
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    .map((d: any) => {
      const { id, value, color } = d;

      return `
        <tr class="bb-tooltip-name-${id}">
          <td class="name"><span style="background-color:${color}"></span>${id}</td>
          <td class="value">${value}</td>
        </tr>
      `;
    })
    .join('');

  return `<table class="bb-tooltip"><tbody>${header}${body}</tbody></table>`;
};
