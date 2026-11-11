import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Em dev, /api/... vai para o back local (localhost:8080), igual ao vercel.json em producao
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
