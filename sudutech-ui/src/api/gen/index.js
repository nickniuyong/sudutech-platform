import request from '@/utils/request'

export const dbTables = (params) => request.get('/api/v1/gen/db/list', { params })
export const importTables = (tables) => request.post('/api/v1/gen/table/sync', { tables })
export const listGenTables = (params) => request.get('/api/v1/gen/table/list', { params })
export const getGenTable = (id) => request.get(`/api/v1/gen/table/${id}`)
export const updateGenTable = (data) => request.put('/api/v1/gen/table', data)
export const delGenTables = (ids) => request.delete(`/api/v1/gen/table/${ids}`)
export const previewGen = (id) => request.get(`/api/v1/gen/preview/${id}`)
export const batchGenCode = (tableIds) =>
  request.post('/api/v1/gen/batchGenCode', { tableIds }, { responseType: 'blob' })
