<script setup lang="ts">
import { useAuthStore } from '@/stores/auth.store'
import LoginForm from '@/components/features/auth/LoginForm.vue'
import AuthLayout from '@/components/layouts/AuthLayout.vue'

const authStore = useAuthStore()
const googleLoginUrl = import.meta.env.VITE_OAUTH2_GOOGLE_URL
const kakaoLoginUrl = import.meta.env.VITE_OAUTH2_KAKAO_URL
</script>

<template>
  <AuthLayout>
    <div class="w-full max-w-md">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900">로그인</h1>
        <p class="mt-2 text-gray-600">서비스를 이용하려면 로그인하세요</p>
      </div>

      <div class="card">
        <LoginForm />

        <div class="mt-6">
          <div class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-300" />
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="bg-white px-2 text-gray-500">또는 소셜 로그인</span>
            </div>
          </div>

          <div class="mt-4 space-y-3">
            <a
              :href="googleLoginUrl"
              class="flex w-full items-center justify-center gap-3 rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
            >
              <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" class="h-5 w-5" alt="Google" />
              Google로 로그인
            </a>
            <a
              :href="kakaoLoginUrl"
              class="flex w-full items-center justify-center gap-3 rounded-lg bg-yellow-400 px-4 py-2.5 text-sm font-medium text-yellow-900 hover:bg-yellow-500 transition-colors"
            >
              카카오로 로그인
            </a>
          </div>
        </div>
      </div>
    </div>
  </AuthLayout>
</template>
