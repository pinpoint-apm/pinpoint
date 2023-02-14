import { defineConfig } from 'tsup'
import defaultTsupConfig from './tsup.config.dev';

export default defineConfig({
  ...defaultTsupConfig,
  target: 'es5',
  minify: true,
});