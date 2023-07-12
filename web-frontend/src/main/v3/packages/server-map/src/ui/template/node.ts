import { Node } from '../../types';
import { defaultTheme } from '../../constants/style/theme';

type TransactionInfo = Node['transactionInfo'];

const MIN_ARC_RATIO = 0.05;
const RADIUS = 47;
const DIAMETER = 2 * Math.PI * RADIUS;

export const getTransactionStatusSVGString = (nodeData: Node): string => {
  const { transactionInfo } = nodeData;
  return (
    'data:image/svg+xml;charset=utf-8,' +
    encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" xmlns:xlink="http://www.w3.org/1999/xlink">
      ${getTransactionStatusSVGCircle(transactionInfo, !transactionInfo)}
    </svg>
  `)
  );
};

type SVGCircleParam = {
  stroke?: string;
  strokeWidth?: number;
  strokeDashOffset?: number;
  strokeDashArray?: string | number;
};

const getSVGCircle = (style: SVGCircleParam) => {
  const { stroke, strokeWidth, strokeDashOffset = 0, strokeDashArray = 'none' } = style;

  return `
    <circle cx="50" cy="50" r="${RADIUS}"
      style="fill:none;
      stroke:${stroke};
      stroke-width:${strokeWidth};
      stroke-dashoffset:${strokeDashOffset};
      stroke-dasharray:${strokeDashArray} 1000" 
    />
  `;
};

const calcArc = (sum: number, value: number): number => {
  return value === 0 ? 0 : value / sum < MIN_ARC_RATIO ? DIAMETER * MIN_ARC_RATIO : (value / sum) * DIAMETER;
};

const getTransactionStatusSVGCircle = (transactionInfo: TransactionInfo, isMerged: boolean): string => {
  const { transactionStatus } = defaultTheme;

  if (isMerged || !transactionInfo) {
    return getSVGCircle({
      stroke: transactionStatus!.default!.stroke,
      strokeWidth: transactionStatus!.default!.strokeWidth,
    });
  } else {
    const sum = Object.keys(transactionInfo).reduce(
      (prev: number, curr: string) => prev + transactionInfo[curr as keyof TransactionInfo],
      0,
    );
    const slowArc = calcArc(sum, transactionInfo.slow);
    const badArc = calcArc(sum, transactionInfo.bad);
    // 원의 중심을 (0,0)이라고 할때, stroke-dashoffset 시작점이 12시방향(0,r)이 아니라 3시방향(r,0)이라서 3/4지름을 기준으로 사용
    const slowArcOffset = -1 * (0.75 * DIAMETER - (slowArc + badArc));
    const badArcOffset = -1 * (0.75 * DIAMETER - badArc);

    return (
      getSVGCircle({
        stroke: transactionStatus.good!.stroke,
        strokeWidth: transactionStatus.good!.strokeWidth,
      }) +
      getSVGCircle({
        stroke: transactionStatus.slow!.stroke,
        strokeWidth: transactionStatus.slow!.strokeWidth,
        strokeDashOffset: slowArcOffset,
        strokeDashArray: slowArc,
      }) +
      getSVGCircle({
        stroke: transactionStatus.bad!.stroke,
        strokeWidth: transactionStatus.bad!.strokeWidth,
        strokeDashOffset: badArcOffset,
        strokeDashArray: badArc,
      })
    );
  }
};
