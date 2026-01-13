import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    allowedHosts: ["myapp.local"],
    //allowedHosts: ["myapp.local","localtest.me"]
 
  },
  resolve: {
    alias: {
      '@': '/src',
    },
  }
})
