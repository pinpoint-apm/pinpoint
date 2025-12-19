import React from 'react';
import { render } from '@testing-library/react';
import '@testing-library/jest-dom';
import { ServerMap } from '../../ui/ServerMap';
import { Node, Edge } from '../../types';

// cytoscape를 mock
jest.mock('cytoscape', () => {
  const mockUseFn = jest.fn();
  const createMockCy = () => {
    const mockOn = jest.fn().mockReturnThis();
    const mockStyle = jest.fn().mockReturnThis();
    
    return {
      use: mockUseFn,
      style: mockStyle,
      on: mockOn,
      off: jest.fn(),
      removeAllListeners: jest.fn(),
      removeData: jest.fn(),
      destroy: jest.fn(),
      data: jest.fn(),
      nodes: jest.fn(() => ({
        map: jest.fn(() => []),
        style: jest.fn(),
        filter: jest.fn(() => ({
          map: jest.fn(() => []),
        })),
      })),
      edges: jest.fn(() => ({
        style: jest.fn(),
      })),
      getElementById: jest.fn(() => ({
        id: jest.fn(() => 'n1'),
        data: jest.fn(() => ({ id: 'n1', label: 'Node 1' })),
        style: jest.fn(),
        remove: jest.fn(),
        connectedEdges: jest.fn(() => ({
          remove: jest.fn(),
        })),
        inside: jest.fn(() => true),
        isNode: jest.fn(() => true),
        isEdge: jest.fn(() => false),
        same: jest.fn(() => false),
        position: jest.fn(() => ({ x: 0, y: 0 })),
        boundingBox: jest.fn(() => ({ y1: 0, y2: 100, h: 100 })),
        width: jest.fn(() => 100),
        predecessors: jest.fn(() => ({
          contains: jest.fn(() => false),
          nodes: jest.fn(() => ({
            toArray: jest.fn(() => []),
          })),
        })),
        successors: jest.fn(() => ({
          contains: jest.fn(() => false),
          nodes: jest.fn(() => ({
            toArray: jest.fn(() => []),
          })),
        })),
        connectedNodes: jest.fn(() => ({
          style: jest.fn(),
        })),
      })),
      add: jest.fn(() => ({
        id: jest.fn(() => 'n1'),
        position: jest.fn(),
        predecessors: jest.fn(() => ({
          contains: jest.fn(() => false),
          nodes: jest.fn(() => ({
            toArray: jest.fn(() => []),
          })),
        })),
        successors: jest.fn(() => ({
          contains: jest.fn(() => false),
          nodes: jest.fn(() => ({
            toArray: jest.fn(() => []),
          })),
        })),
        same: jest.fn(() => false),
        boundingBox: jest.fn(() => ({ y1: 0, y2: 100, h: 100 })),
      })),
      batch: jest.fn((fn) => fn()),
      layout: jest.fn(() => ({
        run: jest.fn(),
        stop: jest.fn(),
        removeAllListeners: jest.fn(),
      })),
      resize: jest.fn(),
      center: jest.fn(),
      container: jest.fn(() => ({
        style: { cursor: 'default' },
      })),
    };
  };
  
  const mockCytoscape = jest.fn(() => createMockCy());
  (mockCytoscape as any).use = mockUseFn;
  
  return {
    __esModule: true,
    default: mockCytoscape,
  };
});

jest.mock('cytoscape-dagre', () => ({}));

describe('ServerMap', () => {
  const mockNodes: Node[] = [
    { id: 'n1', label: 'Node 1', type: 'WAS' },
    { id: 'n2', label: 'Node 2', type: 'DB' },
  ];

  const mockEdges: Edge[] = [
    { id: 'e1', source: 'n1', target: 'n2' },
  ];

  const defaultProps = {
    data: {
      nodes: mockNodes,
      edges: mockEdges,
    },
    baseNodeId: 'n1',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('컴포넌트가 렌더링되어야 함', () => {
    const { container } = render(<ServerMap {...defaultProps} />);
    expect(container.firstChild).toBeInTheDocument();
  });

  it('커스텀 className이 적용되어야 함', () => {
    const { container } = render(<ServerMap {...defaultProps} className="custom-class" />);
    expect(container.firstChild).toHaveClass('custom-class');
  });

  it('커스텀 style이 적용되어야 함', () => {
    const customStyle = { width: '500px', height: '300px' };
    const { container } = render(<ServerMap {...defaultProps} style={customStyle} />);
    const element = container.firstChild as HTMLElement;
    expect(element.style.width).toBe('500px');
    expect(element.style.height).toBe('300px');
  });
});


