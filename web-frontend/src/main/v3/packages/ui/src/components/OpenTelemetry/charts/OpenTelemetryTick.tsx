export const OpenTelemetryTick = (props: any) => {
  const { payload, tickFormatter, x, y } = props;
  const tickString = tickFormatter?.(payload?.value) || '';
  return (
    <g transform={`translate(${x},${y})`}>
      {tickString?.split('\n').map((tString: string, index: number) => (
        <text key={index} x={0} y={index * 10} dy={10} textAnchor="middle" fill="#666">
          {tString}
        </text>
      ))}
    </g>
  );
};
