<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-title">
        <img src="@/assets/logo.png" alt="数岛软件" class="logo-img" />
        <h2>sudutech 快速开发平台</h2>
        <p>数岛软件 出品 · 企业级中后台开发底座</p>
        <p class="sub">系统管理 / 工作流 / 大屏 / 报表 / AI问数 / 代码生成</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="账号" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">默认账号：admin / admin123</div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { useMenuStore } from '@/store/menu'

const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

onMounted(() => {
  if (userStore.token) {
    router.replace('/dashboard')
  }
})

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.doLogin(form)
    await menuStore.initRoutes()
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #1f3b73 0%, #0b1c33 60%, #0a1224 100%);
}
.login-box {
  width: 420px; padding: 40px 36px; background: #fff; border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, .35);
}
.login-title { text-align: center; margin-bottom: 24px; }
.logo-img {
  width: 56px; height: 56px; margin: 0 auto 10px; display: block;
  image-rendering: pixelated; border-radius: 12px;
}
.login-title h2 { font-size: 20px; color: #1f2d3d; }
.login-title p { font-size: 12px; color: #909399; margin-top: 6px; }
.login-title p.sub { font-size: 11px; color: #c0c4cc; }
.login-btn { width: 100%; }
.login-tip { text-align: center; color: #909399; font-size: 12px; }
</style>
