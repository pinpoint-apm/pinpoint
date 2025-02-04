import path from 'path';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import dts from 'vite-plugin-dts';

export default defineConfig({
  plugins: [react(), dts()],
  resolve: {
    alias: {
      '@pinpoint-fe/ui': path.resolve(__dirname, 'src'),
    },
  },
  build: {
    lib: {
      entry: './src/index.ts',
      name: 'index',
      formats: ['es'],
      fileName: () => `index.js`,
    },
    rollupOptions: {
      external: [
        'react',
        'react-dom',
        'react-router-dom',
        'jotai',
        'i18next',
        'react-i18next',
        'tailwindcss',
        'tailwindcss-animate',
        'tailwind-scrollbar-hide',
        'swr',
        '@tanstack/react-query',
      ],
      output: {
        dir: 'dist',
      },
    },
  },
});
