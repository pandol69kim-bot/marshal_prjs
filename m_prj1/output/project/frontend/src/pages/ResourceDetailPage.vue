<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useResource, useDeleteResource } from '@/composables/useResources'

const route = useRoute()
const router = useRouter()
const id = computed(() => Number(route.params.id))

const { data: resource, isLoading } = useResource(id)
const { mutate: deleteResource } = useDeleteResource()

function confirmDelete() {
  if (confirm('정말 삭제하시겠습니까?')) {
    deleteResource(id.value, {
      onSuccess: () => router.push('/resources')
    })
  }
}
</script>

<template>
  <div>
    <div class="mb-6 flex items-center gap-4">
      <router-link to="/resources" class="text-gray-500 hover:text-gray-700">← 목록으로</router-link>
      <h1 class="text-2xl font-bold text-gray-900">리소스 상세</h1>
    </div>

    <div v-if="isLoading" class="card text-center py-8 text-gray-500">로딩 중...</div>

    <div v-else-if="resource" class="card">
      <div class="space-y-4">
        <div>
          <label class="text-sm font-medium text-gray-500">이름</label>
          <p class="mt-1 text-gray-900">{{ resource.name }}</p>
        </div>
        <div>
          <label class="text-sm font-medium text-gray-500">설명</label>
          <p class="mt-1 text-gray-900">{{ resource.description ?? '-' }}</p>
        </div>
        <div>
          <label class="text-sm font-medium text-gray-500">상태</label>
          <div class="mt-1">
            <span
              class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
              :class="resource.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'"
            >
              {{ resource.status }}
            </span>
          </div>
        </div>
        <div>
          <label class="text-sm font-medium text-gray-500">생성일</label>
          <p class="mt-1 text-gray-900">
            {{ new Date(resource.createdAt).toLocaleString('ko-KR') }}
          </p>
        </div>
      </div>

      <div class="mt-6 flex gap-3">
        <button class="btn-secondary" @click="router.push('/resources')">목록으로</button>
        <button class="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition-colors" @click="confirmDelete">
          삭제
        </button>
      </div>
    </div>
  </div>
</template>
