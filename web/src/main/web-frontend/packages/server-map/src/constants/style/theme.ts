import { Css } from 'cytoscape'

export type ServerMapTheme = {
  transactionStatus?: {
    default?: {
      stroke?: string,
      strokeWidth?: number,
    }, 
    good?: {
      stroke?: string,
      strokeWidth?: number,
    },
    slow?: {
      stroke?: string,
      strokeWidth?: number,
    },
    bad?: {
      stroke?: string,
      strokeWidth?: number,
    },
  },
  node?: {
    default?: Css.Node,
    highlight?: Css.Node,
    main?: Css.Node,
  },
  edge?: {
    default?: Css.Edge,
    highlight?: Css.Edge,
    loop?: Css.Edge,
  }
} 

export const defaultTheme: ServerMapTheme = {
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
    }
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
      'font-family': 'Helvetica, Arial, avn85, NanumGothic, ng, dotum, AppleGothic, sans-serif',
      'font-size': 12,
      'font-weight': 'normal',
      'text-wrap': 'wrap',
      'text-max-width': '200',
      'line-height': 1.5,
      'color': '#000',
    },
    highlight: {
      'font-weight': 'bold',
      'font-size': 14,
      'border-color': '#4A61D1'
    },
    main: {
      'font-weight': 'bold',
      'font-size': 14,
    }
  },
  edge: {
    default: {
      'width': 1.5,
      'font-size': '12px',
      'font-weight': 'normal',
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
      'target-arrow-color': '#4763d0'
    },
    loop: {
      'control-point-step-size': 70,
      'loop-direction': '0deg',
      'loop-sweep': '-90deg'
    }
  }
};
