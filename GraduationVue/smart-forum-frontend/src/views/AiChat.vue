<template>
  <div class="ai-chat-page">
    <div class="chat-layout" :class="{ 'sidebar-collapsed': !sidebarOpen }">
      <!-- 侧边栏 -->
      <div class="chat-sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">聊天记录</span>
          <el-tooltip content="新建对话" placement="bottom">
            <el-button text circle @click="startNewChat" class="new-chat-btn">
              <el-icon :size="16"><EditPen /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
        <div class="sidebar-list" v-if="sessions.length > 0">
          <div v-for="group in groupedSessions" :key="group.label" class="session-group">
            <div class="group-label">{{ group.label }}</div>
            <div
              v-for="session in group.items"
              :key="session.id"
              class="session-item"
              :class="{ active: currentSessionId === session.id }"
              @click="loadSession(session.id)"
            >
              <div class="session-item-content">
                <div class="session-title">{{ session.title }}</div>
                <div class="session-meta">{{ formatTime(session.updatedAt) }}</div>
              </div>
              <el-tooltip content="删除" placement="right">
                <button class="session-delete-btn" @click.stop="confirmDeleteSession(session.id)">
                  <el-icon :size="12"><Delete /></el-icon>
                </button>
              </el-tooltip>
            </div>
          </div>
        </div>
        <div v-else class="sidebar-empty">
          <el-icon :size="28" color="var(--text-muted)"><ChatDotRound /></el-icon>
          <p>暂无聊天记录</p>
        </div>
      </div>

      <!-- 主聊天区 -->
      <div class="chat-container fade-in-up">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="chat-header-left">
            <el-tooltip :content="sidebarOpen ? '收起记录' : '展开记录'" placement="bottom">
              <el-button text circle @click="sidebarOpen = !sidebarOpen" class="toggle-btn">
                <el-icon :size="18"><Menu /></el-icon>
              </el-button>
            </el-tooltip>
            <div class="chat-header-info">
              <div class="ai-avatar-wrapper">
                <img :src="currentModel.logo" class="ai-avatar-logo" />
              </div>
              <div>
                <h2>AI 智能助手</h2>
                <p>
                  <span class="status-dot"></span>
                  在线 · 随时为你解答
                </p>
              </div>
            </div>
          </div>
          <div class="chat-header-right">
            <!-- 模型选择器 -->
            <div class="model-selector">
              <el-dropdown @command="switchModel" trigger="click">
                <div class="model-selector-trigger">
                  <img :src="currentModel.logo" class="model-logo" />
                  <span class="model-name">{{ currentModel.label }}</span>
                  <el-icon :size="12" style="color: var(--text-muted)"><ArrowDown /></el-icon>
                </div>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="m in modelList"
                      :key="m.id"
                      :command="m.id"
                      :class="{ 'is-active-model': currentModelId === m.id }"
                    >
                      <div class="model-option">
                        <img :src="m.logo" class="model-option-logo" />
                        <div>
                          <div class="model-option-label">{{ m.label }}</div>
                          <div class="model-option-sub">{{ m.sub }}</div>
                        </div>
                      </div>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
            <el-tooltip content="清空对话" placement="bottom">
              <el-button text circle @click="clearMessages" :disabled="messages.length === 0" class="clear-btn">
                <el-icon :size="16"><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages" ref="messagesRef">
          <div v-if="messages.length === 0" class="welcome-section">
            <div class="welcome-icon">
              <img :src="currentModel.logo" class="welcome-model-logo" />
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

          <div v-for="(msg, index) in messages" :key="index"
               :class="['message', msg.role === 'user' ? 'message-user' : 'message-ai']">
            <div class="message-avatar">
              <el-avatar v-if="msg.role === 'user'" :size="36" :src="userStore.userInfo?.avatar"
                         :style="{ background: 'var(--gradient-primary)', fontSize: '14px', fontWeight: '700' }">
                {{ userStore.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div v-else class="ai-msg-avatar">
                <img :src="msg.modelLogo || currentModel.logo" class="msg-model-logo" />
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

          <div v-if="chatLoading" class="message message-ai">
            <div class="message-avatar">
              <div class="ai-msg-avatar loading-pulse">
                <img :src="currentModel.logo" class="msg-model-logo" />
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
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { aiChat, listAiSessions, getAiSession, createAiSession, updateAiSession, deleteAiSession } from '@/api/ai'
import { useUserStore } from '@/stores/user'
import { ChatDotRound, Delete, MagicStick, Promotion, ChatLineSquare, EditPen, Menu, ArrowDown } from '@element-plus/icons-vue'

const userStore = useUserStore()
const messages = ref([])
const inputText = ref('')
const chatLoading = ref(false)
const messagesRef = ref()
const sidebarOpen = ref(true)
const sessions = ref([])
const currentSessionId = ref(null)

const modelList = [
  {
    id: 'Pro/MiniMaxAI/MiniMax-M2.5',
    label: 'MiniMax / MiniMax-M2.5',
    sub: 'MiniMax',
    logo: 'https://sf-maas-uat-prod.oss-cn-shanghai.aliyuncs.com/Model_LOGO/minimax-color.svg'
  },
  {
    id: 'Pro/zai-org/GLM-5',
    label: 'Zhipu / GLM-5',
    sub: '智谱 AI',
    logo: 'https://sf-maas-uat-prod.oss-cn-shanghai.aliyuncs.com/Model_LOGO/zhipu.svg'
  },
  {
    id: 'Pro/deepseek-ai/DeepSeek-V3.2',
    label: 'DeepSeek / DeepSeek-V3.2',
    sub: 'DeepSeek',
    logo: 'https://sf-maas-uat-prod.oss-cn-shanghai.aliyuncs.com/Model_LOGO/DeepSeek.svg'
  },
  {
    id: 'Pro/moonshotai/Kimi-K2.5',
    label: 'Moonshot / Kimi-K2.5',
    sub: 'Moonshot AI',
    logo: 'https://sf-maas-uat-prod.oss-cn-shanghai.aliyuncs.com/Model_LOGO/moonshotai_new.png'
  }
]

const currentModelId = ref(modelList[0].id)
const currentModel = computed(() => modelList.find(m => m.id === currentModelId.value) || modelList[0])

const switchModel = (id) => {
  currentModelId.value = id
}

const quickPrompts = [
  '推荐几篇热门帖子',
  '论坛最近有什么有趣的讨论？',
  '解释一下 JWT 认证原理',
  '帮我写一段 Java 排序算法'
]

const groupedSessions = computed(() => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)
  const groups = [
    { label: '今天', items: [] },
    { label: '昨天', items: [] },
    { label: '更早', items: [] }
  ]
  for (const s of sessions.value) {
    const d = new Date(s.updatedAt)
    if (d >= today) groups[0].items.push(s)
    else if (d >= yesterday) groups[1].items.push(s)
    else groups[2].items.push(s)
  }
  return groups.filter(g => g.items.length > 0)
})

onMounted(async () => {
  await fetchSessions()
})

const fetchSessions = async () => {
  try {
    const res = await listAiSessions()
    sessions.value = res.data || []
  } catch {
    sessions.value = []
  }
}

const loadSession = async (id) => {
  if (currentSessionId.value === id) return
  try {
    const res = await getAiSession(id)
    const session = res.data
    const msgs = JSON.parse(session.messages || '[]')
    messages.value = msgs.map(m => ({ role: m.role === 'assistant' ? 'ai' : m.role, content: m.content }))
    currentSessionId.value = id
    scrollToBottom()
  } catch {
    ElMessage.error('加载会话失败')
  }
}

const startNewChat = () => {
  messages.value = []
  currentSessionId.value = null
  inputText.value = ''
}

const confirmDeleteSession = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这条聊天记录吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAiSession(id)
    sessions.value = sessions.value.filter(s => s.id !== id)
    if (currentSessionId.value === id) {
      startNewChat()
    }
    ElMessage.success('已删除')
  } catch {
  }
}

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

  const history = messages.value.map(m => ({
    role: m.role === 'user' ? 'user' : 'assistant',
    content: m.content
  }))

  const modelLogo = currentModel.value.logo

  try {
    const res = await aiChat(history, currentModelId.value)
    messages.value.push({ role: 'ai', content: res.data, modelLogo })
    await syncSession(text)
  } catch {
    messages.value.push({ role: 'ai', content: '抱歉，AI 服务暂时不可用，请稍后再试。', modelLogo })
  } finally {
    chatLoading.value = false
    scrollToBottom()
  }
}

const syncSession = async (firstUserText) => {
  const historyForSave = messages.value.map(m => ({
    role: m.role === 'user' ? 'user' : 'assistant',
    content: m.content
  }))
  if (!currentSessionId.value) {
    const title = firstUserText.slice(0, 20)
    try {
      const res = await createAiSession({ title, messages: historyForSave })
      currentSessionId.value = res.data.id
      await fetchSessions()
    } catch {
    }
  } else {
    try {
      await updateAiSession(currentSessionId.value, { messages: historyForSave })
      const idx = sessions.value.findIndex(s => s.id === currentSessionId.value)
      if (idx !== -1) {
        sessions.value[idx].updatedAt = new Date().toISOString()
        sessions.value[idx].messageCount = historyForSave.length
      }
    } catch {
    }
  }
}

const sendQuickPrompt = (prompt) => {
  inputText.value = prompt
  handleSend()
}

const clearMessages = () => {
  messages.value = []
  currentSessionId.value = null
  ElMessage.success('对话已清空')
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  if (d >= today) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
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
  max-width: 1100px;
  margin: 0 auto;
  height: calc(100vh - 64px - 56px - 70px);
  min-height: 500px;
}

.chat-layout {
  display: flex;
  height: 100%;
  gap: 0;
  overflow: hidden;
  border-radius: var(--radius-xl);
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-md);
}

/* ===== Sidebar ===== */
.chat-sidebar {
  width: 240px;
  min-width: 240px;
  background: var(--bg-subtle);
  border-right: 1px solid var(--border-light);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.3s ease, min-width 0.3s ease, opacity 0.3s ease;
}

.chat-layout.sidebar-collapsed .chat-sidebar {
  width: 0;
  min-width: 0;
  opacity: 0;
  border-right: none;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 14px 10px;
  border-bottom: 1px solid var(--border-light);
  flex-shrink: 0;
}

.sidebar-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  white-space: nowrap;
}

.new-chat-btn {
  color: var(--text-muted) !important;
  flex-shrink: 0;
}

.new-chat-btn:hover {
  color: var(--primary) !important;
  background: var(--primary-bg) !important;
}

.sidebar-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.group-label {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 600;
  padding: 8px 14px 4px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 8px 10px 8px 14px;
  cursor: pointer;
  border-radius: var(--radius-sm);
  margin: 1px 6px;
  transition: var(--transition);
  gap: 6px;
}

.session-item:hover {
  background: var(--border-light);
}

.session-item.active {
  background: var(--primary-bg);
}

.session-item-content {
  flex: 1;
  overflow: hidden;
}

.session-title {
  font-size: 13px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-weight: 500;
}

.session-item.active .session-title {
  color: var(--primary);
}

.session-meta {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}

.session-delete-btn {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: var(--text-muted);
  border-radius: var(--radius-sm);
  cursor: pointer;
  opacity: 0;
  transition: var(--transition);
}

.session-item:hover .session-delete-btn {
  opacity: 1;
}

.session-delete-btn:hover {
  color: var(--danger);
  background: var(--danger-bg);
}

.sidebar-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
}

.sidebar-empty p {
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
}

/* ===== Main Chat ===== */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  overflow: hidden;
  min-width: 0;
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

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toggle-btn {
  color: var(--text-muted) !important;
}

.toggle-btn:hover {
  color: var(--primary) !important;
  background: var(--primary-bg) !important;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.ai-avatar-wrapper {
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 14px;
  border: 1px solid var(--border-light);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.15);
  animation: float 6s ease-in-out infinite;
  overflow: hidden;
  padding: 6px;
}

.ai-avatar-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
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

/* ===== Model Selector ===== */
.model-selector {
  cursor: pointer;
}

.model-selector-trigger {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 6px 12px;
  border: 1px solid var(--border);
  border-radius: 20px;
  background: white;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
  transition: var(--transition);
  cursor: pointer;
  user-select: none;
}

.model-selector-trigger:hover {
  border-color: var(--primary-light);
  background: var(--primary-bg);
  color: var(--primary);
}

.model-logo {
  width: 18px;
  height: 18px;
  object-fit: contain;
  border-radius: 4px;
}

.model-name {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 2px 0;
  min-width: 200px;
}

.model-option-logo {
  width: 24px;
  height: 24px;
  object-fit: contain;
  border-radius: 6px;
  flex-shrink: 0;
}

.model-option-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}

.model-option-sub {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 1px;
}

.is-active-model {
  background: var(--primary-bg) !important;
  color: var(--primary) !important;
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
  width: 88px;
  height: 88px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 28px;
  border: 1px solid var(--border-light);
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.15);
  animation: float 6s ease-in-out infinite;
  padding: 14px;
}

.welcome-model-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
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
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 11px;
  border: 1px solid var(--border-light);
  box-shadow: 0 3px 10px rgba(99, 102, 241, 0.12);
  padding: 5px;
}

.msg-model-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
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
  width: 7px;
  height: 7px;
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
