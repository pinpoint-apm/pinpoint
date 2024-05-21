import React from 'react';
import { getRandomColor, getDarkenHexColor, getContrastingTextColor } from '../../../lib/colors';
import { FlameNode as FlameNodeType } from './FlameGraph';

export interface FlameNodeProps<T> {
  node: FlameNodeType<T>;
  svgRef?: React.RefObject<SVGSVGElement>;
  onClickNode?: (e: React.MouseEvent, node: FlameNodeType<T>) => void;
}

export const FlameNode = <T,>({ node, svgRef, onClickNode }: FlameNodeProps<T>) => {
  const { x = 0, y = 0, width = 1, height = 1, name, nodeStyle, textStyle } = node;
  const colorMap = React.useRef<{ [key: string]: { color: string; hoverColor: string } }>({});
  const color = colorMap.current[name]?.color || getRandomColor();
  const hoverColor = colorMap.current[name]?.hoverColor || getDarkenHexColor(color);
  const ellipsizedText = getEllipsizedText(name, width, svgRef);
  const [isHover, setHover] = React.useState(false);

  if (!colorMap.current[name]) colorMap.current[name] = { color, hoverColor };

  return (
    <>
      <g
        className="cursor-pointer"
        onMouseOver={() => setHover(true)}
        onMouseOut={() => setHover(false)}
        onClick={(e) => {
          console.log(e);
          onClickNode?.(e, node);
        }}
      >
        <rect
          x={x}
          y={y}
          width={width}
          height={height}
          fill={isHover ? hoverColor : color}
          style={nodeStyle}
        />
        <text
          x={x + width / 2}
          y={y + height / 2}
          dy=".35em"
          fontSize="0.75rem"
          letterSpacing={-0.5}
          textAnchor="middle"
          fill={getContrastingTextColor(color)}
          style={textStyle}
        >
          {ellipsizedText}
        </text>
      </g>
      {node.children &&
        node.children.map((node, i) => (
          <FlameNode key={i} node={node} svgRef={svgRef} onClickNode={onClickNode} />
        ))}
    </>
  );
};

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
    while (textElement.getComputedTextLength() > maxWidth && ellipsizedText.length > 0) {
      ellipsizedText = ellipsizedText.slice(0, -1);
      textElement.textContent = `${ellipsizedText}...`;
    }

    svg.removeChild(textElement);
    return textElement.textContent;
  }
};
