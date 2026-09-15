import { createRequire } from 'node:module';
import { dirname, join } from 'node:path';
import type { StorybookConfig } from '@storybook/react-vite';
import { mergeConfig } from 'vite';

// Storybook 10 은 main 설정을 네이티브 ESM 으로 읽는다. 이 패키지는 `type: module` 이라
// `require` 가 없으므로 직접 만들어 쓴다.
const require = createRequire(import.meta.url);

/**
 * This function is used to resolve the absolute path of a package.
 * It is needed in projects that use Yarn PnP or are set up within a monorepo.
 */
function getAbsolutePath(value: string): string {
  return dirname(require.resolve(join(value, 'package.json')));
}
const config: StorybookConfig = {
  stories: ['../src/**/*.mdx', '../src/**/*.stories.@(js|jsx|mjs|ts|tsx)'],

  addons: [
    getAbsolutePath('@storybook/addon-links'),
    getAbsolutePath('@storybook/addon-onboarding'),
    getAbsolutePath("@storybook/addon-docs")
  ],

  framework: {
    name: getAbsolutePath('@storybook/react-vite'),
    options: {},
  },

  staticDirs: ['../../../apps/web/public'],

  async viteFinal(config, { configType }) {
    if (configType === 'DEVELOPMENT') {
      return mergeConfig(config, {
        define: { 'process.env': {} },
      });
    }
    return config;
  }
};
export default config;
