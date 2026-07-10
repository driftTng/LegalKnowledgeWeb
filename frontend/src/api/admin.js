import request from './request'

// 法规管理
export function createDocumentApi(data) { return request.post('/admin/documents', data) }
export function updateDocumentApi(id, data) { return request.put(`/admin/documents/${id}`, data) }
export function deleteDocumentApi(id) { return request.delete(`/admin/documents/${id}`) }

// 用户管理
export function getUsersApi() { return request.get('/admin/users') }
export function updateUserRoleApi(id, role) { return request.put(`/admin/users/${id}/role`, { role }) }
