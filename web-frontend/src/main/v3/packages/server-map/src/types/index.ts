export interface Node {
  id: string;
  label: string;
  type?: string;
  imgPath?: string;
  transactionInfo?: {
    good: number;
    slow: number;
    bad: number;
    [key: string]: any;
  };
  shouldNotMerge?: () => boolean;
}

export interface Edge {
  id: string;
  source: string;
  target: string;
  transactionInfo?: {
    [key: string]: any;
  };
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
