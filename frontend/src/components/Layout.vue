<template>
  <div class="layout">
    <el-header class="header">
      <div class="header-left" @click="$router.push('/')">
        <el-icon :size="28"><Reading /></el-icon>
        <span class="logo-text">普法知识网</span>
      </div>
      <div class="header-nav">
        <el-button text @click="$router.push('/')">
          <el-icon><Search /></el-icon> 法规检索
        </el-button>
        <el-button text @click="$router.push('/chat')">
          <el-icon><ChatDotRound /></el-icon> AI 法律咨询
        </el-button>
        <el-button v-if="userStore.isAdmin" text type="warning" @click="$router.push('/admin')">
          <el-icon><Setting /></el-icon> 后台管理
        </el-button>
      </div>
      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <span class="welcome">你好，{{ userStore.username }}</span>
          <el-button type="danger" text @click="handleLogout">退出登录</el-button>
        </template>
        <template v-else>
          <el-button text @click="$router.push('/login')">登录</el-button>
          <el-button type="primary" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>

    <el-footer class="footer">
      <p>© 2024 普法知识网 —— 学习法律知识，维护合法权益</p>
    </el-footer>
  </div>
</template>

<script setup>
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 40px; height: 60px;
  position: sticky; top: 0; z-index: 100;
}

.header-left {
  display: flex; align-items: center; gap: 8px;
  cursor: pointer; color: #409eff;
}

.logo-text { font-size: 20px; font-weight: 700; color: #303133; }

.header-nav { display: flex; align-items: center; gap: 4px; }

.header-right { display: flex; align-items: center; gap: 12px; }

.welcome { color: #606266; font-size: 14px; }

.main { flex: 1; max-width: 1200px; width: 100%; margin: 0 auto; padding: 24px 20px; }

.footer { text-align: center; padding: 20px; color: #909399; font-size: 13px; }
</style>
