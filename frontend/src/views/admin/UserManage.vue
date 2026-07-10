<template>
  <div>
    <div class="page-header">
      <h2>用户权限管理</h2>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="6" animated />
    </div>

    <el-empty v-else-if="users.length === 0" description="暂无用户" />

    <el-table v-else :data="users" stripe class="user-table">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="140" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="role" label="角色" width="120">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
            {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-popconfirm
            v-if="row.role === 'USER'"
            title="确定将该用户设为管理员？"
            @confirm="setRole(row.id, 'ADMIN')"
          >
            <template #reference>
              <el-button size="small" type="warning" text>设为管理员</el-button>
            </template>
          </el-popconfirm>
          <el-popconfirm
            v-else
            title="确定取消该用户的管理员权限？"
            @confirm="setRole(row.id, 'USER')"
          >
            <template #reference>
              <el-button size="small" type="primary" text>取消管理员</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsersApi, updateUserRoleApi } from '../../api/admin'
import { ElMessage } from 'element-plus'

const users = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getUsersApi()
    users.value = res.data
  } finally {
    loading.value = false
  }
})

async function setRole(userId, role) {
  try {
    const res = await updateUserRoleApi(userId, role)
    // 更新列表中的角色
    const user = users.value.find(u => u.id === userId)
    if (user) user.role = res.data.role
    ElMessage.success(role === 'ADMIN' ? '已设为管理员' : '已取消管理员权限')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  }
}
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  color: #303133;
}

.loading {
  max-width: 600px;
}

.user-table {
  background: #fff;
}
</style>
