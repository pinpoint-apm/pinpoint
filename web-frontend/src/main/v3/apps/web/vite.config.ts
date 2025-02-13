import { defineConfig } from 'vite';
import { compression } from 'vite-plugin-compression2';
import svgr from 'vite-plugin-svgr';
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
    compression(),
    compression({
      algorithm: 'brotliCompress',
      exclude: [/\.(br)$/, /\.(gz)$/],
      // deleteOriginalAssets: true,
    }),
  ],
  base: basePath,
});
