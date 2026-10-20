import request from '@/utils/request'

export const listUsers = (params) => request.get('/api/v1/system/user/list', { params })
export const getUser = (id) => request.get(`/api/v1/system/user/${id}`)
export const getUserInit = () => request.get('/api/v1/system/user/init')
export const addUser = (data) => request.post('/api/v1/system/user', data)
export const updateUser = (data) => request.put('/api/v1/system/user', data)
export const delUsers = (ids) => request.delete(`/api/v1/system/user/${ids}`)
export const resetPwd = (data) => request.put('/api/v1/system/user/resetPwd', data)
export const changeUserStatus = (data) => request.put('/api/v1/system/user/changeStatus', data)
