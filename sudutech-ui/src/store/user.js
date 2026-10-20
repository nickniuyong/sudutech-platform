import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: {},
    roles: [],
    permissions: [],
    routers: []
  }),
  actions: {
    async doLogin(form) {
      const res = await login(form)
      this.token = res.data.token
      localStorage.setItem('token', res.data.token)
    },
    async fetchUserInfo() {
      const res = await getUserInfo()
      this.user = res.data.user
      this.roles = res.data.roles
      this.permissions = res.data.permissions
      this.routers = res.data.routers
    },
    async doLogout() {
      await logout().catch(() => {})
      this.token = ''
      this.user = {}
      this.permissions = []
      this.routers = []
      localStorage.removeItem('token')
    }
  }
})
