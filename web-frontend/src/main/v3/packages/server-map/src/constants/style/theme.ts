import { Css } from 'cytoscape';

export enum GraphStyle {
  NODE_WIDTH = 100,
  // 노드가 정사각형이라 폭과 높이가 같은 값인 것은 의도된 것이다.
  // eslint-disable-next-line @typescript-eslint/no-duplicate-enum-values
  NODE_HEIGHT = 100,
  NODE_RADIUS = GraphStyle.NODE_HEIGHT / 2,
  NODE_GAP = 30,
  RANK_SEP = 200,
}

export type ServerMapTheme = {
  transactionStatus?: {
    default?: {
      stroke?: string;
      strokeWidth?: number;
    };
    good?: {
      stroke?: string;
      strokeWidth?: number;
    };
    slow?: {
      stroke?: string;
      strokeWidth?: number;
    };
    bad?: {
      stroke?: string;
      strokeWidth?: number;
    };
  };
  node?: {
    default?: Css.Node;
    highlight?: Css.Node;
    main?: Css.Node;
  };
  edge?: {
    default?: Css.Edge;
    highlight?: Css.Edge;
    loop?: Css.Edge;
  };
};

// ServerMapTheme은 사용자가 일부만 덮어쓸 수 있도록 모든 속성이 optional이다.
// defaultTheme은 그 값들이 빠짐없이 채워진 것이므로 두 단계(섹션 -> 상태/역할)까지 필수로 만든다.
// cytoscape의 Css.Node/Css.Edge 안쪽까지 재귀하면 스타일 속성을 전부 적어야 하므로 그 앞에서 멈춘다.
type RequiredProps<T> = {
  [P in keyof T]-?: NonNullable<T[P]>;
};

type CompleteServerMapTheme = RequiredProps<{
  [P in keyof ServerMapTheme]: RequiredProps<NonNullable<ServerMapTheme[P]>>;
}>;

export const defaultTheme: CompleteServerMapTheme = {
  transactionStatus: {
    default: {
      stroke: 'transparent',
      strokeWidth: 10,
    },
    good: {
      stroke: '#32BA94',
      strokeWidth: 10,
    },
    slow: {
      stroke: '#E48022',
      strokeWidth: 10,
    },
    bad: {
      stroke: '#F0515B',
      strokeWidth: 10,
    },
  },
  node: {
    default: {
      'background-color': '#FFF',
      'border-width': '3',
      'border-color': '#ddd',
      'text-valign': 'bottom',
      'text-halign': 'center',
      'text-margin-y': 4,
      'overlay-opacity': 0,
      'font-family': 'Arial, Helvetica, sans-serif',
      'font-size': 12,
      'font-weight': 'normal',
      'text-wrap': 'wrap',
      'text-max-width': '200',
      'line-height': 1.5,
      color: '#000',
    },
    highlight: {
      'font-weight': 'bold',
      'font-size': 14,
      'border-color': '#4A61D1',
    },
    main: {
      'font-weight': 'bold',
      'font-size': 14,
    },
  },
  edge: {
    default: {
      width: 1.5,
      'font-size': 12,
      'font-weight': 'normal',
      'font-family': 'Arial, Helvetica, sans-serif',
      'line-color': '#C0C3C8',
      'target-arrow-color': '#C0C3C8',
      'target-arrow-shape': 'triangle',
      'curve-style': 'bezier',
      'text-background-color': 'white',
      'text-background-opacity': 0.7,
      'text-background-padding': '5px',
      'text-background-shape': 'roundrectangle',
      'overlay-opacity': 0,
      // color: (ele: any) => ele.data('hasAlert') ? this.serverMapColor.textFail : this.serverMapColor.text,
    },
    highlight: {
      'font-size': '14px',
      'font-weight': 'bold',
      'line-color': '#4763d0',
      'target-arrow-color': '#4763d0',
    },
    loop: {
      'control-point-step-size': 70,
      'loop-direction': '0deg',
      'loop-sweep': '-90deg',
    },
  },
};
