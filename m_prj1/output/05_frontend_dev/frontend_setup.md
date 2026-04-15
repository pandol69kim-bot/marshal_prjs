# 프론트엔드 설정 가이드 (Vue 3 + Vite)

## 1. 프로젝트 초기화

```bash
# Vue 3 + TypeScript + Vite
npm create vue@latest frontend -- \
  --typescript \
  --router \
  --pinia \
  --eslint \
  --prettier

cd frontend

# 추가 의존성
npm install axios @tanstack/vue-query
npm install vee-validate zod @vee-validate/zod
npm install @vueuse/core
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

## 2. vite.config.ts

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 3. 환경 변수 (.env)

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_NAME=My App
VITE_OAUTH2_GOOGLE_URL=http://localhost:8080/oauth2/authorize/google
VITE_OAUTH2_KAKAO_URL=http://localhost:8080/oauth2/authorize/kakao
```

## 4. tailwind.config.js

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff', 100: '#dbeafe',
          500: '#3b82f6', 600: '#2563eb',
          700: '#1d4ed8', 900: '#1e3a8a'
        }
      }
    }
  },
  plugins: []
}
```

## 5. main.ts

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin, QueryClient } from '@tanstack/vue-query'
import App from './App.vue'
import router from './router'
import './assets/main.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60,      // 1분
      retry: 1,
      refetchOnWindowFocus: false
    }
  }
})

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(VueQueryPlugin, { queryClient })
app.mount('#app')
```
