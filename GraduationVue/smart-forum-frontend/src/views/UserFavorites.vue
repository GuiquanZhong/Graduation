<template>
    <div class="list-page">
        <!-- 页头 -->
        <div class="page-header fade-in-up">
            <div class="header-left">
                <el-button circle @click="goBack" class="back-btn">
                    <el-icon>
                        <ArrowLeft />
                    </el-icon>
                </el-button>
                <div class="header-info">
                    <h1 class="page-title">
                        <el-icon class="title-icon">
                            <Star />
                        </el-icon>
                        {{ pageTitle }}
                    </h1>
                    <p class="page-subtitle">共 {{ total }} 条收藏</p>
                </div>
            </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state fade-in-up">
            <el-skeleton :rows="4" animated v-for="i in 3" :key="i" style="margin-bottom: 16px" />
        </div>

        <!-- 空状态 -->
        <div v-else-if="posts.length === 0" class="empty-state fade-in-up">
            <el-empty description="还没有收藏任何文章">
                <el-button type="primary" round @click="$router.push('/')">去首页看看</el-button>
            </el-empty>
        </div>

        <!-- 帖子列表 -->
        <template v-else>
            <div class="post-card fade-in-up" v-for="(post, index) in posts" :key="post.id"
                :style="{ animationDelay: index * 0.05 + 's' }">
                <div class="post-card-body" @click="$router.push(`/post/${post.id}`)">
                    <h3 class="post-title">{{ post.title }}</h3>
                    <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
                    <div class="post-meta">
                        <div class="post-author" @click.stop="$router.push(`/user/${post.userId}`)">
                            <el-avatar :size="22" :src="post.authorAvatar"
                                :style="{ background: getAvatarColor(post.authorName), fontSize: '11px' }">
                                {{ post.authorName?.charAt(0) || '?' }}
                            </el-avatar>
                            <span>{{ post.authorName || '匿名' }}</span>
                        </div>
                        <div class="post-actions">
                            <span class="post-time">
                                <el-icon>
                                    <Clock />
                                </el-icon>
                                {{ formatTime(post.createdAt) }}
                            </span>
                            <el-button v-if="isSelf" type="danger" size="small" plain round
                                @click.stop="handleRemove(post.id, index)" class="remove-btn">
                                <el-icon>
                                    <Delete />
                                </el-icon>
                                取消收藏
                            </el-button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 分页 -->
            <div class="pagination-wrapper fade-in-up" v-if="total > pageSize">
                <el-pagination background layout="prev, pager, next" :current-page="currentPage" :page-size="pageSize"
                    :total="total" @current-change="handlePageChange" />
            </div>
        </template>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserFavorites, removeFavorite, getUserProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, Star, Clock, Delete } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = computed(() => route.params.userId)
const isSelf = computed(() => {
    return userStore.isLoggedIn && userStore.userInfo?.userId == userId.value
})

const posts = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const targetNickname = ref('')

const pageTitle = computed(() => {
    if (isSelf.value) return '我的收藏'
    return `${targetNickname.value || '用户'}的收藏`
})

onMounted(async () => {
    if (!isSelf.value) {
        try {
            const res = await getUserProfile(userId.value)
            targetNickname.value = res.data.nickname || ''
        } catch { }
    }
    loadData()
})

const loadData = async () => {
    loading.value = true
    try {
        const res = await getUserFavorites(userId.value, currentPage.value, pageSize.value)
        posts.value = res.data.records || []
        total.value = Number(res.data.total || 0)
    } finally {
        loading.value = false
    }
}

const handlePageChange = (page) => {
    currentPage.value = page
    loadData()
}

const handleRemove = async (postId, index) => {
    try {
        await ElMessageBox.confirm('确定要取消收藏这篇文章吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        await removeFavorite(postId)
        posts.value.splice(index, 1)
        total.value--
        ElMessage.success('已取消收藏')
    } catch {
        // 用户取消确认
    }
}

const goBack = () => {
    if (isSelf.value) {
        router.push('/profile')
    } else {
        router.push(`/user/${userId.value}`)
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

const getAvatarColor = (name) => {
    const colors = ['#6366f1', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#3b82f6']
    return colors[(name || '').charCodeAt(0) % colors.length]
}
</script>

<style scoped>
.list-page {
    max-width: 820px;
    margin: 0 auto;
}

.page-header {
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    padding: 24px 28px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
    overflow: hidden;
}

.page-header::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 100%;
    background: linear-gradient(135deg, rgba(245, 158, 11, 0.06), rgba(99, 102, 241, 0.04));
    pointer-events: none;
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    position: relative;
    z-index: 1;
}

.back-btn {
    border: none;
    background: var(--bg-secondary, #f0f0f0);
    transition: all 0.3s ease;
}

.back-btn:hover {
    background: var(--primary);
    color: white;
    transform: scale(1.1);
}

.page-title {
    font-size: 20px;
    font-weight: 700;
    color: var(--text-primary);
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 4px;
}

.title-icon {
    color: #f59e0b;
    font-size: 22px;
}

.page-subtitle {
    font-size: 13px;
    color: var(--text-muted);
}

/* 帖子卡片 */
.post-card {
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg, 12px);
    margin-bottom: 12px;
    transition: var(--transition);
    animation: fadeInUp 0.4s ease both;
    overflow: hidden;
}

.post-card:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
    border-color: var(--primary-light);
}

.post-card-body {
    padding: 20px 24px;
    cursor: pointer;
}

.post-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 8px;
    line-height: 1.4;
    transition: color 0.2s;
}

.post-card:hover .post-title {
    color: var(--primary);
}

.post-excerpt {
    color: var(--text-secondary);
    font-size: 13px;
    line-height: 1.6;
    margin-bottom: 14px;
}

.post-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
    gap: 8px;
}

.post-author {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 12px;
    color: var(--text-secondary);
    cursor: pointer;
    transition: color 0.2s;
}

.post-author:hover {
    color: var(--primary);
}

.post-actions {
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

.remove-btn {
    font-size: 12px;
}

.pagination-wrapper {
    display: flex;
    justify-content: center;
    margin-top: 24px;
    padding-bottom: 24px;
}

.loading-state {
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    padding: 24px;
}

.empty-state {
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    padding: 60px 0;
}

.fade-in-up {
    animation: fadeInUp 0.5s ease both;
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
