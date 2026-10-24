import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// The app calls /api/... (catalog-service) and /price-api/... (fake-price-service); in development Vite
// forwards those to the backends, so no CORS is needed. In Docker, nginx does the same (see nginx.conf).
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
      '/price-api': {
        target: 'http://localhost:8081',
        rewrite: (path) => path.replace(/^\/price-api/, ''),
      },
    },
  },
})
