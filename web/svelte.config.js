import adapter from '@sveltejs/adapter-node';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	preprocess: vitePreprocess(),
	kit: {
		adapter: adapter({
			// Production deployment notes:
			// - Response compression should be handled at the infrastructure level:
			//   - Railway: Enable gzip in app settings or use Cloudflare as CDN
			//   - Vercel/Cloudflare: Compression is automatic
			//   - Self-hosted: Use nginx with gzip_types configured
			precompress: true // Generate .gz and .br files for static assets
		})
	}
};

export default config;