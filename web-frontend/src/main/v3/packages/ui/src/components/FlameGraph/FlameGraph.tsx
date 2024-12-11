import React from 'react';
import { throttle } from 'lodash';
import cloneDeep from 'lodash.clonedeep';
import { FlameNode, FlameNodeType, FlameNodeProps } from './FlameNode';
import { FlameAxis } from './FlameAxis';
import { FlameGraphConfigContext, flameGraphDefaultConfig } from './FlameGraphConfigContext';
import { FlameTimeline } from './FlameTimeline';

export interface FlameGraphProps<T>
  extends Pick<FlameNodeProps<T>, 'customNodeStyle' | 'customTextStyle' | 'onClickNode'> {
  data: FlameNodeType<T>[][];
  start?: number;
  end?: number;
}

export const FlameGraph = <T,>({
  data,
  start = 0,
  end = 0,
  onClickNode,
  customNodeStyle,
  customTextStyle,
}: FlameGraphProps<T>) => {
  const widthOffset = end - start || 1;
  const [config] = React.useState(flameGraphDefaultConfig);
  const [zoom, setZoom] = React.useState(1);

  const prevDepth = React.useRef(0);
  const containerRef = React.useRef<HTMLDivElement>(null);
  const svgRef = React.useRef<SVGSVGElement>(null);
  const [width, setWidth] = React.useState(0);
  const containerHeight = getContainerHeight();
  const containerWidth = width * zoom;
  const widthRatio = (containerWidth - config.padding.left - config.padding.right) / widthOffset;
  const [scrollLeft, setScrollLeft] = React.useState(0);

  React.useEffect(() => {
    const handleWheel = (event: WheelEvent) => {
      if (event.metaKey) {
        event.preventDefault();

        const container = containerRef.current;
        if (!container) return;

        const rect = container.getBoundingClientRect();
        const mouseX = event.clientX - rect.left;

        setZoom((prevZoom) => {
          const newZoom = Math.max(1, prevZoom - event.deltaY * 0.01);

          // Adjust the scroll position to keep the mouse position centered
          const scaleChange = newZoom / prevZoom;
          container.scrollLeft += mouseX * (scaleChange - 1) * prevZoom;

          setScrollLeft(container.scrollLeft);

          return newZoom;
        });
      }

      setScrollLeft(containerRef.current?.scrollLeft || 0);
    };

    const container = containerRef.current;

    container?.addEventListener('wheel', handleWheel);

    return () => {
      container?.removeEventListener('wheel', handleWheel);
    };
  }, []);

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

    return data.reduce((acc, group) => {
      const groupHeights = group.map(
        (node) => (getMaxDepth(node) + 1) * height.node + padding.group,
      );
      return acc + Math.max(...groupHeights);
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
      <div className="relative w-full h-full overflow-x-auto" ref={containerRef}>
        <svg width={containerWidth} height={config.height.timeline} className="shadow-md">
          <FlameTimeline width={containerWidth} start={start} end={end} zoom={zoom} />
        </svg>
        <div className="w-fit h-[calc(100%-3rem)] overflow-y-auto overflow-x-hidden">
          <svg width={containerWidth} height={containerHeight} ref={svgRef}>
            <FlameAxis width={containerWidth} zoom={zoom} />
            {containerWidth &&
              // group별 렌더링
              data.map((group, i) => {
                if (i === 0) prevDepth.current = 0;
                else {
                  const prevGroupMaxDepth = Math.max(
                    ...data[i - 1].map((node) => getMaxDepth(node)),
                  );
                  prevDepth.current = prevGroupMaxDepth + prevDepth.current + 1;
                }

                // node별 렌더링
                return group.map((node) => {
                  const { height, padding, color } = config;
                  const newNode = cloneDeep(node);
                  const yOffset = prevDepth.current * height.node + padding.group * i;

                  styleNode(newNode, 0, 0, yOffset);

                  return (
                    <React.Fragment key={node.id}>
                      <FlameNode
                        scrollLeft={scrollLeft}
                        node={newNode}
                        svgRef={svgRef}
                        customNodeStyle={customNodeStyle}
                        customTextStyle={customTextStyle}
                        onClickNode={onClickNode}
                      />
                      <line
                        x1={0}
                        y1={yOffset - padding.group / 2 + padding.top}
                        x2={containerWidth}
                        y2={yOffset - padding.group / 2 + padding.top}
                        stroke={color.axis}
                      />
                    </React.Fragment>
                  );
                });
              })}
          </svg>
        </div>
      </div>
    </FlameGraphConfigContext.Provider>
  );
};
