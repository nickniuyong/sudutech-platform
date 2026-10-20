<template>
  <div class="app-container">
    <el-row class="mb8">
      <el-button size="small" @click="$router.back()">返回</el-button>
      <strong style="margin-left:10px">{{ pageName }}</strong>
      <span style="flex:1" />
      <el-button size="small" type="primary" @click="openChartForm()">添加图表</el-button>
      <el-button size="small" @click="dsVisible = true">数据集管理</el-button>
    </el-row>

    <!-- 图表栅格 -->
    <el-row :gutter="14">
      <el-col v-for="(chart, idx) in charts" :key="chart.chartId" :span="chart.width || 12">
        <el-card shadow="hover" body-style="padding:10px" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span>{{ chart.chartName }}（{{ chart.chartType }}）</span>
              <span>
                <el-button link size="small" type="primary" @click="openChartForm(chart)">编辑</el-button>
                <el-button link size="small" type="danger" @click="removeChart(chart)">删除</el-button>
              </span>
            </div>
          </template>
          <div :ref="(el) => setChartEl(el, idx)" style="height: 300px" />
        </el-card>
      </el-col>
      <el-col :span="24" v-if="!charts.length">
        <el-empty description="点击右上角「添加图表」创建第一个图表" />
      </el-col>
    </el-row>

    <!-- 图表表单 -->
    <el-dialog v-model="chartVisible" :title="chartForm.chartId ? '编辑图表' : '添加图表'" width="680px">
      <el-form label-width="90px">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="图表名" required><el-input v-model="chartForm.chartName" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="图表类型" required>
              <el-select v-model="chartForm.chartType" style="width:100%">
                <el-option v-for="t in ['BAR', 'LINE', 'PIE', 'TABLE', 'FUNNEL', 'GAUGE']" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据集" required>
              <el-select v-model="chartForm.datasetId" style="width:100%">
                <el-option v-for="d in datasets" :key="d.datasetId" :label="d.datasetName" :value="d.datasetId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6"><el-form-item label="栅格宽">
            <el-select v-model="chartForm.width" style="width:100%">
              <el-option v-for="w in [6, 8, 12, 16, 24]" :key="w" :label="w + '/24'" :value="w" />
            </el-select>
          </el-form-item></el-col>
          <el-col :span="6"><el-form-item label="排序"><el-input-number v-model="chartForm.sortNum" :min="0" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="X/维度字段"><el-input v-model="chartForm.xField" placeholder="默认第一列" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="Y/度量字段"><el-input v-model="chartForm.yField" placeholder="默认第二列" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="预览数据">
          <el-button size="small" @click="previewChartData">执行预览</el-button>
          <span style="margin-left:8px;color:#909399;font-size:12px">{{ previewTip }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="chartVisible = false">取消</el-button>
        <el-button type="primary" @click="submitChart">保存并渲染</el-button>
      </template>
    </el-dialog>

    <!-- 数据集管理 -->
    <el-dialog v-model="dsVisible" title="报表数据集" width="860px" top="5vh">
      <el-row class="mb8">
        <el-button size="small" type="primary" @click="openDsForm()">新增数据集</el-button>
      </el-row>
      <el-table :data="datasets" border size="small">
        <el-table-column label="名称" prop="datasetName" width="140" />
        <el-table-column label="SQL" prop="sqlText" show-overflow-tooltip />
        <el-table-column label="行数上限" prop="rowLimit" width="80" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDsForm(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="delDs(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="dsFormVisible" :title="dsForm.datasetId ? '编辑数据集' : '新增数据集'" width="640px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="名称" required><el-input v-model="dsForm.datasetName" /></el-form-item>
        <el-form-item label="数据源" required>
          <el-select v-model="dsForm.datasourceId" style="width:100%">
            <el-option v-for="d in datasources" :key="d.datasourceId" :label="d.datasourceName" :value="d.datasourceId" />
          </el-select>
        </el-form-item>
        <el-form-item label="SQL" required>
          <el-input v-model="dsForm.sqlText" type="textarea" :rows="5" placeholder="只允许 SELECT，支持 :param 占位符" />
        </el-form-item>
        <el-form-item label="行数上限"><el-input-number v-model="dsForm.rowLimit" :min="10" :max="10000" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dsFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDs">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listReportPages, listReportDatasets, saveReportDataset, delReportDatasets,
  executeDataset, listCharts, saveChart, delCharts, listDatasources
} from '@/api/platform'
import { buildChartOption, renderChart } from '@/utils/chart'

const route = useRoute()
const pageId = route.params.pageId
const pageName = ref('报表设计')
const charts = ref([])
const datasets = ref([])
const datasources = ref([])
const chartEls = {}
const chartVisible = ref(false)
const chartForm = reactive({})
const dsVisible = ref(false); const dsFormVisible = ref(false)
const dsForm = reactive({})
const previewTip = ref('')

function setChartEl(el, idx) { if (el) chartEls[idx] = el }

async function renderAll() {
  await nextTick()
  for (let idx = 0; idx < charts.value.length; idx++) {
    const chart = charts.value[idx]
    if (!chartEls[idx]) continue
    if (chart.chartType === 'TABLE') {
      renderTable(chartEls[idx], chart.rows || [])
    } else {
      renderChart(chartEls[idx], buildChartOption(chart.chartType, chart.rows || [], chart.xField, chart.yField))
    }
  }
}

function renderTable(el, rows) {
  el.innerHTML = ''
  if (!rows.length) {
    el.innerHTML = '<p style="color:#909399;text-align:center;padding:40px">暂无数据</p>'
    return
  }
  const cols = Object.keys(rows[0])
  const table = document.createElement('table')
  table.style.cssText = 'width:100%;border-collapse:collapse;font-size:12px'
  table.innerHTML =
    '<thead><tr>' + cols.map((c) => `<th style="border:1px solid #ebeef5;padding:6px;background:#f5f7fa">${c}</th>`).join('') + '</tr></thead>' +
    '<tbody>' + rows.slice(0, 50).map((r) => '<tr>' + cols.map((c) => `<td style="border:1px solid #ebeef5;padding:6px">${r[c] ?? ''}</td>`).join('') + '</tr>').join('') + '</tbody>'
  el.appendChild(table)
}

async function loadCharts() {
  const res = await listCharts({ pageId })
  charts.value = res.data || []
  // 逐个执行数据集并渲染
  for (const chart of charts.value) {
    if (chart.datasetId) {
      try {
        const r = await executeDataset({ datasetId: chart.datasetId, params: {} })
        chart.rows = r.data.rows || []
      } catch (e) { chart.rows = [] }
    }
  }
  renderAll()
}

function openChartForm(row) {
  Object.keys(chartForm).forEach((k) => delete chartForm[k])
  if (row) { Object.assign(chartForm, row) } else { Object.assign(chartForm, { pageId: Number(pageId), chartType: 'BAR', width: 12, sortNum: charts.value.length }) }
  chartVisible.value = true
}

async function previewChartData() {
  if (!chartForm.datasetId) { ElMessage.warning('请选择数据集'); return }
  try {
    const r = await executeDataset({ datasetId: chartForm.datasetId, params: {} })
    previewTip.value = `执行成功，返回 ${r.data.rows.length} 行，列：${(r.data.columns || []).join(', ')}`
  } catch (e) {
    previewTip.value = '执行失败'
  }
}

async function submitChart() {
  if (!chartForm.chartName || !chartForm.chartType || !chartForm.datasetId) { ElMessage.warning('请填写完整'); return }
  await saveChart(chartForm)
  ElMessage.success('已保存')
  chartVisible.value = false
  loadCharts()
}

function removeChart(chart) {
  ElMessageBox.confirm(`确认删除图表「${chart.chartName}」？`, '提示', { type: 'warning' }).then(async () => {
    await delCharts(chart.chartId)
    ElMessage.success('删除成功')
    loadCharts()
  })
}

function openDsForm(row) {
  Object.keys(dsForm).forEach((k) => delete dsForm[k])
  if (row) { Object.assign(dsForm, row) } else { Object.assign(dsForm, { rowLimit: 1000 }) }
  dsFormVisible.value = true
}
async function submitDs() {
  if (!dsForm.datasetName || !dsForm.sqlText || !dsForm.datasourceId) { ElMessage.warning('请填写完整'); return }
  await saveReportDataset(dsForm)
  ElMessage.success('已保存')
  dsFormVisible.value = false
  const res = await listReportDatasets({})
  datasets.value = res.data || []
}
async function delDs(row) {
  await delReportDatasets(row.datasetId)
  ElMessage.success('已删除')
  const res = await listReportDatasets({})
  datasets.value = res.data || []
}

onMounted(async () => {
  const [pageRes, dsRes, sourceRes] = await Promise.all([
    listReportPages({}), listReportDatasets({}), listDatasources()
  ])
  const page = (pageRes.data || []).find((p) => String(p.pageId) === String(pageId))
  if (page) pageName.value = page.pageName
  datasets.value = dsRes.data || []
  datasources.value = sourceRes.data || []
  loadCharts()
})

onBeforeUnmount(() => { /* charts auto GC */ })
</script>

<style scoped>
.chart-card { margin-bottom: 14px; }
.chart-header { display: flex; justify-content: space-between; align-items: center; font-size: 13px; }
</style>
