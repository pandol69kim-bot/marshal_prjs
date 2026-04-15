import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth.api'
import type { UserDto, LoginRequest } from '@/types/auth.types'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
  const user = ref<UserDto | null>(null)

  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  async function login(credentials: LoginRequest) {
    const { data } = await authApi.login(credentials)
    if (!data.data) throw new Error('로그인에 실패했습니다')

    accessToken.value = data.data.accessToken
    user.value = data.data.user
    localStorage.setItem('accessToken', data.data.accessToken)
  }

  async function refreshToken(): Promise<string> {
    const { data } = await authApi.refresh()
    if (!data.data) throw new Error('토큰 갱신 실패')

    accessToken.value = data.data.accessToken
    localStorage.setItem('accessToken', data.data.accessToken)
    return data.data.accessToken
  }

  async function fetchMe() {
    const { data } = await authApi.getMe()
    if (data.data) user.value = data.data
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      clearAuth()
    }
  }

  function clearAuth() {
    accessToken.value = null
    user.value = null
    localStorage.removeItem('accessToken')
  }

  return {
    accessToken,
    user,
    isAuthenticated,
    isAdmin,
    login,
    refreshToken,
    fetchMe,
    logout,
    clearAuth
  }
})
