<template>
  <div>
    <div class="page-header">
      <h2>法规管理</h2>
      <el-button type="primary" @click="$router.push('/admin/edit')">
        <el-icon><Plus /></el-icon> 新增法规
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索法规名称..." clearable @input="doSearch" style="width: 360px">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <el-empty v-else-if="documents.length === 0" description="暂无法规数据" />

    <el-table v-else :data="documents" stripe class="doc-table">
      <el-table-column prop="title" label="法规名称" min-width="240" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="level" label="层级" width="100" />
      <el-table-column prop="publishDate" label="发布日期" width="120" />
      <el-table-column prop="status" label="时效性" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" text @click="editDoc(row.id)">编辑</el-button>
          <el-popconfirm title="确定删除？" @confirm="deleteDoc(row.id)">
            <template #reference>
              <el-button size="small" type="danger" text>删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getDocumentsApi } from '../../api/document'
import { deleteDocumentApi } from '../../api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()
const documents = ref([])
const keyword = ref('')
const loading = ref(true)

onMounted(() => loadDocs())

async function loadDocs() {
  loading.value = true
  try {
    const params = keyword.value ? { keyword: keyword.value } : {}
    const res = await getDocumentsApi(params)
    documents.value = res.data
  } finally {
    loading.value = false
  }
}

function doSearch() {
  // 简单的防抖
  clearTimeout(doSearch._timer)
  doSearch._timer = setTimeout(() => loadDocs(), 300)
}

function editDoc(id) {
  router.push(`/admin/edit/${id}`)
}

async function deleteDoc(id) {
  try {
    await deleteDocumentApi(id)
    ElMessage.success('删除成功')
    loadDocs()
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '删除失败')
  }
}

function statusType(s) {
  if (s === '现行有效') return 'success'
  if (s === '已修改') return 'warning'
  if (s === '已废止') return 'danger'
  return 'info'
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 { font-size: 20px; color: #303133; }

.search-bar { margin-bottom: 16px; }

.loading { max-width: 600px; }

.doc-table { background: #fff; }
</style>
