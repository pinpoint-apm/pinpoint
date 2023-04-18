// const webpack = require('webpack');
const withPlugins = require('next-compose-plugins');
const withTM = require('next-transpile-modules');

const TM = withTM(['@pinpoint-fe/ui']);

const nextConfig = {
  async redirects() {
    return [
      {
        source: '/',
        destination: '/serverMap',
        permanent: true,
      },
      {
        source: '/main/:path*',
        destination: '/serverMap/:path*',
        permanent: true,
      },
    ];
  },
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/:path*',
      },
    ];
  },
  // webpack(config) {
  //   console.log(config);
  //   return {
  //     ...config,
  //     plugins: [
  //       ...config.plugins,
  //       new webpack.DefinePlugin({ 'global.GENTLY': false }),
  //     ],
  //   };
  // },
};

module.exports = withPlugins([TM], nextConfig);
// module.exports = nextConfig;
