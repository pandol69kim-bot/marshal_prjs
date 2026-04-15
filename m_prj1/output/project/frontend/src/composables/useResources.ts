import { useQuery, useMutation, useQueryClient } from '@tanstack/vue-query'
import { computed, type Ref } from 'vue'
import { resourcesApi } from '@/api/resources.api'
import { useToast } from '@/composables/useToast'
import type { PageParams } from '@/types/api.types'
import type { CreateResourceRequest, UpdateResourceRequest } from '@/types/resource.types'

export function useResourceList(
  params: Ref<PageParams & { status?: string; keyword?: string }>
) {
  return useQuery({
    queryKey: computed(() => ['resources', params.value]),
    queryFn: async () => {
      const { data } = await resourcesApi.getList(params.value)
      return data
    },
    placeholderData: (prev) => prev
  })
}

export function useResource(id: Ref<number | null>) {
  return useQuery({
    queryKey: computed(() => ['resources', id.value]),
    queryFn: async () => {
      const { data } = await resourcesApi.getOne(id.value!)
      return data.data
    },
    enabled: computed(() => !!id.value)
  })
}

export function useCreateResource() {
  const queryClient = useQueryClient()
  const { showSuccess, showError } = useToast()

  return useMutation({
    mutationFn: (data: CreateResourceRequest) => resourcesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['resources'] })
      showSuccess('리소스가 생성되었습니다')
    },
    onError: (error: any) => {
      const detail = error.response?.data?.error?.detail ?? '생성에 실패했습니다'
      showError(detail)
    }
  })
}

export function useUpdateResource() {
  const queryClient = useQueryClient()
  const { showSuccess, showError } = useToast()

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateResourceRequest }) =>
      resourcesApi.update(id, data),
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ['resources'] })
      queryClient.invalidateQueries({ queryKey: ['resources', id] })
      showSuccess('리소스가 수정되었습니다')
    },
    onError: () => showError('수정에 실패했습니다')
  })
}

export function useDeleteResource() {
  const queryClient = useQueryClient()
  const { showSuccess, showError } = useToast()

  return useMutation({
    mutationFn: (id: number) => resourcesApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['resources'] })
      showSuccess('리소스가 삭제되었습니다')
    },
    onError: () => showError('삭제에 실패했습니다')
  })
}
