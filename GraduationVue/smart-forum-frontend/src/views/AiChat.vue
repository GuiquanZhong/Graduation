<template>
  <div class="ai-chat-page">
    <div class="chat-container fade-in-up">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="chat-header-info">
          <div class="ai-avatar-wrapper">
            <el-icon :size="24"><ChatDotRound /></el-icon>
          </div>
          <div>
            <h2>AI 智能助手</h2>
            <p>有什么问题尽管问我～</p>
          </div>
        </div>
        <el-button text circle @click="clearMessages" :disabled="messages.length === 0">
          <el-icon :size="18"><Delete /></el-icon>
        </el-button>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesRef">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-section">
          <div class="welcome-icon">
            <el-icon :size="48"><MagicStick /></el-icon>
          </div>
          <h3>你好！我是智能论坛 AI 助手 🤖</h3>
          <p>我可以帮你回答问题、解释概念、编写代码等。试试下面的问题：</p>
          <div class="quick-prompts">
            <button v-for="prompt in quickPrompts" :key="prompt" @click="sendQuickPrompt(prompt)">
              {{ prompt }}
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(msg, index) in messages" :key="index"
             :class="['message', msg.role === 'user' ? 'message-user' : 'message-ai']">
          <div class="message-avatar">
            <el-avatar v-if="msg.role === 'user'" :size="36"
                       :style="{ background: 'var(--gradient-primary)', fontSize: '14px' }">
              {{ userStore.nickname?.charAt(0) }}
            </el-avatar>
            <div v-else class="ai-msg-avatar">
              <el-icon><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="message-bubble">
            <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
          </div>
        </div>

        <!-- 加载指示器 -->
        <div v-if="chatLoading" class="message message-ai">
          <div class="message-avatar">
            <div class="ai-msg-avatar loading-pulse">
              <el-icon><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="message-bubble">
            <div class="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <el-input v-model="inputText" :rows="1" type="textarea" resize="none"
                  placeholder="输入你的问题..." @keydown.enter.exact.prevent="handleSend"
                  :disabled="chatLoading" />
        <el-button type="primary" circle :loading="chatLoading" @click="handleSend"
                   :disabled="!inputText.trim() || chatLoading" class="send-btn">
          <el-icon><Promotion /></el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { aiChat } from '@/api/ai'
import { useUserStore } from '@/stores/user'
import { ChatDotRound, Delete, MagicStick, Promotion } from '@element-plus/icons-vue'

const userStore = useUserStore()
const messages = ref([])
const inputText = ref('')
const chatLoading = ref(false)
const messagesRef = ref()

const quickPrompts = [
  '什么是 Spring Boot？',
  '解释一下 JWT 认证原理',
  'Vue 3 Composition API 有什么优势？',
  '帮我写一段 Java 排序算法'
]

const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || chatLoading.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  chatLoading.value = true
  scrollToBottom()

  try {
    const res = await aiChat(text)
    messages.value.push({ role: 'ai', content: res.data })
  } catch {
    messages.value.push({ role: 'ai', content: '抱歉，AI 服务暂时不可用，请稍后再试。' })
  } finally {
    chatLoading.value = false
    scrollToBottom()
  }
}

const sendQuickPrompt = (prompt) => {
  inputText.value = prompt
  handleSend()
}

const clearMessages = () => {
  messages.value = []
  ElMessage.success('对话已清空')
}

const renderMarkdown = (text) => {
  if (!text) return ''
  return text
    .replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code class="language-$1">$2</code></pre>')
    .replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}
</script>

<style scoped>
.ai-chat-page {
  max-width: 860px;
  margin: 0 auto;
  height: calc(100vh - 64px - 48px - 70px);
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
  background: linear-gradient(135deg, rgba(99,102,241,0.05) 0%, rgba(139,92,246,0.05) 100%);
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar-wrapper {
  width: 44px; height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 14px;
  color: white;
}

.chat-header h2 {
  font-size: 16px;
  font-weight: 600;
}

.chat-header p {
  font-size: 12px;
  color: var(--text-muted);
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 欢迎区域 */
.welcome-section {
  text-align: center;
  padding: 40px 20px;
}

.welcome-icon {
  width: 80px; height: 80px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-bg);
  border-radius: 24px;
  color: var(--primary);
}

.welcome-section h3 {
  font-size: 20px;
  margin-bottom: 8px;
}

.welcome-section p {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 24px;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.quick-prompts button {
  padding: 8px 16px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: white;
  color: var(--text-primary);
  font-size: 13px;
  cursor: pointer;
  transition: var(--transition);
}

.quick-prompts button:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-bg);
}

/* 消息气泡 */
.message {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message-user {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.message-ai {
  align-self: flex-start;
}

.ai-msg-avatar {
  width: 36px; height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 12px;
  color: white;
  flex-shrink: 0;
}

.loading-pulse {
  animation: pulse-glow 1.5s infinite;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 16px;
  max-width: calc(100% - 48px);
}

.message-user .message-bubble {
  background: var(--gradient-primary);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-ai .message-bubble {
  background: var(--bg);
  color: var(--text-primary);
  border-bottom-left-radius: 4px;
}

.message-content {
  font-size: 14px;
  line-height: 1.7;
  word-wrap: break-word;
}

.message-content :deep(pre) {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 12px 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
  font-size: 13px;
}

.message-content :deep(.inline-code) {
  background: rgba(99,102,241,0.1);
  color: var(--primary);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px; height: 8px;
  background: var(--text-muted);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* 输入区域 */
.chat-input-area {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-light);
  background: rgba(255,255,255,0.8);
}

.chat-input-area .el-textarea {
  flex: 1;
}

.chat-input-area :deep(.el-textarea__inner) {
  min-height: 44px !important;
  max-height: 120px;
  padding: 10px 16px;
}

.send-btn {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
}
</style>
