<script setup lang="ts">
import { ref } from 'vue'
import { useQuery, useQueryClient } from '@tanstack/vue-query'
import { notificationsApi } from '@/api/notifications.api'
import { useToast } from '@/composables/useToast'

const queryClient = useQueryClient()
const listParams = { sort: 'createdAt,desc' }
const { showSuccess, showError } = useToast()
const isSending = ref(false)

const { data, isLoading } = useQuery({
  queryKey: ['notifications', listParams.sort],
  queryFn: async () => {
    const { data } = await notificationsApi.getList(listParams)
    return data
  }
})

const sendForm = ref({
  userId: '',
  channel: 'EMAIL' as 'EMAIL' | 'SMS',
  phone: '',
  title: '',
  content: ''
})

async function handleSend() {
  isSending.value = true
  try {
    const payload = {
      userId: sendForm.value.userId,
      channel: sendForm.value.channel,
      title: sendForm.value.title,
      content: sendForm.value.content,
      ...(sendForm.value.channel === 'SMS' ? { phone: sendForm.value.phone } : {})
    }
    await notificationsApi.send(payload)
    showSuccess('알림이 전송되었습니다')
    sendForm.value = { userId: '', channel: 'EMAIL', phone: '', title: '', content: '' }
    // 비동기 발송 이력이 반영될 시간을 주고 목록 갱신
    setTimeout(() => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
    }, 1500)
  } catch {
    showError('알림 전송에 실패했습니다')
  } finally {
    isSending.value = false
  }
}

const STATUS_LABEL: Record<string, string> = {
  SENT: '발송됨',
  FAILED: '실패',
  PENDING: '대기중'
}
</script>

<template>
  <div>
    <!-- 헤더 -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-zinc-900">알림 관리</h1>
      <p class="mt-1 text-sm text-zinc-500">관리자 전용 — 이메일 / SMS 알림을 직접 발송합니다</p>
    </div>

    <!-- 발송 폼 -->
    <div class="card mb-6">
      <h2 class="text-base font-semibold text-zinc-900 mb-4">알림 발송</h2>
      <form class="space-y-4" @submit.prevent="handleSend">
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">사용자 <span class="text-zinc-400 font-normal">(이메일 또는 UUID)</span></label>
            <input
              v-model="sendForm.userId"
              type="text"
              class="input-field"
              placeholder="user@example.com 또는 UUID"
              required
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-zinc-700 mb-1">채널</label>
            <select v-model="sendForm.channel" class="input-field">
              <option value="EMAIL">이메일</option>
              <option value="SMS">SMS</option>
            </select>
          </div>
        </div>

        <!-- SMS 전용 phone 필드 -->
        <div v-if="sendForm.channel === 'SMS'">
          <label class="block text-sm font-medium text-zinc-700 mb-1">
            전화번호 <span class="text-red-500">*</span>
          </label>
          <input
            v-model="sendForm.phone"
            type="tel"
            class="input-field max-w-xs"
            placeholder="01012345678"
            :required="sendForm.channel === 'SMS'"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-zinc-700 mb-1">제목</label>
          <input
            v-model="sendForm.title"
            type="text"
            class="input-field"
            placeholder="알림 제목"
            required
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-zinc-700 mb-1">내용</label>
          <textarea
            v-model="sendForm.content"
            class="input-field resize-none"
            rows="3"
            placeholder="알림 내용을 입력하세요"
            required
          />
        </div>

        <div class="flex items-center gap-3">
          <button type="submit" class="btn-primary" :disabled="isSending">
            <span v-if="isSending" class="mr-2 inline-block h-3.5 w-3.5 animate-spin rounded-full border-2 border-white border-t-transparent" />
            {{ isSending ? '발송 중...' : '발송' }}
          </button>
          <p class="text-xs text-zinc-400">알림은 비동기로 발송되며 결과는 이력에 기록됩니다</p>
        </div>
      </form>
    </div>

    <!-- 발송 이력 -->
    <div class="card overflow-hidden p-0">
      <div class="flex items-center justify-between px-6 py-4 border-b border-zinc-100">
        <h2 class="text-base font-semibold text-zinc-900">발송 이력</h2>
        <button
          class="text-xs text-zinc-400 hover:text-zinc-600 transition-colors"
          @click="queryClient.invalidateQueries({ queryKey: ['notifications'] })"
        >
          새로고침
        </button>
      </div>

      <div v-if="isLoading" class="flex items-center justify-center py-12 text-sm text-zinc-400">
        로딩 중...
      </div>

      <template v-else>
        <table v-if="data?.data?.length" class="min-w-full divide-y divide-zinc-100">
          <thead>
            <tr class="bg-zinc-50">
              <th class="px-5 py-3 text-left text-xs font-medium text-zinc-500 uppercase tracking-wide">채널</th>
              <th class="px-5 py-3 text-left text-xs font-medium text-zinc-500 uppercase tracking-wide">제목</th>
              <th class="px-5 py-3 text-left text-xs font-medium text-zinc-500 uppercase tracking-wide">상태</th>
              <th class="px-5 py-3 text-left text-xs font-medium text-zinc-500 uppercase tracking-wide">발송 시각</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-zinc-100 bg-white">
            <tr v-for="noti in data.data" :key="noti.id" class="hover:bg-zinc-50 transition-colors">
              <td class="px-5 py-3.5">
                <span
                  class="inline-flex items-center rounded-md px-2 py-0.5 text-xs font-medium ring-1 ring-inset"
                  :class="noti.channel === 'EMAIL'
                    ? 'bg-blue-50 text-blue-700 ring-blue-200'
                    : 'bg-purple-50 text-purple-700 ring-purple-200'"
                >
                  {{ noti.channel }}
                </span>
              </td>
              <td class="px-5 py-3.5 text-sm text-zinc-900 max-w-xs truncate">{{ noti.title }}</td>
              <td class="px-5 py-3.5">
                <span
                  class="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
                  :class="{
                    'bg-emerald-50 text-emerald-700': noti.status === 'SENT',
                    'bg-red-50 text-red-700': noti.status === 'FAILED',
                    'bg-amber-50 text-amber-700': noti.status === 'PENDING'
                  }"
                >
                  {{ STATUS_LABEL[noti.status] ?? noti.status }}
                </span>
              </td>
              <td class="px-5 py-3.5 text-sm text-zinc-400">
                {{ noti.sentAt ? new Date(noti.sentAt).toLocaleString('ko-KR') : '—' }}
              </td>
            </tr>
          </tbody>
        </table>

        <!-- 빈 상태 -->
        <div v-else class="flex flex-col items-center justify-center py-14 text-center">
          <svg class="mb-3 h-10 w-10 text-zinc-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
              d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
          </svg>
          <p class="text-sm font-medium text-zinc-500">발송 이력이 없습니다</p>
          <p class="mt-1 text-xs text-zinc-400">알림을 발송하면 여기에 기록됩니다</p>
        </div>
      </template>
    </div>
  </div>
</template>
