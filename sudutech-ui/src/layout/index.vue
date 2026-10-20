<template>
  <el-container class="layout">
    <el-aside :width="isCollapse ? '64px' : '210px'" class="aside">
      <div class="logo">
        <img src="@/assets/logo.png" alt="logo" class="logo-img" />
        <span v-show="!isCollapse" class="logo-text">sudutech 平台</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="route.path"
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="#001529"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <!-- 首页 -->
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <template v-for="top in menuStore.routers" :key="top.path">
            <el-sub-menu :index="'/' + top.path" v-if="top.children && top.children.length">
              <template #title>
                <el-icon><component :is="iconMap(top.meta && top.meta.icon)" /></el-icon>
                <span>{{ top.meta && top.meta.title }}</span>
              </template>
              <el-menu-item
                v-for="child in top.children"
                :key="child.path"
                :index="'/' + top.path + '/' + child.path"
              >
                {{ child.meta && child.meta.title }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta && route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="28" style="background:#409eff">{{ (userStore.user.nickName || 'U').charAt(0) }}</el-avatar>
              <span class="user-name">{{ userStore.user.nickName || userStore.user.userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <component :is="Component" :key="route.path" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useMenuStore } from '@/store/menu'
import * as Icons from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()
const isCollapse = ref(false)

function iconMap(name) {
  return name && Icons[name] ? Icons[name] : Icons.Menu
}

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    await userStore.doLogout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #001529; transition: width 0.2s; display: flex; flex-direction: column; }
.logo { height: 56px; display: flex; align-items: center; justify-content: center; gap: 8px; color: #fff; }
.logo-img { width: 28px; height: 28px; image-rendering: pixelated; }
.logo-text { font-size: 15px; font-weight: 600; white-space: nowrap; }
.aside :deep(.el-menu) { border-right: none; }
.header { height: 56px; background: #fff; display: flex; align-items: center; justify-content: space-between; box-shadow: 0 1px 4px rgba(0,21,41,.08); z-index: 5; }
.header-left { display: flex; align-items: center; gap: 12px; }
.collapse-btn { font-size: 20px; cursor: pointer; }
.header-right { display: flex; align-items: center; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; outline: none; }
.user-name { font-size: 14px; color: #333; }
.main { padding: 0; background: #f5f7fa; overflow: auto; }
</style>
