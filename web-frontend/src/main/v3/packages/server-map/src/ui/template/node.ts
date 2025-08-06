import { Node } from '../../types';
import { defaultTheme } from '../../constants/style/theme';
import { ServerMapProps } from '../ServerMap';

type TransactionInfo = Node['transactionInfo'];
type TimeSeriesApdexInfo = Node['timeSeriesApdexInfo'];

const MIN_ARC_RATIO = 0.05;
const RADIUS = 47;
const DIAMETER = 2 * Math.PI * RADIUS;

export const getNodeSVGString = (nodeData: Node, renderNode?: ServerMapProps['renderNode']) => {
  const { transactionInfo, timeSeriesApdexInfo } = nodeData;

  // timeSeriesApdexInfo가 있을 때는 새로운 Apdex SVG를 사용
  const statusSVGString = timeSeriesApdexInfo
    ? getTimeSeriesApdexStatusSVGCircle(timeSeriesApdexInfo)
    : getTransactionStatusSVGCircle(transactionInfo, !transactionInfo);

  return (
    'data:image/svg+xml;charset=utf-8,' +
    encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" xmlns:xlink="http://www.w3.org/1999/xlink">
      ${renderNode ? renderNode(nodeData, statusSVGString) : statusSVGString}
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

const getTimeSeriesApdexStatusSVGCircle = (timeSeriesApdexInfo: TimeSeriesApdexInfo): string => {
  if (!timeSeriesApdexInfo || timeSeriesApdexInfo.length === 0) {
    return '';
  }

  // Apdex 등급별 색상 매핑
  const colorMap: Record<string, string> = {
    Excellent: defaultTheme!.timeSeriesApdexStatus!.excellent!,
    Good: defaultTheme!.timeSeriesApdexStatus!.good!,
    Fair: defaultTheme!.timeSeriesApdexStatus!.fair!,
    Poor: defaultTheme!.timeSeriesApdexStatus!.poor!,
    Unacceptable: defaultTheme!.timeSeriesApdexStatus!.unacceptable!,
  };

  // Apdex 점수에 따라 등급 반환
  const getApdexGrade = (score: number): string => {
    if (score >= 0.94) return 'Excellent';
    if (score >= 0.85) return 'Good';
    if (score >= 0.7) return 'Fair';
    if (score >= 0.5) return 'Poor';
    return 'Unacceptable';
  };

  const segmentCount = timeSeriesApdexInfo.length;
  const segmentAngle = 360 / segmentCount;
  const cx = 50;
  const cy = 50;
  const r = RADIUS;
  const strokeWidth = 8;
  let svgString = '';

  // 12시 방향부터 반시계방향으로 slot을 채운다. (12~1시가 가장 최신 데이터)
  for (let i = 0; i < segmentCount; i++) {
    const score = timeSeriesApdexInfo[i];
    const grade = getApdexGrade(score);
    const color = colorMap[grade] || '#cccccc';
    // 각 segment의 시작/끝 각도
    const startAngle = -90 - i * segmentAngle;
    const endAngle = startAngle - segmentAngle;
    // 각도를 라디안으로 변환
    const startRad = (startAngle * Math.PI) / 180;
    const endRad = (endAngle * Math.PI) / 180;
    // 시작점, 끝점 좌표 계산
    const x1 = cx + r * Math.cos(startRad);
    const y1 = cy + r * Math.sin(startRad);
    const x2 = cx + r * Math.cos(endRad);
    const y2 = cy + r * Math.sin(endRad);
    // arc 플래그: 180도 이상이면 1, 아니면 0
    const largeArcFlag = segmentAngle > 180 ? 1 : 0;
    svgString += `
      <path d="M ${x1} ${y1}
        A ${r} ${r} 0 ${largeArcFlag} 0 ${x2} ${y2}"
        stroke="${color}"
        stroke-width="${strokeWidth}"
        fill="none"
        />
    `;
  }

  return svgString;
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
