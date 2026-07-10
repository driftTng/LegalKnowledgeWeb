<template>
  <div class="document-detail">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>

    <template v-else-if="document">
      <!-- 面包屑 -->
      <div class="breadcrumb">
        <el-button text @click="$router.push('/')">
          <el-icon><ArrowLeft /></el-icon> 返回列表
        </el-button>
      </div>

      <!-- 标题区 -->
      <div class="doc-header">
        <h1>{{ document.title }}</h1>
        <div class="header-actions">
          <el-button type="primary" @click="downloadPdf">
            <el-icon><Download /></el-icon> 下载 PDF
          </el-button>
        </div>
      </div>

      <!-- 元信息 -->
      <el-descriptions :column="3" border size="small" class="doc-meta">
        <el-descriptions-item v-if="document.documentNumber" label="文号">
          {{ document.documentNumber }}
        </el-descriptions-item>
        <el-descriptions-item v-if="document.issuingAuthority" label="发布机关">
          {{ document.issuingAuthority }}
        </el-descriptions-item>
        <el-descriptions-item v-if="document.level" label="法规层级">
          <el-tag size="small">{{ document.level }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="document.category" label="分类">
          <el-tag size="small" type="warning">{{ document.category }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="document.publishDate" label="发布日期">
          {{ document.publishDate }}
        </el-descriptions-item>
        <el-descriptions-item v-if="document.effectiveDate" label="实施日期">
          {{ document.effectiveDate }}
        </el-descriptions-item>
        <el-descriptions-item v-if="document.status" label="时效性">
          <el-tag size="small" :type="statusType">{{ document.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <!-- 正文 -->
      <div class="doc-content">
        <div
          v-for="(para, i) in paragraphs"
          :key="i"
          class="content-paragraph"
          :class="{ 'paragraph-speaking': speakingIndex === i }"
        >
          {{ para }}
        </div>
      </div>
    </template>

    <div v-else class="empty">
      <el-empty description="法规不存在" />
    </div>

    <!-- 底部朗读控制条 -->
    <div v-if="document && ttsSupported" class="audio-bar">
      <div class="audio-bar-inner">
        <el-button-group class="play-controls">
          <el-button
            v-if="!isSpeaking"
            type="primary"
            @click="startSpeaking"
            :icon="VideoPlay"
          >
            朗读全文
          </el-button>
          <el-button
            v-else-if="isPaused"
            type="primary"
            @click="resumeSpeaking"
            :icon="VideoPlay"
          >
            继续
          </el-button>
          <el-button
            v-else
            @click="pauseSpeaking"
          >
            <el-icon><VideoPause /></el-icon> 暂停
          </el-button>
          <el-button
            v-if="isSpeaking"
            type="danger"
            @click="stopSpeaking"
          >
            <el-icon><Close /></el-icon> 停止
          </el-button>
        </el-button-group>

        <div class="speed-control">
          <span class="speed-label">语速</span>
          <el-slider
            v-model="rate"
            :min="0.5"
            :max="2"
            :step="0.1"
            :marks="rateMarks"
            style="width: 120px"
            @change="updateRate"
          />
        </div>

        <div v-if="isSpeaking" class="speaking-status">
          <span class="pulse"></span>
          正在朗读...
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { getDocumentApi, downloadDocumentPdf } from '../api/document'
import { VideoPlay, VideoPause } from '@element-plus/icons-vue'

const route = useRoute()
const documentRef = ref(null)
const loading = ref(true)

const document = ref(null)

// TTS 状态
const ttsSupported = ref(typeof window !== 'undefined' && 'speechSynthesis' in window)
const isSpeaking = ref(false)
const isPaused = ref(false)
const speakingIndex = ref(-1)
const rate = ref(0.9)
const rateMarks = { 0.5: '0.5x', 0.9: '正常', 1.5: '1.5x', 2: '2x' }

let utteranceIndex = 0
let utteranceQueue = []

const paragraphs = computed(() => {
  if (!document.value?.content) return []
  return document.value.content
    .split('\n')
    .map(p => p.trim())
    .filter(p => p)
})

const statusType = computed(() => {
  const s = document.value?.status
  if (s === '现行有效') return 'success'
  if (s === '已修改') return 'warning'
  if (s === '已废止') return 'danger'
  return 'info'
})

function downloadPdf() {
  window.open(downloadDocumentPdf(document.value.id), '_blank')
}

// ── 朗读逻辑 ──

function startSpeaking() {
  if (!ttsSupported.value) return
  speechSynthesis.cancel()         // 清空之前残留
  utteranceIndex = 0
  utteranceQueue = []
  isSpeaking.value = true
  isPaused.value = false
  speakNext()
}

function pauseSpeaking() {
  speechSynthesis.pause()
  isPaused.value = true
}

function resumeSpeaking() {
  speechSynthesis.resume()
  isPaused.value = false
}

function stopSpeaking() {
  speechSynthesis.cancel()
  isSpeaking.value = false
  isPaused.value = false
  speakingIndex.value = -1
  utteranceQueue = []
}

function updateRate() {
  // 语速变了就重新读
  if (isSpeaking.value) {
    stopSpeaking()
    startSpeaking()
  }
}

/** 取下一个段落，创建 utterance，朗读 */
function speakNext() {
  if (utteranceIndex >= paragraphs.value.length) {
    // 全部读完
    isSpeaking.value = false
    isPaused.value = false
    speakingIndex.value = -1
    return
  }

  const text = paragraphs.value[utteranceIndex]
  if (!text) {
    utteranceIndex++
    speakNext()
    return
  }

  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  utterance.rate = rate.value
  utterance.volume = 1

  utterance.onstart = () => {
    speakingIndex.value = utteranceIndex
  }

  utterance.onend = () => {
    utteranceIndex++
    speakNext()
  }

  utterance.onerror = (e) => {
    // 忽略 interrupted 错误（用户手动停止触发的）
    if (e.error !== 'interrupted') {
      console.warn('TTS error:', e.error)
    }
    if (isSpeaking.value) {
      utteranceIndex++
      speakNext()
    }
  }

  speechSynthesis.speak(utterance)
}

onMounted(async () => {
  try {
    const res = await getDocumentApi(route.params.id)
    document.value = res.data
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  speechSynthesis.cancel()
})
</script>

<style scoped>
.document-detail {
  max-width: 900px;
  margin: 0 auto;
  padding-bottom: 80px; /* 给底部控制条留空间 */
}

.loading { margin-top: 40px; }

.breadcrumb { margin-bottom: 16px; }

.doc-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 20px;
}

.doc-header h1 {
  font-size: 26px;
  color: #303133;
  line-height: 1.4;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.doc-meta { margin-bottom: 8px; }

.doc-content {
  line-height: 2;
  font-size: 15px;
  color: #303133;
}

.content-paragraph {
  margin-bottom: 12px;
  text-indent: 2em;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background 0.3s;
}

/* 正在朗读的段落高亮 */
.paragraph-speaking {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.empty { margin-top: 80px; }

/* ── 底部朗读控制条 ── */
.audio-bar {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  z-index: 200;
  padding: 10px 0;
}

.audio-bar-inner {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 0 20px;
}

.play-controls {
  display: flex;
  gap: 8px;
}

.speed-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.speed-label {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.speaking-status {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
  font-size: 14px;
}

/* 呼吸灯 */
.pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409eff;
  animation: pulse 1.2s infinite ease-in-out;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.3; transform: scale(1.4); }
}
</style>
