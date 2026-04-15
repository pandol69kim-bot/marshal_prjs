import { useNotificationStore } from '@/stores/notification.store'

export function useToast() {
  const store = useNotificationStore()

  return {
    showSuccess: (message: string) => store.addToast('success', message),
    showError: (message: string) => store.addToast('error', message),
    showWarning: (message: string) => store.addToast('warning', message),
    showInfo: (message: string) => store.addToast('info', message)
  }
}
