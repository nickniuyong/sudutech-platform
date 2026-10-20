<template>
  <div class="app-container">
    <el-tabs v-model="activeTab">
      <!-- 流程定义 -->
      <el-tab-pane label="流程定义" name="def">
        <el-row class="mb8">
          <el-button v-perm="'wf:def:list'" type="primary" @click="openDeploy">部署流程</el-button>
        </el-row>
        <div class="table-card">
          <el-table :data="defs" border stripe v-loading="loading">
            <el-table-column label="ID" prop="id" width="60" />
            <el-table-column label="流程标识" prop="processKey" width="140" />
            <el-table-column label="流程名称" prop="processName" />
            <el-table-column label="版本" prop="version" width="70">
              <template #default="{ row }">v{{ row.version }}</template>
            </el-table-column>
            <el-table-column label="关联表单" prop="formKey" width="110" />
            <el-table-column label="节点数" prop="nodeCount" width="80" align="center" />
            <el-table-column label="部署状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.deployStatus === '1' ? 'success' : 'info'" size="small">{{ row.deployStatus === '1' ? '已部署' : '未部署' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">{{ row.status === '0' ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="330" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDesign(row)">设计器</el-button>
                <el-button link type="success" @click="openNodes(row)">节点配置</el-button>
                <el-button link type="warning" @click="toggleStatus(row)">{{ row.status === '0' ? '停用' : '启用' }}</el-button>
                <el-button link type="primary" @click="startFlow(row)">发起</el-button>
                <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 待办 -->
      <el-tab-pane label="待办任务" name="todo">
        <TaskTable :tasks="todoList" type="todo" @refresh="loadAll" />
      </el-tab-pane>
      <!-- 已办 -->
      <el-tab-pane label="已办任务" name="done">
        <TaskTable :tasks="doneList" type="done" @refresh="loadAll" />
      </el-tab-pane>
      <!-- 我的申请 -->
      <el-tab-pane label="我的申请" name="mine">
        <div class="table-card">
          <el-table :data="myList" border stripe>
            <el-table-column label="标题" prop="businessTitle" />
            <el-table-column label="流程" prop="processName" width="150" />
            <el-table-column label="当前节点" prop="currentNode" width="140" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="发起时间" width="170">
              <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ') }}</template>
            </el-table-column>
            <el-table-column label="操作" width="110">
              <template #default="{ row }">
                <el-button link type="primary" @click="openTrace(row)">轨迹</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 部署 -->
    <el-dialog v-model="deployVisible" title="部署流程（BPMN 2.0 XML）" width="720px">
      <el-form label-width="90px">
        <el-row :gutter="10">
          <el-col :span="8"><el-form-item label="流程标识" required><el-input v-model="deployForm.processKey" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="流程名称"><el-input v-model="deployForm.processName" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="关联表单"><el-input v-model="deployForm.formKey" placeholder="wf_form 的 formKey" /></el-form-item></el-col>
        </el-row>
        <el-input v-model="deployForm.bpmnXml" type="textarea" :rows="12" placeholder="粘贴 BPMN 2.0 XML，或点击下方按钮使用设计器" />
        <div class="mt12">
          <el-button @click="useTemplate">填入示例模板</el-button>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="deployVisible = false">取消</el-button>
        <el-button type="primary" @click="submitDeploy">部署</el-button>
      </template>
    </el-dialog>

    <!-- bpmn-js 设计器 -->
    <el-dialog v-model="designerVisible" title="BPMN 流程设计器" width="92%" top="3vh" destroy-on-close>
      <div class="designer-wrap">
        <div ref="bpmnCanvas" class="bpmn-canvas" />
        <div class="designer-side">
          <p style="font-size:12px;color:#909399">绘制流程图后保存为该流程的 BPMN XML（部署版本自动+1）。审批人策略请在「节点配置」中维护。</p>
          <el-button type="primary" @click="saveDesignerXml">保存 XML</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 节点配置 -->
    <el-dialog v-model="nodesVisible" title="审批节点配置" width="820px">
      <el-table :data="nodes" border size="small">
        <el-table-column label="节点Key" width="150">
          <template #default="{ row }"><el-input v-model="row.nodeKey" size="small" placeholder="如 leader" /></template>
        </el-table-column>
        <el-table-column label="节点名" width="130">
          <template #default="{ row }"><el-input v-model="row.nodeName" size="small" /></template>
        </el-table-column>
        <el-table-column label="顺序" width="80">
          <template #default="{ row }"><el-input-number v-model="row.nodeOrder" size="small" :min="1" controls-position="right" style="width:70px" /></template>
        </el-table-column>
        <el-table-column label="审批人类型" width="140">
          <template #default="{ row }">
            <el-select v-model="row.assigneeType" size="small">
              <el-option label="指定用户ID" value="USER" />
              <el-option label="角色" value="ROLE" />
              <el-option label="部门负责人" value="DEPT_LEADER" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="审批人值" width="140">
          <template #default="{ row }"><el-input v-model="row.assigneeValue" size="small" placeholder="用户ID或角色key" /></template>
        </el-table-column>
        <el-table-column label="通过模式" width="120">
          <template #default="{ row }">
            <el-select v-model="row.approveMode" size="small">
              <el-option label="任一通过" value="ANY" />
              <el-option label="会签" value="ALL" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="70">
          <template #default="{ $index }">
            <el-button link type="danger" size="small" @click="nodes.splice($index, 1)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button class="mt12" size="small" @click="nodes.push({ nodeKey: '', nodeName: '', nodeOrder: nodes.length + 1, assigneeType: 'USER', assigneeValue: '', approveMode: 'ANY' })">+ 添加节点</el-button>
      <template #footer>
        <el-button @click="nodesVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNodes">保存</el-button>
      </template>
    </el-dialog>

    <!-- 发起流程 -->
    <el-dialog v-model="startVisible" title="发起流程" width="460px">
      <el-form label-width="80px">
        <el-form-item label="业务标题"><el-input v-model="startForm.businessTitle" /></el-form-item>
        <el-form-item label="业务ID"><el-input v-model="startForm.businessId" placeholder="业务表主键（演示任意值）" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStart">发起</el-button>
      </template>
    </el-dialog>

    <!-- 轨迹 -->
    <el-dialog v-model="traceVisible" title="流程轨迹" width="640px">
      <el-steps direction="vertical" :active="stepsActive">
        <el-step v-for="(l, i) in traceLogs" :key="i" :title="`${l.operation} · ${l.nodeKey || '-'}`"
          :description="`${(l.createTime || '').replace('T', ' ')} ${l.opinion || ''}`" />
      </el-steps>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, defineComponent, h } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Modeler from 'bpmn-js/lib/Modeler'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import {
  listDefs, deployDef, delDef, changeDefStatus, getDefNodes, saveDefNodes,
  todoTasks, doneTasks, myInstances, approveTask, rejectTask, delegateTask, instanceTrace, startInstance
} from '@/api/workflow'

const activeTab = ref('def')
const defs = ref([]); const loading = ref(false)
const todoList = ref([]); const doneList = ref([]); const myList = ref([])

const deployVisible = ref(false)
const deployForm = reactive({ processKey: '', processName: '', formKey: '', bpmnXml: '' })
const designerVisible = ref(false); const bpmnCanvas = ref()
let modeler = null
const nodesVisible = ref(false); const nodes = ref([]); const nodeId = ref(null)
const startVisible = ref(false); const startForm = reactive({ processKey: '', businessId: '1', businessTitle: '' })
const traceVisible = ref(false); const traceLogs = ref([]); const stepsActive = ref(0)

const BPMN_TEMPLATE = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn2:definitions xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="defs_PROCESS_KEY" targetNamespace="http://sudutech.com/bpmn">
  <bpmn2:process id="PROCESS_KEY" name="流程名称" isExecutable="true">
    <bpmn2:startEvent id="StartEvent_1" name="开始"/>
    <bpmn2:userTask id="Activity_1" name="审批节点"/>
    <bpmn2:endEvent id="EndEvent_1" name="结束"/>
    <bpmn2:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="Activity_1"/>
    <bpmn2:sequenceFlow id="Flow_2" sourceRef="Activity_1" targetRef="EndEvent_1"/>
  </bpmn2:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="PROCESS_KEY">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="160" y="100" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1_di" bpmnElement="Activity_1">
        <dc:Bounds x="250" y="78" width="100" height="80"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
        <dc:Bounds x="410" y="100" width="36" height="36"/>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1">
        <di:waypoint x="196" y="118"/>
        <di:waypoint x="250" y="118"/>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_2_di" bpmnElement="Flow_2">
        <di:waypoint x="350" y="118"/>
        <di:waypoint x="410" y="118"/>
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn2:definitions>`

const statusMap = { DRAFT: ['info', '草稿'], RUNNING: ['primary', '审批中'], APPROVED: ['success', '已通过'], REJECTED: ['danger', '已驳回'], CANCELED: ['info', '已取消'] }
const statusText = (s) => (statusMap[s] || [])[1] || s
const statusType = (s) => (statusMap[s] || ['info'])[0]

async function loadAll() {
  loading.value = true
  try {
    const [d, t, dn, m] = await Promise.all([
      listDefs({}), todoTasks({ pageNum: 1, pageSize: 50 }), doneTasks({ pageNum: 1, pageSize: 50 }), myInstances({ pageNum: 1, pageSize: 50 })
    ])
    defs.value = d.data
    todoList.value = t.rows
    doneList.value = dn.rows
    myList.value = m.rows
  } finally { loading.value = false }
}

function openDeploy() {
  Object.assign(deployForm, { processKey: '', processName: '', formKey: '', bpmnXml: '' })
  deployVisible.value = true
}
function useTemplate() {
  deployForm.bpmnXml = BPMN_TEMPLATE.replace('PROCESS_KEY', deployForm.processKey || 'my-process')
}
async function submitDeploy() {
  if (!deployForm.processKey || !deployForm.bpmnXml) { ElMessage.warning('流程标识与 XML 必填'); return }
  await deployDef({ ...deployForm })
  ElMessage.success('部署成功')
  deployVisible.value = false
  loadAll()
}

async function openDesign(row) {
  const xml = row.id ? await getDefXmlSafe(row) : null
  deployForm.processKey = row.processKey
  deployForm.processName = row.processName
  designerVisible.value = true
  await nextTick()
  if (modeler) { modeler.destroy(); modeler = null }
  modeler = new Modeler({ container: bpmnCanvas.value, keyboard: { bindTo: document } })
  try {
    await modeler.importXML(xml || BPMN_TEMPLATE.replace('PROCESS_KEY', row.processKey || 'my-process'))
  } catch (e) {
    await modeler.importXML(BPMN_TEMPLATE.replace('PROCESS_KEY', row.processKey || 'my-process'))
  }
}

async function getDefXmlSafe(row) {
  try {
    const { getDefXml } = await import('@/api/workflow')
    const res = await getDefXml(row.id)
    return res.data
  } catch (e) { return null }
}

async function saveDesignerXml() {
  if (!modeler) return
  const { xml } = await modeler.saveXML({ format: true })
  await deployDef({
    processKey: deployForm.processKey,
    processName: deployForm.processName,
    formKey: deployForm.formKey,
    bpmnXml: xml
  })
  ElMessage.success('已保存并部署新版本')
  designerVisible.value = false
  loadAll()
}

onBeforeUnmount(() => { if (modeler) modeler.destroy() })

async function openNodes(row) {
  nodeId.value = row.id
  const res = await getDefNodes(row.id)
  nodes.value = res.data || []
  nodesVisible.value = true
}
async function submitNodes() {
  await saveDefNodes(nodeId.value, nodes.value)
  ElMessage.success('节点配置已保存')
  nodesVisible.value = false
  loadAll()
}
async function toggleStatus(row) {
  await changeDefStatus({ id: row.id, status: row.status === '0' ? '1' : '0' })
  ElMessage.success('操作成功'); loadAll()
}
function startFlow(row) {
  startForm.processKey = row.processKey
  startForm.businessTitle = row.processName + '-' + new Date().toLocaleString()
  startVisible.value = true
}
async function submitStart() {
  await startInstance({ ...startForm, formData: '{}' })
  ElMessage.success('流程已发起，请在「待办任务」中查看')
  startVisible.value = false
  loadAll()
}
function handleDelete(row) {
  ElMessageBox.confirm(`确认删除流程「${row.processName || row.processKey}」？`, '提示', { type: 'warning' }).then(async () => {
    await delDef(row.id); ElMessage.success('删除成功'); loadAll()
  })
}
async function openTrace(row) {
  const res = await instanceTrace(row.processInstanceId)
  traceLogs.value = res.data.logs || []
  stepsActive.value = traceLogs.value.length
  traceVisible.value = true
}

/** 待办/已办表格子组件 */
const TaskTable = defineComponent({
  props: { tasks: Array, type: String },
  emits: ['refresh'],
  setup(props, { emit }) {
    const comment = ref('')
    const currentTask = ref(null)
    const doApprove = async (task, pass) => {
      const { value } = await ElMessageBox.prompt(pass ? '审批意见' : '驳回原因', pass ? '通过' : '驳回', {
        inputValue: pass ? '同意' : '不同意', confirmButtonText: '确定', cancelButtonText: '取消'
      })
      if (pass) { await approveTask({ taskId: task.id, comment: value }) }
      else { await rejectTask({ taskId: task.id, comment: value }) }
      ElMessage.success('操作成功')
      emit('refresh')
    }
    const doDelegate = async (task) => {
      const { value } = await ElMessageBox.prompt('委派给用户ID', '委派', { confirmButtonText: '确定' })
      await delegateTask({ taskId: task.id, targetUserId: value })
      ElMessage.success('委派成功')
      emit('refresh')
    }
    return () => h('div', { class: 'table-card' }, [
      h('table', { class: 'wf-table' }, [
        h('thead', {}, [h('tr', {}, [
          h('th', {}, '任务ID'), h('th', {}, '标题'), h('th', {}, '节点'),
          h('th', {}, props.type === 'todo' ? '发起人' : '办理人'),
          h('th', {}, props.type === 'todo' ? '操作' : '结果/意见')
        ])]),
        h('tbody', {}, (props.tasks || []).map((t) => h('tr', { key: t.id }, [
          h('td', {}, String(t.id)),
          h('td', {}, t.businessTitle || ''),
          h('td', {}, t.nodeName || t.nodeKey || ''),
          h('td', {}, props.type === 'todo' ? (t.applicantName || '') : (t.assigneeName || '')),
          h('td', {}, props.type === 'todo'
            ? [
                h('button', { class: 'mini-btn primary', onClick: () => doApprove(t, true) }, '通过'),
                h('button', { class: 'mini-btn danger', onClick: () => doApprove(t, false) }, '驳回'),
                h('button', { class: 'mini-btn', onClick: () => doDelegate(t) }, '委派')
              ]
            : `${t.result === 'APPROVE' ? '通过' : '驳回'} · ${t.comment || ''}`)
        ])))
      ]),
      (props.tasks || []).length === 0 ? h('p', { style: 'color:#909399;text-align:center;padding:20px' }, '暂无任务') : null
    ])
  }
})

onMounted(loadAll)
</script>

<style scoped>
.designer-wrap { display: flex; gap: 10px; height: 66vh; }
.bpmn-canvas { flex: 1; border: 1px solid #dcdfe6; border-radius: 4px; }
.designer-side { width: 220px; }
:deep(.wf-table) { width: 100%; border-collapse: collapse; font-size: 13px; }
:deep(.wf-table th), :deep(.wf-table td) { border: 1px solid #ebeef5; padding: 9px 12px; text-align: left; }
:deep(.wf-table th) { background: #f5f7fa; }
:deep(.mini-btn) { border: 1px solid #dcdfe6; background: #fff; border-radius: 4px; padding: 4px 10px; cursor: pointer; margin-right: 6px; font-size: 12px; }
:deep(.mini-btn.primary) { color: #409eff; border-color: #b3d8ff; }
:deep(.mini-btn.danger) { color: #f56c6c; border-color: #fab6b6; }
</style>
