import { ref, computed } from 'vue'

export function usePagination(initialPage = 1, initialSize = 20) {
  const page = ref(initialPage)
  const size = ref(initialSize)

  function goToPage(newPage: number) {
    page.value = newPage
  }

  function nextPage() {
    page.value++
  }

  function prevPage() {
    if (page.value > 1) page.value--
  }

  function resetPage() {
    page.value = 1
  }

  const params = computed(() => ({
    page: page.value - 1, // 백엔드 0-based
    size: size.value
  }))

  return { page, size, params, goToPage, nextPage, prevPage, resetPage }
}
