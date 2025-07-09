
export default {
  bootstrap: () => import('./main.server.mjs').then(m => m.default),
  inlineCriticalCss: true,
  baseHref: '/',
  locale: undefined,
  routes: undefined,
  entryPointToBrowserMapping: {
  "src/app/admin/admin.module.ts": [
    {
      "path": "chunk-SIPZW75S.js",
      "dynamicImport": false
    }
  ],
  "src/app/banque/components/client-list/client-list.component.ts": [
    {
      "path": "chunk-3542ZAOE.js",
      "dynamicImport": false
    },
    {
      "path": "chunk-PHJ6G2XC.js",
      "dynamicImport": false
    }
  ],
  "src/app/banque/banque.module.ts": [
    {
      "path": "chunk-CZT5J2LR.js",
      "dynamicImport": false
    },
    {
      "path": "chunk-PHJ6G2XC.js",
      "dynamicImport": false
    }
  ]
},
  assets: {
    'index.csr.html': {size: 23697, hash: '5aae4229f092890f9247d5151e7917be2d5dff3521a887d994a14d4e79dadf46', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 17271, hash: '41086ea31642c62c79f10c3986870e13c718e190b541055efe2bc89b884c0912', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'styles-36AW6TKX.css': {size: 6979, hash: 'vY6tjD/ce7M', text: () => import('./assets-chunks/styles-36AW6TKX_css.mjs').then(m => m.default)}
  },
};
