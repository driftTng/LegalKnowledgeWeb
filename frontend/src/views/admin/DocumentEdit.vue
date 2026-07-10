<template>
  <div class="document-edit">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑法规' : '新增法规' }}</h2>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="8" animated />
    </div>

    <el-form
      v-else
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      class="edit-form"
    >
      <el-form-item label="法规名称" prop="title">
        <el-input v-model="form.title" placeholder="如：中华人民共和国劳动法" />
      </el-form-item>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="文号">
            <el-input v-model="form.documentNumber" placeholder="如：主席令第28号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发布机关">
            <el-input v-model="form.issuingAuthority" placeholder="如：全国人民代表大会常务委员会" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="分类">
            <el-input v-model="form.category" placeholder="如：劳动法" />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="法规层级">
            <el-select v-model="form.level" placeholder="选择层级" style="width: 100%">
              <el-option label="法律" value="法律" />
              <el-option label="行政法规" value="行政法规" />
              <el-option label="司法解释" value="司法解释" />
              <el-option label="部门规章" value="部门规章" />
              <el-option label="地方性法规" value="地方性法规" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="时效性">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="现行有效" value="现行有效" />
              <el-option label="已修改" value="已修改" />
              <el-option label="已废止" value="已废止" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="发布日期">
            <el-date-picker v-model="form.publishDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="实施日期">
            <el-date-picker v-model="form.effectiveDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="法规正文" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="18"
          placeholder="在此输入法规全文，可使用 Ctrl+V 直接粘贴。建议按章节分段，每章前加「第X章 标题」"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
        <el-button @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDocumentApi } from '../../api/document'
import { createDocumentApi, updateDocumentApi } from '../../api/admin'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

const isEdit = computed(() => !!route.params.id)
const docId = computed(() => route.params.id ? Number(route.params.id) : null)

const form = ref({
  title: '',
  documentNumber: '',
  issuingAuthority: '',
  category: '',
  level: '法律',
  status: '现行有效',
  publishDate: '',
  effectiveDate: '',
  content: '',
})

const rules = {
  title: [{ required: true, message: '请输入法规名称', trigger: 'blur' }],
  content: [{ required: true, message: '请输入法规正文', trigger: 'blur' }],
}

onMounted(async () => {
  if (isEdit.value) {
    loading.value = true
    try {
      const res = await getDocumentApi(docId.value)
      Object.assign(form.value, res.data)
    } finally {
      loading.value = false
    }
  }
})

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateDocumentApi(docId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createDocumentApi(form.value)
      ElMessage.success('创建成功')
    }
    router.push('/admin')
  } catch (err) {
    ElMessage.error(err.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 20px; color: #303133; }

.edit-form {
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  max-width: 900px;
}

.loading { max-width: 600px; }
</style>
