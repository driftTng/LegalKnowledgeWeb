<template>
  <div class="home">
    <!-- 搜索区域 -->
    <div class="search-area">
      <h1>法律法规检索</h1>
      <p class="subtitle">查你想看的法律条文，支持标题搜索、分类浏览、PDF 下载</p>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索法规名称，如：劳动法、劳动合同…"
          size="large"
          clearable
          @keyup.enter="doSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" size="large" @click="doSearch">搜索</el-button>
      </div>
    </div>

    <!-- 分类标签 -->
    <div class="category-bar" v-if="categories.length > 0">
      <el-tag
        v-for="cat in categories"
        :key="cat"
        :type="selectedCategory === cat ? 'primary' : 'info'"
        :effect="selectedCategory === cat ? 'dark' : 'plain'"
        class="category-tag"
        @click="selectCategory(cat)"
      >
        {{ cat }}
      </el-tag>
      <el-tag
        v-if="selectedCategory"
        type="warning"
        class="category-tag"
        @click="selectCategory('')"
      >
        清除筛选
      </el-tag>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- 空状态 -->
    <div v-else-if="documents.length === 0" class="empty">
      <el-empty :description="keyword ? `未找到包含「${keyword}」的法规` : '暂无法规数据'" />
    </div>

    <!-- 法规列表 -->
    <div v-else class="document-list">
      <div class="result-info">
        共 {{ documents.length }} 条结果
      </div>
      <el-card
        v-for="doc in documents"
        :key="doc.id"
        class="doc-card"
        shadow="hover"
        @click="$router.push(`/document/${doc.id}`)"
      >
        <div class="doc-header">
          <h3 class="doc-title">{{ doc.title }}</h3>
          <el-tag size="small" :type="statusType(doc.status)">{{ doc.status }}</el-tag>
        </div>
        <div class="doc-meta">
          <span v-if="doc.issuingAuthority">
            <el-icon><OfficeBuilding /></el-icon> {{ doc.issuingAuthority }}
          </span>
          <span v-if="doc.documentNumber">
            <el-icon><Document /></el-icon> {{ doc.documentNumber }}
          </span>
          <span v-if="doc.publishDate">
            <el-icon><Calendar /></el-icon> {{ doc.publishDate }} 发布
          </span>
          <span v-if="doc.effectiveDate">
            <el-icon><Clock /></el-icon> {{ doc.effectiveDate }} 实施
          </span>
          <span v-if="doc.level">
            <el-tag size="small" type="success">{{ doc.level }}</el-tag>
          </span>
        </div>
        <div class="doc-preview">
          {{ doc.content?.slice(0, 150).replace(/\n/g, ' ') }}...
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDocumentsApi, getCategoriesApi } from '../api/document'

const keyword = ref('')
const documents = ref([])
const categories = ref([])
const selectedCategory = ref('')
const loading = ref(true)

onMounted(async () => {
  try {
    const [docsRes, catsRes] = await Promise.all([
      getDocumentsApi({}),
      getCategoriesApi(),
    ])
    documents.value = docsRes.data
    categories.value = catsRes.data
  } catch (e) {
    console.error('加载法规数据失败', e)
  } finally {
    loading.value = false
  }
})

async function doSearch() {
  loading.value = true
  try {
    const params = {}
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (selectedCategory.value) params.category = selectedCategory.value
    const res = await getDocumentsApi(params)
    documents.value = res.data
  } finally {
    loading.value = false
  }
}

async function selectCategory(cat) {
  selectedCategory.value = cat
  keyword.value = ''
  doSearch()
}

function statusType(status) {
  if (status === '现行有效') return 'success'
  if (status === '已修改') return 'warning'
  if (status === '已废止') return 'danger'
  return 'info'
}
</script>

<style scoped>
.search-area {
  text-align: center;
  padding: 48px 0 32px;
}

.search-area h1 {
  font-size: 32px;
  color: #303133;
  margin-bottom: 8px;
}

.subtitle {
  color: #909399;
  font-size: 15px;
  margin-bottom: 24px;
}

.search-bar {
  display: flex;
  gap: 12px;
  max-width: 600px;
  margin: 0 auto;
}

.search-bar .el-input { flex: 1; }

.category-bar {
  display: flex;
  gap: 10px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 24px;
  padding: 0 20px;
}

.category-tag {
  cursor: pointer;
  padding: 6px 16px;
  font-size: 14px;
}

.loading { max-width: 800px; margin: 0 auto; }

.empty { margin-top: 40px; }

.document-list {
  max-width: 860px;
  margin: 0 auto;
}

.result-info {
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}

.doc-card {
  cursor: pointer;
  margin-bottom: 12px;
  transition: transform 0.2s;
}

.doc-card:hover { transform: translateY(-2px); }

.doc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.doc-title {
  font-size: 17px;
  color: #303133;
}

.doc-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
  align-items: center;
}

.doc-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.doc-preview {
  color: #a8abb2;
  font-size: 12px;
  line-height: 1.5;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
