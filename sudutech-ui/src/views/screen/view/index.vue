<template>
  <div class="view-page" v-loading="loading">
    <div class="view-wrap">
      <div class="view-canvas" :style="{ width: canvas.width + 'px', height: canvas.height + 'px', background: canvas.background, transform: `scale(${scale})` }">
        <div v-for="(comp, idx) in comps" :key="comp.id" class="view-comp"
          :style="{ left: comp.x + 'px', top: comp.y + 'px', width: comp.w + 'px', height: comp.h + 'px' }">
          <template v-if="comp.code.startsWith('chart-')">
            <div :ref="(el) => setChartEl(el, idx)" style="width:100%;height:100%" />
          </template>
          <template v-else-if="comp.code === 'text'">
            <div class="view-text" :style="textStyle(comp)">{{ comp.config.text }}</div>
          </template>
          <template v-else-if="comp.code === 'time'">
            <div class="view-text" :style="textStyle(comp)">{{ nowText }}</div>
          </template>
          <template v-else>
            <div style="width:100%;height:100%" :style="{ border: `${comp.config.borderWidth || 1}px ${comp.config.borderStyle || 'solid'} ${comp.config.borderColor || '#00eaff'}`, borderRadius: (comp.config.borderRadius || 4) + 'px' }" />
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { listView } from '@/api/platform'
import { buildChartOption, renderChart } from '@/utils/chart'

const route = useRoute()
const publishUrl = route.params.publishUrl
const loading = ref(true)
const canvas = reactive({ width: 1920, height: 1080, background: '#0b1c33' })
const comps = ref([])
const datasetRows = {}
const chartEls = {}
const timers = []
let scale = 0.5
const nowText = ref(new Date().toLocaleString('zh-CN'))

function setChartEl(el, idx) { if (el) chartEls[idx] = el }

function textStyle(comp) {
  return {
    fontSize: (comp.config.fontSize || 24) + 'px',
    color: comp.config.color || '#fff',
    fontWeight: comp.config.fontWeight || 'normal',
    display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%'
  }
}

function fit() {
  scale = Math.min(window.innerWidth / canvas.width, window.innerHeight / canvas.height)
}

async function loadAndRender() {
  await nextTick()
  comps.value.forEach((comp, idx) => {
    if (comp.code.startsWith('chart-') && chartEls[idx]) {
      const rows = datasetRows[comp.config.datasetId] || []
      const type = comp.code.replace('chart-', '').toUpperCase()
      renderChart(chartEls[idx], buildChartOption(type, rows, comp.config.xField, comp.config.yField))
    }
  })
}

onMounted(async () => {
  fit()
  window.addEventListener('resize', () => { fit(); loadAndRender() })
  setInterval(() => { nowText.value = new Date().toLocaleString('zh-CN') }, 1000)
  try {
    const res = await listView(publishUrl)
    const page = res.data.page
    if (page) {
      try { Object.assign(canvas, JSON.parse(page.canvasConfig || '{}')) } catch (e) { /* ignore */ }
      comps.value = JSON.parse(page.componentTree || '[]')
    }
    // 数据由后端 view 接口直接附带（匿名可用）
    ;(res.data.datasets || []).forEach((d) => {
      datasetRows[d.datasetId] = d.rows || []
      if (d.refreshInterval > 0) {
        // 服务端定时重取：此处按间隔重新拉取整个视图（简单可靠）
        timers.push(setInterval(async () => {
          try {
            const r = await listView(publishUrl)
            ;(r.data.datasets || []).forEach((nd) => { datasetRows[nd.datasetId] = nd.rows || [] })
            loadAndRender()
          } catch (e) { /* ignore */ }
        }, d.refreshInterval * 1000))
      }
    })
    fit()
    await loadAndRender()
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => { timers.forEach(clearInterval); window.removeEventListener('resize', fit) })
</script>

<style scoped>
.view-page { width: 100vw; height: 100vh; background: #000; overflow: hidden; }
.view-wrap { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; }
.view-canvas { position: relative; transform-origin: center center; }
.view-comp { position: absolute; }
.view-text { width: 100%; }
</style>
