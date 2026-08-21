import { defineConfig } from 'vite';
import { compression } from 'vite-plugin-compression2';
import svgr from 'vite-plugin-svgr';
// [MOCK #10497] 로컬 확인용 임시 mock. 삭제 방법은 ./dev-mock/README.md 참고.
import { serviceMapMockPlugin } from './dev-mock';
// import { visualizer } from 'rollup-plugin-visualizer';
// import react from '@vitejs/plugin-react';

// import { BASE_PATH } from '@pinpoint-fe/ui/src/constants'; // TODO: import from ui
const BASE_PATH = process.env.BASE_PATH || '';
const isDev = process.env.NODE_ENV === 'development';
const target = isDev ? 'http://localhost:8080' : 'http://localhost:8080';
const basePath = isDev ? '/' : BASE_PATH || '/';

// https://vitejs.dev/config/
export default defineConfig({
  define: {
    'process.env': {},
    global: {},
  },
  server: {
    hmr: { overlay: false },
    port: 3000,
    proxy: {
      '/api/': {
        target,
        // secure: false,
        changeOrigin: true,
      },
      '/api/agent/activeThread': {
        target,
        secure: false,
        ws: true,
      },
    },
  },
  plugins: [
    svgr(),
    // [MOCK #10497] MOCK_SERVICE_MAP=1 일 때만 동작한다(= yarn dev:mock).
    serviceMapMockPlugin(),
    compression(),
    compression({
      algorithm: 'brotliCompress',
      exclude: [/\.(br)$/, /\.(gz)$/],
      // deleteOriginalAssets: true,
    }),
    // visualizer({ open: true }),
  ],
  base: basePath,
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          react: ['react', 'react-dom'],
          charts: ['echarts'],
          graph: ['cytoscape'],
        },
      },
    },
  },
});
