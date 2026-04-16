import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/pages/LoginPage.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/pages/RegisterPage.vue'),
      meta: { requiresGuest: true }
    },
    {
      path: '/oauth2/callback',
      name: 'oauth2-callback',
      component: () => import('@/pages/OAuth2CallbackPage.vue')
    },
    {
      path: '/',
      component: () => import('@/components/layouts/AppLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        { path: '', redirect: '/dashboard' },
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('@/pages/DashboardPage.vue')
        },
        {
          path: 'resources',
          name: 'resources',
          component: () => import('@/pages/ResourceListPage.vue')
        },
        {
          path: 'resources/:id',
          name: 'resource-detail',
          component: () => import('@/pages/ResourceDetailPage.vue')
        },
        {
          path: 'admin',
          meta: { requiresAdmin: true },
          children: [
            {
              path: 'notifications',
              name: 'admin-notifications',
              component: () => import('@/pages/admin/NotificationPage.vue')
            }
          ]
        }
      ]
    }
  ]
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    return { path: '/dashboard' }
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { path: '/dashboard' }
  }

  if (authStore.isAuthenticated && !authStore.user) {
    try {
      await authStore.fetchMe()
    } catch {
      authStore.clearAuth()
      return { name: 'login' }
    }
  }
})

export default router
