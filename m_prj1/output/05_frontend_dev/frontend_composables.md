# Vue 3 Composables & API 연동 패턴

## 1. Axios 인스턴스 + Interceptor

### `src/lib/axios.ts`
```typescript
import axios, { type AxiosInstance, type AxiosError } from 'axios'
import { useAuthStore } from '@/stores/auth.store'
import router from '@/router'

const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10_000,
  withCredentials: true,         // Cookie(refreshToken) 전송
  headers: {
    'Content-Type': 'application/json'
  }
})

// 요청 인터셉터: JWT Access Token 자동 첨부
api.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.accessToken) {
    config.headers.Authorization = `Bearer ${authStore.accessToken}`
  }
  // 추적을 위한 요청 ID
  config.headers['X-Request-ID'] = crypto.randomUUID()
  return config
})

// 응답 인터셉터: 401 시 토큰 갱신
let isRefreshing = false
let failedQueue: Array<{ resolve: Function; reject: Function }> = []

const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach(({ resolve, reject }) => {
    error ? reject(error) : resolve(token)
  })
  failedQueue = []
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as any

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          return api(originalRequest)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const authStore = useAuthStore()
        const newToken = await authStore.refreshToken()
        processQueue(null, newToken)
        originalRequest.headers.Authorization = `Bearer ${newToken}`
        return api(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError as Error, null)
        const authStore = useAuthStore()
        authStore.clearAuth()
        router.push('/login')
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  }
)

export default api
```

---

## 2. API 함수 레이어

### `src/api/auth.api.ts`
```typescript
import api from '@/lib/axios'
import type { LoginRequest, LoginResponse, TokenResponse } from '@/types/auth.types'

export const authApi = {
  login(data: LoginRequest) {
    return api.post<ApiResponse<LoginResponse>>('/api/v1/auth/login', data)
  },

  refresh() {
    return api.post<ApiResponse<TokenResponse>>('/api/v1/auth/refresh')
  },

  logout() {
    return api.post<ApiResponse<void>>('/api/v1/auth/logout')
  },

  getMe() {
    return api.get<ApiResponse<UserDto>>('/api/v1/users/me')
  }
}
```

### `src/api/resources.api.ts`
```typescript
import api from '@/lib/axios'
import type { ResourceDto, CreateResourceRequest, UpdateResourceRequest } from '@/types/resource.types'
import type { PaginatedResponse, PageParams } from '@/types/api.types'

export const resourcesApi = {
  getList(params: PageParams & { status?: string; keyword?: string }) {
    return api.get<PaginatedResponse<ResourceDto[]>>('/api/v1/resources', { params })
  },

  getOne(id: number) {
    return api.get<ApiResponse<ResourceDto>>(`/api/v1/resources/${id}`)
  },

  create(data: CreateResourceRequest) {
    return api.post<ApiResponse<ResourceDto>>('/api/v1/resources', data)
  },

  update(id: number, data: UpdateResourceRequest) {
    return api.put<ApiResponse<ResourceDto>>(`/api/v1/resources/${id}`, data)
  },

  delete(id: number) {
    return api.delete<ApiResponse<void>>(`/api/v1/resources/${id}`)
  }
}
```

---

## 3. 타입 정의

### `src/types/api.types.ts`
```typescript
export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: ApiError | null
  meta: PageMeta | null
}

export interface PaginatedResponse<T> extends ApiResponse<T> {
  meta: PageMeta
}

export interface ApiError {
  type: string
  title: string
  status: number
  detail: string
  instance: string
}

export interface PageMeta {
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export interface PageParams {
  page?: number
  size?: number
  sort?: string
}
```

### `src/types/auth.types.ts`
```typescript
export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: UserDto
}

export interface UserDto {
  id: string
  email: string
  name: string
  role: 'USER' | 'ADMIN'
}
```

---

## 4. Pinia Auth Store

### `src/stores/auth.store.ts`
```typescript
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth.api'
import type { UserDto, LoginRequest } from '@/types/auth.types'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
  const user = ref<UserDto | null>(null)

  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(credentials: LoginRequest) {
    const { data } = await authApi.login(credentials)
    if (!data.data) throw new Error('로그인에 실패했습니다')

    accessToken.value = data.data.accessToken
    user.value = data.data.user
    localStorage.setItem('accessToken', data.data.accessToken)
  }

  async function refreshToken(): Promise<string> {
    const { data } = await authApi.refresh()
    if (!data.data) throw new Error('토큰 갱신 실패')

    accessToken.value = data.data.accessToken
    localStorage.setItem('accessToken', data.data.accessToken)
    return data.data.accessToken
  }

  async function fetchMe() {
    const { data } = await authApi.getMe()
    if (data.data) user.value = data.data
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      clearAuth()
    }
  }

  function clearAuth() {
    accessToken.value = null
    user.value = null
    localStorage.removeItem('accessToken')
  }

  return {
    accessToken, user,
    isAuthenticated, isAdmin,
    login, refreshToken, fetchMe, logout, clearAuth
  }
})
```

---

## 5. TanStack Query Composables

### `src/composables/useResources.ts`
```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { computed, type Ref } from 'vue'
import { resourcesApi } from '@/api/resources.api'
import { useToast } from '@/composables/useToast'
import type { PageParams, CreateResourceRequest } from '@/types'

// 목록 조회
export function useResourceList(params: Ref<PageParams & { status?: string; keyword?: string }>) {
  return useQuery({
    queryKey: computed(() => ['resources', params.value]),
    queryFn: async () => {
      const { data } = await resourcesApi.getList(params.value)
      return data
    },
    keepPreviousData: true   // 페이지 전환 시 이전 데이터 유지
  })
}

// 단건 조회
export function useResource(id: Ref<number | null>) {
  return useQuery({
    queryKey: computed(() => ['resources', id.value]),
    queryFn: async () => {
      const { data } = await resourcesApi.getOne(id.value!)
      return data.data
    },
    enabled: computed(() => !!id.value)
  })
}

// 생성
export function useCreateResource() {
  const queryClient = useQueryClient()
  const { showSuccess, showError } = useToast()

  return useMutation({
    mutationFn: (data: CreateResourceRequest) => resourcesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['resources'] })
      showSuccess('리소스가 생성되었습니다')
    },
    onError: (error: any) => {
      const detail = error.response?.data?.error?.detail ?? '생성에 실패했습니다'
      showError(detail)
    }
  })
}

// 삭제
export function useDeleteResource() {
  const queryClient = useQueryClient()
  const { showSuccess, showError } = useToast()

  return useMutation({
    mutationFn: (id: number) => resourcesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['resources'] })
      showSuccess('리소스가 삭제되었습니다')
    },
    onError: () => showError('삭제에 실패했습니다')
  })
}
```

---

## 6. Vue Router (Route Guards)

### `src/router/index.ts`
```typescript
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/pages/LoginPage.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/oauth2/callback',
      name: 'oauth2-callback',
      component: () => import('@/pages/OAuth2CallbackPage.vue')
    },
    {
      path: '/',
      component: () => import('@/components/layouts/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: () => import('@/pages/DashboardPage.vue') },
        { path: 'resources', name: 'resources', component: () => import('@/pages/ResourceListPage.vue') },
        { path: 'resources/:id', name: 'resource-detail', component: () => import('@/pages/ResourceDetailPage.vue') },
        {
          path: 'admin',
          meta: { requiresAdmin: true },
          children: [
            { path: 'notifications', component: () => import('@/pages/admin/NotificationPage.vue') }
          ]
        }
      ]
    }
  ]
})

// Navigation Guard
router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    return { path: '/dashboard' }
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { path: '/dashboard' }
  }

  // 인증된 상태에서 유저 정보 없으면 fetch
  if (authStore.isAuthenticated && !authStore.user) {
    try {
      await authStore.fetchMe()
    } catch {
      authStore.clearAuth()
      return { name: 'login' }
    }
  }
})

export default router
```

---

## 7. OAuth2 Callback 처리

### `src/pages/OAuth2CallbackPage.vue`
```vue
<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  const token = route.query.token as string
  const error = route.query.error as string

  if (error) {
    router.push({ path: '/login', query: { error } })
    return
  }

  if (token) {
    // 백엔드에서 redirect_uri에 token 쿼리 파라미터로 전달
    authStore.accessToken = token
    localStorage.setItem('accessToken', token)
    await authStore.fetchMe()

    const redirect = route.query.redirect as string
    router.push(redirect || '/dashboard')
  }
})
</script>

<template>
  <div class="flex items-center justify-center h-screen">
    <div class="text-center">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600 mx-auto mb-4" />
      <p class="text-gray-600">로그인 처리 중...</p>
    </div>
  </div>
</template>
```
