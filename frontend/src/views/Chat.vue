<template>
  <div class="chat-container">
    <div class="chat-header">
      <h2><el-icon><ChatDotRound /></el-icon> AI 法律咨询</h2>
      <p class="chat-subtitle">基于大模型的法律知识问答，回答仅供参考</p>
    </div>

    <!-- 消息列表 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="chat-empty">
        <el-icon :size="48"><ChatLineSquare /></el-icon>
        <p>开始提问吧，例如：</p>
        <div class="example-questions">
          <el-tag
            v-for="q in examples"
            :key="q"
            class="example-tag"
            @click="sendMessage(q)"
          >{{ q }}</el-tag>
        </div>
      </div>

      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="message-item"
        :class="msg.role"
      >
        <div class="message-avatar">
          <el-icon v-if="msg.role === 'user'"><User /></el-icon>
          <el-icon v-else><Service /></el-icon>
        </div>
        <div class="message-bubble" :class="{ error: msg.isError }">
          <div class="message-content">{{ msg.content }}</div>
          <div v-if="msg.role === 'assistant' && msg.isStreaming && !msg.isError" class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        placeholder="输入你的法律问题..."
        :disabled="isLoading"
        @keydown.enter.exact.prevent="sendMessage(inputText)"
        resize="none"
      />
      <el-button
        type="primary"
        :loading="isLoading"
        :disabled="!inputText.trim()"
        @click="sendMessage(inputText)"
      >
        <el-icon><Promotion /></el-icon> 发送
      </el-button>
    </div>

    <p class="disclaimer">⚠️ AI 回答仅供参考，不构成法律建议。重大法律问题请咨询专业律师。</p>
  </div>
</template>

<script setup>
import { ref, nextTick, onBeforeUnmount } from 'vue'

const examples = [
  '劳动仲裁需要多长时间？',
  '离婚财产如何分割？',
  '被公司无故辞退怎么办？',
  '租房合同到期押金不退合法吗？',
]

const messages = ref([])    // { role: 'user'|'assistant', content: '...', isStreaming?: true }
const inputText = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
let abortController = null

async function sendMessage(text) {
  const question = text?.trim?.() ?? (typeof text === 'string' ? text.trim() : '')
  if (!question || isLoading.value) return

  inputText.value = ''

  // 添加用户消息
  messages.value.push({ role: 'user', content: question })
  scrollToBottom()

  // 添加一个空的 AI 占位消息（用于流式填充）
  const aiMsg = { role: 'assistant', content: '', isStreaming: true }
  messages.value.push(aiMsg)
  scrollToBottom()

  isLoading.value = true
  abortController = new AbortController()

  try {
    // 构建请求体：只发送完成的消息（不含当前流式中的），加上当前用户问题
    const history = []
    for (const m of messages.value) {
      if (m === aiMsg) continue // 跳过正在流式填充的
      if (m.isStreaming) continue
      history.push({ role: m.role, content: m.content })
    }

    const response = await fetch('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ messages: history }),
      signal: abortController.signal,
    })

    if (!response.ok) {
      const err = await response.json().catch(() => ({ message: '请求失败' }))
      throw new Error(err.message || '请求失败')
    }

    // 读取 SSE 流
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      // 追加到 buffer，统一处理 \r\n 和 \n
      buffer += decoder.decode(value, { stream: true })

      // 按换行分割，兼容 \r\n
      const parts = buffer.split(/\r?\n/)
      buffer = parts.pop() // 保留最后一个不完整的片段

      for (let i = 0; i < parts.length; i++) {
        const rawLine = parts[i]
        const line = rawLine.trim()
        if (!line) continue

        // event: 行 — 记录事件类型
        if (line.startsWith('event:')) {
          // 暂存事件类型，接下来 data 行用到
          continue
        }

        // data: 行 — 提取数据
        if (line.startsWith('data:')) {
          // 去掉 "data:" 前缀（冒号后面可能没有空格也可能有多个）
          const jsonStr = line.replace(/^data:\s*/, '')
          if (!jsonStr) continue

          if (jsonStr === '[DONE]') break

          try {
            const obj = JSON.parse(jsonStr)

            // 错误事件
            if (obj.error) {
              aiMsg.content = obj.error || '请求失败，请稍后重试'
              aiMsg.isError = true
              break
            }

            // 正常 OpenAI 格式：提取 delta.content
            const delta = obj?.choices?.[0]?.delta?.content
            if (delta) {
              aiMsg.content += delta
              scrollToBottom()
            }
          } catch (e) {
            console.warn('SSE 数据解析失败, raw:', jsonStr.substring(0, 80))
          }
        }
      }
    }
  } catch (err) {
    if (err.name === 'AbortError') {
      aiMsg.content = aiMsg.content || '已取消'
    } else {
      aiMsg.content = aiMsg.content || `抱歉，请求失败：${err.message}`
    }
  } finally {
    aiMsg.isStreaming = false
    isLoading.value = false
    abortController = null
  }
}

function scrollToBottom() {
  nextTick(() => {
    const el = messagesContainer.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

onBeforeUnmount(() => {
  if (abortController) {
    abortController.abort()
  }
})
</script>

<style scoped>
.chat-container {
  max-width: 800px;
  height: calc(100vh - 140px);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

.chat-header {
  text-align: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.chat-header h2 {
  font-size: 20px;
  color: #303133;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.chat-subtitle {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

/* 消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
}

.chat-empty {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.chat-empty p {
  margin: 16px 0;
}

.example-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.example-tag {
  cursor: pointer;
  transition: transform 0.2s;
}

.example-tag:hover {
  transform: translateY(-1px);
}

/* 单条消息 */
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
}

.message-item.user .message-avatar {
  background: #409eff;
  color: #fff;
}

.message-item.assistant .message-avatar {
  background: #67c23a;
  color: #fff;
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.7;
  font-size: 15px;
}

.message-item.user .message-bubble {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 4px;
}

.message-item.assistant .message-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-top-left-radius: 4px;
}

.message-item.assistant .message-bubble.error {
  background: #fef0f0;
  color: #f56c6c;
  border-color: #fbc4c4;
}

/* 输入区域 */
.chat-input {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid #ebeef5;
}

.chat-input .el-textarea {
  flex: 1;
}

.disclaimer {
  text-align: center;
  color: #c0c4cc;
  font-size: 12px;
  padding-bottom: 8px;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding-top: 8px;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #909399;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 80%, 100% { opacity: 0.3; transform: scale(0.8); }
  40% { opacity: 1; transform: scale(1); }
}
</style>
