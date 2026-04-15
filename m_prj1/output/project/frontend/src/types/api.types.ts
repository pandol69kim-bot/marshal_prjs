export interface ApiResponse<T> {
  success: boolean
  data: T | null
  error: ApiError | null
  meta: PageMeta | null
}

export interface PaginatedResponse<T> extends ApiResponse<T> {
  meta: PageMeta
}

export interface ApiError {
  type: string
  title: string
  status: number
  detail: string
  instance: string
}

export interface PageMeta {
  totalElements: number
  totalPages: number
  page: number
  size: number
}

export interface PageParams {
  page?: number
  size?: number
  sort?: string
}
