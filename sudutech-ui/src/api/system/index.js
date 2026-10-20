import request from '@/utils/request'

export const listRoles = (params) => request.get('/api/v1/system/role/list', { params })
export const getRole = (id) => request.get(`/api/v1/system/role/${id}`)
export const allRoles = () => request.get('/api/v1/system/role/all')
export const roleMenuTree = () => request.get('/api/v1/system/role/menuTree')
export const addRole = (data) => request.post('/api/v1/system/role', data)
export const updateRole = (data) => request.put('/api/v1/system/role', data)
export const delRoles = (ids) => request.delete(`/api/v1/system/role/${ids}`)
export const grantRoleMenu = (data) => request.put('/api/v1/system/role/grantMenu', data)
export const changeRoleStatus = (data) => request.put('/api/v1/system/role/changeStatus', data)

export const listMenus = (params) => request.get('/api/v1/system/menu/tree', { params })
export const getMenu = (id) => request.get(`/api/v1/system/menu/${id}`)
export const addMenu = (data) => request.post('/api/v1/system/menu', data)
export const updateMenu = (data) => request.put('/api/v1/system/menu', data)
export const delMenu = (id) => request.delete(`/api/v1/system/menu/${id}`)

export const deptTree = (params) => request.get('/api/v1/system/dept/tree', { params })
export const getDept = (id) => request.get(`/api/v1/system/dept/${id}`)
export const addDept = (data) => request.post('/api/v1/system/dept', data)
export const updateDept = (data) => request.put('/api/v1/system/dept', data)
export const delDept = (id) => request.delete(`/api/v1/system/dept/${id}`)

export const listPosts = (params) => request.get('/api/v1/system/post/list', { params })
export const getPost = (id) => request.get(`/api/v1/system/post/${id}`)
export const addPost = (data) => request.post('/api/v1/system/post', data)
export const updatePost = (data) => request.put('/api/v1/system/post', data)
export const delPosts = (ids) => request.delete(`/api/v1/system/post/${ids}`)
