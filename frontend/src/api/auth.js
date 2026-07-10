import request from './request'

export function loginApi(username, password) {
  return request.post('/auth/login', { username, password })
}

export function registerApi(username, email, password) {
  return request.post('/auth/register', { username, email, password })
}

export function refreshTokenApi(refreshToken) {
  return request.post('/auth/refresh', { refreshToken })
}
