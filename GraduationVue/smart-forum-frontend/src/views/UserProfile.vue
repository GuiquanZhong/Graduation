<template>
  <div class="profile-page">
    <!-- 个人信息卡片 -->
    <div class="profile-card fade-in-up">
      <div class="profile-header">
        <div class="profile-avatar">
          <el-avatar :size="80" :style="{ background: 'var(--gradient-primary)', fontSize: '32px' }">
            {{ userStore.nickname?.charAt(0) }}
          </el-avatar>
        </div>
        <div class="profile-info">
          <h1 class="profile-name">{{ userStore.nickname }}</h1>
          <p class="profile-username">@{{ userStore.userInfo?.username }}</p>
          <p class="profile-join-time">
            <el-icon>
              <Calendar />
            </el-icon>
            加入于 {{ formatJoinTime() }}
          </p>
        </div>
      </div>

      <!-- 统计栏 -->
      <div class="stats-bar">
        <div class="stat-item stat-clickable" @click="activeTab = 'posts'; handleTabChange()">
          <span class="stat-value">{{ postTotal }}</span>
          <span class="stat-label">帖子</span>
        </div>
        <div class="stat-item stat-clickable" @click="activeTab = 'comments'; handleTabChange()">
          <span class="stat-value">{{ commentTotal }}</span>
          <span class="stat-label">回答</span>
        </div>
        <div class="stat-item stat-clickable" @click="activeTab = 'favorites'; handleTabChange()">
          <span class="stat-value">{{ favoriteTotal }}</span>
          <span class="stat-label">收藏</span>
        </div>
        <div class="stat-item stat-clickable" @click="activeTab = 'likes'; handleTabChange()">
          <span class="stat-value">{{ likeTotal }}</span>
          <span class="stat-label">点赞</span>
        </div>
        <div class="stat-item stat-clickable" @click="$router.push(`/user/${userStore.userInfo?.userId}/following`)">
          <span class="stat-value">{{ followingCount }}</span>
          <span class="stat-label">关注</span>
        </div>
        <div class="stat-item stat-clickable" @click="$router.push(`/user/${userStore.userInfo?.userId}/followers`)">
          <span class="stat-value">{{ followerCount }}</span>
          <span class="stat-label">粉丝</span>
        </div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="content-section fade-in-up" style="animation-delay: 0.1s">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="profile-tabs">
        <el-tab-pane label="帖子" name="posts">
          <template #label>
            <span class="tab-label">
              <el-icon>
                <Document />
              </el-icon>
              帖子
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="回答" name="comments">
          <template #label>
            <span class="tab-label">
              <el-icon>
                <ChatLineSquare />
              </el-icon>
              回答
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已收藏" name="favorites">
          <template #label>
            <span class="tab-label">
              <el-icon>
                <Star />
              </el-icon>
              已收藏
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已点赞" name="likes">
          <template #label>
            <span class="tab-label">
              <el-icon>
                <Flag />
              </el-icon>
              已点赞
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <!-- 帖子列表 -->
      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="4" animated v-for="i in 3" :key="i" style="margin-bottom: 16px" />
      </div>

      <template v-else-if="activeTab !== 'comments'">
        <div v-if="posts.length === 0" class="empty-state">
          <el-empty
            :description="activeTab === 'posts' ? '还没发布过文章' : activeTab === 'favorites' ? '还没有收藏任何文章' : '还没有点赞任何文章'">
            <el-button type="primary" round @click="$router.push('/')">去首页看看</el-button>
          </el-empty>
        </div>
        <template v-else>
          <div class="post-card" v-for="(post, index) in posts" :key="post.id"
            :style="{ animationDelay: index * 0.05 + 's' }" @click="$router.push(`/post/${post.id}`)">
            <div class="post-card-body">
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
              <div class="post-meta">
                <div class="post-author">
                  <el-avatar :size="22" :style="{ background: getAvatarColor(post.authorName), fontSize: '11px' }">
                    {{ post.authorName?.charAt(0) || '?' }}
                  </el-avatar>
                  <span>{{ post.authorName || '匿名' }}</span>
                </div>
                <span class="post-time">
                  <el-icon>
                    <Clock />
                  </el-icon>
                  {{ formatTime(post.createdAt) }}
                </span>
              </div>
            </div>
          </div>

          <!-- 分页 -->
          <div class="pagination-wrapper" v-if="total > pageSize">
            <el-pagination background layout="prev, pager, next" :current-page="currentPage" :page-size="pageSize"
              :total="total" @current-change="handlePageChange" />
          </div>
        </template>
      </template>

      <!-- 回答列表 -->
      <template v-else>
        <div v-if="commentList.length === 0" class="empty-state">
          <el-empty description="还没有回答过任何问题">
            <el-button type="primary" round @click="$router.push('/')">去首页看看</el-button>
          </el-empty>
        </div>
        <template v-else>
          <div class="comment-card" v-for="(comment, index) in commentList" :key="comment.id"
            :style="{ animationDelay: index * 0.05 + 's' }" @click="$router.push(`/post/${comment.postId}`)">
            <div class="comment-card-body">
              <div class="comment-source">
                <el-icon>
                  <Document />
                </el-icon>
                <span>回答于：{{ comment.postTitle || '未知帖子' }}</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
              <div class="comment-time">
                <el-icon>
                  <Clock />
                </el-icon>
                {{ formatTime(comment.createdAt) }}
              </div>
            </div>
          </div>

          <div class="pagination-wrapper" v-if="total > pageSize">
            <el-pagination background layout="prev, pager, next" :current-page="currentPage" :page-size="pageSize"
              :total="total" @current-change="handlePageChange" />
          </div>
        </template>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getUserFavoritePosts } from '@/api/favorite'
import { getUserLikedPosts } from '@/api/like'
import { getUserProfile, getUserPosts, getUserComments } from '@/api/user'
import { Calendar, Star, Flag, Clock, Document, ChatLineSquare } from '@element-plus/icons-vue'

const userStore = useUserStore()

const activeTab = ref('posts')
const posts = ref([])
const commentList = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const postTotal = ref(0)
const commentTotal = ref(0)
const favoriteTotal = ref(0)
const likeTotal = ref(0)
const followingCount = ref(0)
const followerCount = ref(0)

onMounted(() => {
  loadData()
  loadCounts()
  loadFollowCounts()
})

const loadData = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) return

    let res
    if (activeTab.value === 'posts') {
      res = await getUserPosts(userId, currentPage.value, pageSize.value)
      posts.value = res.data.records || []
      total.value = Number(res.data.total || 0)
    } else if (activeTab.value === 'comments') {
      res = await getUserComments(userId, currentPage.value, pageSize.value)
      commentList.value = res.data.records || []
      total.value = Number(res.data.total || 0)
    } else if (activeTab.value === 'favorites') {
      res = await getUserFavoritePosts(currentPage.value, pageSize.value)
      posts.value = res.data.records || []
      total.value = Number(res.data.total || 0)
    } else {
      res = await getUserLikedPosts(currentPage.value, pageSize.value)
      posts.value = res.data.records || []
      total.value = Number(res.data.total || 0)
    }
  } finally {
    loading.value = false
  }
}

const loadCounts = async () => {
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) return

    const [postRes, commentRes, favRes, likeRes] = await Promise.all([
      getUserPosts(userId, 1, 1),
      getUserComments(userId, 1, 1),
      getUserFavoritePosts(1, 1),
      getUserLikedPosts(1, 1)
    ])
    postTotal.value = Number(postRes.data.total || 0)
    commentTotal.value = Number(commentRes.data.total || 0)
    favoriteTotal.value = Number(favRes.data.total || 0)
    likeTotal.value = Number(likeRes.data.total || 0)
  } catch {
    // 静默处理
  }
}

const loadFollowCounts = async () => {
  try {
    if (userStore.userInfo?.userId) {
      const res = await getUserProfile(userStore.userInfo.userId)
      followingCount.value = res.data.followingCount || 0
      followerCount.value = res.data.followerCount || 0
    }
  } catch {
    // 静默处理
  }
}

const handleTabChange = () => {
  currentPage.value = 1
  loadData()
}

const handlePageChange = (page) => {
  currentPage.value = page
  loadData()
}

const getExcerpt = (content) => {
  if (!content) return ''
  const plain = content.replace(/<[^>]+>/g, '').replace(/[#*`>\-\[\]()]/g, '')
  return plain.length > 120 ? plain.substring(0, 120) + '...' : plain
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

const formatJoinTime = () => {
  // userInfo 中暂时没有 createdAt，显示友好信息
  return '智能论坛'
}

const getAvatarColor = (name) => {
  const colors = ['#6366f1', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#3b82f6']
  return colors[(name || '').charCodeAt(0) % colors.length]
}
</script>

<style scoped>
.profile-page {
  max-width: 820px;
  margin: 0 auto;
}

.profile-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: 32px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.profile-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 100px;
  background: var(--gradient-primary);
  opacity: 0.08;
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
  z-index: 1;
  margin-bottom: 28px;
}

.profile-avatar {
  flex-shrink: 0;
}

.profile-avatar .el-avatar {
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.3);
  border: 3px solid white;
}

.profile-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.profile-username {
  font-size: 14px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.profile-join-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.stats-bar {
  display: flex;
  gap: 0;
  border-top: 1px solid var(--border-light);
  padding-top: 20px;
  position: relative;
  z-index: 1;
}

.stat-item {
  flex: 1;
  text-align: center;
  position: relative;
  cursor: pointer;
}

.stat-item:hover .stat-value {
  color: var(--primary-light);
}

.stat-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 1px;
  height: 28px;
  background: var(--border-light);
}

.stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 4px;
  transition: color 0.3s;
}

.stat-label {
  font-size: 13px;
  color: var(--text-secondary);
}

.stat-placeholder .stat-value {
  color: var(--text-muted);
}

.stat-placeholder .stat-label {
  color: var(--text-muted);
}

/* Tab 区域 */
.content-section {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: 24px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 帖子卡片 */
.post-card {
  background: var(--bg-secondary, #f8f9fa);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: var(--transition);
  animation: fadeInUp 0.4s ease both;
}

.post-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--primary-light);
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.post-excerpt {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.post-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.post-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

/* 评论/回答卡片 */
.comment-card {
  background: var(--bg-secondary, #f8f9fa);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: var(--transition);
  animation: fadeInUp 0.4s ease both;
}

.comment-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--primary-light);
}

.comment-source {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--primary);
  font-weight: 500;
  margin-bottom: 10px;
}

.comment-content {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.7;
  margin-bottom: 10px;
}

.comment-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.loading-state {
  padding: 20px 0;
}

.empty-state {
  padding: 60px 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(16px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
