<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useResourceList, useDeleteResource } from '@/composables/useResources'
import { usePagination } from '@/composables/usePagination'
import BasePagination from '@/components/ui/BasePagination.vue'
import ResourceForm from '@/components/features/resources/ResourceForm.vue'

const keyword = ref('')
const statusFilter = ref('')
const showCreateModal = ref(false)

const { page, size, resetPage } = usePagination(1, 20)

const params = computed(() => ({
  page: page.value - 1,
  size: size.value,
  keyword: keyword.value || undefined,
  status: statusFilter.value || undefined
}))

const { data, isLoading, isFetching } = useResourceList(params)
const { mutate: deleteResource, isPending: isDeleting } = useDeleteResource()

watch([keyword, statusFilter], () => resetPage())

function confirmDelete(id: number) {
  if (confirm('정말 삭제하시겠습니까?')) {
    deleteResource(id)
  }
}
</script>

<template>
  <div>
    <div class="mb-6 flex items-center justify-between">
      <h1 class="text-2xl font-bold text-gray-900">리소스 목록</h1>
      <button class="btn-primary" @click="showCreateModal = true">+ 리소스 추가</button>
    </div>

    <!-- 필터 -->
    <div class="card mb-6 flex gap-4">
      <input
        v-model="keyword"
        type="text"
        placeholder="이름 검색..."
        class="input-field max-w-xs"
      />
      <select v-model="statusFilter" class="input-field max-w-xs">
        <option value="">전체 상태</option>
        <option value="ACTIVE">활성</option>
        <option value="INACTIVE">비활성</option>
      </select>
    </div>

    <!-- 테이블 -->
    <div class="card overflow-hidden p-0">
      <div v-if="isLoading" class="p-8 text-center text-gray-500">로딩 중...</div>
      <table v-else class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">이름</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">설명</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">상태</th>
            <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">생성일</th>
            <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">액션</th>
          </tr>
        </thead>
        <tbody class="bg-white divide-y divide-gray-200">
          <tr v-for="resource in data?.data" :key="resource.id" class="hover:bg-gray-50">
            <td class="px-6 py-4">
              <router-link
                :to="`/resources/${resource.id}`"
                class="font-medium text-primary-600 hover:text-primary-700"
              >
                {{ resource.name }}
              </router-link>
            </td>
            <td class="px-6 py-4 text-sm text-gray-500 max-w-xs truncate">
              {{ resource.description ?? '-' }}
            </td>
            <td class="px-6 py-4">
              <span
                class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
                :class="resource.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'"
              >
                {{ resource.status }}
              </span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-500">
              {{ new Date(resource.createdAt).toLocaleDateString('ko-KR') }}
            </td>
            <td class="px-6 py-4 text-right">
              <button
                class="text-sm text-red-600 hover:text-red-700 font-medium"
                :disabled="isDeleting"
                @click="confirmDelete(resource.id)"
              >
                삭제
              </button>
            </td>
          </tr>
          <tr v-if="!data?.data?.length">
            <td colspan="5" class="px-6 py-8 text-center text-gray-500">리소스가 없습니다</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 페이지네이션 -->
    <BasePagination
      v-if="data?.meta"
      :current-page="page"
      :total-pages="data.meta.totalPages"
      :total-elements="data.meta.totalElements"
      class="mt-4"
      @change="page = $event"
    />

    <!-- 생성 모달 -->
    <ResourceForm
      v-if="showCreateModal"
      @close="showCreateModal = false"
      @created="showCreateModal = false"
    />
  </div>
</template>
