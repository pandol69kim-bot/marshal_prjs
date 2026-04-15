import { ref } from 'vue'
import { defineStore } from 'pinia'

export interface Toast {
  id: string
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
}

export const useNotificationStore = defineStore('notification', () => {
  const toasts = ref<Toast[]>([])

  function addToast(type: Toast['type'], message: string, duration = 3000) {
    const id = crypto.randomUUID()
    toasts.value = [...toasts.value, { id, type, message }]

    setTimeout(() => {
      removeToast(id)
    }, duration)
  }

  function removeToast(id: string) {
    toasts.value = toasts.value.filter((t) => t.id !== id)
  }

  return { toasts, addToast, removeToast }
})
