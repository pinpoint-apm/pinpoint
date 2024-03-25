import path from 'path';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import dts from 'vite-plugin-dts';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), dts()],
  resolve: {
    alias: {
      '@': path.join(__dirname, './src'),
    },
  },
  build: {
    lib: {
      entry: './src/index.ts',
      name: 'index',
      fileName: (format) => {
        const ext = format === 'umd' ? 'cjs' : 'js';

        return `pinpoint-fe-common-ui.${format}.${ext}`;
      },
    },
    rollupOptions: {
      external: [
        '@pinpoint-fe/atoms',
        '@pinpoint-fe/hooks',
        'react',
        'react-dom',
        'react-router-dom',
        'jotai',
        'i18next',
        'react-i18next',
      ],
      output: {
        assetFileNames: 'pinpoint-fe-common-ui.[ext]',
        globals: {
          react: 'React',
          'react-dom': 'ReactDOM',
        },
      },
    },
  },
});
