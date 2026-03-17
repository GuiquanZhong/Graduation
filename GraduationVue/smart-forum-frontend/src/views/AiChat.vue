<template>
  <div class="ai-chat-page">
    <div class="chat-container fade-in-up">
      <!-- 聊天头部 -->
      <div class="chat-header">
        <div class="chat-header-info">
          <div class="ai-avatar-wrapper">
            <el-icon :size="22"><ChatDotRound /></el-icon>
          </div>
          <div>
            <h2>AI 智能助手</h2>
            <p>
              <span class="status-dot"></span>
              在线 · 随时为你解答
            </p>
          </div>
        </div>
        <el-tooltip content="清空对话" placement="bottom">
          <el-button text circle @click="clearMessages" :disabled="messages.length === 0" class="clear-btn">
            <el-icon :size="16"><Delete /></el-icon>
          </el-button>
        </el-tooltip>
      </div>

      <!-- 消息列表 -->
      <div class="chat-messages" ref="messagesRef">
        <!-- 欢迎区域 -->
        <div v-if="messages.length === 0" class="welcome-section">
          <div class="welcome-icon">
            <el-icon :size="40"><MagicStick /></el-icon>
          </div>
          <h3>你好！我是智能论坛 AI 助手 🤖</h3>
          <p>我可以帮你回答问题、解释概念、编写代码等。<br>试试下面的问题，或直接输入你的问题：</p>
          <div class="quick-prompts">
            <button v-for="prompt in quickPrompts" :key="prompt" @click="sendQuickPrompt(prompt)">
              <el-icon><ChatLineSquare /></el-icon>
              {{ prompt }}
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div v-for="(msg, index) in messages" :key="index"
             :class="['message', msg.role === 'user' ? 'message-user' : 'message-ai']">
          <div class="message-avatar">
            <el-avatar v-if="msg.role === 'user'" :size="36"
                       :style="{ background: 'var(--gradient-primary)', fontSize: '14px', fontWeight: '700' }">
              {{ userStore.nickname?.charAt(0) || 'U' }}
            </el-avatar>
            <div v-else class="ai-msg-avatar">
              <el-icon :size="16"><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="message-bubble-wrapper">
            <div class="message-role-label">
              {{ msg.role === 'user' ? (userStore.nickname || '你') : 'AI 助手' }}
            </div>
            <div class="message-bubble">
              <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
            </div>
          </div>
        </div>

        <!-- 加载指示器 -->
        <div v-if="chatLoading" class="message message-ai">
          <div class="message-avatar">
            <div class="ai-msg-avatar loading-pulse">
              <el-icon :size="16"><ChatDotRound /></el-icon>
            </div>
          </div>
          <div class="message-bubble-wrapper">
            <div class="message-role-label">AI 助手</div>
            <div class="message-bubble">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input-area">
        <div class="input-wrapper">
          <el-input v-model="inputText" :rows="1" type="textarea" resize="none"
                    :autosize="{ minRows: 1, maxRows: 4 }"
                    placeholder="输入你的问题，按 Enter 发送，Shift+Enter 换行..."
                    @keydown.enter.exact.prevent="handleSend"
                    :disabled="chatLoading" class="chat-input" />
          <div class="input-actions">
            <span class="input-hint">Enter 发送</span>
            <el-button type="primary" :loading="chatLoading" @click="handleSend"
                       :disabled="!inputText.trim() || chatLoading" class="send-btn" round>
              <el-icon v-if="!chatLoading"><Promotion /></el-icon>
              <span>发送</span>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { aiChat } from '@/api/ai'
import { useUserStore } from '@/stores/user'
import { ChatDotRound, Delete, MagicStick, Promotion, ChatLineSquare } from '@element-plus/icons-vue'

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
    messagesRef.value.scrollTo({
      top: messagesRef.value.scrollHeight,
      behavior: 'smooth'
    })
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
  max-width: 900px;
  margin: 0 auto;
  height: calc(100vh - 64px - 56px - 70px);
  min-height: 500px;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-md);
  overflow: hidden;
}

/* ===== Header ===== */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
  background: linear-gradient(135deg, rgba(99,102,241,0.04) 0%, rgba(139,92,246,0.04) 100%);
  flex-shrink: 0;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.ai-avatar-wrapper {
  width: 46px; height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 14px;
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  animation: float 6s ease-in-out infinite;
}

.chat-header h2 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 3px;
}

.chat-header p {
  font-size: 12px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 5px;
}

.status-dot {
  width: 7px;
  height: 7px;
  background: var(--success);
  border-radius: 50%;
  display: inline-block;
  animation: pulse 2s infinite;
}

.clear-btn {
  color: var(--text-muted) !important;
}

.clear-btn:hover {
  color: var(--danger) !important;
  background: var(--danger-bg) !important;
}

/* ===== Messages ===== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 28px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ===== Welcome Section ===== */
.welcome-section {
  text-align: center;
  padding: 48px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-icon {
  width: 88px; height: 88px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 28px;
  color: white;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
  animation: float 6s ease-in-out infinite;
}

.welcome-section h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 10px;
}

.welcome-section p {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 28px;
  line-height: 1.7;
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
  max-width: 600px;
}

.quick-prompts button {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 10px 18px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: white;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--transition);
}

.quick-prompts button:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-bg);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.15);
}

/* ===== Messages ===== */
.message {
  display: flex;
  gap: 12px;
  max-width: 88%;
}

.message-user {
  flex-direction: row-reverse;
  align-self: flex-end;
}

.message-ai {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
}

.ai-msg-avatar {
  width: 36px; height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 11px;
  color: white;
  box-shadow: 0 3px 10px rgba(99, 102, 241, 0.25);
}

.loading-pulse {
  animation: pulse-glow 1.5s infinite;
}

.message-bubble-wrapper {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.message-user .message-bubble-wrapper {
  align-items: flex-end;
}

.message-ai .message-bubble-wrapper {
  align-items: flex-start;
}

.message-role-label {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 500;
  padding: 0 4px;
}

.message-bubble {
  padding: 12px 18px;
  border-radius: 16px;
  max-width: 100%;
}

.message-user .message-bubble {
  background: var(--gradient-primary);
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.25);
}

.message-ai .message-bubble {
  background: var(--bg-subtle);
  color: var(--text-primary);
  border-bottom-left-radius: 4px;
  border: 1px solid var(--border-light);
}

.message-content {
  font-size: 14px;
  line-height: 1.75;
  word-wrap: break-word;
}

.message-content :deep(pre) {
  background: #1a1a2e;
  color: #e2e8f0;
  padding: 14px 16px;
  border-radius: var(--radius-sm);
  overflow-x: auto;
  margin: 10px 0;
  font-size: 13px;
  line-height: 1.6;
  border: 1px solid rgba(99, 102, 241, 0.15);
}

.message-content :deep(code) {
  font-family: 'JetBrains Mono', Consolas, monospace;
}

.message-user .message-content :deep(.inline-code) {
  background: rgba(255,255,255,0.2);
  color: white;
  padding: 1px 5px;
  border-radius: 4px;
}

.message-ai .message-content :deep(.inline-code) {
  background: rgba(99,102,241,0.1);
  color: var(--primary);
  padding: 1px 5px;
  border-radius: 4px;
  font-size: 13px;
}

/* ===== Typing Indicator ===== */
.typing-indicator {
  display: flex;
  gap: 5px;
  padding: 4px 0;
  align-items: center;
}

.typing-indicator span {
  width: 7px; height: 7px;
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

/* ===== Input Area ===== */
.chat-input-area {
  padding: 16px 20px;
  border-top: 1px solid var(--border-light);
  background: white;
  flex-shrink: 0;
}

.input-wrapper {
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 12px 12px 10px 16px;
  transition: var(--transition);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.input-wrapper:focus-within {
  border-color: var(--primary-light);
  background: white;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.chat-input :deep(.el-textarea__inner) {
  border: none !important;
  background: transparent !important;
  padding: 0 !important;
  box-shadow: none !important;
  font-size: 14px !important;
  line-height: 1.6 !important;
  resize: none !important;
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.input-hint {
  font-size: 11px;
  color: var(--text-muted);
}

.send-btn {
  height: 34px !important;
  padding: 0 16px !important;
  font-size: 13px !important;
  font-weight: 600 !important;
}
</style>
