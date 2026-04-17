import api from '@/lib/axios'
import type { ApiResponse, PaginatedResponse } from '@/types/api.types'

export interface NotificationDto {
  id: number
  channel: 'EMAIL' | 'SMS' | 'SLACK'
  title: string
  content: string
  status: 'PENDING' | 'SENT' | 'FAILED'
  sentAt: string | null
  createdAt: string
}

export interface SendNotificationRequest {
  userId: string
  channel: 'EMAIL' | 'SMS'
  phone?: string   // SMS 채널 선택 시 필수
  title: string
  content: string
}

export interface NotificationListParams {
  sort?: string
}

export const notificationsApi = {
  getList(params?: NotificationListParams) {
    return api.get<PaginatedResponse<NotificationDto[]>>('/api/v1/notifications', { params })
  },

  send(data: SendNotificationRequest) {
    return api.post<ApiResponse<void>>('/api/v1/notifications/send', data)
  }
}
