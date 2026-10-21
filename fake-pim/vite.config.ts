import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// The app calls /api/...; in development Vite forwards that to the backend, so no CORS is needed.
// In Docker, nginx does the same (see nginx.conf).
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
