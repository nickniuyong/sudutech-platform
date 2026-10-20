<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" class="search-form">
      <el-form-item label="模块"><el-input v-model="queryParams.title" clearable style="width:130px" /></el-form-item>
      <el-form-item label="操作人"><el-input v-model="queryParams.operName" clearable style="width:130px" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button v-perm="'system:oper:remove'" type="danger" plain @click="handleClean">清空</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="编号" prop="operId" width="80" />
        <el-table-column label="模块" prop="title" width="100" />
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ ['', '新增', '修改', '删除', '授权', '导出', '导入', '强退', '清空', '生成'][row.businessType] || '其它' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="方法" prop="method" show-overflow-tooltip />
        <el-table-column label="操作人" prop="operName" width="100" />
        <el-table-column label="URL" prop="operUrl" show-overflow-tooltip width="200" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">{{ row.status === 0 ? '正常' : '异常' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时(ms)" prop="costTime" width="90" />
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ (row.operTime || '').replace('T', ' ') }}</template>
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
import { listOperLogs, cleanOperLogs } from '@/api/system/dict'

const list = ref([]); const total = ref(0); const loading = ref(false)
const queryParams = reactive({ pageNum: 1, pageSize: 10, title: '', operName: '' })

async function getList() {
  loading.value = true
  try { const res = await listOperLogs(queryParams); list.value = res.rows; total.value = res.total }
  finally { loading.value = false }
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function handleClean() {
  ElMessageBox.confirm('确认清空全部操作日志？', '警告', { type: 'warning' }).then(async () => {
    await cleanOperLogs(); ElMessage.success('已清空'); getList()
  })
}
onMounted(getList)
</script>
