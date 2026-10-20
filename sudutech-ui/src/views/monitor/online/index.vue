<template>
  <div class="app-container">
    <div class="table-card">
      <el-row class="mb8">
        <el-input v-model="userName" placeholder="搜索账号" clearable style="width:200px" @keyup.enter="getList">
          <template #append><el-button @click="getList">搜索</el-button></template>
        </el-input>
      </el-row>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="会话编号" prop="tokenId" width="280" show-overflow-tooltip />
        <el-table-column label="账号" prop="userName" width="120" />
        <el-table-column label="昵称" prop="nickName" width="130" />
        <el-table-column label="部门" prop="deptName" />
        <el-table-column label="剩余有效期(秒)" prop="remainSeconds" width="140" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-perm="'monitor:online:list'" link type="danger" @click="handleForce(row)">强退</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOnline, forceLogout } from '@/api/system/dict'

const list = ref([]); const loading = ref(false); const userName = ref('')

async function getList() {
  loading.value = true
  try {
    const res = await listOnline({ userName: userName.value })
    list.value = res.data || []
  } finally { loading.value = false }
}
function handleForce(row) {
  ElMessageBox.confirm(`确认强制下线「${row.userName}」？`, '提示', { type: 'warning' }).then(async () => {
    await forceLogout(row.tokenId); ElMessage.success('已强退'); getList()
  })
}
onMounted(getList)
</script>
