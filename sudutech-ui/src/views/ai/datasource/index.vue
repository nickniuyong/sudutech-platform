<template>
  <div class="app-container">
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'ai:datasource:add'" type="primary" @click="openForm()">新增数据源</el-button>
      </el-row>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="编号" prop="datasourceId" width="70" />
        <el-table-column label="名称" prop="datasourceName" width="160" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }"><el-tag size="small">{{ row.dbType }}</el-tag></template>
        </el-table-column>
        <el-table-column label="JDBC URL" prop="jdbcUrl" show-overflow-tooltip />
        <el-table-column label="默认库" prop="dbName" width="130" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">{{ row.status === '0' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'ai:datasource:query'" link type="primary" @click="handleTest(row)">测试</el-button>
            <el-button v-perm="'ai:datasource:refresh'" link type="success" @click="handleRefresh(row)">Schema采集</el-button>
            <el-button v-perm="'ai:datasource:query'" link type="warning" @click="openSchema(row)">表结构</el-button>
            <el-button v-perm="'ai:datasource:edit'" link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-perm="'ai:datasource:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="formVisible" :title="form.datasourceId ? '编辑数据源' : '新增数据源'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称" required><el-input v-model="form.datasourceName" /></el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.dbType" style="width:100%">
            <el-option label="MySQL" value="mysql" />
            <el-option label="PostgreSQL" value="pg" />
            <el-option label="ClickHouse" value="clickhouse" />
          </el-select>
        </el-form-item>
        <el-form-item label="JDBC URL" required>
          <el-input v-model="form.jdbcUrl" placeholder="jdbc:mysql://127.0.0.1:3306/db?useSSL=false" />
        </el-form-item>
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item :label="form.datasourceId ? '密码(留空不改)' : '密码'">
          <el-input v-model="form.plainPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="默认库"><el-input v-model="form.dbName" placeholder="NL2SQL 默认 schema" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="schemaVisible" title="已采集表结构" width="760px">
      <el-table :data="schemaRows" border size="small" max-height="420" v-loading="schemaLoading">
        <el-table-column label="表名" prop="tableName" width="200" />
        <el-table-column label="注释" prop="tableComment" width="180" />
        <el-table-column label="字段数">
          <template #default="{ row }">{{ (JSON.parse(row.columnsJson || '[]')).length }}</template>
        </el-table-column>
        <el-table-column label="采集时间" width="170">
          <template #default="{ row }">{{ (row.refreshTime || '').replace('T', ' ') }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDatasources, saveDatasource, delDatasources, testDatasource, refreshSchema, schemaList } from '@/api/platform'

const list = ref([]); const loading = ref(false); const formVisible = ref(false)
const form = reactive({})
const schemaVisible = ref(false); const schemaRows = ref([]); const schemaLoading = ref(false)

async function getList() {
  loading.value = true
  try { const res = await listDatasources(); list.value = res.data } finally { loading.value = false }
}
function openForm(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) { Object.assign(form, row); form.plainPassword = '' }
  else { Object.assign(form, { dbType: 'mysql', status: '0' }) }
  formVisible.value = true
}
async function submitForm() {
  if (!form.datasourceName || !form.jdbcUrl) { ElMessage.warning('名称与JDBC URL必填'); return }
  await saveDatasource(form)
  ElMessage.success('保存成功'); formVisible.value = false; getList()
}
async function handleTest(row) {
  await testDatasource({ ...row, plainPassword: '' })
  ElMessage.success('连接成功')
}
async function handleRefresh(row) {
  const res = await refreshSchema(row.datasourceId)
  ElMessage.success(res.msg || '采集完成')
}
async function openSchema(row) {
  schemaVisible.value = true; schemaLoading.value = true
  try { const res = await schemaList(row.datasourceId); schemaRows.value = res.data || [] }
  finally { schemaLoading.value = false }
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除数据源「${row.datasourceName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delDatasources(row.datasourceId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(getList)
</script>
