/**
 * uni-app (vue-cli) build config.
 *
 * uview-plus ships some Vue SFCs with TS that triggers fork-ts-checker diagnostics
 * in HBuilderX dev mode. These type errors are inside dependencies and should not
 * block compilation/runtime, so we disable the checker.
 */
module.exports = {
  chainWebpack: config => {
    // Disable TypeScript type-checking plugin to avoid errors in node_modules
    config.plugins.delete('fork-ts-checker');
  }
};

