export interface Node {
  id: string;
  label: string;
  type?: string;
  imgPath?: string;
  apdex?: {
    apdexScore: number;
    apdexFormula: {
      satisfiedCount: number;
      toleratingCount: number;
      totalSamples: number;
    };
  };
  transactionInfo?: {
    good: number;
    slow: number;
    bad: number;
    [key: string]: any;
  };
  timeSeriesApdexInfo?: number[];
  subNodesCount?: number;
  shouldNotMerge?: () => boolean;
  /**
   * 이 노드를 감싸는 부모(compound) 노드의 id. cytoscape가 그대로 읽는 필드다.
   * 부모 노드도 같은 `nodes` 배열에 담아 넘겨야 한다.
   */
  parent?: string;
  /**
   * 새로 생길 때 놓일 자리의 기준 노드 id. 이 노드가 **바로 전 데이터에 그려져 있던** 경우에만
   * 쓰인다 — 묶인 노드를 펼칠 때 자식들은 묶음 노드 자리에, 다시 묶을 때 묶음 노드는 부모 자리에
   * 놓인다. 추가된 노드가 모두 이 경우면 전체 배치를 다시 돌리지 않는다.
   */
  anchorId?: string;
  /**
   * 이 노드가 대신 그리고 있는 노드들의 id(묶음 노드의 자식들). 선택된 노드가 그래프에 없으면
   * 이 목록에 그 id를 가진 노드를 대신 하이라이트한다.
   */
  memberIds?: string[];
}

export interface Edge {
  id: string;
  source: string;
  target: string;
  transactionInfo?: {
    [key: string]: any;
  };
  /** 이 링크가 대신 그리고 있는 링크들의 id(묶음 링크의 자식들). `Node#memberIds`와 같다. */
  memberIds?: string[];
}

export interface MergeInfo {
  types: string[];
}

export interface MergedNode extends Node {
  nodes?: Node[];
}

export interface MergedEdge extends Edge {
  edges?: Edge[];
}
