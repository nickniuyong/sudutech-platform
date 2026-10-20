import { createRouter, createWebHistory } from 'vue-router'

const Layout = () => import('@/layout/index.vue')

export const constantRoutes = [
  { path: '/login', component: () => import('@/views/login/index.vue'), meta: { title: '登录' } },
  {
    path: '/screen/view/:publishUrl',
    component: () => import('@/views/screen/view/index.vue'),
    meta: { title: '大屏预览' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('@/views/dashboard/index.vue'), name: 'Dashboard', meta: { title: '首页', icon: 'Odometer' } }
    ]
  },
  {
    path: '/screen/designer/:projectId',
    component: () => import('@/views/screen/designer/index.vue'),
    meta: { title: '大屏设计' }
  },
  {
    path: '/report/designer/:pageId',
    component: () => import('@/views/report/designer/index.vue'),
    meta: { title: '报表设计' }
  },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/error/404.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes: constantRoutes
})

const whiteList = ['/login']
router.beforeEach(async (to) => {
  const token = localStorage.getItem('token')
  if (whiteList.includes(to.path)) {
    return true
  }
  if (!token) {
    return { path: '/login', query: to.fullPath === '/' ? {} : { redirect: to.fullPath } }
  }
  const { useUserStore } = await import('@/store/user')
  const { useMenuStore } = await import('@/store/menu')
  const userStore = useUserStore()
  const menuStore = useMenuStore()
  if (!userStore.user || !userStore.user.userId) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      return '/login'
    }
  }
  if (!menuStore.added) {
    await menuStore.initRoutes()
    return { path: to.fullPath, replace: true }
  }
  return true
})

export default router
