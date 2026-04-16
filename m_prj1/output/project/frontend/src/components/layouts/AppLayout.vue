<script setup lang="ts">
import { ref, computed } from 'vue'
import { RouterView, RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const authStore = useAuthStore()
const router = useRouter()
const sidebarOpen = ref(false)

const userInitial = computed(() =>
  authStore.user?.name?.charAt(0)?.toUpperCase() ?? '?'
)

async function handleLogout() {
  await authStore.logout()
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-zinc-50">

    <!-- Mobile backdrop -->
    <Transition name="fade">
      <div
        v-if="sidebarOpen"
        class="fixed inset-0 z-20 bg-black/30 backdrop-blur-sm lg:hidden"
        @click="sidebarOpen = false"
      />
    </Transition>

    <!-- Sidebar -->
    <aside
      class="fixed inset-y-0 left-0 z-30 flex w-60 flex-col bg-white border-r border-zinc-100
             transition-transform duration-300 ease-in-out lg:translate-x-0"
      :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
    >
      <!-- Logo -->
      <div class="flex h-16 items-center justify-between px-5 border-b border-zinc-100">
        <div class="flex items-center gap-2.5">
          <div class="h-7 w-7 rounded-lg bg-primary-600 flex items-center justify-center flex-shrink-0">
            <svg class="h-3.5 w-3.5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <span class="text-sm font-semibold text-zinc-900 tracking-tight">API Service</span>
        </div>
        <!-- Close (mobile) -->
        <button
          class="lg:hidden -mr-1 rounded-md p-1.5 text-zinc-400 hover:text-zinc-600 hover:bg-zinc-50 transition-colors"
          @click="sidebarOpen = false"
        >
          <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <!-- Nav -->
      <nav class="flex-1 overflow-y-auto px-3 py-4 space-y-0.5">
        <p class="px-3 pb-2 pt-1 text-[10px] font-semibold uppercase tracking-widest text-zinc-400">메뉴</p>

        <RouterLink
          to="/dashboard"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-zinc-500
                 hover:bg-zinc-50 hover:text-zinc-900 transition-colors"
          active-class="!bg-primary-50 !text-primary-700"
          @click="sidebarOpen = false"
        >
          <svg class="h-4 w-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.75"
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
          </svg>
          대시보드
        </RouterLink>

        <RouterLink
          to="/resources"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-zinc-500
                 hover:bg-zinc-50 hover:text-zinc-900 transition-colors"
          active-class="!bg-primary-50 !text-primary-700"
          @click="sidebarOpen = false"
        >
          <svg class="h-4 w-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.75"
              d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" />
          </svg>
          리소스
        </RouterLink>

        <RouterLink
          v-if="authStore.isAdmin"
          to="/admin/notifications"
          class="flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-zinc-500
                 hover:bg-zinc-50 hover:text-zinc-900 transition-colors"
          active-class="!bg-primary-50 !text-primary-700"
          @click="sidebarOpen = false"
        >
          <svg class="h-4 w-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.75"
              d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
          알림 관리
        </RouterLink>
      </nav>

      <!-- User -->
      <div class="border-t border-zinc-100 p-3 space-y-0.5">
        <div class="flex items-center gap-3 rounded-lg px-3 py-2">
          <div class="h-7 w-7 flex-shrink-0 rounded-full bg-primary-100 flex items-center justify-center">
            <span class="text-xs font-semibold text-primary-700">{{ userInitial }}</span>
          </div>
          <div class="min-w-0 flex-1">
            <p class="truncate text-sm font-medium text-zinc-900">{{ authStore.user?.name }}</p>
            <p class="truncate text-xs text-zinc-400">{{ authStore.user?.email }}</p>
          </div>
        </div>
        <button
          class="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-zinc-500
                 hover:bg-zinc-50 hover:text-red-600 transition-colors"
          @click="handleLogout"
        >
          <svg class="h-4 w-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.75"
              d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          로그아웃
        </button>
      </div>
    </aside>

    <!-- Mobile topbar -->
    <header
      class="fixed inset-x-0 top-0 z-10 flex h-16 items-center border-b border-zinc-100 bg-white/90 backdrop-blur-sm px-4 lg:hidden"
    >
      <button
        class="rounded-lg p-2 text-zinc-500 hover:bg-zinc-50 hover:text-zinc-700 transition-colors"
        @click="sidebarOpen = true"
      >
        <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7" />
        </svg>
      </button>
      <div class="ml-3 flex items-center gap-2">
        <div class="h-6 w-6 rounded-md bg-primary-600 flex items-center justify-center">
          <svg class="h-3 w-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        <span class="text-sm font-semibold text-zinc-900">API Service</span>
      </div>
    </header>

    <!-- Main content -->
    <main class="lg:pl-60">
      <div class="pt-16 lg:pt-0">
        <div class="mx-auto max-w-5xl px-4 py-8 sm:px-6 lg:px-8">
          <RouterView />
        </div>
      </div>
    </main>

  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
