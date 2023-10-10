const CompressionPlugin = require("compression-webpack-plugin");
const BrotliPlugin = require("brotli-webpack-plugin");

module.exports = {
  plugins: [
    new CompressionPlugin({
        test: /\.(js|css|html)$/,
        algorithm: "gzip",
    }),
    new BrotliPlugin({
        test: /\.(js|css|html)$/,
    }),
  ],
};
