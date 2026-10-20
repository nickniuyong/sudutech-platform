<template>
  <div class="app-container">
    <el-row :gutter="16">
      <!-- 部门树 -->
      <el-col :span="5">
        <el-card shadow="never" body-style="padding:10px">
          <el-input v-model="deptName" placeholder="搜索部门" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-tree
            :data="depts"
            :props="{ label: 'deptName', children: 'children' }"
            :filter-node-method="filterDept"
            ref="deptTreeRef"
            node-key="deptId"
            highlight-current
            default-expand-all
            @node-click="handleDeptClick"
          />
        </el-card>
      </el-col>
      <!-- 用户列表 -->
      <el-col :span="19">
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="账号"><el-input v-model="queryParams.userName" placeholder="请输入" clearable style="width:150px" /></el-form-item>
          <el-form-item label="昵称"><el-input v-model="queryParams.nickName" placeholder="请输入" clearable style="width:150px" /></el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="全部" clearable style="width:120px">
              <el-option v-for="d in dicts.status" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
            <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
          </el-form-item>
        </el-form>
        <div class="table-card">
          <el-row class="mb8">
            <el-button v-perm="'system:user:add'" type="primary" @click="openForm()"><el-icon><Plus /></el-icon>新增</el-button>
          </el-row>
          <el-table :data="list" border stripe v-loading="loading">
            <el-table-column label="编号" prop="userId" width="70" />
            <el-table-column label="账号" prop="userName" width="110" />
            <el-table-column label="昵称" prop="nickName" width="120" />
            <el-table-column label="部门" prop="deptName" width="140" />
            <el-table-column label="手机" prop="phone" width="130" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-switch :model-value="row.status === '0'" @change="(v) => toggleStatus(row, v)" />
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" width="170">
              <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ') }}</template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button v-perm="'system:user:edit'" link type="primary" @click="openForm(row)">编辑</el-button>
                <el-button v-perm="'system:user:resetPwd'" link type="warning" @click="openResetPwd(row)">重置密码</el-button>
                <el-button v-perm="'system:user:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :total="total" layout="total, prev, pager, next" class="mt12"
            @current-change="getList"
          />
        </div>
      </el-col>
    </el-row>

    <!-- 新增/编辑 -->
    <el-dialog v-model="formVisible" :title="form.userId ? '编辑用户' : '新增用户'" width="640px">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="账号" required><el-input v-model="form.userName" :disabled="!!form.userId" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="昵称" required><el-input v-model="form.nickName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="部门">
            <el-tree-select v-model="form.deptId" :data="depts" :props="{ label: 'deptName', value: 'deptId', children: 'children' }" check-strictly style="width:100%" />
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别">
            <el-select v-model="form.sex" style="width:100%">
              <el-option v-for="d in dicts.sex" :key="d.value" :label="d.label" :value="d.value" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="角色">
            <el-select v-model="form.roleIds" multiple style="width:100%">
              <el-option v-for="r in roles" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="岗位">
            <el-select v-model="form.postIds" multiple style="width:100%">
              <el-option v-for="p in posts" :key="p.postId" :label="p.postName" :value="p.postId" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog v-model="pwdVisible" title="重置密码" width="420px">
      <el-form label-width="80px">
        <el-form-item label="新密码"><el-input v-model="pwdForm.password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listUsers, getUser, getUserInit, addUser, updateUser, delUsers, resetPwd, changeUserStatus } from '@/api/system/user'
import { dictDataByType } from '@/api/system/dict'

const dicts = reactive({ status: [], sex: [] })
const depts = ref([])
const roles = ref([])
const posts = ref([])
const deptName = ref('')
const deptTreeRef = ref()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const queryParams = reactive({ pageNum: 1, pageSize: 10, userName: '', nickName: '', status: '', deptId: null })
const formVisible = ref(false)
const pwdVisible = ref(false)
const pwdForm = reactive({ userId: null, password: '123456' })
const form = ref({})

watch(deptName, (v) => deptTreeRef.value && deptTreeRef.value.filter(v))

function filterDept(value, data) {
  return !value || data.deptName.includes(value)
}

async function loadDict() {
  dicts.status = (await dictDataByType('sys_normal_status')).data
  dicts.sex = (await dictDataByType('sys_user_sex')).data
}

async function loadInit() {
  const res = await getUserInit()
  depts.value = res.data.depts
  roles.value = res.data.roles
  posts.value = res.data.posts
}

async function getList() {
  loading.value = true
  try {
    const res = await listUsers(queryParams)
    list.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() {
  Object.assign(queryParams, { pageNum: 1, userName: '', nickName: '', status: '', deptId: null })
  getList()
}
function handleDeptClick(node) {
  queryParams.deptId = node.deptId
  handleQuery()
}

function openForm(row) {
  if (row) {
    getUser(row.userId).then((res) => {
      form.value = { ...res.data.user, roleIds: res.data.roleIds, postIds: res.data.postIds }
      formVisible.value = true
    })
  } else {
    form.value = { sex: '0', status: '0', roleIds: [], postIds: [] }
    formVisible.value = true
  }
}

async function submitForm() {
  if (!form.value.userName || !form.value.nickName) {
    ElMessage.warning('账号与昵称必填')
    return
  }
  if (form.value.userId) {
    await updateUser(form.value)
  } else {
    await addUser(form.value)
  }
  ElMessage.success('保存成功')
  formVisible.value = false
  getList()
}

function openResetPwd(row) {
  pwdForm.userId = row.userId
  pwdForm.password = '123456'
  pwdVisible.value = true
}

async function submitResetPwd() {
  await resetPwd(pwdForm)
  ElMessage.success('重置成功')
  pwdVisible.value = false
}

async function toggleStatus(row, v) {
  await changeUserStatus({ userId: row.userId, status: v ? '0' : '1' })
  ElMessage.success('操作成功')
  getList()
}

function handleDelete(row) {
  ElMessageBox.confirm(`确认删除用户「${row.userName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delUsers(row.userId)
    ElMessage.success('删除成功')
    getList()
  })
}

onMounted(() => { loadDict(); loadInit(); getList() })
</script>
