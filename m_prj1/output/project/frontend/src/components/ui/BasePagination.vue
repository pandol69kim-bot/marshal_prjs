<script setup lang="ts">
const props = defineProps<{
  currentPage: number
  totalPages: number
  totalElements: number
}>()

const emit = defineEmits<{
  change: [page: number]
}>()

function pages() {
  const result: (number | '...')[] = []
  const { totalPages, currentPage } = props

  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, i) => i + 1)
  }

  result.push(1)
  if (currentPage > 3) result.push('...')

  const start = Math.max(2, currentPage - 1)
  const end = Math.min(totalPages - 1, currentPage + 1)
  for (let i = start; i <= end; i++) result.push(i)

  if (currentPage < totalPages - 2) result.push('...')
  result.push(totalPages)

  return result
}
</script>

<template>
  <div class="flex items-center justify-between">
    <p class="text-sm text-gray-700">
      총 <span class="font-medium">{{ totalElements }}</span>개
    </p>
    <div class="flex gap-1">
      <button
        class="px-3 py-1.5 text-sm rounded-lg border border-gray-300 hover:bg-gray-50 disabled:opacity-50"
        :disabled="currentPage === 1"
        @click="emit('change', currentPage - 1)"
      >
        이전
      </button>
      <template v-for="p in pages()" :key="p">
        <button
          v-if="p !== '...'"
          class="px-3 py-1.5 text-sm rounded-lg border"
          :class="p === currentPage
            ? 'bg-primary-600 text-white border-primary-600'
            : 'border-gray-300 hover:bg-gray-50'"
          @click="emit('change', p as number)"
        >
          {{ p }}
        </button>
        <span v-else class="px-3 py-1.5 text-sm text-gray-500">...</span>
      </template>
      <button
        class="px-3 py-1.5 text-sm rounded-lg border border-gray-300 hover:bg-gray-50 disabled:opacity-50"
        :disabled="currentPage === totalPages"
        @click="emit('change', currentPage + 1)"
      >
        다음
      </button>
    </div>
  </div>
</template>
