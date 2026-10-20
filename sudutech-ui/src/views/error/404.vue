<template>
  <div class="err-page">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
  </div>
</template>
<style scoped>
.err-page { height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.err-page h1 { font-size: 80px; color: #409eff; }
.err-page p { color: #909399; margin-bottom: 20px; }
</style>
