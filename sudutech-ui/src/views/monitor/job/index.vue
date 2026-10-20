<template>
  <div class="app-container">
    <div class="table-card">
      <el-row class="mb8">
        <el-button v-perm="'monitor:job:add'" type="primary" @click="openForm()">新增任务</el-button>
        <el-button @click="logVisible = true">调度日志</el-button>
      </el-row>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column label="编号" prop="jobId" width="70" />
        <el-table-column label="任务名" prop="jobName" />
        <el-table-column label="调用目标" prop="invokeTarget" show-overflow-tooltip />
        <el-table-column label="cron" prop="cronExpression" width="150" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch :model-value="row.status === '0'" @change="(v) => toggleStatus(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'monitor:job:edit'" link type="primary" @click="openForm(row)">编辑</el-button>
            <el-button v-perm="'monitor:job:run'" link type="success" @click="handleRun(row)">执行一次</el-button>
            <el-button v-perm="'monitor:job:remove'" link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
        :total="total" layout="total, prev, pager, next" class="mt12" @current-change="getList" />
    </div>

    <el-dialog v-model="formVisible" :title="form.jobId ? '编辑任务' : '新增任务'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="任务名" required><el-input v-model="form.jobName" /></el-form-item>
        <el-form-item label="调用目标" required>
          <el-input v-model="form.invokeTarget" placeholder="如 ryTask.withParams('hello',3)" />
        </el-form-item>
        <el-form-item label="cron表达式" required>
          <el-input v-model="form.cronExpression" placeholder="如 0/10 * * * * ?" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="logVisible" title="调度日志" width="760px">
      <el-table :data="jobLogs" border size="small" max-height="400">
        <el-table-column label="任务名" prop="jobName" width="120" />
        <el-table-column label="调用目标" prop="invokeTarget" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">{{ row.status === '0' ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="信息" prop="jobMessage" />
        <el-table-column label="执行时间" width="170">
          <template #default="{ row }">{{ (row.jobTime || '').replace('T', ' ') }}</template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listJobs, getJob, addJob, updateJob, delJobs, runJob, changeJobStatus, listJobLogs } from '@/api/system/dict'

const list = ref([]); const total = ref(0); const loading = ref(false)
const queryParams = reactive({ pageNum: 1, pageSize: 10 })
const formVisible = ref(false); const form = reactive({})
const logVisible = ref(false); const jobLogs = ref([])

async function getList() {
  loading.value = true
  try { const res = await listJobs(queryParams); list.value = res.rows; total.value = res.total }
  finally { loading.value = false }
}
async function loadLogs() {
  const res = await listJobLogs({ pageNum: 1, pageSize: 50 })
  jobLogs.value = res.rows
}
function openForm(row) {
  Object.keys(form).forEach((k) => delete form[k])
  if (row) { getJob(row.jobId).then((res) => { Object.assign(form, res.data); formVisible.value = true }) }
  else { Object.assign(form, { jobGroup: 'DEFAULT', misfirePolicy: '3', concurrent: '1', status: '1' }); formVisible.value = true }
}
async function submitForm() {
  if (!form.jobName || !form.invokeTarget || !form.cronExpression) { ElMessage.warning('请填写完整'); return }
  if (form.jobId) { await updateJob(form) } else { await addJob(form) }
  ElMessage.success('保存成功'); formVisible.value = false; getList()
}
async function toggleStatus(row, v) {
  await changeJobStatus({ jobId: row.jobId, status: v ? '0' : '1' })
  ElMessage.success('操作成功'); getList()
}
async function handleRun(row) {
  await runJob(row.jobId)
  ElMessage.success('已触发执行'); setTimeout(loadLogs, 1000)
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除任务「${row.jobName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delJobs(row.jobId); ElMessage.success('删除成功'); getList()
  })
}
onMounted(() => { getList(); loadLogs() })
</script>
