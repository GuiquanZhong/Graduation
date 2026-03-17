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
                            <User />
                        </el-icon>
                        {{ pageTitle }}
                    </h1>
                    <p class="page-subtitle">共 {{ total }} 人</p>
                </div>
            </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state fade-in-up">
            <el-skeleton :rows="3" animated v-for="i in 3" :key="i" style="margin-bottom: 16px" />
        </div>

        <!-- 空状态 -->
        <div v-else-if="users.length === 0" class="empty-state fade-in-up">
            <el-empty :description="type === 'following' ? '还没有关注任何人' : '还没有粉丝'">
                <el-button type="primary" round @click="$router.push('/')">去首页看看</el-button>
            </el-empty>
        </div>

        <!-- 用户列表 -->
        <template v-else>
            <div class="user-card fade-in-up" v-for="(user, index) in users" :key="user.id"
                :style="{ animationDelay: index * 0.05 + 's' }" @click="$router.push(`/user/${user.id}`)">
                <div class="user-card-body">
                    <div class="user-info-left">
                        <el-avatar :size="48" :src="user.avatar"
                            :style="{ background: getAvatarColor(user.nickname), fontSize: '20px' }">
                            {{ user.nickname?.charAt(0) || '?' }}
                        </el-avatar>
                        <div class="user-details">
                            <h3 class="user-nickname">{{ user.nickname || '匿名用户' }}</h3>
                            <p class="user-username">@{{ user.username }}</p>
                        </div>
                    </div>
                    <div class="user-actions">
                        <el-button v-if="isSelf && type === 'following'" type="danger" size="small" plain round
                            @click.stop="handleRemoveFollow(user.id, index)" class="remove-btn">
                            <el-icon>
                                <Close />
                            </el-icon>
                            取消关注
                        </el-button>
                        <el-icon v-else class="arrow-icon">
                            <ArrowRight />
                        </el-icon>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserFollowing, getUserFollowers, removeFollow, getUserProfile } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { ArrowLeft, ArrowRight, User, Close } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = computed(() => route.params.userId)
const type = computed(() => route.params.type) // 'following' 或 'followers'
const isSelf = computed(() => {
    return userStore.isLoggedIn && userStore.userInfo?.userId == userId.value
})

const users = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const targetNickname = ref('')

const pageTitle = computed(() => {
    const prefix = isSelf.value ? '我的' : `${targetNickname.value || '用户'}的`
    return type.value === 'following' ? `${prefix}关注` : `${prefix}粉丝`
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

// 监听 type 变化
watch(() => route.params.type, () => {
    if (route.name === 'UserFollowList') {
        currentPage.value = 1
        loadData()
    }
})

const loadData = async () => {
    loading.value = true
    try {
        const apiFn = type.value === 'following' ? getUserFollowing : getUserFollowers
        const res = await apiFn(userId.value, currentPage.value, pageSize.value)
        users.value = res.data.records || []
        total.value = Number(res.data.total || 0)
    } finally {
        loading.value = false
    }
}

const handlePageChange = (page) => {
    currentPage.value = page
    loadData()
}

const handleRemoveFollow = async (targetUserId, index) => {
    try {
        await ElMessageBox.confirm('确定要取消关注这个用户吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
        })
        await removeFollow(targetUserId)
        users.value.splice(index, 1)
        total.value--
        ElMessage.success('已取消关注')
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

const getAvatarColor = (name) => {
    const colors = ['#6366f1', '#8b5cf6', '#ec4899', '#f59e0b', '#10b981', '#3b82f6']
    return colors[(name || '').charCodeAt(0) % colors.length]
}
</script>

<style scoped>
.list-page {
    max-width: 860px;
    margin: 0 auto;
}

.page-header {
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-xl);
    padding: 20px 24px;
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
    overflow: hidden;
    box-shadow: var(--shadow-sm);
}

.page-header::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 3px;
    background: var(--gradient-primary);
}

.header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    position: relative;
    z-index: 1;
}

.back-btn {
    border: 1px solid var(--border) !important;
    background: white !important;
    transition: var(--transition) !important;
}

.back-btn:hover {
    background: var(--primary-bg) !important;
    color: var(--primary) !important;
    border-color: var(--primary-light) !important;
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
    color: var(--primary, #6366f1);
    font-size: 22px;
}

.page-subtitle {
    font-size: 13px;
    color: var(--text-muted);
}

/* 用户卡片 */
.user-card {
    background: var(--bg-card);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-md);
    margin-bottom: 10px;
    transition: var(--transition);
    animation: fadeInUp 0.4s ease both;
    cursor: pointer;
    overflow: hidden;
}

.user-card:hover {
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
    border-color: rgba(99, 102, 241, 0.2);
}

.user-card-body {
    padding: 16px 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.user-info-left {
    display: flex;
    align-items: center;
    gap: 16px;
}

.user-info-left .el-avatar {
    box-shadow: 0 2px 12px rgba(99, 102, 241, 0.15);
    border: 2px solid white;
    flex-shrink: 0;
}

.user-details {
    flex: 1;
}

.user-nickname {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 2px;
    transition: color 0.2s;
}

.user-card:hover .user-nickname {
    color: var(--primary);
}

.user-username {
    font-size: 13px;
    color: var(--text-muted);
}

.user-actions {
    flex-shrink: 0;
}

.arrow-icon {
    color: var(--text-muted);
    font-size: 16px;
    transition: transform 0.2s;
}

.user-card:hover .arrow-icon {
    color: var(--primary);
    transform: translateX(4px);
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
