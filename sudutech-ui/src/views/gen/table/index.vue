<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="生成配置" name="config">
        <el-form :inline="true" class="search-form">
          <el-form-item label="表名"><el-input v-model="query.tableName" clearable style="width:180px" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="getList">搜索</el-button>
            <el-button v-perm="'gen:table:import'" type="success" @click="importVisible = true">导入表</el-button>
          </el-form-item>
        </el-form>
        <div class="table-card">
          <el-table :data="list" border stripe v-loading="loading">
            <el-table-column label="表名" prop="tableName" width="200" />
            <el-table-column label="描述" prop="tableComment" />
            <el-table-column label="实体类" prop="className" width="150" />
            <el-table-column label="模块" prop="moduleName" width="100" />
            <el-table-column label="创建时间" width="170">
              <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ') }}</template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <el-button v-perm="'gen:table:preview'" link type="primary" @click="openPreview(row)">预览</el-button>
                <el-button v-perm="'gen:table:edit'" link type="success" @click="openEdit(row)">配置</el-button>
                <el-button v-perm="'gen:table:genCode'" link type="warning" @click="handleGen(row)">生成下载</el-button>
                <el-button v-perm="'gen:table:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 导入表 -->
    <el-dialog v-model="importVisible" title="导入数据库表" width="620px">
      <el-input v-model="dbFilter" placeholder="过滤表名" clearable class="mb8" />
      <el-table ref="dbTableRef" :data="filteredDbTables" border size="small" max-height="380" @selection-change="(rows) => (selectedTables = rows)">
        <el-table-column type="selection" width="45" />
        <el-table-column label="表名" prop="TABLE_NAME" />
        <el-table-column label="描述" prop="TABLE_COMMENT" />
      </el-table>
      <template #footer>
        <el-button @click="importVisible = false">取消</el-button>
        <el-button type="primary" :loading="importing" @click="handleImport">导入</el-button>
      </template>
    </el-dialog>

    <!-- 生成配置 -->
    <el-dialog v-model="editVisible" title="生成配置" width="900px" top="6vh">
      <el-form :model="editForm" label-width="100px">
        <el-row :gutter="12">
          <el-col :span="8"><el-form-item label="表名"><el-input v-model="editForm.tableName" disabled /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="实体类名"><el-input v-model="editForm.className" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="功能名"><el-input v-model="editForm.functionName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="模块名"><el-input v-model="editForm.moduleName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="业务名"><el-input v-model="editForm.businessName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="包路径"><el-input v-model="editForm.packageName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="作者"><el-input v-model="editForm.authorName" /></el-form-item></el-col>
        </el-row>
        <el-divider content-position="left">字段配置</el-divider>
        <el-table :data="editForm.columns" border size="small" max-height="360">
          <el-table-column label="字段" prop="columnName" width="130" />
          <el-table-column label="注释" prop="columnComment" width="130" />
          <el-table-column label="Java类型" width="110">
            <template #default="{ row }"><el-input v-model="row.javaType" size="small" /></template>
          </el-table-column>
          <el-table-column label="Java属性" width="120">
            <template #default="{ row }"><el-input v-model="row.javaField" size="small" /></template>
          </el-table-column>
          <el-table-column label="列表" width="50" align="center">
            <template #default="{ row }"><el-checkbox v-model="row.isListBool" /></template>
          </el-table-column>
          <el-table-column label="编辑" width="50" align="center">
            <template #default="{ row }"><el-checkbox v-model="row.isEditBool" /></template>
          </el-table-column>
          <el-table-column label="查询" width="50" align="center">
            <template #default="{ row }"><el-checkbox v-model="row.isQueryBool" /></template>
          </el-table-column>
          <el-table-column label="查询方式" width="100">
            <template #default="{ row }">
              <el-select v-model="row.queryType" size="small">
                <el-option v-for="q in ['EQ', 'LIKE', 'GT', 'LT', 'NE', 'BETWEEN']" :key="q" :label="q" :value="q" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="控件" width="110">
            <template #default="{ row }">
              <el-select v-model="row.htmlType" size="small">
                <el-option v-for="h in ['input', 'textarea', 'select', 'radio', 'checkbox', 'datetime', '']" :key="h" :label="h || '无'" :value="h" />
              </el-select>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存配置</el-button>
      </template>
    </el-dialog>

    <!-- 代码预览 -->
    <el-dialog v-model="previewVisible" title="生成代码预览" width="860px" top="5vh">
      <el-tabs v-model="previewTab">
        <el-tab-pane v-for="(content, file) in previewData" :key="file" :label="fileName(file)" :name="file">
          <pre class="code-block">{{ content }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dbTables, importTables, listGenTables, getGenTable, updateGenTable, delGenTables, previewGen, batchGenCode } from '@/api/gen'

const activeTab = ref('config')
const list = ref([]); const loading = ref(false); const query = reactive({ tableName: '' })
const importVisible = ref(false); const dbTableList = ref([]); const dbFilter = ref('')
const selectedTables = ref([]); const importing = ref(false)
const editVisible = ref(false); const editForm = reactive({ columns: [] })
const previewVisible = ref(false); const previewData = ref({}); const previewTab = ref('')

const filteredDbTables = computed(() =>
  dbTableList.value.filter((t) => !dbFilter.value || (t.TABLE_NAME || t.table_name || '').includes(dbFilter.value)))

function fileName(f) {
  const parts = f.split('/')
  return parts[parts.length - 1]
}

async function getList() {
  loading.value = true
  try { const res = await listGenTables({ ...query, pageNum: 1, pageSize: 100 }); list.value = res.data }
  finally { loading.value = false }
}
async function loadDb() {
  const res = await dbTables()
  dbTableList.value = res.data || []
}
async function handleImport() {
  if (!selectedTables.value.length) { ElMessage.warning('请选择要导入的表'); return }
  importing.value = true
  try {
    const names = selectedTables.value.map((t) => t.TABLE_NAME || t.table_name)
    const res = await importTables(names)
    ElMessage.success(res.msg || '导入成功')
    importVisible.value = false
    getList()
  } finally { importing.value = false }
}
async function openEdit(row) {
  const res = await getGenTable(row.tableId)
  Object.assign(editForm, res.data)
  editForm.columns = (res.data.columns || []).map((c) => ({
    ...c,
    isListBool: c.isList === '1', isEditBool: c.isEdit === '1', isQueryBool: c.isQuery === '1'
  }))
  editVisible.value = true
}
async function submitEdit() {
  const payload = { ...editForm, columns: editForm.columns.map((c) => ({
    ...c, isList: c.isListBool ? '1' : '0', isEdit: c.isEditBool ? '1' : '0', isQuery: c.isQueryBool ? '1' : '0'
  })) }
  await updateGenTable(payload)
  ElMessage.success('配置已保存')
  editVisible.value = false
}
async function openPreview(row) {
  const res = await previewGen(row.tableId)
  previewData.value = res.data || {}
  const keys = Object.keys(previewData.value)
  previewTab.value = keys[0] || ''
  previewVisible.value = true
}
async function handleGen(row) {
  const res = await batchGenCode([row.tableId])
  const blob = new Blob([res.data], { type: 'application/zip' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${row.tableName}-code.zip`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已下载')
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除「${row.tableName}」的生成配置？`, '提示', { type: 'warning' }).then(async () => {
    await delGenTables(row.tableId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(() => { getList(); loadDb() })
</script>

<style scoped>
.code-block {
  background: #0d1117; color: #c9d1d9; padding: 14px; border-radius: 6px;
  font-size: 12px; line-height: 1.6; max-height: 60vh; overflow: auto;
  font-family: Consolas, Monaco, "Courier New", monospace;
  white-space: pre-wrap; word-break: break-all;
}
</style>
