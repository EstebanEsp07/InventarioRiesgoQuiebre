import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// El navegador llama a /api en el mismo origen; Vite reenvía al backend.
// Así funciona igual en local, en docker compose y en Codespaces (sin CORS).
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.API_PROXY_TARGET ?? 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
