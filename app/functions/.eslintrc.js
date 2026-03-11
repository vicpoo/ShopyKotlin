module.exports = {
  env: {
    node: true,
    es2021: true,
  },
  extends: [],
  parserOptions: {
    ecmaVersion: 12,
    sourceType: 'module',
  },
  rules: {
    'no-unused-vars': 'off',
    'object-curly-spacing': 'off',
  },
  ignorePatterns: ['node_modules/'],
};