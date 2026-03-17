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
        <div class="profile-actions">
          <el-button type="primary" plain round @click="openProfileEditDialog">
            <el-icon><Edit /></el-icon> 编辑资料
          </el-button>
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
            :style="{ animationDelay: index * 0.05 + 's' }">
            <div class="post-card-body" @click="$router.push(`/post/${post.id}`)">
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
            <!-- 编辑/删除按钮（仅帖子Tab显示） -->
            <div class="post-actions" v-if="activeTab === 'posts'" @click.stop>
              <el-button type="primary" plain size="small" @click="openPostEditDialog(post)">
                <el-icon><Edit /></el-icon>
              </el-button>
              <el-button type="danger" plain size="small" @click="handleDeletePost(post)">
                <el-icon><Delete /></el-icon>
              </el-button>
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

  <!-- 编辑资料对话框 -->
  <el-dialog v-model="profileEditDialogVisible" title="编辑资料" width="480px" :close-on-click-modal="false">
    <el-tabs v-model="profileEditTab" class="profile-edit-tabs">
      <!-- 修改昵称 -->
      <el-tab-pane label="修改昵称" name="nickname">
        <div class="edit-section">
          <div class="form-group">
            <label class="form-label">新昵称</label>
            <el-input v-model="newNickname" placeholder="请输入新昵称" maxlength="30" show-word-limit />
          </div>
          <el-button type="primary" :loading="nicknameLoading" @click="handleUpdateNickname"
                     :disabled="!newNickname.trim()" style="width:100%">
            保存昵称
          </el-button>
        </div>
      </el-tab-pane>

      <!-- 修改密码 -->
      <el-tab-pane label="修改密码" name="password">
        <div class="edit-section">
          <div class="form-group">
            <label class="form-label">原密码</label>
            <el-input v-model="oldPassword" type="password" placeholder="请输入原密码" show-password />
          </div>
          <div class="form-group">
            <label class="form-label">新密码</label>
            <el-input v-model="newPassword" type="password" placeholder="请输入新密码（至少6位）" show-password />
          </div>
          <div class="form-group">
            <label class="form-label">确认新密码</label>
            <el-input v-model="confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
          </div>
          <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword"
                     :disabled="!oldPassword || !newPassword || !confirmPassword" style="width:100%">
            保存密码
          </el-button>
        </div>
      </el-tab-pane>
    </el-tabs>
    <template #footer>
      <el-button @click="profileEditDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 编辑帖子对话框 -->
  <el-dialog v-model="postEditDialogVisible" title="编辑文章" width="700px" :close-on-click-modal="false">
    <div class="edit-form">
      <div class="form-group">
        <label class="form-label">文章标题</label>
        <el-input v-model="editPostTitle" placeholder="请输入文章标题" maxlength="100" show-word-limit />
      </div>
      <div class="form-group">
        <label class="form-label">文章内容</label>
        <el-input v-model="editPostContent" type="textarea" :rows="12" placeholder="请输入文章内容" resize="vertical" />
      </div>
    </div>
    <template #footer>
      <el-button @click="postEditDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="postEditLoading" @click="handlePostEditSubmit"
                 :disabled="!editPostTitle.trim() || !editPostContent.trim()">
        保存修改
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getUserFavoritePosts } from '@/api/favorite'
import { getUserLikedPosts } from '@/api/like'
import { getUserProfile, getUserPosts, getUserComments, getUserInfo, updateNickname, changePassword } from '@/api/user'
import { updatePost, deletePost } from '@/api/post'
import { Calendar, Star, Flag, Clock, Document, ChatLineSquare, Edit, Delete } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

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
const joinTime = ref('')

// 编辑资料相关
const profileEditDialogVisible = ref(false)
const profileEditTab = ref('nickname')
const newNickname = ref('')
const nicknameLoading = ref(false)
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const passwordLoading = ref(false)

// 编辑帖子相关
const postEditDialogVisible = ref(false)
const editPostTitle = ref('')
const editPostContent = ref('')
const editPostLoading = ref(false)
const editingPostId = ref(null)

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
      const [profileRes, infoRes] = await Promise.all([
        getUserProfile(userStore.userInfo.userId),
        getUserInfo()
      ])
      followingCount.value = profileRes.data.followingCount || 0
      followerCount.value = profileRes.data.followerCount || 0
      joinTime.value = infoRes.data.createdAt || ''
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

// 编辑资料
const openProfileEditDialog = () => {
  newNickname.value = userStore.nickname
  oldPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  profileEditTab.value = 'nickname'
  profileEditDialogVisible.value = true
}

const handleUpdateNickname = async () => {
  nicknameLoading.value = true
  try {
    await updateNickname(newNickname.value.trim())
    // 同步更新 store 中的昵称
    if (userStore.userInfo) {
      userStore.userInfo.nickname = newNickname.value.trim()
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    }
    ElMessage.success('昵称修改成功')
  } finally {
    nicknameLoading.value = false
  }
}

const handleChangePassword = async () => {
  if (newPassword.value !== confirmPassword.value) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }
  if (newPassword.value.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  passwordLoading.value = true
  try {
    await changePassword(oldPassword.value, newPassword.value)
    oldPassword.value = ''
    newPassword.value = ''
    confirmPassword.value = ''
    ElMessage.success('密码修改成功，请重新登录')
    profileEditDialogVisible.value = false
    userStore.logout()
    router.push('/login')
  } finally {
    passwordLoading.value = false
  }
}

// 编辑帖子
const openPostEditDialog = (post) => {
  editingPostId.value = post.id
  editPostTitle.value = post.title
  editPostContent.value = post.content
  postEditDialogVisible.value = true
}

const handlePostEditSubmit = async () => {
  editPostLoading.value = true
  try {
    await updatePost(editingPostId.value, { title: editPostTitle.value.trim(), content: editPostContent.value })
    const post = posts.value.find(p => p.id === editingPostId.value)
    if (post) {
      post.title = editPostTitle.value.trim()
      post.content = editPostContent.value
    }
    postEditDialogVisible.value = false
    ElMessage.success('文章修改成功')
  } finally {
    editPostLoading.value = false
  }
}

const handleDeletePost = async (post) => {
  try {
    await ElMessageBox.confirm(`确定要删除「${post.title}」吗？删除后无法恢复。`, '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deletePost(post.id)
    posts.value = posts.value.filter(p => p.id !== post.id)
    total.value = Math.max(0, total.value - 1)
    postTotal.value = Math.max(0, postTotal.value - 1)
    ElMessage.success('文章已删除')
  } catch {
    // 拦截器已处理
  }
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
  if (!joinTime.value) return '智能论坛'
  return new Date(joinTime.value).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const getAvatarColor = (name) => {
  const colors = ['#6366f1', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#3b82f6']
  return colors[(name || '').charCodeAt(0) % colors.length]
}
</script>

<style scoped>
.profile-page {
  max-width: 860px;
  margin: 0 auto;
}

/* ===== Profile Card ===== */
.profile-card {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: 0;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.profile-card::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 200px;
  background: var(--gradient-primary);
  opacity: 0.9;
}

.profile-header {
  display: flex;
  align-items: flex-end;
  gap: 24px;
  position: relative;
  z-index: 1;
  padding: 20px 32px 20px;
  margin-bottom: 20px;
}

.profile-avatar {
  flex-shrink: 0;
  margin-top: 60px;
}

.profile-avatar :deep(.el-avatar) {
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
  border: 4px solid white !important;
}

.profile-info {
  flex: 1;
  padding-top: 72px;
  padding-bottom: 4px;
}

.profile-actions {
  flex-shrink: 0;
  padding-bottom: 8px;
}

.profile-actions :deep(.el-button) {
  color: #fff !important;
  border-color: rgba(255,255,255,0.5) !important;
  background: rgba(255,255,255,0.15) !important;
  backdrop-filter: blur(4px);
}

.profile-actions :deep(.el-button:hover) {
  background: rgba(255,255,255,0.3) !important;
  border-color: rgba(255,255,255,0.8) !important;
}

.profile-name {
  font-size: 24px;
  font-weight: 800;
  color: #fff;
  margin-bottom: 4px;
  letter-spacing: -0.5px;
  text-shadow: 0 1px 4px rgba(0,0,0,0.15);
}

.profile-username {
  font-size: 14px;
  color: rgba(255,255,255,0.85);
  margin-bottom: 8px;
}

.profile-join-time {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: rgba(255,255,255,0.75);
}

/* ===== Stats Bar ===== */
.stats-bar {
  display: flex;
  gap: 0;
  padding: 0 32px 20px;
  position: relative;
  z-index: 1;
  margin-top: 4px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 14px 8px;
  border-radius: var(--radius-md);
  transition: var(--transition);
  position: relative;
}

.stat-clickable {
  cursor: pointer;
}

.stat-clickable:hover {
  background: var(--primary-bg);
}

.stat-clickable:hover .stat-value {
  color: var(--primary);
}

.stat-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 25%;
  height: 50%;
  width: 1px;
  background: var(--border-light);
}

.stat-value {
  display: block;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 3px;
  transition: color 0.2s;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  font-weight: 500;
}

/* ===== Content Section (Tabs) ===== */
.content-section {
  background: var(--bg-card);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-xl);
  padding: 24px 28px;
  box-shadow: var(--shadow-sm);
}

.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 20px;
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
  padding: 0 16px;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  font-weight: 700;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: 2px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* ===== Post Cards (in profile) ===== */
.post-card {
  background: var(--bg-subtle);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  margin-bottom: 10px;
  transition: var(--transition);
  animation: fadeInUp 0.4s ease both;
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.post-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: rgba(99, 102, 241, 0.2);
  background: white;
}

.post-card-body {
  flex: 1;
  cursor: pointer;
}

.post-card:hover .post-title {
  color: var(--primary);
}

.post-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-shrink: 0;
}

.post-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
  line-height: 1.4;
  transition: color 0.2s;
}

.post-excerpt {
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 10px;
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

/* ===== Comment/Answer Cards ===== */
.comment-card {
  background: var(--bg-subtle);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: var(--transition);
  animation: fadeInUp 0.4s ease both;
}

.comment-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: rgba(99, 102, 241, 0.2);
  background: white;
}

.comment-source {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--primary);
  font-weight: 600;
  margin-bottom: 10px;
  padding: 4px 10px;
  background: var(--primary-bg);
  border-radius: var(--radius-full);
  width: fit-content;
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

/* ===== Misc ===== */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.loading-state {
  padding: 8px 0;
}

.empty-state {
  padding: 60px 0;
}

/* ===== Edit Dialog ===== */
.edit-section {
  padding: 12px 0;
}

.edit-section .form-group,
.edit-form .form-group {
  margin-bottom: 20px;
}

.edit-section .form-label,
.edit-form .form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.profile-edit-tabs :deep(.el-tabs__header) {
  margin-bottom: 8px;
}

.profile-edit-tabs :deep(.el-tabs__item) {
  font-weight: 500;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
