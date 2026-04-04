<template>
  <div class="post-detail-page">
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="8" animated />
    </div>

    <template v-else-if="post">
      <!-- 文章头部 -->
      <div class="article-header fade-in-up">
        <el-button text @click="$router.push('/')" class="back-btn">
          <el-icon><ArrowLeft /></el-icon> 返回首页
        </el-button>

        <h1 class="article-title">{{ post.title }}</h1>

        <div class="article-meta">
          <div class="meta-left">
            <el-avatar :size="36" :src="post.authorAvatar" class="clickable-author"
                       :style="{ background: 'var(--gradient-primary)', fontSize: '14px', fontWeight: '700' }"
                       @click="goToUser(post.userId)">
              {{ post.authorName?.charAt(0) }}
            </el-avatar>
            <div class="author-info">
              <span class="author-name clickable-author" @click="goToUser(post.userId)">{{ post.authorName }}</span>
              <span class="publish-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(post.createdAt) }}
              </span>
            </div>
          </div>
          <!-- 作者操作按钮 -->
          <div class="meta-right" v-if="isAuthor">
            <el-button size="small" plain @click="openEditDialog" class="edit-btn">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="danger" plain size="small" @click="handleDeletePost" class="delete-btn">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 文章内容 -->
      <div class="article-content fade-in-up" style="animation-delay:0.12s">
        <MdPreview :modelValue="post.content" class="md-preview-custom" />
        <!-- AI 摘要展示（生成后显示） -->
        <div v-if="post.summary" class="ai-summary-content">
          <el-icon class="quote-icon"><MagicStick /></el-icon>
          {{ post.summary }}
        </div>
        <!-- 文章内容左下角交互按钮 -->
        <div class="article-interact-row">
          <button class="mini-interact-btn" :class="{ active: post.isLiked }" @click="handleLike">
            <Icon :icon="post.isLiked ? 'mdi:thumb-up' : 'mdi:thumb-up-outline'" width="14" height="14" />
            <span>{{ post.likeCount || 0 }}</span>
          </button>
          <button class="mini-interact-btn" :class="{ active: post.isFavorited }" @click="handleFavorite">
            <Icon :icon="post.isFavorited ? 'mdi:heart' : 'mdi:heart-outline'" width="14" height="14" />
            <span>{{ post.favoriteCount || 0 }}</span>
          </button>
          <button class="mini-interact-btn summary-btn" :class="{ loading: summaryLoading }" @click="handleGenerateSummary" :disabled="summaryLoading">
            <el-icon :size="14"><MagicStick /></el-icon>
            <span>{{ post.summary ? '重新生成' : '生成摘要' }}</span>
          </button>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="comment-section fade-in-up" style="animation-delay:0.2s">
        <div class="comment-section-header">
          <h3 class="comment-title">
            <el-icon><ChatLineSquare /></el-icon>
            评论
            <span class="comment-count-badge">{{ comments.length }}</span>
          </h3>
        </div>

        <!-- 评论输入 -->
        <div class="comment-input-wrapper" v-if="userStore.isLoggedIn">
          <el-avatar :size="38" :src="userStore.userInfo?.avatar" :style="{ background: 'var(--gradient-primary)', fontSize: '14px', fontWeight: '700', flexShrink: 0 }">
            {{ userStore.nickname?.charAt(0) }}
          </el-avatar>
          <div class="comment-input-area">
            <el-input v-model="commentText" type="textarea" :rows="3"
                      placeholder="写下你的评论..." resize="none"
                      class="comment-textarea" />
            <div class="comment-input-footer">
              <span class="comment-chars">{{ commentText.length }}/500</span>
              <el-button type="primary" round :loading="commentLoading" @click="handleAddComment"
                         :disabled="!commentText.trim()" size="small" class="comment-submit-btn">
                发表评论
              </el-button>
            </div>
          </div>
        </div>
        <div v-else class="login-prompt">
          <el-icon><ChatLineSquare /></el-icon>
          <span>登录后即可参与讨论</span>
          <router-link to="/login">
            <el-button type="primary" size="small" round>去登录</el-button>
          </router-link>
        </div>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-if="comments.length === 0" class="no-comments">
            <el-icon :size="36" style="color: var(--text-muted); margin-bottom: 8px;"><ChatLineSquare /></el-icon>
            <p>暂无评论，来发表第一条评论吧 👋</p>
          </div>
          <div v-for="(comment, idx) in comments" :key="comment.id"
               class="comment-item fade-in-up" :style="{ animationDelay: idx * 0.04 + 's' }">
            <el-avatar :size="38" :src="comment.authorAvatar"
                       :style="{ background: getAvatarColor(comment.authorName), fontSize: '14px', fontWeight: '700', flexShrink: 0 }"
                       class="clickable-author" @click="goToUser(comment.userId)">
              {{ comment.authorName?.charAt(0) || '?' }}
            </el-avatar>
            <div class="comment-body">
              <div class="comment-header">
                <div class="comment-header-left">
                  <span class="comment-author clickable-author" @click="goToUser(comment.userId)">{{ comment.authorName }}</span>
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                </div>
                <el-button v-if="userStore.isLoggedIn && userStore.userInfo?.userId == comment.userId"
                           type="danger" text size="small"
                           @click.stop="handleDeleteComment(comment.id)" class="delete-comment-btn">
                  删除
                </el-button>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>

  <!-- 编辑文章对话框 -->
  <el-dialog v-model="editDialogVisible" title="编辑文章" width="720px"
             :close-on-click-modal="false" class="edit-dialog">
    <div class="edit-form">
      <div class="form-group">
        <label class="form-label">文章标题</label>
        <el-input v-model="editTitle" placeholder="请输入文章标题" maxlength="100" show-word-limit size="large" />
      </div>
      <div class="form-group">
        <label class="form-label">文章内容</label>
        <MdEditor v-model="editContent" :height="420" language="zh-CN" />
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="editDialogVisible = false" round>取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEditSubmit"
                   :disabled="!editTitle.trim() || !editContent.trim()" round>
          <el-icon><Check /></el-icon>
          保存修改
        </el-button>
      </div>
    </template>
  </el-dialog>

  <!-- AI 悬浮按钮 -->
  <div v-if="post" class="ai-fab" @click="openAiChat" title="问问 AI">
    <span class="ai-fab-text">AI</span>
  </div>

  <!-- AI 问帖对话弹窗 -->
  <div v-if="aiChatVisible" class="ai-chat-overlay" @click.self="aiChatVisible = false">
    <div class="ai-chat-panel">
      <div class="ai-chat-header">
        <div class="ai-chat-title">
          <span class="ai-chat-icon">✨</span>
          AI 帖子助手
        </div>
        <button class="ai-chat-close" @click="aiChatVisible = false">✕</button>
      </div>
      <div class="ai-chat-body" ref="aiChatBody">
        <div v-for="(msg, idx) in aiMessages" :key="idx"
             class="ai-msg-row" :class="msg.role === 'user' ? 'user-row' : 'ai-row'">
          <div v-if="msg.role === 'assistant'" class="ai-avatar">AI</div>
          <div class="ai-msg-bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
            {{ msg.content }}
          </div>
          <div v-if="msg.role === 'user'" class="user-avatar">
            <el-avatar :size="30" :src="userStore.userInfo?.avatar"
                       :style="{ background: 'linear-gradient(135deg, #f59e0b, #ec4899)', fontSize: '12px', fontWeight: '700' }">
              {{ userStore.nickname?.charAt(0) || '我' }}
            </el-avatar>
          </div>
        </div>
        <div v-if="aiLoading" class="ai-msg-row ai-row">
          <div class="ai-avatar">AI</div>
          <div class="ai-msg-bubble ai-bubble ai-typing">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
      <div class="ai-chat-footer">
        <el-input
          v-model="aiInputText"
          type="textarea"
          :rows="2"
          placeholder="输入问题，Enter 发送，Shift+Enter 换行"
          resize="none"
          class="ai-input"
          @keydown="handleAiInputKeydown"
          :disabled="aiLoading"
        />
        <el-button type="primary" :loading="aiLoading" @click="sendAiMessage"
                   :disabled="!aiInputText.trim() || aiLoading" class="ai-send-btn" round>
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MdEditor, MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getPostDetail, updatePost, deletePost } from '@/api/post'
import { getComments, addComment, deleteComment } from '@/api/comment'
import { generateSummary, aiPostChat } from '@/api/ai'
import { toggleLike } from '@/api/like'
import { toggleFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, Clock, MagicStick, ChatLineSquare, Edit, Delete, Check } from '@element-plus/icons-vue'
import { Icon } from '@iconify/vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const post = ref(null)
const comments = ref([])
const loading = ref(true)
const summaryLoading = ref(false)
const commentText = ref('')
const commentLoading = ref(false)

const editDialogVisible = ref(false)
const editTitle = ref('')
const editContent = ref('')
const editLoading = ref(false)

const isAuthor = computed(() => {
  return userStore.isLoggedIn && post.value && userStore.userInfo?.userId == post.value.userId
})

onMounted(async () => {
  try {
    const [postRes, commentRes] = await Promise.all([
      getPostDetail(route.params.id),
      getComments(route.params.id)
    ])
    post.value = postRes.data
    comments.value = commentRes.data
  } finally {
    loading.value = false
  }
})

const openEditDialog = () => {
  editTitle.value = post.value.title
  editContent.value = post.value.content
  editDialogVisible.value = true
}

const handleEditSubmit = async () => {
  editLoading.value = true
  try {
    await updatePost(post.value.id, { title: editTitle.value.trim(), content: editContent.value })
    post.value.title = editTitle.value.trim()
    post.value.content = editContent.value
    editDialogVisible.value = false
    ElMessage.success('文章修改成功')
  } finally {
    editLoading.value = false
  }
}

const handleDeletePost = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇文章吗？删除后无法恢复。', '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deletePost(post.value.id)
    ElMessage.success('文章已删除')
    router.push('/')
  } catch { }
}

const handleGenerateSummary = async () => {
  summaryLoading.value = true
  try {
    const res = await generateSummary(post.value.content)
    post.value.summary = res.data
    ElMessage.success('摘要生成成功')
  } finally {
    summaryLoading.value = false
  }
}

const handleAddComment = async () => {
  commentLoading.value = true
  try {
    await addComment({ postId: post.value.id, content: commentText.value.trim() })
    ElMessage.success('评论成功')
    commentText.value = ''
    const res = await getComments(route.params.id)
    comments.value = res.data
  } finally {
    commentLoading.value = false
  }
}

const handleDeleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteComment(commentId)
    ElMessage.success('评论已删除')
    const res = await getComments(route.params.id)
    comments.value = res.data
  } catch { }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await toggleLike(post.value.id)
    post.value.isLiked = res.data
    post.value.likeCount = (post.value.likeCount || 0) + (res.data ? 1 : -1)
    ElMessage.success(res.data ? '点赞成功 ❤️' : '已取消点赞')
  } catch { }
}

const handleFavorite = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await toggleFavorite(post.value.id)
    post.value.isFavorited = res.data
    post.value.favoriteCount = (post.value.favoriteCount || 0) + (res.data ? 1 : -1)
    ElMessage.success(res.data ? '收藏成功 ⭐' : '已取消收藏')
  } catch { }
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit'
  })
}

const getAvatarColor = (name) => {
  const colors = ['#6366f1', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#3b82f6']
  return colors[(name || '').charCodeAt(0) % colors.length]
}

const goToUser = (userId) => {
  if (userId) router.push(`/user/${userId}`)
}

// ===== AI 问帖对话 =====
const aiChatVisible = ref(false)
const aiMessages = ref([])
const aiInputText = ref('')
const aiLoading = ref(false)
const aiChatBody = ref(null)

const openAiChat = () => {
  if (aiMessages.value.length === 0) {
    aiMessages.value = [{
      role: 'assistant',
      content: `你好！我已阅读这篇帖子《${post.value?.title}》，有什么想问的吗？例如：这篇帖子讲了什么、主角是谁等。`
    }]
  }
  aiChatVisible.value = true
}

const scrollAiToBottom = () => {
  setTimeout(() => {
    if (aiChatBody.value) {
      aiChatBody.value.scrollTop = aiChatBody.value.scrollHeight
    }
  }, 50)
}

const sendAiMessage = async () => {
  const text = aiInputText.value.trim()
  if (!text || aiLoading.value) return
  aiMessages.value.push({ role: 'user', content: text })
  aiInputText.value = ''
  aiLoading.value = true
  scrollAiToBottom()
  try {
    const history = aiMessages.value.map(m => ({ role: m.role, content: m.content }))
    const res = await aiPostChat(post.value?.title || '', post.value?.content || '', history)
    aiMessages.value.push({ role: 'assistant', content: res.data })
  } catch {
    aiMessages.value.push({ role: 'assistant', content: '抱歉，AI 暂时无法回答，请稍后重试。' })
  } finally {
    aiLoading.value = false
    scrollAiToBottom()
  }
}

const handleAiInputKeydown = (e) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendAiMessage()
  }
}
</script>

<style scoped>
.post-detail-page {
  max-width: 860px;
  margin: 0 auto;
}

.loading-wrapper {
  padding: 48px 0;
}

/* ===== Article Header ===== */
.back-btn {
  margin-bottom: 20px;
  color: var(--text-secondary) !important;
  font-size: 13px;
  font-weight: 500;
}

.back-btn:hover {
  color: var(--primary) !important;
}

.article-header {
  margin-bottom: 28px;
}

.article-title {
  font-size: 30px;
  font-weight: 800;
  line-height: 1.35;
  color: var(--text-primary);
  margin-bottom: 20px;
  letter-spacing: -0.5px;
}

.article-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-light);
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.publish-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.meta-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-btn {
  border-color: var(--primary-light) !important;
  color: var(--primary) !important;
}

.edit-btn:hover {
  background: var(--primary-bg) !important;
}

.delete-btn {
  border-color: rgba(239, 68, 68, 0.3) !important;
}

/* ===== AI Summary ===== */
.ai-summary-content {
  margin-bottom: 16px;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.85;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(99,102,241,0.05) 0%, rgba(139,92,246,0.05) 100%);
  border-radius: var(--radius-sm);
  border: 1px solid rgba(99, 102, 241, 0.15);
}

.quote-icon {
  color: var(--primary-light);
  margin-right: 6px;
  opacity: 0.6;
  vertical-align: middle;
}

/* ===== Article Content ===== */
.article-content {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 36px 40px;
  margin-bottom: 28px;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.md-preview-custom {
  background: transparent !important;
  padding: 0 !important;
  font-size: 16px;
  line-height: 1.9;
}

/* ===== Article Interact Row ===== */
.article-interact-row {
  display: flex;
  gap: 8px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-light);
  justify-content: flex-end;
}

.mini-interact-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border: 1px solid var(--border);
  border-radius: var(--radius-full);
  background: transparent;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: var(--transition);
}

.mini-interact-btn:hover {
  border-color: var(--primary-light);
  background: var(--primary-bg);
  color: var(--primary);
}

.mini-interact-btn.active {
  border-color: var(--primary);
  background: var(--primary-bg);
  color: var(--primary);
}

.mini-interact-btn.summary-btn {
  color: var(--primary);
  border-color: var(--primary-light);
}

.mini-interact-btn.summary-btn:hover {
  background: var(--primary-bg);
  border-color: var(--primary);
}

.mini-interact-btn.summary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* ===== Comment Section ===== */
.comment-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 28px;
  border: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.comment-section-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-light);
}

.comment-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
}

.comment-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 22px;
  height: 22px;
  padding: 0 6px;
  background: var(--primary-bg);
  color: var(--primary);
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 700;
}

.comment-input-wrapper {
  display: flex;
  gap: 14px;
  margin-bottom: 28px;
  padding-bottom: 28px;
  border-bottom: 1px solid var(--border-light);
}

.comment-input-area {
  flex: 1;
}

.comment-textarea :deep(.el-textarea__inner) {
  border-radius: 10px !important;
  padding: 12px 14px !important;
  font-size: 14px !important;
  transition: var(--transition) !important;
}

.comment-input-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.comment-chars {
  font-size: 12px;
  color: var(--text-muted);
}

.comment-submit-btn {
  font-weight: 600 !important;
}

.login-prompt {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
  border: 1px dashed var(--border);
  margin-bottom: 24px;
  color: var(--text-secondary);
  font-size: 14px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.no-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 0;
  color: var(--text-muted);
  font-size: 14px;
}

.comment-item {
  display: flex;
  gap: 14px;
  padding: 20px 0;
  border-bottom: 1px solid var(--border-light);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.comment-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.delete-comment-btn {
  padding: 0 4px;
  height: 20px;
}

.comment-author {
  font-weight: 600;
  font-size: 14px;
  color: var(--text-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--text-muted);
}

.comment-text {
  font-size: 14px;
  line-height: 1.7;
  color: var(--text-secondary);
}

/* ===== Clickable Author ===== */
.clickable-author {
  cursor: pointer;
  transition: color 0.2s ease;
}

.clickable-author:hover {
  color: var(--primary) !important;
}

/* ===== Edit Dialog ===== */
.edit-form .form-group {
  margin-bottom: 24px;
}

.edit-form .form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* ===== AI Floating Button ===== */
.ai-fab {
  position: fixed;
  right: 32px;
  top: 50%;
  transform: translateY(-50%);
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.45);
  z-index: 1000;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  user-select: none;
}

.ai-fab:hover {
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 6px 24px rgba(99, 102, 241, 0.6);
}

.ai-fab-text {
  color: #fff;
  font-weight: 800;
  font-size: 15px;
  letter-spacing: 0.5px;
}

/* ===== AI Chat Panel ===== */
.ai-chat-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  pointer-events: none;
}

.ai-chat-panel {
  pointer-events: all;
  position: fixed;
  right: 96px;
  top: 50%;
  transform: translateY(-50%);
  width: 380px;
  max-height: 560px;
  background: var(--bg-card, #fff);
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.18);
  border: 1px solid var(--border-light, #e5e7eb);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: slideInRight 0.22s ease;
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateY(-50%) translateX(20px); }
  to   { opacity: 1; transform: translateY(-50%) translateX(0); }
}

.ai-chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 18px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  flex-shrink: 0;
}

.ai-chat-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-weight: 700;
  font-size: 15px;
}

.ai-chat-icon {
  font-size: 16px;
}

.ai-chat-close {
  background: rgba(255,255,255,0.2);
  border: none;
  color: #fff;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.ai-chat-close:hover {
  background: rgba(255,255,255,0.35);
}

.ai-chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.ai-msg-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.user-row {
  flex-direction: row-reverse;
}

.ai-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-avatar {
  flex-shrink: 0;
}

.ai-msg-bubble {
  max-width: 76%;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.ai-bubble {
  background: var(--bg-subtle, #f3f4f6);
  color: var(--text-primary, #111);
  border-bottom-left-radius: 4px;
}

.user-bubble {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  border-bottom-right-radius: 4px;
}

/* Typing dots */
.ai-typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 12px 16px;
}

.ai-typing span {
  width: 7px;
  height: 7px;
  background: #8b5cf6;
  border-radius: 50%;
  display: inline-block;
  animation: typingBounce 1.2s infinite ease-in-out;
}

.ai-typing span:nth-child(2) { animation-delay: 0.2s; }
.ai-typing span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 80%, 100% { transform: scale(0.7); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.ai-chat-footer {
  padding: 12px 14px;
  border-top: 1px solid var(--border-light, #e5e7eb);
  display: flex;
  gap: 10px;
  align-items: flex-end;
  flex-shrink: 0;
}

.ai-input {
  flex: 1;
}

.ai-input :deep(.el-textarea__inner) {
  border-radius: 10px !important;
  font-size: 13px !important;
  padding: 8px 12px !important;
  resize: none !important;
}

.ai-send-btn {
  font-weight: 600 !important;
  height: 36px;
  padding: 0 16px !important;
}
</style>
