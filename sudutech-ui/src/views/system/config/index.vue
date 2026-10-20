<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" class="search-form">
      <el-form-item label="参数名"><el-input v-model="queryParams.configName" clearable style="width:150px" /></el-form-item>
      <el-form-item label="键名"><el-input v-model="queryParams.configKey" clearable style="width:150px" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'system:config:add'" type="primary" @click="openForm()">新增参数</el-button>
      </el-row>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="编号" prop="configId" width="70" />
        <el-table-column label="参数名称" prop="configName" />
        <el-table-column label="键名" prop="configKey" />
        <el-table-column label="键值" prop="configValue" />
        <el-table-column label="内置" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.configType === 'Y' ? 'danger' : 'info'" size="small">{{ row.configType === 'Y' ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-perm="'system:config:edit'" link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-perm="'system:config:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" layout="total, prev, pager, next" class="mt12" @current-change="getList" />
    </div>

    <el-dialog v-model="formVisible" :title="form.configId ? '编辑参数' : '新增参数'" width="500px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="参数名" required><el-input v-model="form.configName" /></el-form-item>
        <el-form-item label="键名" required><el-input v-model="form.configKey" placeholder="如 sys.demo" /></el-form-item>
        <el-form-item label="键值" required><el-input v-model="form.configValue" /></el-form-item>
        <el-form-item label="内置">
          <el-radio-group v-model="form.configType">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listConfigs, getConfig, addConfig, updateConfig, delConfigs } from '@/api/system/dict'

const list = ref([]); const total = ref(0); const loading = ref(false)
const queryParams = reactive({ pageNum: 1, pageSize: 10, configName: '', configKey: '' })
const formVisible = ref(false); const form = reactive({})

async function getList() {
  loading.value = true
  try { const res = await listConfigs(queryParams); list.value = res.rows; total.value = res.total }
  finally { loading.value = false }
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { Object.assign(queryParams, { pageNum: 1, configName: '', configKey: '' }); getList() }
function openForm(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) { getConfig(row.configId).then((res) => { Object.assign(form, res.data); formVisible.value = true }) }
  else { Object.assign(form, { configType: 'N' }); formVisible.value = true }
}
async function submitForm() {
  if (!form.configName || !form.configKey || !form.configValue) { ElMessage.warning('请填写完整'); return }
  if (form.configId) { await updateConfig(form) } else { await addConfig(form) }
  ElMessage.success('保存成功'); formVisible.value = false; getList()
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除参数「${row.configName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delConfigs(row.configId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(getList)
</script>
