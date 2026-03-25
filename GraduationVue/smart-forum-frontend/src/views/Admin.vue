<template>
  <div class="admin-page">
    <!-- 顶部标题 -->
    <div class="admin-header">
      <div class="admin-header-content">
        <div class="admin-header-left">
          <div class="admin-icon-wrapper">
            <el-icon :size="24"><Setting /></el-icon>
          </div>
          <div>
            <h1 class="admin-title">管理后台</h1>
            <p class="admin-subtitle">管理用户、帖子和查看数据统计</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card stat-card-users">
        <div class="stat-icon-wrapper">
          <el-icon :size="22"><User /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.userCount || 0 }}</span>
          <span class="stat-label">注册用户</span>
        </div>
      </div>
      <div class="stat-card stat-card-posts">
        <div class="stat-icon-wrapper">
          <el-icon :size="22"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stats.postCount || 0 }}</span>
          <span class="stat-label">发布文章</span>
        </div>
      </div>
      <div class="stat-card stat-card-date">
        <div class="stat-icon-wrapper">
          <el-icon :size="22"><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value stat-date">{{ stats.date || '-' }}</span>
          <span class="stat-label">当前日期</span>
        </div>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="admin-tabs">
      <div class="tab-item" :class="{ active: activeTab === 'users' }" @click="activeTab = 'users'">
        <el-icon><User /></el-icon>
        <span>用户管理</span>
      </div>
      <div class="tab-item" :class="{ active: activeTab === 'posts' }" @click="activeTab = 'posts'">
        <el-icon><Document /></el-icon>
        <span>帖子管理</span>
      </div>
    </div>

    <!-- 用户管理 -->
    <div v-if="activeTab === 'users'" class="admin-section">
      <div class="section-toolbar">
        <el-input v-model="userKeyword" placeholder="搜索用户名或昵称..." clearable @keyup.enter="loadUsers"
          @clear="loadUsers" class="toolbar-search">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadUsers" round>
          <el-icon><Search /></el-icon>搜索
        </el-button>
      </div>

      <div class="table-wrapper">
        <el-table :data="users" stripe style="width: 100%" v-loading="loadingUsers" 
          :header-cell-style="{ background: '#f7f8fc', color: '#3d4155', fontWeight: 600 }">
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="username" label="用户名" width="140" />
          <el-table-column prop="nickname" label="昵称" width="140" />
          <el-table-column prop="role" label="角色" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small" effect="dark" round>
                {{ row.role === 'admin' ? '管理员' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small" effect="plain" round>
                {{ row.status === 'active' ? '正常' : '封禁' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="160" align="center">
            <template #default="{ row }">
              <template v-if="row.role !== 'admin'">
                <el-button v-if="row.status === 'active'" type="danger" size="small" round plain
                  @click="handleBan(row)">
                  <el-icon><Lock /></el-icon>封禁
                </el-button>
                <el-button v-else type="success" size="small" round plain @click="handleUnban(row)">
                  <el-icon><Unlock /></el-icon>解封
                </el-button>
              </template>
              <span v-else class="admin-label">—</span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-wrapper">
        <el-pagination background layout="prev, pager, next" :current-page="userPage" :page-size="userPageSize"
          :total="userTotal" @current-change="handleUserPageChange" />
      </div>
    </div>

    <!-- 帖子管理 -->
    <div v-if="activeTab === 'posts'" class="admin-section">
      <div class="section-toolbar">
        <el-input v-model="postKeyword" placeholder="搜索帖子标题..." clearable @keyup.enter="loadPosts"
          @clear="loadPosts" class="toolbar-search">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="loadPosts" round>
          <el-icon><Search /></el-icon>搜索
        </el-button>
      </div>

      <div class="table-wrapper">
        <el-table :data="posts" stripe style="width: 100%" v-loading="loadingPosts"
          :header-cell-style="{ background: '#f7f8fc', color: '#3d4155', fontWeight: 600 }">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="post-title-cell">
                <el-tag v-if="row.isTop === 1" type="warning" size="small" effect="dark" round
                  class="top-tag">置顶</el-tag>
                <span class="clickable-title" @click="goToPost(row.id)">{{ row.title }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="authorName" label="作者" width="120" />
          <el-table-column label="互动数据" width="200" align="center">
            <template #default="{ row }">
              <div class="interaction-stats">
                <span class="stat-item" title="浏览"><el-icon><View /></el-icon>{{ row.viewCount || 0 }}</span>
                <span class="stat-item" title="点赞"><el-icon><Star /></el-icon>{{ row.likeCount || 0 }}</span>
                <span class="stat-item" title="评论"><el-icon><ChatDotRound /></el-icon>{{ row.commentCount || 0 }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="发布时间" width="170">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" align="center">
            <template #default="{ row }">
              <el-button :type="row.isTop === 1 ? 'warning' : 'primary'" size="small" round plain
                @click="handleToggleTop(row)">
                <el-icon><Top /></el-icon>{{ row.isTop === 1 ? '取消置顶' : '置顶' }}
              </el-button>
              <el-button type="danger" size="small" round plain @click="handleDeletePost(row)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div class="pagination-wrapper">
        <el-pagination background layout="prev, pager, next" :current-page="postPage" :page-size="postPageSize"
          :total="postTotal" @current-change="handlePostPageChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, User, Document, Calendar, Search, Lock, Unlock,
  View, Star, ChatDotRound, Top, Delete
} from '@element-plus/icons-vue'
import { getAdminStats, getAdminUsers, banUser, unbanUser, getAdminPosts, adminDeletePost, toggleTopPost } from '@/api/admin'

const router = useRouter()

// 统计
const stats = ref({})

// 用户管理
const activeTab = ref('users')
const users = ref([])
const loadingUsers = ref(false)
const userPage = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)
const userKeyword = ref('')

// 帖子管理
const posts = ref([])
const loadingPosts = ref(false)
const postPage = ref(1)
const postPageSize = ref(10)
const postTotal = ref(0)
const postKeyword = ref('')

onMounted(() => {
  loadStats()
  loadUsers()
  loadPosts()
})

const loadStats = async () => {
  try {
    const res = await getAdminStats()
    stats.value = res.data
  } catch (e) { /* ignore */ }
}

const loadUsers = async () => {
  loadingUsers.value = true
  try {
    const res = await getAdminUsers(userPage.value, userPageSize.value, userKeyword.value)
    users.value = res.data.records
    userTotal.value = Number(res.data.total)
  } finally {
    loadingUsers.value = false
  }
}

const loadPosts = async () => {
  loadingPosts.value = true
  try {
    const res = await getAdminPosts(postPage.value, postPageSize.value, postKeyword.value)
    posts.value = res.data.records
    postTotal.value = Number(res.data.total)
  } finally {
    loadingPosts.value = false
  }
}

const handleUserPageChange = (page) => {
  userPage.value = page
  loadUsers()
}

const handlePostPageChange = (page) => {
  postPage.value = page
  loadPosts()
}

const handleBan = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要封禁用户 "${user.nickname || user.username}" 吗？封禁后该用户将无法登录。`, '封禁确认', {
      confirmButtonText: '确定封禁',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await banUser(user.id)
    ElMessage.success('已封禁')
    loadUsers()
  } catch { /* cancelled */ }
}

const handleUnban = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要解封用户 "${user.nickname || user.username}" 吗？`, '解封确认', {
      confirmButtonText: '确定解封',
      cancelButtonText: '取消',
      type: 'info'
    })
    await unbanUser(user.id)
    ElMessage.success('已解封')
    loadUsers()
  } catch { /* cancelled */ }
}

const handleToggleTop = async (post) => {
  try {
    await toggleTopPost(post.id)
    ElMessage.success(post.isTop === 1 ? '已取消置顶' : '已置顶')
    loadPosts()
  } catch { /* ignore */ }
}

const handleDeletePost = async (post) => {
  try {
    await ElMessageBox.confirm(`确定要删除帖子 "${post.title}" 吗？此操作不可恢复。`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    await adminDeletePost(post.id)
    ElMessage.success('已删除')
    loadPosts()
    loadStats()
  } catch { /* cancelled */ }
}

const goToPost = (id) => router.push(`/post/${id}`)

const formatDate = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  return d.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
/* ===== Page ===== */
.admin-page {
  max-width: 1100px;
  margin: 0 auto;
}

/* ===== Header ===== */
.admin-header {
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 40%, #4338ca 100%);
  border-radius: 18px;
  padding: 36px 36px 32px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

.admin-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 30px 30px;
}

.admin-header-content {
  position: relative;
  z-index: 1;
}

.admin-header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-icon-wrapper {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 14px;
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.admin-title {
  font-size: 26px;
  font-weight: 800;
  color: white;
  margin: 0 0 4px;
  letter-spacing: -0.5px;
}

.admin-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

/* ===== Stats ===== */
.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 22px 24px;
  border-radius: 14px;
  background: white;
  border: 1px solid var(--border-light, #e5e7eb);
  transition: all 0.25s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-icon-wrapper {
  width: 46px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: white;
}

.stat-card-users .stat-icon-wrapper {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.stat-card-posts .stat-icon-wrapper {
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
}

.stat-card-date .stat-icon-wrapper {
  background: linear-gradient(135deg, #f59e0b, #f97316);
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-value {
  font-size: 26px;
  font-weight: 800;
  color: #1e293b;
  line-height: 1;
}

.stat-date {
  font-size: 16px !important;
}

.stat-label {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

/* ===== Tabs ===== */
.admin-tabs {
  display: flex;
  gap: 4px;
  background: #f1f5f9;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 20px;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-item:hover {
  color: #6366f1;
}

.tab-item.active {
  background: white;
  color: #6366f1;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* ===== Section ===== */
.admin-section {
  background: white;
  border-radius: 14px;
  border: 1px solid var(--border-light, #e5e7eb);
  padding: 24px;
}

.section-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.toolbar-search {
  max-width: 360px;
}

/* ===== Table ===== */
.table-wrapper {
  border-radius: 10px;
  overflow: hidden;
}

.table-wrapper :deep(.el-table) {
  border-radius: 10px;
}

.admin-label {
  color: #94a3b8;
  font-size: 13px;
}

/* ===== Post Title Cell ===== */
.post-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-tag {
  flex-shrink: 0;
}

.clickable-title {
  cursor: pointer;
  transition: color 0.2s;
}

.clickable-title:hover {
  color: #6366f1;
}

/* ===== Interaction Stats ===== */
.interaction-stats {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  color: #64748b;
}

/* ===== Pagination ===== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* ===== Admin Badge in navbar (global supplement) ===== */
.admin-badge {
  background: linear-gradient(135deg, #ef4444, #f97316) !important;
}
</style>
