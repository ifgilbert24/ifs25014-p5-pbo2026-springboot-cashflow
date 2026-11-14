import { Plugin, UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated.ts';

const USED_VAADIN_ICONS = new Set([
  'wallet',
  'user',
  'users',
  'chevron-down-small',
  'sign-out',
  'angle-down',
  'eye',
  'eye-slash',
  'menu',
  'upload',
  'close-small',
  'close',
  'check',
  'warning',
]);

const AUTH_SHELL_PREFIXES = [
  '@vaadin/app-layout/',
  '@vaadin/icons/',
  '@vaadin/icon/',
  '@vaadin/menu-bar/',
  '@vaadin/context-menu/',
  'Frontend/generated/jar-resources/vaadin-menu-bar/',
  'Frontend/generated/jar-resources/vaadin-context-menu/',
  '@vaadin/avatar/',
];

const AUTH_GRID_PREFIXES = [
  '@vaadin/grid/',
  'Frontend/generated/jar-resources/vaadin-grid/',
  '@vaadin/checkbox/',
  'Frontend/generated/jar-resources/lit-renderer.ts',
  'lit/directives/live.js',
];

const AUTH_OVERLAY_PREFIXES = [
  '@vaadin/dialog/',
  '@vaadin/progress-bar/',
  '@vaadin/select/',
  '@vaadin/upload/',
  'Frontend/generated/jar-resources/vaadin-upload/',
  '@vaadin/tooltip/',
];

type FlowBucket = 'shell' | 'grid' | 'overlay' | 'core';

function classifyFlowImport(specifier: string): FlowBucket {
  if (AUTH_SHELL_PREFIXES.some((p) => specifier.startsWith(p))) return 'shell';
  if (AUTH_GRID_PREFIXES.some((p) => specifier.startsWith(p))) return 'grid';
  if (AUTH_OVERLAY_PREFIXES.some((p) => specifier.startsWith(p))) return 'overlay';
  return 'core';
}

function optimizeVaadinBundlePlugin(): Plugin {
  const virtualBuckets: Record<string, string[]> = {
    'virtual:flow-shell-imports': [],
    'virtual:flow-grid-imports': [],
    'virtual:flow-overlay-imports': [],
  };

  return {
    name: 'optimize-vaadin-bundle',
    resolveId(source) {
      if (source in virtualBuckets) {
        return '\0' + source;
      }
      return null;
    },
    load(id) {
      if (id.startsWith('\0virtual:flow-')) {
        const key = id.slice(1);
        const imports = virtualBuckets[key] || [];
        return imports.map((spec) => `import '${spec}';`).join('\n') || 'export {};';
      }
      return null;
    },
    transform(code, id) {
      const normalizedId = id.replace(/\\/g, '/');

      if (normalizedId.endsWith('/@vaadin/icons/vaadin-iconset.js')) {
        const lines = code.split(/\r?\n/);
        const filtered = lines.filter((line) => {
          const m = line.match(/^<g id="vaadin:([^"]+)">/);
          return !m || USED_VAADIN_ICONS.has(m[1]);
        });
        return { code: filtered.join('\n'), map: null };
      }

      if (normalizedId.endsWith('/@vaadin/common-frontend/ConnectionIndicator.js')) {
        let updated = code.replace(
          'document.body.appendChild($wnd.Vaadin.connectionIndicator);',
          '(document.getElementById("app-status-landmark") || document.body).appendChild($wnd.Vaadin.connectionIndicator);'
        );
        updated = updated.replace(
          'display: contents;',
          'display: block;\n        height: 0;\n        overflow: visible;'
        );
        return { code: updated, map: null };
      }

      if (normalizedId.includes('/generated/flow/chunks/chunk-') && normalizedId.endsWith('.js')) {
        return { code: 'export {};', map: null };
      }

      if (normalizedId.endsWith('/generated/flow/generated-flow-imports.js')) {
        for (const key of Object.keys(virtualBuckets)) {
          virtualBuckets[key] = [];
        }

        const lines = code.split(/\r?\n/);
        const coreLines: string[] = [];
        for (const line of lines) {
          const m = line.match(/^\s*import\s+['"]([^'"]+)['"]\s*;/);
          if (m) {
            const spec = m[1];
            const bucket = classifyFlowImport(spec);
            if (bucket !== 'core') {
              virtualBuckets[`virtual:flow-${bucket}-imports`].push(spec);
              continue;
            }
          }
          coreLines.push(line);
        }

        const loaderBlock = `
let __authPromise = null;
let __overlayPromise = null;
const __loadOverlay = () => {
  if (!__overlayPromise) {
    __overlayPromise = import('virtual:flow-overlay-imports');
  }
  return __overlayPromise;
};
const __needsOverlayNow = () => /^\\/profile\\/?$/.test(window.location.pathname);
const __loadAuthModules = (withOverlay = false) => {
  if (!__authPromise) {
    const tasks = [
      import('virtual:flow-shell-imports'),
      import('virtual:flow-grid-imports'),
    ];
    if (withOverlay || __needsOverlayNow()) {
      tasks.push(__loadOverlay());
    }
    __authPromise = Promise.all(tasks);
  }
  return withOverlay ? Promise.all([__authPromise, __loadOverlay()]) : __authPromise;
};
const __isPublicAuth = () => /^\\/(login|register)\\/?$/.test(window.location.pathname);
if (!__isPublicAuth()) {
  await __loadAuthModules(__needsOverlayNow());
}
const __pre = () => { __loadAuthModules(true); };
window.addEventListener('pointerdown', __pre, { once: true, passive: true, capture: true });
window.addEventListener('keydown', __pre, { once: true, passive: true, capture: true });
window.addEventListener('touchstart', __pre, { once: true, passive: true, capture: true });
window.addEventListener('click', __pre, { once: true, passive: true, capture: true });
window.addEventListener('popstate', () => { if (!__isPublicAuth()) __loadAuthModules(true); });
`;

        let out = coreLines.join('\n');
        out = out.replace(
          'const loadOnDemand = (key) => {',
          `${loaderBlock}\nconst loadOnDemand = (key) => {`
        );
        out = out.replace(
          '  const pending = [];',
          '  const pending = __isPublicAuth() ? [] : [__loadAuthModules(__needsOverlayNow())];'
        );
        out = out.replaceAll(
          'pending.push(import(',
          'pending.push((__isPublicAuth() ? Promise.resolve() : __loadAuthModules(__needsOverlayNow())), import('
        );
        return { code: out, map: null };
      }

      return null;
    },
  };
}

const customConfig: UserConfigFn = (env) => ({
  plugins: [optimizeVaadinBundlePlugin()],
  build: {
    target: 'es2022',
    cssMinify: true,
    minify: true,
    modulePreload: {
      polyfill: false
    }
  }
});

export default overrideVaadinConfig(customConfig);


