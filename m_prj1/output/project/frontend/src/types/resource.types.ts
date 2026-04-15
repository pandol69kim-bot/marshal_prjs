export interface ResourceDto {
  id: number
  name: string
  description: string | null
  status: 'ACTIVE' | 'INACTIVE' | 'DELETED'
  ownerId: string
  ownerName: string
  createdAt: string
  updatedAt: string
}

export interface CreateResourceRequest {
  name: string
  description?: string
}

export interface UpdateResourceRequest {
  name: string
  description?: string
  status?: 'ACTIVE' | 'INACTIVE'
}
