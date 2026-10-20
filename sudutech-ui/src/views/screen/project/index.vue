<template>
  <div class="app-container">
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'screen:project:list'" type="primary" @click="openForm()">新建大屏</el-button>
      </el-row>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="ID" prop="projectId" width="60" />
        <el-table-column label="项目名" prop="projectName" width="200" />
        <el-table-column label="编码" prop="projectCode" width="120" />
        <el-table-column label="描述" prop="description" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'info'" size="small">{{ row.status === '1' ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="分享码" prop="publishUrl" width="140" />
        <el-table-column label="操作" width="330" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDesigner(row)">设计</el-button>
            <el-button link type="success" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.publishUrl" link type="warning" @click="openShare(row)">分享页</el-button>
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.projectId ? '编辑大屏' : '新建大屏'" width="500px">
      <el-form label-width="80px">
        <el-form-item label="项目名" required><el-input v-model="form.projectName" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.projectCode" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listProjects, addProject, updateProject, delProjects, publishProject } from '@/api/platform'

const router = useRouter()
const list = ref([]); const loading = ref(false); const visible = ref(false)
const form = reactive({})

async function getList() {
  loading.value = true
  try { const res = await listProjects({}); list.value = res.data } finally { loading.value = false }
}
function openForm(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) { Object.assign(form, row) } else { Object.assign(form, { status: '0' }) }
  visible.value = true
}
async function submit() {
  if (!form.projectName) { ElMessage.warning('项目名必填'); return }
  if (form.projectId) { await updateProject(form) } else { await addProject(form) }
  ElMessage.success('保存成功'); visible.value = false; getList()
}
function openDesigner(row) {
  router.push(`/screen/designer/${row.projectId}`)
}
async function handlePublish(row) {
  const res = await publishProject(row.projectId)
  ElMessage.success('发布成功，分享码：' + res.data.publishUrl)
  getList()
}
function openShare(row) {
  window.open(`/screen/view/${row.publishUrl}`, '_blank')
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除大屏「${row.projectName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delProjects(row.projectId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(getList)
</script>
