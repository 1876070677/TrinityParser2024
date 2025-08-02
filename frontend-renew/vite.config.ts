import { defineConfig, ConfigEnv, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import fs from 'fs';

export default defineConfig(({ mode }) => {
  const isDev = mode === 'development';
  return {
    base: '/fe',
    plugins: [react()],
    server: {
      ...(isDev && {
        https: {
          key: fs.readFileSync('./mkcert/localhost+1-key.pem'),
          cert: fs.readFileSync('./mkcert/localhost+1.pem'),
        },
        proxy: {
          '/manage': {
            target: 'https://trinity.dobby.kr',
            changeOrigin: true,
            secure: false,
            rewrite: (path) => path,  // 경로 그대로 전달
          },
          '/trinity': {
            target: 'https://trinity.dobby.kr',
            changeOrigin: true,
            secure: false,
            rewrite: (path) => path,  // 경로 그대로 전달
          }
        }
      })
    }
  }
});