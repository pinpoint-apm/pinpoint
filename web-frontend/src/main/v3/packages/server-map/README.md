<img src="./assets/img/server-map-logo.png" />

<br />

> alpha version

# @pinpoint-fe/server-map
- [documentation](https://pinpoint-apm.github.io/pinpoint-fe-docs)
- [demo](https://pinpoint-apm.github.io/pinpoint-fe-docs/examples)

## 🔎 Overview

- ServerMap component is an open-source network-map library, specifically for application topology.
- With ServerMap component, you will find it easy to understand how your services are interconnected and how the transactions are going on between them.
- Besides, since we provide a various of cool features such as merge and customizing label, feel free to check out them if you are interested in making your topology look nicer.
- For your information, ServerMap component builds the network-map using [cytoscape.js](https://js.cytoscape.org/) which is light, highly optimized and well-maintained so it's all ready to provide a pleasant experience.

## ⚙️ Installation
```sh
npm install @pinpoint-fe/server-map
```

or 

```sh
yarn add @pinpoint-fe/server-map
```

## 🚀 Quick Start

### Create your first ServerMap

```typescript
import React from 'react';
import { ServerMap } from '@pinpoint-fe/server-map';

export default function MyServerMapPage() {
  return (
    <ServerMap 
      data={data}
      baseNodeId={'MY-APP'} 
    />
  );
}
```

### Props

| Props | Type | Required | Description |
| --- | --- | --- | --- |
| data | <code>{ nodes: Node[], edges: Edge[] }</code> | ✔️ | Data to render |
| baseNodeId | string | ✔️ | Central node id in the server-map              |
| customTheme | ThemeType | | 	Custom style object  |
| onClickNode       | `ClickEventHandler<MergedNode>`    |  | Callback to execute when clicking nodes   |
| onClickEdge       | `ClickEventHandler<MergedEdge>`    |  | Callback to execute when clicking edges    |
| onClickBackground | `ClickEventHandler<{}>`             |  | 	Callback to execute when clicking background  |
| renderNodeLabel   | `(node: MergedNode) => string ㅣ undefined` |  | Custom node label                                         |
| renderEdgeLabel   | `(node: MergedEdge) => string ㅣ undefined` |  | Custom edge label                                 |
