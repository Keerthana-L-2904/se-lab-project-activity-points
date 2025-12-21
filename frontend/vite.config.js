import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    allowedHosts: ["myapp.local"],
    proxy: {
      '/api': 'http://localhost:8080', // Proxy API calls to Spring Boot
    },
    //allowedHosts: ["myapp.local","localtest.me"]
 
  },
  resolve: {
    alias: {
      '@': '/src',
    },
  }
})
