import * as echarts from 'echarts'

/**
 * 按图表类型与数据组装 ECharts option
 * @param chartType BAR/LINE/PIE/TABLE/MAP/FUNNEL/GAUGE
 * @param rows 数据行
 * @param xField 维度字段（可选，默认第一列）
 * @param yField 度量字段（可选，默认第二列）
 */
export function buildChartOption(chartType, rows, xField, yField) {
  const type = (chartType || 'BAR').toUpperCase()
  if (!rows || !rows.length || type === 'TABLE') {
    return {
      title: { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#909399', fontSize: 14, fontWeight: 'normal' } }
    }
  }
  const cols = Object.keys(rows[0])
  const x = xField || cols[0]
  const y = yField || cols[1] || cols[0]
  const categories = rows.map((r) => String(r[x]))
  const values = rows.map((r) => Number(r[y]) || 0)
  const base = { tooltip: { trigger: 'axis' } }
  if (type === 'PIE' || type === 'FUNNEL') {
    return {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        type: type === 'PIE' ? 'pie' : 'funnel',
        radius: type === 'PIE' ? '62%' : undefined,
        label: { formatter: '{b}: {c}' },
        data: rows.map((r) => ({ name: String(r[x]), value: Number(r[y]) || 0 }))
      }]
    }
  }
  if (type === 'GAUGE') {
    return {
      series: [{ type: 'gauge', data: [{ value: values[values.length - 1] || 0, name: x }] }]
    }
  }
  return {
    ...base,
    grid: { left: 60, right: 24, top: 30, bottom: 34 },
    xAxis: { type: 'category', data: categories, axisLabel: { rotate: categories.length > 6 ? 30 : 0 } },
    yAxis: { type: 'value' },
    series: [{ type: type.toLowerCase(), data: values, itemStyle: { color: '#409eff' }, smooth: type === 'LINE' }]
  }
}

/** 渲染 ECharts 到指定元素（复用已有实例，避免重复 init 报错） */
export function renderChart(el, option) {
  if (!el) return null
  let chart = echarts.getInstanceByDom(el)
  if (!chart) {
    chart = echarts.init(el)
  }
  chart.setOption(option, true)
  chart.resize()
  return chart
}

export { echarts }
