import path from 'path';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import svgr from 'vite-plugin-svgr';
import dts from 'vite-plugin-dts';
import removeAttr from 'remove-attr';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    svgr(),
    react(),
    removeAttr({
      extensions: ['tsx', 'ts'],
      attributes: ['data-testid'],
    }),
    dts(),
  ],
  resolve: {
    alias: {
      '@': path.join(__dirname, './src'),
    },
  },
  build: {
    lib: {
      entry: './src/index.ts',
      name: 'index',
      formats: ['es', 'cjs'],
      fileName: (format) => {
        if (format === 'cjs') {
          return 'index.cjs';
        }
        return 'index.js';
      },
    },
    rollupOptions: {
      external: ['react', 'react-dom'],
      output: {
        dir: 'dist',
        // assetFileNames: 'rich-datetime-picker.[ext]',
        // globals: {
        //   react: 'React',
        //   'react-dom': 'ReactDOM',
        // },
      },
    },
  },
});
