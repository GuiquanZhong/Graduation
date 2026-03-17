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
        <div class="article-body" v-html="renderContent(post.content)"></div>
        <!-- AI 摘要展示（生成后显示） -->
        <div v-if="post.summary" class="ai-summary-content">
          <el-icon class="quote-icon"><MagicStick /></el-icon>
          {{ post.summary }}
        </div>
        <!-- 文章内容左下角交互按钮 -->
        <div class="article-interact-row">
          <button class="mini-interact-btn" :class="{ active: post.isLiked }" @click="handleLike">
            <el-icon :size="14"><component :is="post.isLiked ? 'StarFilled' : 'Star'" /></el-icon>
            <span>{{ post.likeCount || 0 }}</span>
          </button>
          <button class="mini-interact-btn" :class="{ active: post.isFavorited }" @click="handleFavorite">
            <el-icon :size="14"><component :is="post.isFavorited ? 'CollectionTag' : 'Collection'" /></el-icon>
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
          <el-avatar :size="38" :style="{ background: 'var(--gradient-primary)', fontSize: '14px', fontWeight: '700', flexShrink: 0 }">
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
            <el-avatar :size="38"
                       :style="{ background: getAvatarColor(comment.authorName), fontSize: '14px', fontWeight: '700', flexShrink: 0 }"
                       class="clickable-author" @click="goToUser(comment.userId)">
              {{ comment.authorName?.charAt(0) || '?' }}
            </el-avatar>
            <div class="comment-body">
              <div class="comment-header">
                <span class="comment-author clickable-author" @click="goToUser(comment.userId)">{{ comment.authorName }}</span>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
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
        <el-input v-model="editContent" type="textarea" :rows="14" placeholder="请输入文章内容" resize="vertical" />
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPostDetail, updatePost, deletePost } from '@/api/post'
import { getComments, addComment } from '@/api/comment'
import { generateSummary } from '@/api/ai'
import { toggleLike } from '@/api/like'
import { toggleFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, Clock, MagicStick, ChatLineSquare, Star, StarFilled, Collection, CollectionTag, Edit, Delete, Check } from '@element-plus/icons-vue'

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

const renderContent = (content) => {
  if (!content) return ''
  return content
    .replace(/\n/g, '<br>')
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
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

.article-body {
  font-size: 16px;
  line-height: 1.9;
  color: var(--text-primary);
  word-wrap: break-word;
}

.article-body :deep(pre) {
  background: #1a1a2e;
  color: #e2e8f0;
  padding: 20px;
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin: 20px 0;
  font-size: 14px;
  line-height: 1.6;
  border: 1px solid rgba(99, 102, 241, 0.2);
}

.article-body :deep(code) {
  font-family: 'JetBrains Mono', 'Fira Code', Consolas, monospace;
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
  gap: 10px;
  margin-bottom: 8px;
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
</style>
