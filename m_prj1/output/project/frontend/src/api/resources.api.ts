import api from '@/lib/axios'
import type { ApiResponse, PageParams, PaginatedResponse } from '@/types/api.types'
import type { ResourceDto, CreateResourceRequest, UpdateResourceRequest } from '@/types/resource.types'

export const resourcesApi = {
  getList(params: PageParams & { status?: string; keyword?: string }) {
    return api.get<PaginatedResponse<ResourceDto[]>>('/api/v1/resources', { params })
  },

  getOne(id: number) {
    return api.get<ApiResponse<ResourceDto>>(`/api/v1/resources/${id}`)
  },

  create(data: CreateResourceRequest) {
    return api.post<ApiResponse<ResourceDto>>('/api/v1/resources', data)
  },

  update(id: number, data: UpdateResourceRequest) {
    return api.put<ApiResponse<ResourceDto>>(`/api/v1/resources/${id}`, data)
  },

  delete(id: number) {
    return api.delete<ApiResponse<void>>(`/api/v1/resources/${id}`)
  }
}
