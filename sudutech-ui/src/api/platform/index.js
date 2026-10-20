import request from '@/utils/request'

export const listProjects = (params) => request.get('/api/v1/screen/project/list', { params })
export const getProject = (id) => request.get(`/api/v1/screen/project/${id}`)
export const addProject = (data) => request.post('/api/v1/screen/project', data)
export const updateProject = (data) => request.put('/api/v1/screen/project', data)
export const delProjects = (ids) => request.delete(`/api/v1/screen/project/${ids}`)
export const publishProject = (id) => request.post(`/api/v1/screen/publish/${id}`)

export const getPage = (projectId) => request.get(`/api/v1/screen/page/${projectId}`)
export const savePage = (data) => request.put('/api/v1/screen/page', data)
export const componentList = () => request.get('/api/v1/screen/component/list')

export const listScreenDatasets = (params) => request.get('/api/v1/screen/dataset/list', { params })
export const saveScreenDataset = (data) => request.post('/api/v1/screen/dataset', data)
export const delScreenDatasets = (ids) => request.delete(`/api/v1/screen/dataset/${ids}`)
export const previewDataset = (data) => request.post('/api/v1/screen/dataset/preview', data)

export const listView = (publishUrl) => request.get(`/api/v1/screen/view/${publishUrl}`)

export const listReportPages = (params) => request.get('/api/v1/report/page/list', { params })
export const saveReportPage = (data) => request.post('/api/v1/report/page', data)
export const delReportPages = (ids) => request.delete(`/api/v1/report/page/${ids}`)
export const listReportDatasets = (params) => request.get('/api/v1/report/dataset/list', { params })
export const saveReportDataset = (data) => request.post('/api/v1/report/dataset', data)
export const executeDataset = (data) => request.post('/api/v1/report/dataset/execute', data)
export const delReportDatasets = (ids) => request.delete(`/api/v1/report/dataset/${ids}`)
export const listCharts = (params) => request.get('/api/v1/report/chart/list', { params })
export const saveChart = (data) => request.post('/api/v1/report/chart', data)
export const delCharts = (ids) => request.delete(`/api/v1/report/chart/${ids}`)

export const listDatasources = (params) => request.get('/api/v1/ai/datasource/list', { params })
export const saveDatasource = (data) => request.post('/api/v1/ai/datasource', data)
export const delDatasources = (ids) => request.delete(`/api/v1/ai/datasource/${ids}`)
export const testDatasource = (data) => request.post('/api/v1/ai/datasource/test', data)
export const refreshSchema = (id) => request.post(`/api/v1/ai/schema/refresh/${id}`)
export const schemaList = (id) => request.get(`/api/v1/ai/schema/list/${id}`)

export const listSessions = (params) => request.get('/api/v1/ai/chat/session/list', { params })
export const createSession = (data) => request.post('/api/v1/ai/chat/session', data)
export const delSession = (id) => request.delete(`/api/v1/ai/chat/session/${id}`)
export const listMessages = (sessionId) => request.get(`/api/v1/ai/chat/message/${sessionId}`)
export const sendChat = (data) => request.post('/api/v1/ai/chat/message', data)
