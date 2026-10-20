<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" class="search-form">
      <el-form-item label="标题"><el-input v-model="queryParams.noticeTitle" clearable style="width:160px" /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="queryParams.noticeType" clearable style="width:120px">
          <el-option label="通知" value="1" /><el-option label="公告" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'system:notice:add'" type="primary" @click="openForm()">新增公告</el-button>
      </el-row>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="编号" prop="noticeId" width="70" />
        <el-table-column label="标题" prop="noticeTitle" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.noticeType === '1' ? 'warning' : 'success'" size="small">{{ row.noticeType === '1' ? '通知' : '公告' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建者" prop="createBy" width="110" />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-perm="'system:notice:edit'" link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-perm="'system:notice:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" layout="total, prev, pager, next" class="mt12" @current-change="getList" />
    </div>

    <el-dialog v-model="formVisible" :title="form.noticeId ? '编辑公告' : '新增公告'" width="640px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required><el-input v-model="form.noticeTitle" /></el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.noticeType">
            <el-radio value="1">通知</el-radio>
            <el-radio value="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.noticeContent" type="textarea" :rows="6" />
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
import { listNotices, getNotice, addNotice, updateNotice, delNotices } from '@/api/system/dict'

const list = ref([]); const total = ref(0); const loading = ref(false)
const queryParams = reactive({ pageNum: 1, pageSize: 10, noticeTitle: '', noticeType: '' })
const formVisible = ref(false); const form = reactive({})

async function getList() {
  loading.value = true
  try { const res = await listNotices(queryParams); list.value = res.rows; total.value = res.total }
  finally { loading.value = false }
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { Object.assign(queryParams, { pageNum: 1, noticeTitle: '', noticeType: '' }); getList() }
function openForm(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) { getNotice(row.noticeId).then((res) => { Object.assign(form, res.data); formVisible.value = true }) }
  else { Object.assign(form, { noticeType: '1', status: '0' }); formVisible.value = true }
}
async function submitForm() {
  if (!form.noticeTitle) { ElMessage.warning('标题必填'); return }
  if (form.noticeId) { await updateNotice(form) } else { await addNotice(form) }
  ElMessage.success('保存成功'); formVisible.value = false; getList()
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除公告「${row.noticeTitle}」？`, '提示', { type: 'warning' }).then(async () => {
    await delNotices(row.noticeId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(getList)
</script>
