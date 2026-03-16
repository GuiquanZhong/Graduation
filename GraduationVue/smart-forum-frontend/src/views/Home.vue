<template>
  <div class="home-page">
    <!-- Hero区域 -->
    <div class="hero-section">
      <div class="hero-content">
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
              <el-button @click="handleSearch" :icon="Search">搜索</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <!-- 文章列表 -->
    <div class="post-list-section">
      <div class="section-header">
        <h2>
          <el-icon><Document /></el-icon>
          {{ isSearching ? '搜索结果' : '最新文章' }}
        </h2>
        <el-tag v-if="isSearching" closable @close="clearSearch" type="primary">
          "{{ searchKeyword }}" · {{ total }} 条结果
        </el-tag>
      </div>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="4" animated v-for="i in 3" :key="i" style="margin-bottom:20px" />
      </div>

      <div v-else-if="posts.length === 0" class="empty-state">
        <el-empty description="暂无文章，快来发布第一篇吧！">
          <el-button type="primary" @click="$router.push('/post/create')">发布文章</el-button>
        </el-empty>
      </div>

      <template v-else>
        <div class="post-card fade-in-up" v-for="(post, index) in posts" :key="post.id"
             :style="{ animationDelay: index * 0.06 + 's' }" @click="$router.push(`/post/${post.id}`)">
          <div class="post-card-body">
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
            <div class="post-meta">
              <div class="post-author clickable-author" @click.stop="goToUser(post.userId)">
                <el-avatar :size="24" :src="post.authorAvatar"
                           :style="{ background: getAvatarColor(post.authorName), fontSize: '12px' }">
                  {{ post.authorName?.charAt(0) || '?' }}
                </el-avatar>
                <span>{{ post.authorName || '匿名' }}</span>
              </div>
              <span class="post-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(post.createdAt) }}
              </span>
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
import { Search, Document, Clock } from '@element-plus/icons-vue'

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
  return plain.length > 150 ? plain.substring(0, 150) + '...' : plain
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
.hero-section {
  background: var(--gradient-hero);
  border-radius: var(--radius-xl);
  padding: 56px 40px;
  margin-bottom: 32px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 30% 50%, rgba(99,102,241,0.2) 0%, transparent 50%),
              radial-gradient(circle at 70% 80%, rgba(139,92,246,0.15) 0%, transparent 50%);
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-title {
  font-size: 36px;
  font-weight: 700;
  color: white;
  margin-bottom: 12px;
}

.hero-desc {
  color: rgba(255,255,255,0.7);
  font-size: 16px;
  margin-bottom: 28px;
}

.hero-search {
  max-width: 560px;
  margin: 0 auto;
}

.hero-search :deep(.el-input__wrapper) {
  border-radius: 14px !important;
  padding: 4px 4px 4px 16px;
  box-shadow: var(--shadow-lg) !important;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-header h2 {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.post-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 24px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: var(--transition);
}

.post-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--primary-light);
}

.post-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.post-excerpt {
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 14px;
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
}

.post-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-muted);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.loading-state {
  padding: 20px 0;
}

.empty-state {
  padding: 60px 0;
}

.clickable-author {
  cursor: pointer;
  transition: color 0.2s ease;
}

.clickable-author:hover {
  color: var(--primary) !important;
}

.clickable-author:hover span {
  text-decoration: underline;
}
</style>
