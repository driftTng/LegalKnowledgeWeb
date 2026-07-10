import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')

  const isLoggedIn = computed(() => !!accessToken.value)
  const isAdmin = computed(() => role.value === 'ADMIN')

  function setTokens(access, refresh) {
    accessToken.value = access
    refreshToken.value = refresh
    localStorage.setItem('accessToken', access)
    localStorage.setItem('refreshToken', refresh)
  }

  function setUser(name, userRole) {
    username.value = name
    role.value = userRole
    localStorage.setItem('username', name)
    localStorage.setItem('role', userRole)
  }

  function loginSuccess(data) {
    setTokens(data.accessToken, data.refreshToken)
    setUser(data.username, data.role)
  }

  function logout() {
    accessToken.value = ''
    refreshToken.value = ''
    username.value = ''
    role.value = ''
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
  }

  return { accessToken, refreshToken, username, role, isLoggedIn, isAdmin, loginSuccess, logout, setTokens }
})
