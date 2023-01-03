> alpha version

# @pinpoint-fe/scatter-chart
- [documentation](https://pinpoint-apm.github.io/pinpoint-fe-docs/scatterchart/introduction)

## 🔎 Overview

- An open-source scatter-chart library written in pure JS based on canvas api
- Specifically designed for visualizing request and response patterns over time
- Easy to monitor any unexpected transaction

## ⚙️ Installation
```sh
npm install @pinpoint-fe/scatter-chart
```

or 

```sh
yarn add @pinpoint-fe/scatter-chart
```

## 🚀 Quick Start

### Create your first Scatter Chart

```typescript
import { ScatterChart } from '@pinpoint-fe/scatter-chart';

const SC = new ScatterChart(
  document.getElementById('scatterWrapper'), 
  {
    axis: {
      x: {
        min: 1669103462000,
        max: 1669103509335,
        format: (value) => `
          ${String(date.getHours()).padStart(2, '0')}:
          ${String(date.getMinutes()).padStart(2, '0')}:
          ${String(date.getSeconds()).padStart(2, '0')}
        `;,
      },
      y: {
        min: 0,
        max: 10000,
        format: (value) => value.toLocaleString(),
      }
    },
    data: [
      {
        type: 'success',
        color: 'green',
        priority: 1,
      },
      {
        type: 'fail',
        color: 'red',
        priority: 2,
      },
    ],
    legend: {
      formatLabel: (label) => label.toUpperCase(),
    }
  }
);

SC.render(data);
```

### Parameters

| Params | Type | Required | Description |
| --- | --- | --- | --- |
| wrapper | HTMLElement | ✔️ | Wrapper element where chart will be rendered |
| options | <a href="https://pinpoint-apm.github.io/pinpoint-fe-docs/scatterchart/guide/options">ScatterChartOption</a> | ✔️ | ScatterChart options |
