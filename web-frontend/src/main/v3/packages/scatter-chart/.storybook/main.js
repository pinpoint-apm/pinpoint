const {
  dirname,
  join
} = require("node:path");

module.exports = {
  stories: ['../src/stories/**/*.stories.mdx', '../src/stories/**/*.stories.@(js|jsx|ts|tsx)'],
  addons: [getAbsolutePath("@storybook/addon-links"), getAbsolutePath("storybook-css-modules"), getAbsolutePath("@storybook/addon-docs")],
  framework: {
    name: getAbsolutePath("@storybook/html-vite"),
    options: {},
  },
  // webpackFinal: async (config, { configType }) => {
  //   // `configType` has a value of 'DEVELOPMENT' or 'PRODUCTION'
  //   // You can change the configuration based on that.
  //   // 'PRODUCTION' is used when building the static version of storybook.

  //   // Make whatever fine-grained changes you need
  //   config.module.rules.push({
  //     test: /\.css$/,
  //     use: ['style-loader', 'css-loader'],
  //     include: path.resolve(__dirname, '../'),
  //   });

  //   return config;
  // },
};

function getAbsolutePath(value) {
  return dirname(require.resolve(join(value, "package.json")));
}
