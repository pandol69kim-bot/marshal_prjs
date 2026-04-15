import api from '@/lib/axios'
import type { ApiResponse } from '@/types/api.types'
import type { LoginRequest, LoginResponse, RegisterRequest, TokenResponse, UserDto } from '@/types/auth.types'

export const authApi = {
  register(data: RegisterRequest) {
    return api.post<ApiResponse<UserDto>>('/api/v1/auth/register', data)
  },

  login(data: LoginRequest) {
    return api.post<ApiResponse<LoginResponse>>('/api/v1/auth/login', data)
  },

  refresh() {
    return api.post<ApiResponse<TokenResponse>>('/api/v1/auth/refresh')
  },

  logout() {
    return api.post<ApiResponse<void>>('/api/v1/auth/logout')
  },

  getMe() {
    return api.get<ApiResponse<UserDto>>('/api/v1/users/me')
  }
}
