<script setup lang="ts">
import LoginForm from '@/components/features/auth/LoginForm.vue'
import AuthLayout from '@/components/layouts/AuthLayout.vue'

const googleLoginUrl = import.meta.env.VITE_OAUTH2_GOOGLE_URL
const kakaoLoginUrl = import.meta.env.VITE_OAUTH2_KAKAO_URL
</script>

<template>
  <AuthLayout>
    <div class="mx-auto w-full max-w-sm">
      <!-- Header -->
      <div class="mb-8 text-center">
        <div class="mx-auto mb-4 flex h-10 w-10 items-center justify-center rounded-xl bg-primary-600">
          <svg class="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-zinc-900">로그인</h1>
        <p class="mt-1.5 text-sm text-zinc-500">서비스를 이용하려면 로그인하세요</p>
      </div>

      <!-- Card -->
      <div class="rounded-2xl border border-zinc-200 bg-white p-8">
        <LoginForm />

        <p class="mt-5 text-center text-sm text-zinc-500">
          계정이 없으신가요?
          <RouterLink to="/register" class="font-medium text-primary-600 hover:text-primary-700">
            회원가입
          </RouterLink>
        </p>

        <!-- Divider -->
        <div class="my-6 flex items-center gap-3">
          <div class="h-px flex-1 bg-zinc-100" />
          <span class="text-xs text-zinc-400">또는 소셜 로그인</span>
          <div class="h-px flex-1 bg-zinc-100" />
        </div>

        <!-- Social buttons -->
        <div class="space-y-2.5">
          <a
            :href="googleLoginUrl"
            class="flex w-full items-center justify-center gap-3 rounded-lg border border-zinc-200 bg-white
                   px-4 py-2.5 text-sm font-medium text-zinc-700 hover:bg-zinc-50 transition-colors"
          >
            <img
              src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
              class="h-4 w-4"
              alt="Google"
            />
            Google로 로그인
          </a>
          <a
            :href="kakaoLoginUrl"
            class="flex w-full items-center justify-center gap-3 rounded-lg bg-[#FEE500]
                   px-4 py-2.5 text-sm font-medium text-[#191919] hover:bg-[#F5DC00] transition-colors"
          >
            <svg class="h-4 w-4" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 3C6.477 3 2 6.477 2 11c0 2.96 1.614 5.564 4.065 7.2L5.1 21.6a.5.5 0 00.74.52l4.16-2.77A12.3 12.3 0 0012 19c5.523 0 10-3.477 10-8S17.523 3 12 3z" />
            </svg>
            카카오로 로그인
          </a>
        </div>
      </div>
    </div>
  </AuthLayout>
</template>
