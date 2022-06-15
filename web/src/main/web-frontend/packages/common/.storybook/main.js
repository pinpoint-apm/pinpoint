const path = require('path');
const fs = require('fs');

module.exports = {
  stories: [
    '../**/*.stories.mdx',
    '../**/*.stories.@(js|jsx|ts|tsx)'
  ],
  addons: [
    '@storybook/addon-links',
    '@storybook/addon-essentials',
    '@storybook/addon-interactions'
  ],
  framework: '@storybook/react',
  core: {
    builder: '@storybook/builder-webpack5'
  },
  typescript: { reactDocgen: false },
  staticDirs: ['../../../apps/web/public'],
  webpackFinal: async (config) => {
    config.resolve = {
      ...config.resolve,
      alias: {
        ...config.resolve.alias,
        // path alias
        '@/hooks': path.resolve(__dirname, '../hooks'),
        '@/utils': path.resolve(__dirname, '../utils'),
        '@/styles': path.resolve(__dirname, '../styles'),
        '@/components': path.resolve(__dirname, '../components'),
        // for emotion
        '@emotion/core': getPackageDir('@emotion/react'),
        '@emotion/styled': getPackageDir('@emotion/styled'),
        'emotion-theming': getPackageDir('@emotion/react'),
      },
    };

    return config;
  },
}

function getPackageDir(filepath) {
  let currDir = path.dirname(require.resolve(filepath));
  while (true) {
    if (fs.existsSync(path.join(currDir, 'package.json'))) {
      return currDir;
    }
    const { dir, root } = path.parse(currDir);

    if (dir === root) {
      throw new Error(
        `Could not find package.json in the parent directories starting from ${filepath}.`
      );
    }
    currDir = dir;
  }
}