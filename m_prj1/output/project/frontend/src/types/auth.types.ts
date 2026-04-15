export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  name: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: UserDto
}

export interface TokenResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
}

export interface UserDto {
  id: string
  email: string
  name: string
  role: 'USER' | 'ADMIN'
  avatarUrl?: string
}
