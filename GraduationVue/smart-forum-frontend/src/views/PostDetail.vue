<template>
  <div class="post-detail-page">
    <div v-if="loading" style="padding:40px 0">
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
            <el-avatar :size="32" :src="post.authorAvatar" class="clickable-author"
                       :style="{ background: 'var(--gradient-primary)', fontSize: '14px' }"
                       @click="goToUser(post.userId)">
              {{ post.authorName?.charAt(0) }}
            </el-avatar>
            <span class="author-name clickable-author" @click="goToUser(post.userId)">{{ post.authorName }}</span>
            <el-divider direction="vertical" />
            <span class="publish-time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(post.createdAt) }}
            </span>
          </div>
        </div>
      </div>

      <!-- AI 摘要区域 -->
      <div class="ai-summary-section fade-in-up" style="animation-delay:0.1s">
        <div class="ai-summary-header">
          <div class="ai-badge">
            <el-icon><MagicStick /></el-icon>
            <span>AI 智能摘要</span>
          </div>
          <el-button type="primary" size="small" round :loading="summaryLoading"
                     @click="handleGenerateSummary">
            {{ post.summary ? '重新生成' : '生成摘要' }}
          </el-button>
        </div>
        <div v-if="post.summary" class="ai-summary-content">
          {{ post.summary }}
        </div>
        <div v-else class="ai-summary-placeholder">
          点击"生成摘要"按钮，AI 将为你提取文章核心内容
        </div>
      </div>

      <!-- 文章内容 -->
      <div class="article-content fade-in-up" style="animation-delay:0.15s">
        <div class="article-body" v-html="renderContent(post.content)"></div>
      </div>

      <!-- 交互操作栏 -->
      <div class="interaction-bar fade-in-up" style="animation-delay:0.18s">
        <button class="interact-btn" :class="{ active: post.isLiked }" @click="handleLike">
          <el-icon :size="20"><component :is="post.isLiked ? 'StarFilled' : 'Star'" /></el-icon>
          <span class="interact-count">{{ post.likeCount || 0 }}</span>
          <span class="interact-text">{{ post.isLiked ? '已点赞' : '点赞' }}</span>
        </button>
        <button class="interact-btn" :class="{ active: post.isFavorited }" @click="handleFavorite">
          <el-icon :size="20"><component :is="post.isFavorited ? 'CollectionTag' : 'Collection'" /></el-icon>
          <span class="interact-count">{{ post.favoriteCount || 0 }}</span>
          <span class="interact-text">{{ post.isFavorited ? '已收藏' : '收藏' }}</span>
        </button>
      </div>

      <!-- 评论区 -->
      <div class="comment-section fade-in-up" style="animation-delay:0.2s">
        <h3 class="comment-title">
          <el-icon><ChatLineSquare /></el-icon>
          评论 ({{ comments.length }})
        </h3>

        <!-- 评论输入 -->
        <div class="comment-input" v-if="userStore.isLoggedIn">
          <el-avatar :size="36" :style="{ background: 'var(--gradient-primary)', fontSize: '14px' }">
            {{ userStore.nickname?.charAt(0) }}
          </el-avatar>
          <div class="comment-input-area">
            <el-input v-model="commentText" type="textarea" :rows="3"
                      placeholder="写下你的评论..." resize="none" />
            <el-button type="primary" :loading="commentLoading" @click="handleAddComment"
                       :disabled="!commentText.trim()" style="margin-top:10px;float:right">
              发表评论
            </el-button>
          </div>
        </div>
        <el-alert v-else title="登录后即可发表评论" type="info" :closable="false" show-icon style="margin-bottom:20px">
          <template #default>
            <router-link to="/login" style="color:var(--primary)">去登录</router-link>
          </template>
        </el-alert>

        <!-- 评论列表 -->
        <div class="comment-list">
          <div v-if="comments.length === 0" class="no-comments">
            暂无评论，来发表第一条评论吧
          </div>
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <el-avatar :size="36" :style="{ background: getAvatarColor(comment.authorName), fontSize: '14px' }"
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
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPostDetail } from '@/api/post'
import { getComments, addComment } from '@/api/comment'
import { generateSummary } from '@/api/ai'
import { toggleLike } from '@/api/like'
import { toggleFavorite } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, Clock, MagicStick, ChatLineSquare, Star, StarFilled, Collection, CollectionTag } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const post = ref(null)
const comments = ref([])
const loading = ref(true)
const summaryLoading = ref(false)
const commentText = ref('')
const commentLoading = ref(false)

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
    ElMessage.success(res.data ? '点赞成功' : '已取消点赞')
  } catch {
    // 错误已由拦截器处理
  }
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
    ElMessage.success(res.data ? '收藏成功' : '已取消收藏')
  } catch {
    // 错误已由拦截器处理
  }
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
  max-width: 820px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 16px;
  color: var(--text-secondary);
}

.article-header {
  margin-bottom: 24px;
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.4;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.article-meta {
  display: flex;
  align-items: center;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--text-secondary);
}

.author-name {
  font-weight: 500;
}

.publish-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* AI 摘要 */
.ai-summary-section {
  background: linear-gradient(135deg, rgba(99,102,241,0.06) 0%, rgba(139,92,246,0.06) 100%);
  border: 1px solid rgba(99,102,241,0.15);
  border-radius: var(--radius-md);
  padding: 20px 24px;
  margin-bottom: 24px;
}

.ai-summary-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.ai-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--primary);
  font-weight: 600;
  font-size: 15px;
}

.ai-summary-content {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.8;
}

.ai-summary-placeholder {
  color: var(--text-muted);
  font-size: 14px;
  font-style: italic;
}

/* 文章内容 */
.article-content {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 32px;
  margin-bottom: 32px;
  border: 1px solid var(--border-light);
}

.article-body {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-primary);
  word-wrap: break-word;
}

.article-body :deep(pre) {
  background: #1e1e2e;
  color: #cdd6f4;
  padding: 16px;
  border-radius: var(--radius-sm);
  overflow-x: auto;
  margin: 16px 0;
}

/* 评论区 */
.comment-section {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  padding: 24px;
  border: 1px solid var(--border-light);
}

.comment-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
}

.comment-input {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-light);
}

.comment-input-area {
  flex: 1;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.no-comments {
  text-align: center;
  padding: 40px 0;
  color: var(--text-muted);
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.comment-author {
  font-weight: 500;
  font-size: 14px;
}

.comment-time {
  font-size: 12px;
  color: var(--text-muted);
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
}

/* 交互操作栏 */
.interaction-bar {
  display: flex;
  gap: 16px;
  padding: 16px 0;
  margin-bottom: 24px;
}

.interact-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 20px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  color: var(--text-secondary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.interact-btn:hover {
  border-color: var(--primary-light);
  color: var(--primary);
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
}

.interact-btn.active {
  background: linear-gradient(135deg, rgba(99,102,241,0.08) 0%, rgba(139,92,246,0.08) 100%);
  border-color: var(--primary);
  color: var(--primary);
}

.interact-count {
  font-weight: 600;
}

.interact-text {
  font-size: 13px;
}

.clickable-author {
  cursor: pointer;
  transition: color 0.2s ease;
}

.clickable-author:hover {
  color: var(--primary) !important;
}
</style>