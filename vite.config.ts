import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated.ts';

const customConfig: UserConfigFn = (env) => ({
  build: {
    cssMinify: true,
    minify: 'esbuild',
    modulePreload: {
      polyfill: false
    }
  }
});

export default overrideVaadinConfig(customConfig);


