<script setup lang="ts">
</script>

<template>
  <div class="min-h-screen bg-zinc-50 flex items-center justify-center p-4">
    <!-- Subtle background pattern -->
    <div class="pointer-events-none fixed inset-0 overflow-hidden" aria-hidden="true">
      <div class="absolute -top-40 -right-40 h-[480px] w-[480px] rounded-full bg-primary-100/40 blur-3xl" />
      <div class="absolute -bottom-40 -left-40 h-[480px] w-[480px] rounded-full bg-primary-50/60 blur-3xl" />
    </div>
    <div class="relative w-full">
      <slot />
    </div>
  </div>
</template>
