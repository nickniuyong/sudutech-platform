<template>
  <div class="app-container">
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'wf:form:list'" type="primary" @click="openForm()">新增表单</el-button>
      </el-row>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="ID" prop="id" width="60" />
        <el-table-column label="表单名" prop="formName" />
        <el-table-column label="表单Key" prop="formKey" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">{{ row.status === '0' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" width="170">
          <template #default="{ row }">{{ (row.updateTime || row.createTime || '').replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="form.id ? '编辑表单' : '新增表单'" width="680px">
      <el-form label-width="80px">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="表单名" required><el-input v-model="form.formName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="表单Key" required><el-input v-model="form.formKey" placeholder="如 leave-form" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="Schema">
          <el-input v-model="form.formContent" type="textarea" :rows="12" placeholder="JSON Schema 字段定义，如：
{&quot;fields&quot;:[{&quot;key&quot;:&quot;days&quot;,&quot;label&quot;:&quot;请假天数&quot;,&quot;type&quot;:&quot;number&quot;,&quot;required&quot;:true},{&quot;key&quot;:&quot;reason&quot;,&quot;label&quot;:&quot;事由&quot;,&quot;type&quot;:&quot;text&quot;}]}" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">启用</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listWfForms, getWfForm, saveWfForm, delWfForm } from '@/api/workflow'

const list = ref([]); const loading = ref(false); const visible = ref(false)
const form = reactive({})

async function getList() {
  loading.value = true
  try { const res = await listWfForms({}); list.value = res.data } finally { loading.value = false }
}
function openForm(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) { getWfForm(row.id).then((res) => { Object.assign(form, res.data); visible.value = true }) }
  else { Object.assign(form, { status: '0' }); visible.value = true }
}
async function submit() {
  if (!form.formName || !form.formKey) { ElMessage.warning('名称与Key必填'); return }
  if (form.formContent) {
    try { JSON.parse(form.formContent) } catch (e) { ElMessage.warning('Schema 不是合法 JSON'); return }
  }
  await saveWfForm(form)
  ElMessage.success('保存成功'); visible.value = false; getList()
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除表单「${row.formName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delWfForm(row.id); ElMessage.success('删除成功'); getList()
  })
}
onMounted(getList)
</script>
