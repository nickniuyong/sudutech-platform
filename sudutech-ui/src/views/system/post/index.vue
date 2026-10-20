<template>
  <div class="app-container">
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'system:post:add'" type="primary" @click="openForm()">新增岗位</el-button>
      </el-row>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="编号" prop="postId" width="70" />
        <el-table-column label="岗位编码" prop="postCode" />
        <el-table-column label="岗位名称" prop="postName" />
        <el-table-column label="排序" prop="postSort" width="80" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-perm="'system:post:edit'" link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-perm="'system:post:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="formVisible" :title="form.postId ? '编辑岗位' : '新增岗位'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="岗位编码" required><el-input v-model="form.postCode" /></el-form-item>
        <el-form-item label="岗位名称" required><el-input v-model="form.postName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.postSort" :min="0" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { listPosts, getPost, addPost, updatePost, delPosts } from '@/api/system/index'

const list = ref([]); const loading = ref(false); const formVisible = ref(false)
const form = reactive({})

async function getList() {
  loading.value = true
  try { const res = await listPosts({ pageNum: 1, pageSize: 100 }); list.value = res.rows }
  finally { loading.value = false }
}
function openForm(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) { getPost(row.postId).then((res) => { Object.assign(form, res.data); formVisible.value = true }) }
  else { Object.assign(form, { postSort: 0, status: '0' }); formVisible.value = true }
}
async function submitForm() {
  if (!form.postCode || !form.postName) { ElMessage.warning('编码与名称必填'); return }
  if (form.postId) { await updatePost(form) } else { await addPost(form) }
  ElMessage.success('保存成功'); formVisible.value = false; getList()
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除岗位「${row.postName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delPosts(row.postId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(getList)
</script>
