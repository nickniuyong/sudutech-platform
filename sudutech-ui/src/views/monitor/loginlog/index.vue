<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" class="search-form">
      <el-form-item label="账号"><el-input v-model="queryParams.userName" clearable style="width:130px" /></el-form-item>
      <el-form-item label="IP"><el-input v-model="queryParams.ipaddr" clearable style="width:130px" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button v-perm="'system:loginlog:remove'" type="danger" plain @click="handleClean">清空</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="编号" prop="infoId" width="80" />
        <el-table-column label="账号" prop="userName" width="120" />
        <el-table-column label="IP" prop="ipaddr" width="140" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">{{ row.status === '0' ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="消息" prop="msg" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ (row.loginTime || '').replace('T', ' ') }}</template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" layout="total, prev, pager, next" class="mt12" @current-change="getList" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listLoginLogs, cleanLoginLogs } from '@/api/system/dict'

const list = ref([]); const total = ref(0); const loading = ref(false)
const queryParams = reactive({ pageNum: 1, pageSize: 10, userName: '', ipaddr: '' })

async function getList() {
  loading.value = true
  try { const res = await listLoginLogs(queryParams); list.value = res.rows; total.value = res.total }
  finally { loading.value = false }
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function handleClean() {
  ElMessageBox.confirm('确认清空全部登录日志？', '警告', { type: 'warning' }).then(async () => {
    await cleanLoginLogs(); ElMessage.success('已清空'); getList()
  })
}
onMounted(getList)
</script>
