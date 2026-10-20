import { useUserStore } from '@/store/user'

/** v-perm 按钮权限指令 */
export const hasPerm = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const perm = binding.value
    if (perm && userStore.permissions) {
      const ok = userStore.permissions.includes('*:*:*') || userStore.permissions.includes(perm)
      if (!ok) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}
