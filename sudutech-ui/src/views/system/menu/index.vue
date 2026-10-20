<template>
  <div class="app-container">
    <el-form :inline="true" class="search-form">
      <el-form-item label="菜单名"><el-input v-model="queryName" clearable style="width:160px" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="getList">搜索</el-button>
        <el-button @click="queryName = ''; getList()">重置</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'system:menu:add'" type="primary" @click="openForm()">新增</el-button>
        <el-button @click="expand = !expand">{{ expand ? '折叠' : '展开' }}</el-button>
      </el-row>
      <el-table :data="list" border row-key="menuId" :default-expand-all="expand"
        :tree-props="{ children: 'children' }" v-loading="loading">
        <el-table-column label="菜单名" prop="menuName" width="200" />
        <el-table-column label="图标" width="70" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon && row.icon !== '#' && Icons[row.icon]"><component :is="Icons[row.icon]" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.menuType === 'M' ? '' : row.menuType === 'C' ? 'success' : 'warning'" size="small">
              {{ { M: '目录', C: '菜单', F: '按钮' }[row.menuType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="排序" prop="orderNum" width="70" />
        <el-table-column label="权限标识" prop="perms" />
        <el-table-column label="组件路径" prop="component" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'system:menu:edit'" link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-perm="'system:menu:add'" v-if="row.menuType !== 'F'" link type="success" @click="openForm(null, row.menuId)">新增</el-button>
            <el-button v-perm="'system:menu:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="formVisible" :title="form.menuId ? '编辑菜单' : '新增菜单'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select v-model="form.parentId" :data="list" check-strictly
            :props="{ label: 'menuName', value: 'menuId', children: 'children' }" style="width:100%" />
        </el-form-item>
        <el-form-item label="菜单类型">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名" required><el-input v-model="form.menuName" /></el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="路由地址"><el-input v-model="form.path" placeholder="如 user" /></el-form-item>
        <el-form-item v-if="form.menuType === 'C'" label="组件路径"><el-input v-model="form.component" placeholder="如 system/user/index" /></el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="权限标识"><el-input v-model="form.perms" placeholder="如 system:user:list" /></el-form-item>
        <el-form-item v-if="form.menuType !== 'F'" label="图标"><el-input v-model="form.icon" placeholder="Element 图标名，如 user" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.orderNum" :min="0" /></el-form-item>
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
import * as Icons from '@element-plus/icons-vue'
import { listMenus, getMenu, addMenu, updateMenu, delMenu } from '@/api/system/index'

const list = ref([]); const loading = ref(false); const expand = ref(false)
const queryName = ref('')
const formVisible = ref(false)
const form = reactive({})

async function getList() {
  loading.value = true
  try { const res = await listMenus({ menuName: queryName.value }); list.value = res.data }
  finally { loading.value = false }
}

function openForm(row, parentId) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) {
    getMenu(row.menuId).then((res) => { Object.assign(form, res.data); formVisible.value = true })
  } else {
    Object.assign(form, { menuType: 'C', parentId: parentId || 0, orderNum: 0, status: '0', icon: '#' })
    formVisible.value = true
  }
}
async function submitForm() {
  if (!form.menuName) { ElMessage.warning('菜单名必填'); return }
  if (form.menuId) { await updateMenu(form) } else { await addMenu(form) }
  ElMessage.success('保存成功'); formVisible.value = false; getList()
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除菜单「${row.menuName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delMenu(row.menuId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(getList)
</script>
