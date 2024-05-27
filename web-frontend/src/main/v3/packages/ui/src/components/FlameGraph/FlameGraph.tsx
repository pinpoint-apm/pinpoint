import React from 'react';
import { throttle } from 'lodash';
import cloneDeep from 'lodash.clonedeep';
import { FlameNode, FlameNodeClickHandler, FlameNodeType } from './FlameNode';
import { FlameAxis } from './FlameAxis';
import { FlameGraphConfigContext, flameGraphDefaultConfig } from './FlameGraphConfigContext';
import { FlameTimeline } from './FlameTimeline';

export interface FlameGraphProps<T> {
  data: FlameNodeType<T>[];
  start?: number;
  end?: number;
  onClickNode?: FlameNodeClickHandler<T>;
}

export const FlameGraph = <T,>({ data, start = 0, end = 0, onClickNode }: FlameGraphProps<T>) => {
  const widthOffset = end - start || 1;
  const [config] = React.useState(flameGraphDefaultConfig);

  const prevDepth = React.useRef(0);
  const containerRef = React.useRef<HTMLDivElement>(null);
  const svgRef = React.useRef<SVGSVGElement>(null);
  const [containerWidth, setWidth] = React.useState(0);
  const containerHeight = getContainerHeight();
  const widthRatio = (containerWidth - config.padding.left - config.padding.right) / widthOffset;

  React.useEffect(() => {
    if (containerRef.current) {
      const throttledCalculateHeight = throttle(() => {
        setWidth(containerRef.current?.clientWidth || containerWidth);
      }, 200);

      const resizeObserver = new ResizeObserver(() => {
        throttledCalculateHeight();
      });

      resizeObserver.observe(containerRef.current);

      return () => {
        resizeObserver.disconnect();
      };
    }
  }, []);

  const styleNode = (node: FlameNodeType<T>, depth = 0, xOffset = 0, yOffset = 0) => {
    const children = node.children || [];
    const { height, padding } = config;
    const widthPerNode = node.duration * widthRatio;
    node.x = (node.start - start) * widthRatio + padding.left;
    node.y = depth * height.node + yOffset + padding.top;
    node.width = widthPerNode;
    node.height = height.node;
    let currentXOffset = xOffset;

    children.forEach((child) => {
      styleNode(child, depth + 1, currentXOffset, yOffset);
      currentXOffset += widthPerNode;
    });
  };

  function getContainerHeight() {
    const { height, padding } = config;
    return data.reduce((acc, curr) => {
      return acc + (getMaxDepth(curr) + 1) * height.node + padding.group;
    }, 2 * padding.bottom);
  }

  function getMaxDepth(node: FlameNodeType<T>, depth = 0) {
    let MaxDepth = depth;

    node.children.forEach((child) => {
      const childDepth = getMaxDepth(child, depth + 1);

      MaxDepth = Math.max(MaxDepth, childDepth);
    });

    return MaxDepth;
  }

  return (
    <FlameGraphConfigContext.Provider value={{ config }}>
      <div className="relative w-full h-full overflow-x-hidden" ref={containerRef}>
        <svg width={containerWidth} height={config.height.timeline} className="shadow-md">
          <FlameTimeline width={containerWidth} start={start} end={end} />
        </svg>
        <div className="w-full h-[calc(100%-3rem)] overflow-y-auto overflow-x-hidden">
          <svg width={containerWidth} height={containerHeight} ref={svgRef}>
            <FlameAxis width={containerWidth} />
            {containerWidth &&
              // group별 렌더링
              data.map((node, i) => {
                if (i === 0) prevDepth.current = 0;

                const { height, padding, color } = config;
                const newNode = cloneDeep(node);
                const currentNodeDepth = getMaxDepth(newNode);
                const yOffset = prevDepth.current * height.node + padding.group * i;

                styleNode(newNode, 0, 0, yOffset);
                prevDepth.current = currentNodeDepth + prevDepth.current + 1;

                return (
                  <React.Fragment key={node.id}>
                    <FlameNode node={newNode} svgRef={svgRef} onClickNode={onClickNode} />
                    <line
                      x1={0}
                      y1={yOffset - padding.group / 2 + padding.top}
                      x2={containerWidth}
                      y2={yOffset - padding.group / 2 + padding.top}
                      stroke={color.axis}
                    />
                  </React.Fragment>
                );
              })}
          </svg>
        </div>
      </div>
    </FlameGraphConfigContext.Provider>
  );
};
