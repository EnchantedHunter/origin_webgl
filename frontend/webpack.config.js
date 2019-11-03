const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    mode: 'development',
    entry: './src/main.ts',
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: {
                    loader: 'awesome-typescript-loader',
                    options: {
                        useCache: true
                    }
                },
                exclude: '/node_modules/',
            },
        ],
    },
    plugins: [
        new HtmlWebpackPlugin({
            hash: true,
            title: 'Origin',
            template: "./src/index.html",
            filename: '../dist/index.html'
        })
    ],
    resolve: {
        modules: ["node_modules"],
        extensions: [".js", ".ts", ".tsx"]
    },
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(path.join(__dirname, "..", "dist")),
    },
    devServer: {
        hot: true,
        compress: true,
        overlay: true
    },
    target: "web"
};