import { defineConfig } from 'tsup';
import defaultTsupConfig from './tsup.config.dev';

export default defineConfig({
  ...defaultTsupConfig,
  target: 'es6',
  minify: true,
});
