<template>
  <div class="app-container">
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'report:page:list'" type="primary" @click="openForm()">新建报表</el-button>
      </el-row>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="ID" prop="pageId" width="60" />
        <el-table-column label="报表名" prop="pageName" width="200" />
        <el-table-column label="编码" prop="pageCode" width="140" />
        <el-table-column label="备注" prop="remark" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '1' ? 'success' : 'info'" size="small">{{ row.status === '1' ? '已发布' : '草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDesigner(row)">设计</el-button>
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.pageId ? '编辑报表' : '新建报表'" width="500px">
      <el-form label-width="80px">
        <el-form-item label="报表名" required><el-input v-model="form.pageName" /></el-form-item>
        <el-form-item label="编码"><el-input v-model="form.pageCode" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
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
import { listReportPages, saveReportPage, delReportPages } from '@/api/platform'

const router = useRouter()
const list = ref([]); const loading = ref(false); const visible = ref(false)
const form = reactive({})

async function getList() {
  loading.value = true
  try { const res = await listReportPages({}); list.value = res.data } finally { loading.value = false }
}
function openForm(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) { Object.assign(form, row) } else { Object.assign(form, { status: '0' }) }
  visible.value = true
}
async function submit() {
  if (!form.pageName) { ElMessage.warning('报表名必填'); return }
  await saveReportPage(form)
  ElMessage.success('保存成功'); visible.value = false; getList()
}
function openDesigner(row) {
  router.push(`/report/designer/${row.pageId}`)
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除报表「${row.pageName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delReportPages(row.pageId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(getList)
</script>
