import { getNodeSVGString } from '../../../ui/template/node';
import { Node } from '../../../types';
import { ServerMapProps } from '../../../ui/ServerMap';

describe('getNodeSVGString', () => {
  it('기본 SVG 문자열을 반환해야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      type: 'WAS',
    };

    const result = getNodeSVGString(node);
    expect(result).toContain('data:image/svg+xml;charset=utf-8,');
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    expect(decoded).toContain('<svg');
  });

  it('transactionInfo가 있으면 트랜잭션 상태 SVG를 생성해야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      type: 'WAS',
      transactionInfo: {
        good: 100,
        slow: 20,
        bad: 5,
      },
    };

    const result = getNodeSVGString(node);
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    expect(decoded).toContain('circle');
    expect(decoded).toContain('stroke');
  });

  it('timeSeriesApdexInfo가 있으면 타임시리즈 Apdex SVG를 생성해야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      type: 'WAS',
      timeSeriesApdexInfo: [0.95, 0.88, 0.72, 0.55, 0.45],
    };

    const result = getNodeSVGString(node);
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    expect(decoded).toContain('path');
    expect(decoded).toContain('A ');
  });

  it('timeSeriesApdexInfo가 transactionInfo보다 우선해야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      type: 'WAS',
      transactionInfo: {
        good: 100,
        slow: 20,
        bad: 5,
      },
      timeSeriesApdexInfo: [0.95, 0.88],
    };

    const result = getNodeSVGString(node);
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    expect(decoded).toContain('path'); // timeSeriesApdexInfo 사용
  });

  it('renderNode 함수가 제공되면 사용해야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      type: 'WAS',
      transactionInfo: {
        good: 100,
        slow: 20,
        bad: 5,
      },
    };

    const renderNode: ServerMapProps['renderNode'] = jest.fn((nodeData, svg) => {
      return `<custom>${svg}</custom>`;
    });

    const result = getNodeSVGString(node, renderNode);
    expect(renderNode).toHaveBeenCalled();
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    expect(decoded).toContain('<custom>');
  });

  it('transactionInfo가 없으면 기본 SVG를 생성해야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      type: 'WAS',
    };

    const result = getNodeSVGString(node);
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    expect(decoded).toContain('circle');
    expect(decoded).toContain('transparent');
  });
});

describe('timeSeriesApdexInfo 처리', () => {
  it('빈 배열이면 기본 SVG가 생성되어야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      timeSeriesApdexInfo: [],
    };

    const result = getNodeSVGString(node);
    // 빈 배열이면 transactionInfo가 없으므로 기본 SVG가 생성됨
    expect(result).toBeDefined();
    expect(result).toContain('svg');
  });

  it('Apdex 점수에 따라 올바른 색상을 사용해야 함', () => {
    const excellentNode: Node = {
      id: 'n1',
      label: 'Node 1',
      timeSeriesApdexInfo: [0.95],
    };

    const poorNode: Node = {
      id: 'n2',
      label: 'Node 2',
      timeSeriesApdexInfo: [0.55],
    };

    const excellentResult = getNodeSVGString(excellentNode);
    const poorResult = getNodeSVGString(poorNode);

    const excellentDecoded = decodeURIComponent(excellentResult.split(',')[1] || '');
    const poorDecoded = decodeURIComponent(poorResult.split(',')[1] || '');

    expect(excellentDecoded).toContain('#41c464'); // Excellent
    expect(poorDecoded).toContain('#ff8c00'); // Poor
  });

  it('여러 세그먼트를 올바르게 렌더링해야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      timeSeriesApdexInfo: [0.95, 0.88, 0.72, 0.55, 0.45, 0.3],
    };

    const result = getNodeSVGString(node);
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    const pathMatches = decoded.match(/<path/g);
    expect(pathMatches?.length).toBe(6);
  });

  it('다양한 Apdex 등급을 올바르게 처리해야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      timeSeriesApdexInfo: [0.95, 0.88, 0.72, 0.55, 0.45],
    };

    const result = getNodeSVGString(node);
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    expect(decoded).toContain('#41c464'); // Excellent (0.95)
    expect(decoded).toContain('#469ae4'); // Good (0.88)
    expect(decoded).toContain('#f7d84a'); // Fair (0.72)
    expect(decoded).toContain('#ff8c00'); // Poor (0.55)
    expect(decoded).toContain('#eb4747'); // Unacceptable (0.45)
  });
});

describe('getTransactionStatusSVGCircle', () => {
  it('good, slow, bad 값에 따라 원을 생성해야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      transactionInfo: {
        good: 100,
        slow: 20,
        bad: 5,
      },
    };

    const result = getNodeSVGString(node);
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    expect(decoded).toContain('#32BA94'); // good
    expect(decoded).toContain('#E48022'); // slow
    expect(decoded).toContain('#F0515B'); // bad
  });

  it('모든 값이 0이면 기본 원만 생성해야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      transactionInfo: {
        good: 0,
        slow: 0,
        bad: 0,
      },
    };

    const result = getNodeSVGString(node);
    expect(result).toBeDefined();
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    expect(decoded).toContain('circle');
  });

  it('작은 값도 최소 arc ratio로 표시되어야 함', () => {
    const node: Node = {
      id: 'n1',
      label: 'Node 1',
      transactionInfo: {
        good: 1000,
        slow: 1,
        bad: 1,
      },
    };

    const result = getNodeSVGString(node);
    const decoded = decodeURIComponent(result.split(',')[1] || '');
    expect(decoded).toContain('circle');
  });
});

