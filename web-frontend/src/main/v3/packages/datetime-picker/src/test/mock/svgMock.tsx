// __mocks__/svgMock.tsx
/* eslint-disable @typescript-eslint/no-explicit-any */
import React from 'react';

const SvgMock = (props: any) => (
  <svg {...props}>
    <text>SVG Mock</text>
  </svg>
);
export default SvgMock;
