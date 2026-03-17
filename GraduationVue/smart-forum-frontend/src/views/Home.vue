<template>
  <div class="home-page">
    <!-- Hero区域 -->
    <div class="hero-section">
      <!-- 背景装饰 -->
      <div class="hero-bg">
        <div class="hero-orb hero-orb-1"></div>
        <div class="hero-orb hero-orb-2"></div>
        <div class="hero-orb hero-orb-3"></div>
        <div class="hero-grid"></div>
      </div>

      <div class="hero-content">
        <div class="hero-badge">
          <el-icon><MagicStick /></el-icon>
          <span>AI 驱动的智能社区</span>
        </div>
        <h1 class="hero-title">
          <span>探索知识，</span>
          <span class="ai-shimmer">AI 赋能交流</span>
        </h1>
        <p class="hero-desc">在这里分享想法、讨论技术、获取 AI 智能辅助</p>
        <div class="hero-search">
          <el-input v-model="searchKeyword" placeholder="搜索文章标题或内容..." size="large"
                    @keyup.enter="handleSearch" clearable @clear="loadPosts">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button @click="handleSearch" type="primary">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="hero-stat-value">{{ total }}+</span>
            <span class="hero-stat-label">篇文章</span>
          </div>
          <div class="hero-stat-divider"></div>
          <div class="hero-stat">
            <span class="hero-stat-value">AI</span>
            <span class="hero-stat-label">智能辅助</span>
          </div>
          <div class="hero-stat-divider"></div>
          <div class="hero-stat">
            <span class="hero-stat-value">24h</span>
            <span class="hero-stat-label">活跃社区</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 文章列表 -->
    <div class="post-list-section">
      <div class="section-header">
        <div class="section-title">
          <div class="title-indicator"></div>
          <h2>{{ isSearching ? '搜索结果' : '最新文章' }}</h2>
        </div>
        <div class="section-actions">
          <el-tag v-if="isSearching" closable @close="clearSearch" type="primary" effect="plain" round>
            "{{ searchKeyword }}" · {{ total }} 条结果
          </el-tag>
          <span v-else class="post-count">共 {{ total }} 篇</span>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <div v-for="i in 4" :key="i" class="skeleton-card">
          <el-skeleton :rows="3" animated />
        </div>
      </div>

      <div v-else-if="posts.length === 0" class="empty-state">
        <el-empty description="暂无文章，快来发布第一篇吧！">
          <el-button type="primary" round @click="$router.push('/post/create')">
            <el-icon><Edit /></el-icon>
            发布文章
          </el-button>
        </el-empty>
      </div>

      <template v-else>
        <div class="post-card fade-in-up" v-for="(post, index) in posts" :key="post.id"
             :style="{ animationDelay: index * 0.05 + 's' }" @click="$router.push(`/post/${post.id}`)">
          <div class="post-card-body">
            <div class="post-card-top">
              <h3 class="post-title">{{ post.title }}</h3>
              <el-icon class="post-arrow"><ArrowRight /></el-icon>
            </div>
            <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
            <div class="post-meta">
              <div class="post-author clickable-author" @click.stop="goToUser(post.userId)">
                <el-avatar :size="26" :src="post.authorAvatar"
                           :style="{ background: getAvatarColor(post.authorName), fontSize: '12px', fontWeight: '700' }">
                  {{ post.authorName?.charAt(0) || '?' }}
                </el-avatar>
                <span class="author-name">{{ post.authorName || '匿名' }}</span>
              </div>
              <div class="post-meta-right">
                <span class="post-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(post.createdAt) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination background layout="prev, pager, next"
                         :current-page="currentPage" :page-size="pageSize" :total="total"
                         @current-change="handlePageChange" />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPostList, searchPosts } from '@/api/post'
import { Search, Document, Clock, MagicStick, ArrowRight, Edit } from '@element-plus/icons-vue'

const router = useRouter()

const posts = ref([])
const loading = ref(true)
const searchKeyword = ref('')
const isSearching = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(() => loadPosts())

const loadPosts = async () => {
  loading.value = true
  isSearching.value = false
  try {
    const res = await getPostList(currentPage.value, pageSize.value)
    posts.value = res.data.records
    total.value = Number(res.data.total)
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadPosts()
    return
  }
  loading.value = true
  isSearching.value = true
  currentPage.value = 1
  try {
    const res = await searchPosts(searchKeyword.value.trim(), 1, pageSize.value)
    posts.value = res.data.records
    total.value = Number(res.data.total)
  } finally {
    loading.value = false
  }
}

const clearSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadPosts()
}

const handlePageChange = (page) => {
  currentPage.value = page
  if (isSearching.value) handleSearch()
  else loadPosts()
}

const getExcerpt = (content) => {
  if (!content) return ''
  const plain = content.replace(/<[^>]+>/g, '').replace(/[#*`>\-\[\]()]/g, '')
  return plain.length > 160 ? plain.substring(0, 160) + '...' : plain
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = (now - d) / 1000
  if (diff < 60) return '刚刚'
  if (diff < 3600) return Math.floor(diff / 60) + ' 分钟前'
  if (diff < 86400) return Math.floor(diff / 3600) + ' 小时前'
  if (diff < 2592000) return Math.floor(diff / 86400) + ' 天前'
  return d.toLocaleDateString('zh-CN')
}

const getAvatarColor = (name) => {
  const colors = ['#6366f1', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#3b82f6']
  const index = (name || '').charCodeAt(0) % colors.length
  return colors[index]
}

const goToUser = (userId) => {
  if (userId) router.push(`/user/${userId}`)
}
</script>

<style scoped>
/* ===== Hero ===== */
.hero-section {
  background: var(--gradient-hero);
  border-radius: var(--radius-xl);
  padding: 72px 40px 60px;
  margin-bottom: 36px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.hero-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  animation: float 10s ease-in-out infinite;
}

.hero-orb-1 {
  width: 360px; height: 360px;
  background: rgba(99, 102, 241, 0.25);
  top: -120px; left: -60px;
}

.hero-orb-2 {
  width: 280px; height: 280px;
  background: rgba(139, 92, 246, 0.2);
  bottom: -80px; right: -40px;
  animation-delay: 3s;
}

.hero-orb-3 {
  width: 180px; height: 180px;
  background: rgba(167, 139, 250, 0.15);
  top: 40%; left: 55%;
  animation-delay: 6s;
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px);
  background-size: 40px 40px;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-full);
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 20px;
  backdrop-filter: blur(8px);
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: white;
  margin-bottom: 14px;
  line-height: 1.2;
  letter-spacing: -1px;
}

.hero-desc {
  color: rgba(255, 255, 255, 0.65);
  font-size: 16px;
  margin-bottom: 32px;
}

.hero-search {
  max-width: 580px;
  margin: 0 auto 36px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.25);
  border-radius: 14px;
  overflow: hidden;
}

.hero-search :deep(.el-input__wrapper) {
  border-radius: 0 !important;
  padding: 6px 6px 6px 16px;
  box-shadow: none !important;
  background: rgba(255, 255, 255, 0.96) !important;
}

.hero-search :deep(.el-input-group__append) {
  background: var(--gradient-primary) !important;
  border-radius: 0 !important;
  padding: 0 20px !important;
  color: white !important;
  border: none !important;
  box-shadow: none !important;
}

.hero-search :deep(.el-input-group__append .el-button) {
  color: white !important;
  font-weight: 600 !important;
  background: transparent !important;
  box-shadow: none !important;
}

.hero-stats {
  display: inline-flex;
  align-items: center;
  gap: 24px;
  padding: 12px 28px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: var(--radius-xl);
  backdrop-filter: blur(8px);
}

.hero-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.hero-stat-value {
  font-size: 20px;
  font-weight: 800;
  color: white;
  line-height: 1;
}

.hero-stat-label {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 500;
}

.hero-stat-divider {
  width: 1px;
  height: 32px;
  background: rgba(255, 255, 255, 0.2);
}

/* ===== Section ===== */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-indicator {
  width: 4px;
  height: 22px;
  background: var(--gradient-primary);
  border-radius: 2px;
}

.section-title h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.section-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.post-count {
  font-size: 13px;
  color: var(--text-muted);
}

/* ===== Post Cards ===== */
.post-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 24px 28px;
  margin-bottom: 14px;
  cursor: pointer;
  transition: var(--transition);
  position: relative;
  overflow: hidden;
}

.post-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--gradient-primary);
  opacity: 0;
  transition: opacity 0.25s ease;
}

.post-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: rgba(99, 102, 241, 0.2);
}

.post-card:hover::before {
  opacity: 1;
}

.post-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.post-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  flex: 1;
  transition: color 0.2s;
}

.post-card:hover .post-title {
  color: var(--primary);
}

.post-arrow {
  color: var(--text-muted);
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 3px;
  transition: var(--transition);
  opacity: 0;
}

.post-card:hover .post-arrow {
  opacity: 1;
  transform: translateX(2px);
  color: var(--primary);
}

.post-excerpt {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.7;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  transition: color 0.2s;
}

.author-name {
  font-weight: 500;
}

.post-meta-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.post-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

/* ===== Loading State ===== */
.loading-state {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.skeleton-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 24px 28px;
}

/* ===== Empty State ===== */
.empty-state {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 60px 0;
}

/* ===== Pagination ===== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}

/* ===== Clickable Author ===== */
.clickable-author {
  cursor: pointer;
  transition: color 0.2s ease;
}

.clickable-author:hover {
  color: var(--primary) !important;
}

.clickable-author:hover .author-name {
  text-decoration: underline;
  text-underline-offset: 2px;
}
</style>
