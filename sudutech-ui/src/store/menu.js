import { useUserStore } from '@/store/user'
import router, { constantRoutes } from '@/router'
import { defineStore } from 'pinia'

/** 将后端 routers 注入为路由 */
export function buildRoutes(routers) {
  const routes = []
  for (const r of routers || []) {
    if (!r.component) continue
    const route = {
      path: '/' + r.path,
      name: r.name,
      meta: r.meta,
      component: r.component === 'Layout' ? undefined : loadView(r.component),
      children: []
    }
    if (r.component === 'Layout') {
      route.component = () => import('@/layout/index.vue')
      route.children = (r.children || []).filter((c) => c.component && c.component !== 'Layout').map((c) => ({
        path: c.path,
        name: c.name,
        meta: c.meta,
        component: loadView(c.component)
      }))
    } else {
      route.meta = r.meta
    }
    routes.push(route)
  }
  return routes
}

const viewModules = import.meta.glob('@/views/**/*.vue')

function loadView(component) {
  const key = '/src/views/' + component + '.vue'
  return viewModules[key]
}

export const useMenuStore = defineStore('menu', {
  state: () => ({
    routers: [],
    added: false
  }),
  actions: {
    async initRoutes() {
      const userStore = useUserStore()
      if (!userStore.routers || userStore.routers.length === 0) {
        await userStore.fetchUserInfo()
      }
      this.routers = userStore.routers
      const dynamic = buildRoutes(this.routers)
      dynamic.forEach((r) => router.addRoute(r))
      this.added = true
      return dynamic
    }
  }
})
