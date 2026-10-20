<template>
  <div class="app-container">
    <el-row :gutter="16" class="mb8">
      <el-col :span="24">
        <el-card shadow="never">
          <div class="welcome">
            <h2>{{ greeting }}，{{ userStore.user.nickName || '管理员' }}！</h2>
            <p>欢迎使用数岛软件 sudutech 快速开发平台 —— 系统管理 / 代码生成 / 工作流 / 大屏可视化 / 报表 / AI问数</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in statCards" :key="card.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat">
            <el-icon :size="34" :color="card.color"><component :is="card.icon" /></el-icon>
            <div>
              <div class="stat-num">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" class="mt12">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>平台能力</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统管理">用户 / 角色 / 菜单 / 部门 / 岗位 / 字典 / 参数 / 公告 / 日志 / 在线用户</el-descriptions-item>
            <el-descriptions-item label="系统工具">代码生成（Velocity 模板，一键生成前后端代码）· 定时任务（Quartz）· 数据源管理</el-descriptions-item>
            <el-descriptions-item label="工作流">流程定义部署（BPMN 2.0）· 动态表单 · 节点审批配置 · 待办/已办/轨迹</el-descriptions-item>
            <el-descriptions-item label="数据大屏">拖拽设计器 · ECharts 组件库 · SQL 数据集 · 发布分享 · 自动刷新</el-descriptions-item>
            <el-descriptions-item label="报表中心">报表设计器 · 数据集管理 · BAR/LINE/PIE/TABLE/FUNNEL 图表</el-descriptions-item>
            <el-descriptions-item label="AI 问数">火山方舟豆包大模型 · 自然语言生成 SQL · 图表自动推荐</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>最新公告</template>
          <el-empty v-if="!notices.length" description="暂无公告" />
          <div v-for="n in notices" :key="n.noticeId" class="notice-item">
            <el-tag size="small" :type="n.noticeType === '1' ? 'warning' : 'success'">
              {{ n.noticeType === '1' ? '通知' : '公告' }}
            </el-tag>
            <span class="notice-title">{{ n.noticeTitle }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

const userStore = useUserStore()
const greeting = computed(() => {
  const h = new Date().getHours()
  return h < 9 ? '早上好' : h < 12 ? '上午好' : h < 14 ? '中午好' : h < 18 ? '下午好' : '晚上好'
})

const statCards = ref([
  { title: '技术栈', value: 'Java 17 + Vue3', icon: 'Cpu', color: '#409eff' },
  { title: 'ORM / 安全', value: 'MyBatis-Flex + Sa-Token', icon: 'Lock', color: '#67c23a' },
  { title: '可视化', value: 'ECharts 5', icon: 'DataLine', color: '#e6a23c' },
  { title: 'AI 引擎', value: '火山方舟豆包', icon: 'MagicStick', color: '#f56c6c' }
])
const notices = ref([])

onMounted(async () => {
  try {
    const res = await request.get('/api/v1/system/notice/list', { params: { pageNum: 1, pageSize: 5 } })
    notices.value = res.rows || []
  } catch (e) { /* 忽略 */ }
})
</script>

<style scoped>
.welcome h2 { margin-bottom: 6px; }
.welcome p { color: #909399; font-size: 13px; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; }
.stat { display: flex; align-items: center; gap: 14px; }
.stat-num { font-size: 15px; font-weight: 600; }
.stat-title { font-size: 12px; color: #909399; }
.notice-item { display: flex; align-items: center; gap: 8px; padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.notice-title { font-size: 13px; }
</style>
