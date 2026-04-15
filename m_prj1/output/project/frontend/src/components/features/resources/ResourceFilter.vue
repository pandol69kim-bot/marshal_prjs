<script setup lang="ts">
defineProps<{
  keyword: string
  status: string
}>()

defineEmits<{
  'update:keyword': [value: string]
  'update:status': [value: string]
}>()
</script>

<template>
  <div class="flex flex-wrap gap-4">
    <div class="flex-1 min-w-48">
      <input
        :value="keyword"
        type="text"
        placeholder="이름 검색..."
        class="input-field"
        @input="$emit('update:keyword', ($event.target as HTMLInputElement).value)"
      />
    </div>
    <div>
      <select
        :value="status"
        class="input-field"
        @change="$emit('update:status', ($event.target as HTMLSelectElement).value)"
      >
        <option value="">전체 상태</option>
        <option value="ACTIVE">활성</option>
        <option value="INACTIVE">비활성</option>
      </select>
    </div>
  </div>
</template>
