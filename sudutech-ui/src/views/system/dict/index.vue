<template>
  <div class="app-container">
    <el-form :inline="true" :model="typeQuery" class="search-form">
      <el-form-item label="字典名称"><el-input v-model="typeQuery.dictName" clearable style="width:150px" /></el-form-item>
      <el-form-item label="字典类型"><el-input v-model="typeQuery.dictType" clearable style="width:150px" /></el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">搜索</el-button>
        <el-button v-perm="'system:dict:add'" type="primary" plain @click="openTypeForm()">新增类型</el-button>
      </el-form-item>
    </el-form>
    <div class="table-card">
      <el-table :data="typeList" border stripe v-loading="loading">
        <el-table-column label="编号" prop="dictId" width="70" />
        <el-table-column label="字典名称" prop="dictName" />
        <el-table-column label="字典类型">
          <template #default="{ row }">
            <el-link type="primary" @click="openData(row)">{{ row.dictType }}</el-link>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">{{ row.status === '0' ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-perm="'system:dict:edit'" link type="primary" @click="openTypeForm(row)">编辑</el-button>
            <el-button v-perm="'system:dict:remove'" link type="danger" @click="deleteType(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="typeQuery.pageNum" v-model:page-size="typeQuery.pageSize"
        :total="typeTotal" layout="total, prev, pager, next" class="mt12" @current-change="getList" />
    </div>

    <!-- 字典类型表单 -->
    <el-dialog v-model="typeVisible" :title="typeForm.dictId ? '编辑字典类型' : '新增字典类型'" width="480px">
      <el-form :model="typeForm" label-width="90px">
        <el-form-item label="字典名称" required><el-input v-model="typeForm.dictName" /></el-form-item>
        <el-form-item label="字典类型" required><el-input v-model="typeForm.dictType" placeholder="如 sys_demo" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="typeForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitType">确定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据 -->
    <el-dialog v-model="dataVisible" :title="`字典数据 - ${currentType}`" width="720px">
      <el-row class="mb8">
        <el-button v-perm="'system:dict:add'" type="primary" size="small" @click="openDataForm()">新增数据</el-button>
      </el-row>
      <el-table :data="dataList" border size="small" max-height="360">
        <el-table-column label="标签" prop="dictLabel" />
        <el-table-column label="键值" prop="dictValue" />
        <el-table-column label="排序" prop="dictSort" width="60" />
        <el-table-column label="样式" prop="listClass" width="90" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button v-perm="'system:dict:edit'" link type="primary" size="small" @click="openDataForm(row)">编辑</el-button>
            <el-button v-perm="'system:dict:remove'" link type="danger" size="small" @click="deleteData(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="dataFormVisible" :title="dataForm.dictCode ? '编辑数据' : '新增数据'" width="460px" append-to-body>
      <el-form :model="dataForm" label-width="90px">
        <el-form-item label="标签" required><el-input v-model="dataForm.dictLabel" /></el-form-item>
        <el-form-item label="键值" required><el-input v-model="dataForm.dictValue" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="dataForm.dictSort" :min="0" /></el-form-item>
        <el-form-item label="标签样式">
          <el-select v-model="dataForm.listClass" clearable>
            <el-option label="default" value="" /><el-option label="primary" value="primary" />
            <el-option label="success" value="success" /><el-option label="warning" value="warning" />
            <el-option label="danger" value="danger" /><el-option label="info" value="info" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitData">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listDictTypes, addDictType, updateDictType, delDictTypes,
  listDictData, addDictData, updateDictData, delDictData
} from '@/api/system/dict'

const typeList = ref([]); const typeTotal = ref(0); const loading = ref(false)
const typeQuery = reactive({ pageNum: 1, pageSize: 10, dictName: '', dictType: '' })
const typeVisible = ref(false); const typeForm = reactive({})
const dataVisible = ref(false); const dataList = ref([]); const currentType = ref('')
const dataFormVisible = ref(false); const dataForm = reactive({})

async function getList() {
  loading.value = true
  try { const res = await listDictTypes(typeQuery); typeList.value = res.rows; typeTotal.value = res.total }
  finally { loading.value = false }
}
function handleQuery() { typeQuery.pageNum = 1; getList() }

function openTypeForm(row) {
  Object.keys(typeForm).forEach((k) => delete typeForm[k])
  if (row) { Object.assign(typeForm, row) } else { Object.assign(typeForm, { status: '0' }) }
  typeVisible.value = true
}
async function submitType() {
  if (!typeForm.dictName || !typeForm.dictType) { ElMessage.warning('名称与类型必填'); return }
  if (typeForm.dictId) { await updateDictType(typeForm) } else { await addDictType(typeForm) }
  ElMessage.success('保存成功'); typeVisible.value = false; getList()
}
function deleteType(row) {
  ElMessageBox.confirm(`确认删除字典「${row.dictName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delDictTypes(row.dictId); ElMessage.success('删除成功'); getList()
  })
}
async function openData(row) {
  currentType.value = row.dictType
  const res = await listDictData({ dictType: row.dictType, pageNum: 1, pageSize: 100 })
  dataList.value = res.rows
  dataVisible.value = true
}
function openDataForm(row) {
  Object.keys(dataForm).forEach((k) => delete dataForm[k])
  if (row) { Object.assign(dataForm, row) } else { Object.assign(dataForm, { dictType: currentType.value, dictSort: 0, status: '0' }) }
  dataFormVisible.value = true
}
async function submitData() {
  if (!dataForm.dictLabel || !dataForm.dictValue) { ElMessage.warning('标签与键值必填'); return }
  if (dataForm.dictCode) { await updateDictData(dataForm) } else { await addDictData(dataForm) }
  ElMessage.success('保存成功'); dataFormVisible.value = false
  await openData({ dictType: currentType.value })
}
function deleteData(row) {
  ElMessageBox.confirm('确认删除该字典数据？', '提示', { type: 'warning' }).then(async () => {
    await delDictData(row.dictCode); ElMessage.success('删除成功')
    await openData({ dictType: currentType.value })
  })
}
onMounted(getList)
</script>
