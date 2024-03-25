import { defineConfig } from 'tsup'

export default defineConfig({
  entry: {
    index: 'src/index.ts',
  },
  splitting: true,
  dts: true,
  format: ['cjs', 'esm'],
  external: ['react'],
});
