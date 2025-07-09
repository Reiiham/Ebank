
export default {
  bootstrap: () => import('./main.server.mjs').then(m => m.default),
  inlineCriticalCss: true,
  baseHref: '/',
  locale: undefined,
  routes: undefined,
  entryPointToBrowserMapping: {
  "src/app/admin/admin.module.ts": [
    {
      "path": "chunk-EKVJFI5C.js",
      "dynamicImport": false
    }
  ],
  "src/app/banque/components/client-list/client-list.component.ts": [
    {
      "path": "chunk-KUTWDLIM.js",
      "dynamicImport": false
    },
    {
      "path": "chunk-X2QOSOHC.js",
      "dynamicImport": false
    }
  ],
  "src/app/banque/banque.module.ts": [
    {
      "path": "chunk-GB5RHHVO.js",
      "dynamicImport": false
    },
    {
      "path": "chunk-X2QOSOHC.js",
      "dynamicImport": false
    }
  ]
},
  assets: {
    'index.csr.html': {size: 23697, hash: 'faa17324d5f8b1df1e764c537618fecec95856c009000ef23705c332976c77d2', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 17271, hash: 'c1d7a0aa7c2ab3dbeaa33f59fc6ac8bd23aeb8742e36ade23744a347ef0460ee', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'styles-36AW6TKX.css': {size: 6979, hash: 'vY6tjD/ce7M', text: () => import('./assets-chunks/styles-36AW6TKX_css.mjs').then(m => m.default)}
  },
};
