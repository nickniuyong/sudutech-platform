<template>
  <div class="app-container">
    <el-form :inline="true" class="search-form">
      <el-form-item label="部门名"><el-input v-model="queryName" clearable style="width:160px" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="getList">搜索</el-button>
        <el-button @click="queryName = ''; getList()">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'system:dept:add'" type="primary" @click="openForm()">新增</el-button>
      </el-row>
      <el-table :data="list" border row-key="deptId" default-expand-all :tree-props="{ children: 'children' }" v-loading="loading">
        <el-table-column label="部门名" prop="deptName" width="220" />
        <el-table-column label="排序" prop="orderNum" width="80" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-perm="'system:dept:edit'" link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-perm="'system:dept:add'" link type="success" @click="openForm(null, row.deptId)">新增</el-button>
            <el-button v-perm="'system:dept:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="formVisible" :title="form.deptId ? '编辑部门' : '新增部门'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级部门">
          <el-tree-select v-model="form.parentId" :data="treeData" check-strictly
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }" style="width:100%" />
        </el-form-item>
        <el-form-item label="部门名" required><el-input v-model="form.deptName" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" /></el-form-item>
        <el-form-item label="负责人"><el-input v-model="form.leaderName" disabled placeholder="暂不支持，按用户ID维护" /></el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptTree, getDept, addDept, updateDept, delDept } from '@/api/system/index'

const list = ref([]); const loading = ref(false); const queryName = ref('')
const formVisible = ref(false)
const form = reactive({})
const treeData = computed(() => [{ deptId: 0, deptName: '顶级部门', children: list.value }])

async function getList() {
  loading.value = true
  try { const res = await deptTree({ deptName: queryName.value }); list.value = res.data }
  finally { loading.value = false }
}
function openForm(row, parentId) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) {
    getDept(row.deptId).then((res) => { Object.assign(form, res.data); formVisible.value = true })
  } else {
    Object.assign(form, { parentId: parentId || 0, orderNum: 0, status: '0' })
    formVisible.value = true
  }
}
async function submitForm() {
  if (!form.deptName) { ElMessage.warning('部门名必填'); return }
  if (form.deptId) { await updateDept(form) } else { await addDept(form) }
  ElMessage.success('保存成功'); formVisible.value = false; getList()
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除部门「${row.deptName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delDept(row.deptId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(getList)
</script>
