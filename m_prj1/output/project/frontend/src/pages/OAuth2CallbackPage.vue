<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  const token = route.query.token as string
  const error = route.query.error as string

  if (error) {
    router.push({ path: '/login', query: { error } })
    return
  }

  if (token) {
    authStore.accessToken = token
    localStorage.setItem('accessToken', token)
    await authStore.fetchMe()

    const redirect = route.query.redirect as string
    router.push(redirect || '/dashboard')
  } else {
    router.push('/login')
  }
})
</script>

<template>
  <div class="flex items-center justify-center h-screen bg-gray-50">
    <div class="text-center">
      <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-primary-600 mx-auto mb-4" />
      <p class="text-gray-600">로그인 처리 중...</p>
    </div>
  </div>
</template>
