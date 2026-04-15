<script setup lang="ts">
defineProps<{
  variant?: 'primary' | 'secondary' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  disabled?: boolean
  type?: 'button' | 'submit' | 'reset'
}>()
</script>

<template>
  <button
    :type="type ?? 'button'"
    :disabled="disabled || loading"
    class="inline-flex items-center justify-center rounded-lg font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed"
    :class="{
      'px-4 py-2 text-sm': size !== 'sm' && size !== 'lg',
      'px-3 py-1.5 text-xs': size === 'sm',
      'px-6 py-3 text-base': size === 'lg',
      'bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500': variant !== 'secondary' && variant !== 'danger',
      'bg-white text-gray-700 border border-gray-300 hover:bg-gray-50 focus:ring-primary-500': variant === 'secondary',
      'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500': variant === 'danger'
    }"
  >
    <span v-if="loading" class="mr-2 h-4 w-4 animate-spin rounded-full border-2 border-white border-t-transparent" />
    <slot />
  </button>
</template>
