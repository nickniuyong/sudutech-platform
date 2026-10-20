import request from '@/utils/request'

export const login = (data) => request.post('/api/v1/auth/login', data)
export const logout = () => request.post('/api/v1/auth/logout')
export const getUserInfo = () => request.get('/api/v1/auth/userinfo')
