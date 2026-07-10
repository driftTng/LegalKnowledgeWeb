<template>
  <div class="admin-layout">
    <div class="admin-sidebar">
      <h3>后台管理</h3>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/admin">
          <el-icon><Document /></el-icon>
          <span>法规管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/edit">
          <el-icon><Plus /></el-icon>
          <span>新增法规</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><UserFilled /></el-icon>
          <span>用户权限</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button text @click="$router.push('/')">
          <el-icon><ArrowLeft /></el-icon> 返回前台
        </el-button>
      </div>
    </div>
    <div class="admin-main">
      <router-view />
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: calc(100vh - 120px);
  margin: -24px -20px;
}

.admin-sidebar {
  width: 200px;
  background: #304156;
  display: flex;
  flex-direction: column;
}

.admin-sidebar h3 {
  color: #fff;
  text-align: center;
  padding: 20px 0;
  font-size: 16px;
  border-bottom: 1px solid #3a4b60;
}

.sidebar-footer {
  margin-top: auto;
  padding: 16px;
  text-align: center;
}

.sidebar-footer .el-button { color: #bfcbd9; }

.admin-main {
  flex: 1;
  background: #f0f2f5;
  padding: 24px;
}
</style>
