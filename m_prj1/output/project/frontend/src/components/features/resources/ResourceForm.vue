<script setup lang="ts">
import { ref } from 'vue'
import { useCreateResource } from '@/composables/useResources'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'

const emit = defineEmits<{
  close: []
  created: []
}>()

const { mutate: createResource, isPending } = useCreateResource()

const form = ref({ name: '', description: '' })

function handleSubmit() {
  if (!form.value.name.trim()) return
  createResource(
    { name: form.value.name, description: form.value.description || undefined },
    { onSuccess: () => emit('created') }
  )
}
</script>

<template>
  <div class="fixed inset-0 bg-black/50 flex items-center justify-center z-40 p-4">
    <div class="bg-white rounded-xl shadow-xl w-full max-w-md">
      <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-900">리소스 추가</h2>
        <button class="text-gray-400 hover:text-gray-600" @click="emit('close')">✕</button>
      </div>
      <form class="p-6 space-y-4" @submit.prevent="handleSubmit">
        <BaseInput
          v-model="form.name"
          label="이름"
          placeholder="리소스 이름"
          required
        />
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">설명 (선택)</label>
          <textarea
            v-model="form.description"
            class="input-field"
            rows="3"
            placeholder="리소스 설명"
          />
        </div>
        <div class="flex gap-3 justify-end">
          <BaseButton variant="secondary" @click="emit('close')">취소</BaseButton>
          <BaseButton type="submit" :loading="isPending">추가</BaseButton>
        </div>
      </form>
    </div>
  </div>
</template>
