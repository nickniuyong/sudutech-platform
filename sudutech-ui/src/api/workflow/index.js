import request from '@/utils/request'

export const listDefs = (params) => request.get('/api/v1/wf/definition/list', { params })
export const deployDef = (data) => request.post('/api/v1/wf/definition/deploy', data)
export const getDefXml = (id) => request.get(`/api/v1/wf/definition/${id}/xml`)
export const delDef = (id) => request.delete(`/api/v1/wf/definition/${id}`)
export const changeDefStatus = (data) => request.put('/api/v1/wf/definition/status', data)
export const getDefNodes = (id) => request.get(`/api/v1/wf/definition/${id}/nodes`)
export const saveDefNodes = (id, nodes) => request.post(`/api/v1/wf/definition/${id}/nodes`, nodes)

export const listWfForms = (params) => request.get('/api/v1/wf/form/list', { params })
export const getWfForm = (id) => request.get(`/api/v1/wf/form/${id}`)
export const saveWfForm = (data) => (data.id ? request.put('/api/v1/wf/form', data) : request.post('/api/v1/wf/form', data))
export const delWfForm = (id) => request.delete(`/api/v1/wf/form/${id}`)

export const startInstance = (data) => request.post('/api/v1/wf/instance/start', data)
export const myInstances = (params) => request.get('/api/v1/wf/instance/my', { params })
export const todoTasks = (params) => request.get('/api/v1/wf/task/todo', { params })
export const doneTasks = (params) => request.get('/api/v1/wf/task/done', { params })
export const approveTask = (data) => request.post('/api/v1/wf/task/approve', data)
export const rejectTask = (data) => request.post('/api/v1/wf/task/reject', data)
export const delegateTask = (data) => request.post('/api/v1/wf/task/delegate', data)
export const instanceTrace = (instanceId) => request.get(`/api/v1/wf/instance/${instanceId}/trace`)
