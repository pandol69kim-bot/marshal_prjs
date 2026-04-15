<script setup lang="ts">
import { useAuthStore } from '@/stores/auth.store'
import { useResourceList } from '@/composables/useResources'
import { ref } from 'vue'

const authStore = useAuthStore()
const params = ref({ page: 0, size: 5 })
const { data, isLoading } = useResourceList(params)
</script>

<template>
  <div>
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-900">대시보드</h1>
      <p class="mt-1 text-gray-600">안녕하세요, {{ authStore.user?.name }}님!</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="card">
        <p class="text-sm font-medium text-gray-500">총 리소스</p>
        <p class="mt-2 text-3xl font-bold text-gray-900">
          {{ data?.meta?.totalElements ?? '-' }}
        </p>
      </div>
      <div class="card">
        <p class="text-sm font-medium text-gray-500">활성 리소스</p>
        <p class="mt-2 text-3xl font-bold text-primary-600">-</p>
      </div>
      <div class="card">
        <p class="text-sm font-medium text-gray-500">역할</p>
        <p class="mt-2 text-3xl font-bold text-gray-900">{{ authStore.user?.role }}</p>
      </div>
    </div>

    <div class="card">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">최근 리소스</h2>
      <div v-if="isLoading" class="text-center py-4 text-gray-500">로딩 중...</div>
      <ul v-else class="divide-y divide-gray-200">
        <li
          v-for="resource in data?.data"
          :key="resource.id"
          class="py-3 flex items-center justify-between"
        >
          <div>
            <p class="font-medium text-gray-900">{{ resource.name }}</p>
            <p class="text-sm text-gray-500">{{ resource.description }}</p>
          </div>
          <span
            class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
            :class="resource.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'"
          >
            {{ resource.status }}
          </span>
        </li>
      </ul>
      <div class="mt-4">
        <router-link to="/resources" class="text-sm text-primary-600 hover:text-primary-700 font-medium">
          모두 보기 →
        </router-link>
      </div>
    </div>
  </div>
</template>
