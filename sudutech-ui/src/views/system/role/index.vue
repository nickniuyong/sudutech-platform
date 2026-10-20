<template>
  <div class="app-container">
    <el-form :inline="true" :model="queryParams" class="search-form">
      <el-form-item label="角色名"><el-input v-model="queryParams.roleName" clearable style="width:150px" /></el-form-item>
      <el-form-item label="权限字符"><el-input v-model="queryParams.roleKey" clearable style="width:150px" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'system:role:add'" type="primary" @click="openForm()">新增</el-button>
      </el-row>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="编号" prop="roleId" width="70" />
        <el-table-column label="角色名" prop="roleName" />
        <el-table-column label="权限字符" prop="roleKey" />
        <el-table-column label="数据范围" width="130">
          <template #default="{ row }">{{ dataScopeText(row.dataScope) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.status === '0'" @change="(v) => toggleStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'system:role:edit'" link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-perm="'system:role:edit'" link type="success" @click="openGrant(row)">菜单权限</el-button>
            <el-button v-perm="'system:role:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" layout="total, prev, pager, next" class="mt12" @current-change="getList" />
    </div>

    <el-dialog v-model="formVisible" :title="form.roleId ? '编辑角色' : '新增角色'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="角色名" required><el-input v-model="form.roleName" /></el-form-item>
        <el-form-item label="权限字符" required><el-input v-model="form.roleKey" placeholder="如 finance" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.roleSort" :min="0" /></el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope" style="width:100%">
            <el-option label="全部数据" value="1" />
            <el-option label="自定义" value="2" />
            <el-option label="本部门" value="3" />
            <el-option label="本部门及以下" value="4" />
            <el-option label="仅本人" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="grantVisible" title="分配菜单权限" width="480px">
      <el-tree ref="menuTreeRef" :data="menuTree" show-checkbox node-key="menuId" default-expand-all
        :props="{ label: 'menuName', children: 'children' }" />
      <template #footer>
        <el-button @click="grantVisible = false">取消</el-button>
        <el-button type="primary" @click="submitGrant">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listRoles, getRole, addRole, updateRole, delRoles, grantRoleMenu, changeRoleStatus, roleMenuTree } from '@/api/system/index'

const list = ref([]); const total = ref(0); const loading = ref(false)
const queryParams = reactive({ pageNum: 1, pageSize: 10, roleName: '', roleKey: '' })
const formVisible = ref(false); const grantVisible = ref(false)
const form = ref({}); const menuTree = ref([]); const menuTreeRef = ref()

const scopeMap = { 1: '全部数据', 2: '自定义', 3: '本部门', 4: '本部门及以下', 5: '仅本人' }
const dataScopeText = (v) => scopeMap[v] || v

async function getList() {
  loading.value = true
  try { const res = await listRoles(queryParams); list.value = res.rows; total.value = res.total }
  finally { loading.value = false }
}
function handleQuery() { queryParams.pageNum = 1; getList() }
function resetQuery() { Object.assign(queryParams, { pageNum: 1, roleName: '', roleKey: '' }); getList() }

function openForm(row) {
  if (row) {
    getRole(row.roleId).then((res) => { form.value = res.data.role; formVisible.value = true })
  } else {
    form.value = { roleSort: 0, dataScope: '1', status: '0' }
    formVisible.value = true
  }
}
async function submitForm() {
  if (!form.value.roleName || !form.value.roleKey) { ElMessage.warning('角色名与权限字符必填'); return }
  if (form.value.roleId) { await updateRole(form.value) } else { await addRole(form.value) }
  ElMessage.success('保存成功'); formVisible.value = false; getList()
}
const grantRoleId = ref(null)
async function openGrant(row) {
  grantRoleId.value = row.roleId
  const [treeRes, roleRes] = await Promise.all([roleMenuTree(), getRole(row.roleId)])
  menuTree.value = treeRes.data
  grantVisible.value = true
  await nextTick()
  // 仅回显叶子节点，父节点由 el-tree 父子联动自动推导；保存时合并半选节点即可完整还原
  const parentIds = new Set()
  const walk = (nodes) => (nodes || []).forEach((n) => {
    if (n.children && n.children.length) { parentIds.add(n.menuId); walk(n.children) }
  })
  walk(treeRes.data)
  menuTreeRef.value.setCheckedKeys((roleRes.data.menuIds || []).filter((id) => !parentIds.has(id)))
}
async function submitGrant() {
  const menuIds = menuTreeRef.value.getCheckedKeys().concat(menuTreeRef.value.getHalfCheckedKeys())
  await grantRoleMenu({ roleId: grantRoleId.value, menuIds })
  ElMessage.success('授权成功'); grantVisible.value = false
}
async function toggleStatus(row, v) {
  await changeRoleStatus({ roleId: row.roleId, status: v ? '0' : '1' })
  ElMessage.success('操作成功'); getList()
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除角色「${row.roleName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delRoles(row.roleId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(getList)
</script>
