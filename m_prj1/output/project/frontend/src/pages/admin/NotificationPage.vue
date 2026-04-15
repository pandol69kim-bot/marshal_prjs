<script setup lang="ts">
import { ref } from 'vue'
import { useQuery } from '@tanstack/vue-query'
import { notificationsApi } from '@/api/notifications.api'
import { useToast } from '@/composables/useToast'

const params = ref({ page: 0, size: 20 })
const { showSuccess, showError } = useToast()

const { data, isLoading } = useQuery({
  queryKey: ['notifications', params],
  queryFn: async () => {
    const { data } = await notificationsApi.getList(params.value)
    return data
  }
})

const sendForm = ref({ userId: '', channel: 'EMAIL' as 'EMAIL' | 'SMS', title: '', content: '' })

async function handleSend() {
  try {
    await notificationsApi.send(sendForm.value)
    showSuccess('알림이 전송되었습니다')
    sendForm.value = { userId: '', channel: 'EMAIL', title: '', content: '' }
  } catch {
    showError('알림 전송에 실패했습니다')
  }
}
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">알림 관리 (관리자)</h1>

    <div class="card mb-6">
      <h2 class="text-lg font-semibold mb-4">알림 발송</h2>
      <form class="space-y-4" @submit.prevent="handleSend">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">사용자 ID</label>
            <input v-model="sendForm.userId" type="text" class="input-field" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">채널</label>
            <select v-model="sendForm.channel" class="input-field">
              <option value="EMAIL">이메일</option>
              <option value="SMS">SMS</option>
            </select>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">제목</label>
          <input v-model="sendForm.title" type="text" class="input-field" required />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">내용</label>
          <textarea v-model="sendForm.content" class="input-field" rows="3" required />
        </div>
        <button type="submit" class="btn-primary">발송</button>
      </form>
    </div>

    <div class="card">
      <h2 class="text-lg font-semibold mb-4">발송 이력</h2>
      <div v-if="isLoading" class="text-center py-4 text-gray-500">로딩 중...</div>
      <table v-else class="min-w-full divide-y divide-gray-200">
        <thead>
          <tr>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">채널</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">제목</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">상태</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">발송시각</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-gray-200">
          <tr v-for="noti in data?.data" :key="noti.id">
            <td class="px-4 py-3 text-sm">{{ noti.channel }}</td>
            <td class="px-4 py-3 text-sm">{{ noti.title }}</td>
            <td class="px-4 py-3">
              <span
                class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium"
                :class="{
                  'bg-green-100 text-green-800': noti.status === 'SENT',
                  'bg-red-100 text-red-800': noti.status === 'FAILED',
                  'bg-yellow-100 text-yellow-800': noti.status === 'PENDING'
                }"
              >{{ noti.status }}</span>
            </td>
            <td class="px-4 py-3 text-sm text-gray-500">
              {{ noti.sentAt ? new Date(noti.sentAt).toLocaleString('ko-KR') : '-' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
