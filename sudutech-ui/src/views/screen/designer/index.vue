<template>
  <div class="designer">
    <!-- 顶栏 -->
    <div class="top-bar">
      <el-button size="small" @click="$router.back()">返回</el-button>
      <strong>{{ project ? project.projectName : '大屏设计' }}</strong>
      <span class="spacer" />
      <el-button size="small" type="primary" :loading="saving" @click="handleSave">保存画布</el-button>
      <el-button size="small" type="success" @click="handlePublish">发布</el-button>
    </div>

    <div class="designer-body">
      <!-- 组件面板：支持拖入画布或点击添加 -->
      <div class="palette">
        <div class="panel-title">组件库（拖入或点击添加）</div>
        <div v-for="c in components" :key="c.componentId" class="palette-item"
          @mousedown="startPanelDrag(c, $event)">
          <el-icon><component :is="iconOf(c.componentCode)" /></el-icon>
          <span>{{ c.componentName }}</span>
        </div>
        <el-divider content-position="left">数据集</el-divider>
        <el-button size="small" style="width:100%" @click="datasetVisible = true">管理数据集</el-button>
      </div>

      <!-- 画布 -->
      <div class="canvas-wrap" ref="canvasWrapRef">
        <div class="canvas" ref="canvasElRef" :style="canvasStyle">
          <div v-for="(comp, idx) in comps" :key="comp.id" class="comp"
            :class="{ selected: idx === selectedIndex }"
            :style="{ left: comp.x + 'px', top: comp.y + 'px', width: comp.w + 'px', height: comp.h + 'px' }"
            @mousedown.stop="onCompMouseDown($event, idx)">
            <!-- 渲染 -->
            <template v-if="comp.code.startsWith('chart-')">
              <div :ref="(el) => setChartEl(el, idx)" class="comp-chart" />
            </template>
            <template v-else-if="comp.code === 'text'">
              <div class="comp-text" :style="textStyle(comp)">{{ comp.config.text }}</div>
            </template>
            <template v-else-if="comp.code === 'time'">
              <div class="comp-text" :style="textStyle(comp)">{{ nowText }}</div>
            </template>
            <template v-else>
              <div class="comp-border" :style="{ border: `${comp.config.borderWidth || 1}px ${comp.config.borderStyle || 'solid'} ${comp.config.borderColor || '#00eaff'}`, borderRadius: (comp.config.borderRadius || 4) + 'px' }" />
            </template>
            <!-- 删除 -->
            <span class="comp-del" @mousedown.stop @click.stop="removeComp(idx)">×</span>
          </div>
        </div>
        <!-- 拖拽跟随提示 -->
        <div v-if="dragState.active" class="drag-ghost"
          :style="{ left: dragState.ghostX + 12 + 'px', top: dragState.ghostY + 12 + 'px' }">
          {{ dragState.comp && dragState.comp.componentName }}
        </div>
      </div>

      <!-- 属性面板 -->
      <div class="props">
        <div class="panel-title">属性配置</div>
        <template v-if="current">
          <el-form size="small" label-width="70px">
            <el-form-item label="X"><el-input-number v-model="current.x" :step="10" style="width:100%" /></el-form-item>
            <el-form-item label="Y"><el-input-number v-model="current.y" :step="10" style="width:100%" /></el-form-item>
            <el-form-item label="宽"><el-input-number v-model="current.w" :min="50" style="width:100%" /></el-form-item>
            <el-form-item label="高"><el-input-number v-model="current.h" :min="40" style="width:100%" /></el-form-item>
            <template v-if="current.code === 'text'">
              <el-form-item label="文本"><el-input v-model="current.config.text" @change="rerender" /></el-form-item>
              <el-form-item label="字号"><el-input-number v-model="current.config.fontSize" :min="12" :max="72" style="width:100%" /></el-form-item>
              <el-form-item label="颜色"><el-color-picker v-model="current.config.color" /></el-form-item>
            </template>
            <template v-if="current.code.startsWith('chart-')">
              <el-form-item label="数据集">
                <el-select v-model="current.config.datasetId" clearable style="width:100%" @change="loadCompData(current)">
                  <el-option v-for="d in datasets" :key="d.datasetId" :label="d.datasetName" :value="d.datasetId" />
                </el-select>
              </el-form-item>
              <el-form-item label="刷新(秒)"><el-input-number v-model="current.config.refreshInterval" :min="0" style="width:100%" /></el-form-item>
              <el-form-item label="X字段"><el-input v-model="current.config.xField" placeholder="默认第一列" @change="rerender" /></el-form-item>
              <el-form-item label="Y字段"><el-input v-model="current.config.yField" placeholder="默认第二列" @change="rerender" /></el-form-item>
            </template>
          </el-form>
        </template>
        <el-empty v-else description="点击组件进行配置" :image-size="60" />
      </div>
    </div>

    <!-- 数据集管理 -->
    <el-dialog v-model="datasetVisible" title="数据集管理" width="860px" top="5vh">
      <el-row class="mb8">
        <el-button size="small" type="primary" @click="openDsForm()">新增数据集</el-button>
      </el-row>
      <el-table :data="datasets" border size="small">
        <el-table-column label="名称" prop="datasetName" width="140" />
        <el-table-column label="SQL" prop="sqlText" show-overflow-tooltip />
        <el-table-column label="刷新(秒)" prop="refreshInterval" width="80" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDsForm(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="delDs(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog v-model="dsFormVisible" :title="dsForm.datasetId ? '编辑数据集' : '新增数据集'" width="640px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="名称" required><el-input v-model="dsForm.datasetName" /></el-form-item>
        <el-form-item label="数据源" required>
          <el-select v-model="dsForm.datasourceId" style="width:100%">
            <el-option v-for="d in datasources" :key="d.datasourceId" :label="d.datasourceName" :value="d.datasourceId" />
          </el-select>
        </el-form-item>
        <el-form-item label="SQL" required>
          <el-input v-model="dsForm.sqlText" type="textarea" :rows="5" placeholder="只允许 SELECT，支持 :param 占位符" />
        </el-form-item>
        <el-form-item label="刷新(秒)"><el-input-number v-model="dsForm.refreshInterval" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dsFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDs">保存并预览</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as Icons from '@element-plus/icons-vue'
import { getPage, savePage, componentList, listScreenDatasets, saveScreenDataset, delScreenDatasets, previewDataset, publishProject, getProject, listDatasources } from '@/api/platform'
import { buildChartOption, renderChart } from '@/utils/chart'

const route = useRoute()
const router = useRouter()
const projectId = route.params.projectId

const project = ref(null)
const components = ref([])
const datasets = ref([])
const datasources = ref([])
const canvas = reactive({ width: 1920, height: 1080, background: '#0b1c33', scaleMode: 'fit' })
const comps = ref([])
const selectedIndex = ref(-1)
const saving = ref(false)
const datasetVisible = ref(false)
const dsFormVisible = ref(false)
const dsForm = reactive({})
const nowText = ref(new Date().toLocaleString('zh-CN'))
const chartEls = {}
const compTimers = {}

const current = computed(() => comps.value[selectedIndex.value])
const canvasStyle = computed(() => ({
  width: canvas.width + 'px',
  height: canvas.height + 'px',
  background: canvas.background,
  transform: 'scale(' + scale.value + ')'
}))

/** 画布自适应缩放 */
const scale = ref(0.5)
function fitScale() {
  const wrap = canvasWrapRef.value || document.querySelector('.canvas-wrap')
  if (!wrap) return
  scale.value = Math.min(wrap.clientWidth / canvas.width, wrap.clientHeight / canvas.height) * 0.96
}

/** 自研 useDrag 拖拽引擎（鼠标事件，兼容缩放画布） */
const canvasWrapRef = ref(null)
const canvasElRef = ref(null)
const dragState = reactive({ active: false, comp: null, ghostX: 0, ghostY: 0, moved: false })

/** 面板按下：开始拖入 */
function startPanelDrag(c, e) {
  e.preventDefault()
  dragState.active = true
  dragState.comp = c
  dragState.moved = false
  dragState.ghostX = e.clientX
  dragState.ghostY = e.clientY
  dragState.startX = e.clientX
  dragState.startY = e.clientY
  document.addEventListener('mousemove', onPanelDragMove)
  document.addEventListener('mouseup', onPanelDragUp)
}

function onPanelDragMove(e) {
  dragState.ghostX = e.clientX
  dragState.ghostY = e.clientY
  if (Math.abs(e.clientX - dragState.startX) > 4 || Math.abs(e.clientY - dragState.startY) > 4) {
    dragState.moved = true
  }
}

function onPanelDragUp(e) {
  document.removeEventListener('mousemove', onPanelDragMove)
  document.removeEventListener('mouseup', onPanelDragUp)
  if (!dragState.active || !dragState.comp) return
  const canvasEl = canvasElRef.value
  let added = false
  if (canvasEl) {
    const rect = canvasEl.getBoundingClientRect()
    // 落点在画布内 → 按落点添加；否则（视为单击）→ 添加到画布中心
    const inside = e.clientX >= rect.left && e.clientX <= rect.right && e.clientY >= rect.top && e.clientY <= rect.bottom
    const c = dragState.comp
    const w = c.defaultWidth || 400
    const h = c.defaultHeight || 300
    const cx = inside ? (e.clientX - rect.left) / scale.value - w / 2 : canvas.width / 2 - w / 2
    const cy = inside ? (e.clientY - rect.top) / scale.value - h / 2 : canvas.height / 2 - h / 2
    addComp(c, cx, cy)
    added = true
  }
  dragState.active = false
  dragState.comp = null
  return added
}

function addComp(c, cx, cy) {
  let config = {}
  try { config = JSON.parse(c.defaultConfig || '{}') } catch (err) { /* ignore */ }
  comps.value.push({
    id: Date.now() + Math.floor(Math.random() * 100),
    code: c.componentCode,
    x: Math.max(0, Math.round(cx)), y: Math.max(0, Math.round(cy)),
    w: c.defaultWidth || 400, h: c.defaultHeight || 300,
    config: { ...config, datasetId: null, refreshInterval: 0, xField: '', yField: '' },
    rows: null
  })
  selectedIndex.value = comps.value.length - 1
  nextTick(() => renderComp(selectedIndex.value))
}

/** 画布内组件：按下选中并支持拖动移动 */
function onCompMouseDown(e, idx) {
  selectedIndex.value = idx
  const comp = comps.value[idx]
  if (!comp) return
  const startX = e.clientX
  const startY = e.clientY
  const ox = comp.x
  const oy = comp.y
  let moved = false
  const move = (ev) => {
    const dx = (ev.clientX - startX) / scale.value
    const dy = (ev.clientY - startY) / scale.value
    if (Math.abs(ev.clientX - startX) > 3 || Math.abs(ev.clientY - startY) > 3) moved = true
    comp.x = Math.max(0, Math.round(ox + dx))
    comp.y = Math.max(0, Math.round(oy + dy))
  }
  const up = () => {
    document.removeEventListener('mousemove', move)
    document.removeEventListener('mouseup', up)
  }
  document.addEventListener('mousemove', move)
  document.addEventListener('mouseup', up)
}

function removeComp(idx) {
  stopTimer(idx)
  comps.value.splice(idx, 1)
  selectedIndex.value = -1
}

function setChartEl(el, idx) { if (el) chartEls[idx] = el }

function iconOf(code) {
  const map = { 'chart-line': 'TrendCharts', 'chart-bar': 'Histogram', 'chart-pie': 'PieChart', 'chart-gauge': 'Odometer', 'chart-funnel': 'Filter', 'chart-table': 'Grid', text: 'Document', time: 'Clock', border: 'Crop' }
  return map[code] || 'Box'
}

function textStyle(comp) {
  return {
    fontSize: (comp.config.fontSize || 24) + 'px',
    color: comp.config.color || '#fff',
    fontWeight: comp.config.fontWeight || 'normal',
    display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%'
  }
}

/** 渲染组件图表 */
async function renderComp(idx) {
  const comp = comps.value[idx]
  if (!comp || !comp.code.startsWith('chart-')) return
  const el = chartEls[idx]
  if (!el) return
  const type = comp.code.replace('chart-', '').toUpperCase()
  if (comp.rows && comp.rows.length) {
    renderChart(el, buildChartOption(type, comp.rows, comp.config.xField, comp.config.yField))
  } else if (!comp.config.datasetId) {
    // 未绑定数据集时显示占位
    renderChart(el, buildChartOption(type, null))
  }
  await loadCompData(comp, idx)
}

async function loadCompData(comp, idx) {
  if (!comp.config.datasetId) return
  const i = idx !== undefined ? idx : comps.value.indexOf(comp)
  const type = comp.code.replace('chart-', '').toUpperCase()
  try {
    const res = await previewDataset({ datasetId: comp.config.datasetId, params: {}, limit: 200 })
    comp.rows = res.data.rows || []
    const el = chartEls[i]
    if (el) {
      renderChart(el, buildChartOption(type, comp.rows, comp.config.xField, comp.config.yField))
    }
    // 自动刷新
    stopTimer(i)
    const interval = comp.config.refreshInterval || (datasets.value.find((d) => d.datasetId === comp.config.datasetId) || {}).refreshInterval
    if (interval > 0) {
      compTimers[i] = setInterval(async () => {
        try {
          const r = await previewDataset({ datasetId: comp.config.datasetId, params: {}, limit: 200 })
          comp.rows = r.data.rows || []
          const el2 = chartEls[i]
          if (el2) renderChart(el2, buildChartOption(type, comp.rows, comp.config.xField, comp.config.yField))
        } catch (e) { /* ignore */ }
      }, interval * 1000)
    }
  } catch (e) { /* 数据集执行失败静默 */ }
}

function stopTimer(idx) {
  if (compTimers[idx]) { clearInterval(compTimers[idx]); delete compTimers[idx] }
}
function rerender() {
  if (selectedIndex.value >= 0) renderComp(selectedIndex.value)
}

async function handleSave() {
  saving.value = true
  try {
    const payload = {
      pageId: pageId.value,
      projectId: Number(projectId),
      pageName: '主页面',
      canvasConfig: JSON.stringify(canvas),
      componentTree: JSON.stringify(comps.value.map((c) => ({
        id: c.id, code: c.code, x: c.x, y: c.y, w: c.w, h: c.h, config: c.config
      })))
    }
    const res = await savePage(payload)
    pageId.value = res.data.pageId
    ElMessage.success('画布已保存')
  } finally { saving.value = false }
}

async function handlePublish() {
  await handleSave()
  const res = await publishProject(Number(projectId))
  ElMessage.success('已发布，分享码：' + res.data.publishUrl)
}

// 数据集管理
function openDsForm(row) {
  Object.keys(dsForm).forEach((k) => delete dsForm[k])
  if (row) { Object.assign(dsForm, row) } else { Object.assign(dsForm, { projectId: Number(projectId), refreshInterval: 0 }) }
  dsFormVisible.value = true
}
async function submitDs() {
  if (!dsForm.datasetName || !dsForm.sqlText || !dsForm.datasourceId) { ElMessage.warning('请填写完整'); return }
  const res = await saveScreenDataset(dsForm)
  dsFormVisible.value = false
  await loadDatasets()
  ElMessage.success('数据集已保存')
  previewDataset({ datasetId: res.data.datasetId, params: {}, limit: 10 }).then(() => {
    ElMessage.success('SQL 执行验证通过')
  }).catch(() => { /* 错误已提示 */ })
}
async function delDs(row) {
  await delScreenDatasets(row.datasetId)
  ElMessage.success('已删除')
  loadDatasets()
}

const pageId = ref(null)
async function loadDatasets() {
  const res = await listScreenDatasets({ projectId })
  datasets.value = res.data || []
}

onMounted(async () => {
  await nextTick()
  fitScale()
  setTimeout(fitScale, 300)
  window.addEventListener('resize', () => { fitScale(); rerender() })
  setInterval(() => { nowText.value = new Date().toLocaleString('zh-CN') }, 1000)
  const [projRes, compRes, dsRes, sourceRes] = await Promise.all([
    getProject(projectId), componentList(), listScreenDatasets({ projectId }), listDatasources()
  ])
  project.value = projRes.data
  components.value = compRes.data || []
  datasets.value = dsRes.data || []
  datasources.value = sourceRes.data || []
  // 读取已有画布
  try {
    const pageRes = await getPage(projectId)
    if (pageRes.data) {
      pageId.value = pageRes.data.pageId
      try { Object.assign(canvas, JSON.parse(pageRes.data.canvasConfig || '{}')) } catch (e) { /* ignore */ }
      const tree = JSON.parse(pageRes.data.componentTree || '[]')
      comps.value = tree.map((c) => ({ ...c, rows: null }))
      fitScale()
      await nextTick()
      comps.value.forEach((_, i) => renderComp(i))
    }
  } catch (e) { /* 新项目无页面 */ }
})

onBeforeUnmount(() => {
  Object.keys(compTimers).forEach((k) => clearInterval(compTimers[k]))
})
</script>

<style scoped>
.designer { height: 100vh; display: flex; flex-direction: column; background: #1a1a2e; }
.top-bar { height: 46px; background: #16213e; color: #fff; display: flex; align-items: center; gap: 12px; padding: 0 14px; }
.spacer { flex: 1; }
.designer-body { flex: 1; display: flex; overflow: hidden; }
.palette { width: 180px; background: #16213e; color: #cfd8e3; padding: 10px; overflow: auto; }
.panel-title { font-size: 13px; margin-bottom: 8px; color: #7f8fa6; }
.palette-item { display: flex; align-items: center; gap: 8px; padding: 9px 10px; border: 1px solid #2a3a5c; border-radius: 6px; margin-bottom: 8px; cursor: grab; font-size: 13px; user-select: none; }
.palette-item:active { cursor: grabbing; border-color: #409eff; }
.drag-ghost { position: fixed; z-index: 99; pointer-events: none; background: rgba(64, 158, 255, .9); color: #fff; padding: 4px 10px; border-radius: 4px; font-size: 12px; }
.palette-item:hover { border-color: #409eff; color: #409eff; }
.canvas-wrap { flex: 1; overflow: hidden; display: flex; align-items: center; justify-content: center; background: #12162a; }
.canvas { position: relative; transform-origin: center center; box-shadow: 0 0 30px rgba(0, 0, 0, .5); }
.comp { position: absolute; border: 1px dashed transparent; }
.comp:hover { border-color: rgba(64, 158, 255, .5); }
.comp.selected { border: 1px solid #409eff; }
.comp-chart { width: 100%; height: 100%; }
.comp-text { width: 100%; }
.comp-del { position: absolute; top: -9px; right: -9px; width: 18px; height: 18px; background: #f56c6c; color: #fff; border-radius: 50%; font-size: 13px; display: none; align-items: center; justify-content: center; cursor: pointer; line-height: 1; z-index: 3; }
.comp.selected .comp-del { display: flex; }
.props { width: 240px; background: #16213e; color: #cfd8e3; padding: 10px; overflow: auto; }
.props :deep(.el-form-item__label) { color: #a4b0be; }
</style>
