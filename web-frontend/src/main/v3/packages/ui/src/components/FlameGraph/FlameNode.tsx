import React from 'react';
import { getRandomColor, getDarkenHexColor, getContrastingTextColor } from '../../lib/colors';
import { flameGraphDefaultConfig } from './FlameGraphConfigContext';

export interface FlameNodeType<T> {
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
  children: FlameNodeType<T>[];
}

export type FlameNodeColorType = {
  color: string;
  hoverColor: string;
};

export type FlameNodeClickHandler<T> = (node: FlameNodeType<T>) => void;

export interface FlameNodeProps<T> {
  node: FlameNodeType<T>;
  svgRef?: React.RefObject<SVGSVGElement>;
  onClickNode: FlameNodeClickHandler<T>;
  customNodeStyle?: (node: FlameNodeType<T>, color: FlameNodeColorType) => React.CSSProperties;
  customTextStyle?: (node: FlameNodeType<T>, color: string) => React.CSSProperties;
  scrollLeft?: number;
  // renderText?: (
  //   text: string,
  //   elementAttributes: React.SVGTextElementAttributes<SVGTextElement>,
  // ) => ReactElement;
}

const FlameNodeComponent = <T,>({
  node,
  svgRef,
  onClickNode,
  customNodeStyle,
  customTextStyle,
  scrollLeft = 0,
}: FlameNodeProps<T>) => {
  const [config] = React.useState(flameGraphDefaultConfig);
  const { x = 0, y = 0, width = 1, height = 1, name } = node;
  const colorMap = React.useRef<{ [key: string]: FlameNodeColorType }>({});
  const color = colorMap.current[name]?.color || getRandomColor();
  const hoverColor = colorMap.current[name]?.hoverColor || getDarkenHexColor(color);
  const isFullyVisible =
    scrollLeft + config?.padding?.left <= x || scrollLeft + config?.padding?.left >= x + width;

  const ellipsizedText = React.useMemo(
    () =>
      getEllipsizedText(
        name,
        isFullyVisible
          ? width - config?.padding?.right - config?.padding?.left
          : Math.max(x + width - scrollLeft - config?.padding?.left - config?.padding?.right, 0),
        svgRef,
      ),
    [name, width, svgRef, scrollLeft],
  );

  const [isHover, setHover] = React.useState(false);
  if (!colorMap.current[name]) colorMap.current[name] = { color, hoverColor };
  const contrastringTextColor = getContrastingTextColor(color);
  const nodeStyle = {
    ...node.nodeStyle,
    ...customNodeStyle?.(node, colorMap.current[name]),
  };
  const textStyle = {
    ...node.textStyle,
    ...customTextStyle?.(node, contrastringTextColor),
  };

  return (
    <>
      <g
        className="cursor-pointer"
        onMouseOver={() => setHover(true)}
        onMouseOut={() => setHover(false)}
        onClick={() => {
          onClickNode?.(node);
        }}
      >
        <rect
          x={x}
          y={y}
          width={width}
          height={height}
          style={{
            fill: isHover ? hoverColor : color,
            ...nodeStyle,
          }}
        />
        <text
          x={
            isFullyVisible
              ? x + config?.padding?.left
              : Math.max(config?.padding?.left + scrollLeft, 0)
          }
          y={y + height / 2}
          dy=".35em"
          fontSize="0.75rem"
          letterSpacing={-0.5}
          textAnchor="start"
          fill={contrastringTextColor}
          style={textStyle}
        >
          {ellipsizedText}
        </text>
      </g>
      {node.children &&
        node.children.map((childNode, i) => {
          return (
            <FlameNode<T>
              key={i}
              node={childNode}
              svgRef={svgRef}
              onClickNode={onClickNode}
              customNodeStyle={customNodeStyle}
              customTextStyle={customTextStyle}
              scrollLeft={scrollLeft}
            />
          );
        })}
    </>
  );
};

export const FlameNode = React.memo(FlameNodeComponent) as <T>(
  props: FlameNodeProps<T>,
) => React.ReactElement;

const getEllipsizedText = (text: string, maxWidth = 1, svgRef?: React.RefObject<SVGSVGElement>) => {
  if (!svgRef?.current) return text;

  if (maxWidth <= 0) return null;
  else {
    const svg = svgRef.current;
    const textElement = document.createElementNS('http://www.w3.org/2000/svg', 'text');
    textElement.setAttribute('font-size', '0.75rem');
    textElement.style.visibility = 'hidden';
    textElement.textContent = text;
    svg.appendChild(textElement);

    let ellipsizedText = text;

    if (maxWidth < textElement.getComputedTextLength()) {
      let low = 0;
      let high = text.length;

      while (low < high) {
        const mid = Math.floor((low + high) / 2);
        const candidateText = text.slice(0, mid);
        textElement.textContent = candidateText;

        if (textElement.getComputedTextLength() <= maxWidth) {
          low = mid + 1;
        } else {
          high = mid;
        }
      }

      ellipsizedText = text.slice(0, low - 1) + '...';
    }

    svg.removeChild(textElement);
    return ellipsizedText;
  }
};
