import React from 'react';
import { throttle } from 'lodash';
import cloneDeep from 'lodash.clonedeep';
import { FlameNode } from './FlameNode';

export interface FlameNode<T> {
  id: string;
  name: string;
  duration: number;
  start: number;
  nodeStyle?: React.CSSProperties;
  textStyle?: React.CSSProperties;
  x?: number;
  y?: number;
  width?: number;
  height?: number;
  detail: T;
  children: FlameNode<T>[];
}

export interface FlameGraphProps<T> {
  data: FlameNode<T>[];
  start?: number;
  end?: number;
  onClickNode?: (e: React.MouseEvent, node: FlameNode<T>) => void;
}

export const FlameGraph = <T,>({ data, start = 0, end = 0, onClickNode }: FlameGraphProps<T>) => {
  const nodeHeight = 20;
  const padding = 0;
  const groupPadding = 20;
  const widthOffset = end - start || 1;

  const prevDepth = React.useRef(0);
  const containerRef = React.useRef<HTMLDivElement>(null);
  const svgRef = React.useRef<SVGSVGElement>(null);
  const [width, setWidth] = React.useState(0);
  const widthRatio = width / widthOffset;

  React.useEffect(() => {
    if (containerRef.current) {
      const throttledCalculateHeight = throttle(() => {
        setWidth(containerRef.current?.clientWidth || width);
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

  const styleNode = (node: FlameNode<T>, depth = 0, xOffset = 0, yOffset = 0) => {
    const children = node.children || [];
    const widthPerNode = node.duration * widthRatio;
    node.x = (node.start - start) * widthRatio;
    node.y = depth * (nodeHeight + padding) + yOffset;
    node.width = widthPerNode;
    node.height = nodeHeight;
    let currentXOffset = xOffset;

    children.forEach((child) => {
      styleNode(child, depth + 1, currentXOffset, yOffset);
      currentXOffset += widthPerNode;
    });
  };

  const getContainerHeight = () => {
    return data.reduce((acc, curr, i) => {
      return acc + getMaxDepth(curr) * nodeHeight + groupPadding * i;
    }, 0);
  };

  const getMaxDepth = (node: FlameNode<T>, depth = 0) => {
    let MaxDepth = depth;

    node.children.forEach((child) => {
      const childDepth = getMaxDepth(child, depth + 1);

      MaxDepth = Math.max(MaxDepth, childDepth);
    });

    return MaxDepth;
  };

  return (
    <div className="relative w-full h-full overflow-x-hidden overflow-y-auto" ref={containerRef}>
      <svg width={width} height={getContainerHeight()} ref={svgRef}>
        {width &&
          // group별 렌더링
          data.map((node, i) => {
            if (i === 0) prevDepth.current = 0;

            const newNode = cloneDeep(node);
            const yOffset = prevDepth.current * nodeHeight + groupPadding * i;

            styleNode(newNode, 0, 0, yOffset);
            prevDepth.current = getMaxDepth(newNode) + prevDepth.current + 1;

            return (
              <FlameNode key={node.id} node={newNode} svgRef={svgRef} onClickNode={onClickNode} />
            );
          })}
      </svg>
    </div>
  );
};
