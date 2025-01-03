import path from 'path';
import { defineConfig } from 'vite';
import { compression } from 'vite-plugin-compression2';
// import react from '@vitejs/plugin-react';

// import { BASE_PATH } from '@pinpoint-fe/ui'; TODO: import from ui
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
  resolve: {
    alias: {
      '@': path.join(__dirname, './src'),
      // '@pinpoint-fe/ui/dist': path.join(__dirname, '../../packages/ui2/dist'),
      // '@pinpoint-fe/ui': path.join(__dirname, '../../packages/ui2/src'),
    },
  },
  plugins: [
    compression(),
    compression({
      algorithm: 'brotliCompress',
      exclude: [/\.(br)$/, /\.(gz)$/],
      // deleteOriginalAssets: true,
    }),
  ],
  base: basePath,
});
