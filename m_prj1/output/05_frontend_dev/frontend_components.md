# Vue 3 컴포넌트 구현

## 1. 로그인 폼 (VeeValidate + Zod)

### `src/pages/LoginPage.vue`
```vue
<script setup lang="ts">
import { useForm } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { z } from 'zod'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter, useRoute } from 'vue-router'
import { ref } from 'vue'

const loginSchema = toTypedSchema(
  z.object({
    email: z.string().email('올바른 이메일을 입력하세요'),
    password: z.string().min(8, '비밀번호는 8자 이상이어야 합니다')
  })
)

const { handleSubmit, errors, isSubmitting } = useForm({ validationSchema: loginSchema })
const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()
const apiError = ref<string | null>(null)

const onSubmit = handleSubmit(async (values) => {
  apiError.value = null
  try {
    await authStore.login(values)
    const redirect = route.query.redirect as string
    router.push(redirect || '/dashboard')
  } catch (err: any) {
    apiError.value = err.response?.data?.error?.detail ?? '로그인에 실패했습니다'
  }
})

const oauthLogin = (provider: 'google' | 'kakao') => {
  const url = provider === 'google'
    ? import.meta.env.VITE_OAUTH2_GOOGLE_URL
    : import.meta.env.VITE_OAUTH2_KAKAO_URL
  window.location.href = url
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50">
    <div class="max-w-md w-full space-y-8 p-8 bg-white rounded-xl shadow-md">
      <h2 class="text-2xl font-bold text-center text-gray-900">로그인</h2>

      <!-- 에러 메시지 -->
      <div v-if="apiError" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded">
        {{ apiError }}
      </div>

      <form @submit="onSubmit" class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-gray-700">이메일</label>
          <input
            v-model="email"
            type="email"
            class="mt-1 block w-full border rounded-md px-3 py-2 focus:ring-primary-500"
            :class="{ 'border-red-500': errors.email }"
          />
          <p v-if="errors.email" class="mt-1 text-sm text-red-500">{{ errors.email }}</p>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700">비밀번호</label>
          <input
            v-model="password"
            type="password"
            class="mt-1 block w-full border rounded-md px-3 py-2 focus:ring-primary-500"
            :class="{ 'border-red-500': errors.password }"
          />
          <p v-if="errors.password" class="mt-1 text-sm text-red-500">{{ errors.password }}</p>
        </div>

        <button
          type="submit"
          :disabled="isSubmitting"
          class="w-full py-2 px-4 bg-primary-600 text-white rounded-md hover:bg-primary-700 disabled:opacity-50"
        >
          {{ isSubmitting ? '로그인 중...' : '로그인' }}
        </button>
      </form>

      <div class="relative">
        <div class="absolute inset-0 flex items-center">
          <div class="w-full border-t border-gray-300" />
        </div>
        <div class="relative flex justify-center text-sm">
          <span class="px-2 bg-white text-gray-500">또는</span>
        </div>
      </div>

      <div class="space-y-2">
        <button
          @click="oauthLogin('google')"
          class="w-full flex items-center justify-center gap-2 py-2 px-4 border border-gray-300 rounded-md hover:bg-gray-50"
        >
          <img src="/google-icon.svg" class="w-5 h-5" alt="Google" />
          Google로 로그인
        </button>
        <button
          @click="oauthLogin('kakao')"
          class="w-full flex items-center justify-center gap-2 py-2 px-4 bg-yellow-400 text-yellow-900 rounded-md hover:bg-yellow-500"
        >
          카카오로 로그인
        </button>
      </div>
    </div>
  </div>
</template>
```

---

## 2. 리소스 목록 페이지 (페이지네이션 + 필터)

### `src/pages/ResourceListPage.vue`
```vue
<script setup lang="ts">
import { ref, computed } from 'vue'
import { useResourceList, useDeleteResource } from '@/composables/useResources'
import BasePagination from '@/components/ui/BasePagination.vue'
import ResourceFilter from '@/components/features/resources/ResourceFilter.vue'

const page = ref(0)
const size = ref(20)
const status = ref<string | undefined>()
const keyword = ref('')

const params = computed(() => ({
  page: page.value,
  size: size.value,
  sort: 'createdAt,desc',
  status: status.value,
  keyword: keyword.value || undefined
}))

const { data, isLoading, isError } = useResourceList(params)
const { mutate: deleteResource, isPending: isDeleting } = useDeleteResource()

const resources = computed(() => data.value?.data ?? [])
const meta = computed(() => data.value?.meta)

function handleDelete(id: number) {
  if (confirm('정말 삭제하시겠습니까?')) {
    deleteResource(id)
  }
}
</script>

<template>
  <div class="p-6">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">리소스 목록</h1>
      <router-link
        to="/resources/new"
        class="px-4 py-2 bg-primary-600 text-white rounded-md hover:bg-primary-700"
      >
        + 새 리소스
      </router-link>
    </div>

    <!-- 필터 -->
    <ResourceFilter v-model:status="status" v-model:keyword="keyword" @search="page = 0" />

    <!-- 로딩 -->
    <div v-if="isLoading" class="flex justify-center py-12">
      <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600" />
    </div>

    <!-- 에러 -->
    <div v-else-if="isError" class="text-center py-12 text-red-500">
      데이터를 불러오는 중 오류가 발생했습니다.
    </div>

    <!-- 테이블 -->
    <div v-else class="bg-white rounded-lg shadow overflow-hidden">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">이름</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">상태</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">생성일</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">액션</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-if="resources.length === 0">
            <td colspan="5" class="px-6 py-12 text-center text-gray-500">
              데이터가 없습니다
            </td>
          </tr>
          <tr v-for="item in resources" :key="item.id" class="hover:bg-gray-50">
            <td class="px-6 py-4 text-sm text-gray-500">{{ item.id }}</td>
            <td class="px-6 py-4">
              <router-link :to="`/resources/${item.id}`" class="text-primary-600 hover:underline">
                {{ item.name }}
              </router-link>
            </td>
            <td class="px-6 py-4">
              <span
                class="px-2 py-1 text-xs rounded-full"
                :class="{
                  'bg-green-100 text-green-800': item.status === 'ACTIVE',
                  'bg-gray-100 text-gray-800': item.status === 'INACTIVE'
                }"
              >
                {{ item.status }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-500">
              {{ new Date(item.createdAt).toLocaleDateString('ko-KR') }}
            </td>
            <td class="px-6 py-4 text-right space-x-2">
              <router-link :to="`/resources/${item.id}/edit`" class="text-sm text-blue-600 hover:underline">
                편집
              </router-link>
              <button
                @click="handleDelete(item.id)"
                :disabled="isDeleting"
                class="text-sm text-red-600 hover:underline disabled:opacity-50"
              >
                삭제
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 페이지네이션 -->
      <div class="px-6 py-4 border-t border-gray-200">
        <BasePagination
          v-if="meta"
          :current-page="meta.page"
          :total-pages="meta.totalPages"
          :total-elements="meta.totalElements"
          @update:page="page = $event"
        />
      </div>
    </div>
  </div>
</template>
```

---

## 3. 페이지네이션 컴포넌트

### `src/components/ui/BasePagination.vue`
```vue
<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  currentPage: number
  totalPages: number
  totalElements: number
  maxVisible?: number
}>()

const emit = defineEmits<{
  'update:page': [page: number]
}>()

const maxVisible = props.maxVisible ?? 5

const pages = computed(() => {
  const half = Math.floor(maxVisible / 2)
  let start = Math.max(0, props.currentPage - half)
  let end = Math.min(props.totalPages - 1, start + maxVisible - 1)
  if (end - start < maxVisible - 1) {
    start = Math.max(0, end - maxVisible + 1)
  }
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})
</script>

<template>
  <div class="flex items-center justify-between">
    <p class="text-sm text-gray-700">
      총 <span class="font-medium">{{ totalElements }}</span>건
    </p>

    <nav class="flex items-center space-x-1">
      <button
        @click="emit('update:page', currentPage - 1)"
        :disabled="currentPage === 0"
        class="px-2 py-1 rounded text-sm disabled:opacity-40 hover:bg-gray-100"
      >
        ←
      </button>

      <button
        v-for="p in pages"
        :key="p"
        @click="emit('update:page', p)"
        class="px-3 py-1 rounded text-sm"
        :class="p === currentPage
          ? 'bg-primary-600 text-white'
          : 'hover:bg-gray-100 text-gray-700'"
      >
        {{ p + 1 }}
      </button>

      <button
        @click="emit('update:page', currentPage + 1)"
        :disabled="currentPage >= totalPages - 1"
        class="px-2 py-1 rounded text-sm disabled:opacity-40 hover:bg-gray-100"
      >
        →
      </button>
    </nav>
  </div>
</template>
```

---

## 4. Toast 알림 시스템

### `src/composables/useToast.ts`
```typescript
import { ref } from 'vue'

interface Toast {
  id: string
  type: 'success' | 'error' | 'info' | 'warning'
  message: string
}

const toasts = ref<Toast[]>([])

export function useToast() {
  function show(type: Toast['type'], message: string, duration = 3000) {
    const id = crypto.randomUUID()
    toasts.value.push({ id, type, message })
    setTimeout(() => {
      toasts.value = toasts.value.filter(t => t.id !== id)
    }, duration)
  }

  return {
    toasts,
    showSuccess: (msg: string) => show('success', msg),
    showError: (msg: string) => show('error', msg),
    showInfo: (msg: string) => show('info', msg),
    showWarning: (msg: string) => show('warning', msg)
  }
}
```

### `src/components/ui/ToastContainer.vue`
```vue
<script setup lang="ts">
import { useToast } from '@/composables/useToast'
const { toasts } = useToast()
</script>

<template>
  <div class="fixed top-4 right-4 z-50 space-y-2">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="px-4 py-3 rounded-lg shadow-lg text-white text-sm max-w-sm"
        :class="{
          'bg-green-500': toast.type === 'success',
          'bg-red-500': toast.type === 'error',
          'bg-blue-500': toast.type === 'info',
          'bg-yellow-500': toast.type === 'warning'
        }"
      >
        {{ toast.message }}
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-enter-active, .toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from { opacity: 0; transform: translateX(100%); }
.toast-leave-to { opacity: 0; transform: translateX(100%); }
</style>
```
