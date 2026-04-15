<script setup lang="ts">
import { RouterView, RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 사이드바 -->
    <aside class="fixed inset-y-0 left-0 w-64 bg-white border-r border-gray-200 flex flex-col">
      <div class="px-6 py-5 border-b border-gray-200">
        <h1 class="text-xl font-bold text-primary-600">API Service</h1>
      </div>

      <nav class="flex-1 px-4 py-4 space-y-1">
        <RouterLink
          to="/dashboard"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 transition-colors"
          active-class="bg-primary-50 text-primary-700"
        >
          대시보드
        </RouterLink>
        <RouterLink
          to="/resources"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 transition-colors"
          active-class="bg-primary-50 text-primary-700"
        >
          리소스
        </RouterLink>
        <RouterLink
          v-if="authStore.isAdmin"
          to="/admin/notifications"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 transition-colors"
          active-class="bg-primary-50 text-primary-700"
        >
          알림 관리
        </RouterLink>
      </nav>

      <div class="px-4 py-4 border-t border-gray-200">
        <div class="flex items-center gap-3 px-3 py-2">
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-900 truncate">{{ authStore.user?.name }}</p>
            <p class="text-xs text-gray-500 truncate">{{ authStore.user?.email }}</p>
          </div>
        </div>
        <button
          class="mt-2 w-full text-left px-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors"
          @click="handleLogout"
        >
          로그아웃
        </button>
      </div>
    </aside>

    <!-- 메인 콘텐츠 -->
    <main class="pl-64">
      <div class="px-8 py-8 max-w-6xl">
        <RouterView />
      </div>
    </main>
  </div>
</template>
