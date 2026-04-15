<script setup lang="ts">
import { useNotificationStore } from '@/stores/notification.store'

const store = useNotificationStore()
</script>

<template>
  <teleport to="body">
    <div class="fixed top-4 right-4 z-50 space-y-2 w-80">
      <transition-group name="toast">
        <div
          v-for="toast in store.toasts"
          :key="toast.id"
          class="flex items-start gap-3 rounded-lg p-4 shadow-lg text-sm"
          :class="{
            'bg-green-50 border border-green-200 text-green-800': toast.type === 'success',
            'bg-red-50 border border-red-200 text-red-800': toast.type === 'error',
            'bg-yellow-50 border border-yellow-200 text-yellow-800': toast.type === 'warning',
            'bg-blue-50 border border-blue-200 text-blue-800': toast.type === 'info'
          }"
        >
          <span class="flex-1">{{ toast.message }}</span>
          <button
            class="opacity-60 hover:opacity-100 transition-opacity"
            @click="store.removeToast(toast.id)"
          >
            ✕
          </button>
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<style scoped>
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}
.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}
</style>
