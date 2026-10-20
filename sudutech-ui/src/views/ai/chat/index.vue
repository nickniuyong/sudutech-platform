<template>
  <div class="chat-container">
    <!-- 会话侧栏 -->
    <div class="chat-side">
      <el-button type="primary" class="new-btn" @click="handleNewSession">+ 新会话</el-button>
      <el-select v-model="currentDatasource" placeholder="选择数据源" size="small" style="margin: 10px 0; width: 100%">
        <el-option v-for="d in datasources" :key="d.datasourceId" :label="d.datasourceName" :value="d.datasourceId" />
      </el-select>
      <div class="session-list">
        <div v-for="s in sessions" :key="s.sessionId"
          class="session-item" :class="{ active: s.sessionId === currentSession }"
          @click="switchSession(s)">
          <el-icon><ChatDotRound /></el-icon>
          <span class="session-title">{{ s.sessionTitle }}</span>
          <el-icon class="del-btn" @click.stop="handleDelSession(s)"><Delete /></el-icon>
        </div>
      </div>
    </div>

    <!-- 消息区 -->
    <div class="chat-main">
      <div class="messages" ref="messagesRef">
        <el-empty v-if="!messages.length" description="输入自然语言问题，AI 自动生成 SQL 并绘制图表" />
        <div v-for="(m, i) in messages" :key="i" class="msg-row" :class="m.role">
          <div class="msg-bubble">
            <div class="msg-text">{{ m.content }}</div>
            <div v-if="m.generatedSql" class="msg-sql">
              <div class="sql-label"><el-icon><Document /></el-icon> 生成SQL <el-button size="small" text type="primary" @click="copySql(m.generatedSql)">复制</el-button></div>
              <pre>{{ m.generatedSql }}</pre>
            </div>
            <div v-if="m.chartRows && m.chartRows.length" class="msg-chart">
              <div :ref="(el) => setChartRef(el, i)" class="chart-box" />
            </div>
          </div>
        </div>
      </div>
      <div class="input-bar">
        <el-input v-model="question" placeholder="例如：统计每个部门的用户数量，用柱状图展示" size="large"
          :disabled="sending" @keyup.enter="handleSend">
          <template #append>
            <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
          </template>
        </el-input>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listDatasources, listSessions, createSession, delSession, listMessages, sendChat
} from '@/api/platform'
import { buildChartOption, renderChart } from '@/utils/chart'

const datasources = ref([])
const sessions = ref([])
const currentSession = ref(null)
const currentDatasource = ref(null)
const messages = ref([])
const question = ref('')
const sending = ref(false)
const messagesRef = ref()
const chartRefs = {}

function setChartRef(el, i) {
  if (el) chartRefs[i] = el
}

async function loadDatasources() {
  const res = await listDatasources()
  datasources.value = res.data || []
  if (datasources.value.length) {
    currentDatasource.value = datasources.value[0].datasourceId
  }
}

async function loadSessions() {
  const res = await listSessions({ pageNum: 1, pageSize: 50 })
  sessions.value = res.rows || []
}

async function handleNewSession() {
  const res = await createSession({ datasourceId: currentDatasource.value })
  await loadSessions()
  currentSession.value = res.data.sessionId
  messages.value = []
}

async function switchSession(s) {
  currentSession.value = s.sessionId
  if (s.datasourceId) currentDatasource.value = s.datasourceId
  const res = await listMessages(s.sessionId)
  messages.value = (res.rows || []).map((m) => ({
    role: m.role,
    content: m.content,
    generatedSql: m.generatedSql,
    chartType: m.chartType,
    chartRows: m.chartData ? parseRows(m.chartData) : null
  }))
  await nextTick()
  renderAllCharts()
}

function parseRows(chartData) {
  try {
    const d = JSON.parse(chartData)
    return d.rows || []
  } catch (e) { return null }
}

function handleDelSession(s) {
  ElMessageBox.confirm('确认删除该会话？', '提示', { type: 'warning' }).then(async () => {
    await delSession(s.sessionId)
    if (currentSession.value === s.sessionId) { currentSession.value = null; messages.value = [] }
    loadSessions()
  })
}

async function handleSend() {
  if (!question.value.trim()) return
  if (!currentDatasource.value) { ElMessage.warning('请先选择数据源'); return }
  const q = question.value
  question.value = ''
  messages.value.push({ role: 'user', content: q })
  sending.value = true
  scrollBottom()
  try {
    const res = await sendChat({
      sessionId: currentSession.value,
      datasourceId: currentDatasource.value,
      question: q
    })
    currentSession.value = res.data.sessionId
    messages.value.push({
      role: 'assistant',
      content: res.data.summary,
      generatedSql: res.data.sql,
      chartType: res.data.chartType,
      chartRows: res.data.rows
    })
    await loadSessions()
    await nextTick()
    renderAllCharts()
    scrollBottom()
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '查询失败：' + (e.message || '未知错误') })
  } finally {
    sending.value = false
    scrollBottom()
  }
}

function scrollBottom() {
  nextTick(() => {
    if (messagesRef.value) messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  })
}

function copySql(sql) {
  navigator.clipboard.writeText(sql)
  ElMessage.success('已复制')
}

function renderAllCharts() {
  messages.value.forEach((m, i) => {
    if (m.chartRows && m.chartRows.length && chartRefs[i] && m.chartType !== 'TABLE') {
      renderChart(chartRefs[i], buildChartOption(m.chartType, m.chartRows))
    }
  })
}

onMounted(async () => {
  await loadDatasources()
  await loadSessions()
})
</script>

<style scoped>
.chat-container { display: flex; height: calc(100vh - 56px); background: #f5f7fa; }
.chat-side { width: 240px; background: #fff; border-right: 1px solid #e4e7ed; padding: 12px; display: flex; flex-direction: column; }
.new-btn { width: 100%; }
.session-list { flex: 1; overflow: auto; margin-top: 6px; }
.session-item { display: flex; align-items: center; gap: 6px; padding: 9px 10px; border-radius: 6px; cursor: pointer; font-size: 13px; }
.session-item:hover { background: #f5f7fa; }
.session-item.active { background: #ecf5ff; color: #409eff; }
.session-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.del-btn { opacity: 0; }
.session-item:hover .del-btn { opacity: 1; }
.chat-main { flex: 1; display: flex; flex-direction: column; }
.messages { flex: 1; overflow: auto; padding: 20px 30px; }
.msg-row { display: flex; margin-bottom: 14px; }
.msg-row.user { justify-content: flex-end; }
.msg-bubble { max-width: 72%; background: #fff; border-radius: 10px; padding: 12px 16px; box-shadow: 0 1px 3px rgba(0,0,0,.06); }
.msg-row.user .msg-bubble { background: #409eff; color: #fff; }
.msg-text { font-size: 14px; line-height: 1.7; white-space: pre-wrap; }
.msg-sql { margin-top: 8px; background: #0d1117; border-radius: 6px; overflow: hidden; }
.sql-label { display: flex; align-items: center; gap: 4px; color: #8b949e; font-size: 12px; padding: 4px 10px; border-bottom: 1px solid #21262d; }
.msg-sql pre { margin: 0; padding: 10px; color: #79c0ff; font-size: 12px; font-family: Consolas, monospace; white-space: pre-wrap; }
.msg-chart { margin-top: 10px; }
.chart-box { width: 480px; height: 280px; }
.input-bar { padding: 14px 30px; background: #fff; border-top: 1px solid #e4e7ed; }
</style>
