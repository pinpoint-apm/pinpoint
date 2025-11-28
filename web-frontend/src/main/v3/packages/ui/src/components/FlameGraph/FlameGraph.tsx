import React from 'react';
import { throttle } from 'lodash';
import cloneDeep from 'lodash.clonedeep';
import { FlameNode, FlameNodeType, FlameNodeProps } from './FlameNode';
import { FlameAxis } from './FlameAxis';
import { FlameGraphConfigContext, flameGraphDefaultConfig } from './FlameGraphConfigContext';
import { FlameTimeline } from './FlameTimeline';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '../ui';
import { GoZoomIn, GoZoomOut } from 'react-icons/go';
import { Button } from '../ui';
import { TraceViewerData } from '@pinpoint-fe/ui/src/constants';
import { FlameArrow } from './FlameArrow';

export interface FlameGraphProps<T>
  extends Pick<FlameNodeProps<T>, 'customNodeStyle' | 'customTextStyle' | 'onClickNode'> {
  data: FlameNodeType<T>[][];
  start?: number;
  end?: number;
  nodeFlows?: {
    prev: string[];
    selected?: string;
    next: string[];
  };
}

export const FlameGraph = <T,>({
  data,
  start = 0,
  end = 0,
  nodeFlows,
  onClickNode,
  customNodeStyle,
  customTextStyle,
}: FlameGraphProps<T>) => {
  const widthOffset = end - start || 1;
  const [config] = React.useState(flameGraphDefaultConfig);
  const [zoom, setZoom] = React.useState(1);
  const [arrows, setArrows] = React.useState({
    prev: [] as FlameNodeType<T>[],
    selected: undefined as FlameNodeType<T> | undefined,
    next: [] as FlameNodeType<T>[],
  });

  const containerRef = React.useRef<HTMLDivElement>(null);
  const svgRef = React.useRef<SVGSVGElement>(null);
  const [width, setWidth] = React.useState(0);
  const containerHeight = getContainerHeight();
  const containerWidth = width * zoom;
  const widthRatio = (containerWidth - config.padding.left - config.padding.right) / widthOffset;
  const [scrollLeft, setScrollLeft] = React.useState(0);

  React.useEffect(() => {
    setScrollLeft(0);
  }, [data]);

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

  React.useEffect(() => {
    if (!nodeFlows?.selected || (nodeFlows?.prev?.length === 0 && nodeFlows?.next?.length === 0)) {
      setArrows({
        prev: [],
        selected: undefined,
        next: [],
      });
      return;
    }
  }, [nodeFlows]);

  const styleArrow = React.useCallback(
    (node: FlameNodeType<T>) => {
      const detail = node?.detail as TraceViewerData.TraceEvent | undefined;

      if (nodeFlows?.selected && nodeFlows?.selected === detail?.args?.id) {
        return {
          type: 'selected',
          node,
        };
      }

      if (detail?.args?.id && nodeFlows?.prev?.includes(detail?.args?.id)) {
        return {
          type: 'prev',
          node,
        };
      }

      if (detail?.args?.id && nodeFlows?.next?.includes(detail?.args?.id)) {
        return {
          type: 'next',
          node,
        };
      }

      for (const child of node.children || []) {
        return styleArrow(child);
      }

      return;
    },
    [nodeFlows],
  );

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

  const dataForRender = React.useMemo(() => {
    let pd = 0;
    const prevNodes: FlameNodeType<T>[] = [];
    const nextNodes: FlameNodeType<T>[] = [];
    let selectedNode;

    const returnData = data.map((group, i) => {
      if (i === 0) pd = 0;
      else {
        const prevGroupMaxDepth = Math.max(...data[i - 1].map((node) => getMaxDepth(node)));
        pd = prevGroupMaxDepth + pd + 1;
      }

      // node별 렌더링
      return group.map((node) => {
        const { height, padding, color } = config;
        const newNode = cloneDeep(node);
        const yOffset = pd * height.node + padding.group * i;

        styleNode(newNode, 0, 0, yOffset);
        const temp = styleArrow(newNode);

        if (temp?.type === 'selected') {
          selectedNode = temp.node;
        } else if (temp?.type === 'prev') {
          prevNodes.push(temp.node);
        } else if (temp?.type === 'next') {
          nextNodes.push(temp.node);
        }

        return {
          ...newNode,
          yOffset,
          color,
          padding,
        };
      });
    });

    setArrows({
      prev: prevNodes,
      selected: selectedNode,
      next: nextNodes,
    });

    return returnData;
  }, [data, config, widthRatio, start]);

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

  function handleZoomClick(type: 'in' | 'out') {
    setZoom((prevZoom) => {
      if (type === 'in') {
        return prevZoom + 1;
      }
      return Math.max(1, prevZoom - 1);
    });
  }

  return (
    <FlameGraphConfigContext.Provider value={{ config }}>
      <TooltipProvider>
        <Tooltip delayDuration={0}>
          <TooltipTrigger asChild>
            <div className="absolute space-x-1 right-[275px] -top-10">
              <Button
                variant={'outline'}
                size="sm"
                className="h-7"
                onClick={() => handleZoomClick('in')}
              >
                <GoZoomIn size={15} />
              </Button>
              <Button
                variant={'outline'}
                size="sm"
                className="h-7"
                onClick={() => handleZoomClick('out')}
              >
                <GoZoomOut size={15} />
              </Button>
            </div>
          </TooltipTrigger>
          <TooltipContent>Zoom In/Out: Command + Scroll</TooltipContent>
        </Tooltip>
      </TooltipProvider>
      <div className="relative w-full h-full overflow-x-auto" ref={containerRef}>
        <svg width={containerWidth} height={config.height.timeline} className="shadow-md">
          <FlameTimeline width={containerWidth} start={start} end={end} zoom={zoom} />
        </svg>
        <div className="w-fit h-[calc(100%-3rem)] overflow-y-auto overflow-x-hidden">
          <svg width={containerWidth} height={containerHeight} ref={svgRef}>
            <FlameAxis width={containerWidth} zoom={zoom} />
            {containerWidth &&
              // group별 렌더링
              dataForRender.map((group, i) => {
                // node별 렌더링
                return group.map((node) => {
                  return (
                    <React.Fragment key={node.id}>
                      <FlameNode
                        scrollLeft={scrollLeft}
                        node={node}
                        svgRef={svgRef as React.RefObject<SVGSVGElement>}
                        customNodeStyle={customNodeStyle}
                        customTextStyle={customTextStyle}
                        onClickNode={onClickNode}
                      />
                      <line
                        x1={0}
                        y1={node?.yOffset - node?.padding?.group / 2 + node?.padding?.top}
                        x2={containerWidth}
                        y2={node?.yOffset - node?.padding.group / 2 + node?.padding.top}
                        stroke={node?.color.axis}
                      />
                    </React.Fragment>
                  );
                });
              })}
            {arrows.next.map((nextNode, i) => {
              return (
                <FlameArrow
                  key={i}
                  x1={(arrows?.selected?.x || 0) + (arrows?.selected?.width || 0)}
                  y1={(arrows?.selected?.y || 0) + (arrows?.selected?.height || 0) / 2}
                  x2={nextNode?.x}
                  y2={(nextNode?.y || 0) + (nextNode?.height || 0) / 2}
                />
              );
            })}
            {arrows.prev.map((prevNode, i) => {
              return (
                <FlameArrow
                  key={i}
                  x1={(prevNode?.x || 0) + (prevNode?.width || 0)}
                  y1={(prevNode?.y || 0) + (prevNode?.height || 0) / 2}
                  x2={arrows?.selected?.x}
                  y2={(arrows?.selected?.y || 0) + (arrows?.selected?.height || 0) / 2}
                />
              );
            })}
          </svg>
        </div>
      </div>
    </FlameGraphConfigContext.Provider>
  );
};
