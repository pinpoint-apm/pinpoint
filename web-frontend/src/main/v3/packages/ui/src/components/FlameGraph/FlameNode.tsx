import React from 'react';
import { getRandomColor, getDarkenHexColor, getContrastingTextColor } from '../../lib/colors';

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
}: FlameNodeProps<T>) => {
  const { x = 0, y = 0, width = 1, height = 1, name } = node;
  const colorMap = React.useRef<{ [key: string]: FlameNodeColorType }>({});
  const color = colorMap.current[name]?.color || getRandomColor();
  const hoverColor = colorMap.current[name]?.hoverColor || getDarkenHexColor(color);
  const ellipsizedText = React.useMemo(
    () => getEllipsizedText(name, width, svgRef),
    [name, width, svgRef],
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
            ...nodeStyle,
            fill: isHover ? hoverColor : color,
          }}
        />
        <text
          x={x + width / 2}
          y={y + height / 2}
          dy=".35em"
          fontSize="0.75rem"
          letterSpacing={-0.5}
          textAnchor="middle"
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
            />
          );
        })}
    </>
  );
};

export const FlameNode = React.memo(FlameNodeComponent) as <T>(
  props: FlameNodeProps<T>,
) => JSX.Element;

const getEllipsizedText = (text: string, maxWidth = 1, svgRef?: React.RefObject<SVGSVGElement>) => {
  if (!svgRef?.current) return text;
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
