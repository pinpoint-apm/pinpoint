export interface FlameArrowProps {
  x1?: number;
  y1?: number;
  x2?: number;
  y2?: number;
}

export const FlameArrow = ({ x1, y1, x2, y2 }: FlameArrowProps) => {
  if (!x1 || !y1 || !x2 || !y2 || (x1 === x2 && y1 === y2)) {
    return null;
  }

  const dx = Math.abs(x2 - x1);
  const dy = Math.abs(y2 - y1);

  const cx1 = x1 + dx * 0.3;
  const cy1 = y2 > y1 ? y1 + dy * 0.5 : y1 - dy * 0.5;
  const cx2 = x2 - dx * 0.4 - 20;
  const cy2 = y2 > y1 ? y2 - dy * 0.5 : y2 + dy * 0.5;

  return (
    <>
      <marker
        id="arrowhead"
        markerWidth="8"
        markerHeight="5.6"
        refX="8"
        refY="2.8"
        orient="auto"
        markerUnits="strokeWidth"
      >
        <polygon points="0 0, 8 2.8, 0 5.6" fill="black" />
      </marker>
      <path
        d={`M ${x1},${y1} C ${cx1},${cy1} ${cx2},${cy2} ${x2},${y2}`}
        fill="none"
        stroke="black"
        strokeWidth="1.25"
        markerEnd="url(#arrowhead)"
      />
    </>
  );
};
