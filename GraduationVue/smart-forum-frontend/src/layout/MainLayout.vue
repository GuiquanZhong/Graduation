<template>
  <div class="layout">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="navbar-inner">
        <div class="navbar-left">
          <router-link to="/" class="logo">
            <el-icon class="logo-icon"><Cpu /></el-icon>
            <span class="logo-text">智能论坛</span>
          </router-link>
          <nav class="nav-links">
            <router-link to="/" class="nav-link" active-class="active">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </router-link>
            <router-link to="/ai-chat" class="nav-link" active-class="active">
              <el-icon><ChatDotRound /></el-icon>
              <span>AI 问答</span>
            </router-link>
          </nav>
        </div>

        <div class="navbar-right">
          <template v-if="userStore.isLoggedIn">
            <el-button type="primary" round @click="$router.push('/post/create')">
              <el-icon><Edit /></el-icon>
              <span>发布文章</span>
            </el-button>
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-avatar">
                <el-avatar :size="36" :style="{ background: 'var(--gradient-primary)' }">
                  {{ userStore.nickname?.charAt(0) }}
                </el-avatar>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item disabled>
                    <span style="font-weight:600">{{ userStore.nickname }}</span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="profile">
                    <el-icon><User /></el-icon>
                    个人主页
                  </el-dropdown-item>
                  <el-dropdown-item command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button type="primary" round @click="$router.push('/login')">
              <el-icon><User /></el-icon>
              <span>登录</span>
            </el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 页面内容 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <p>© 2026 智能论坛 · 基于 Spring Boot & Vue 3 & AI 构建</p>
    </footer>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const handleCommand = (cmd) => {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      router.push('/')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.logo-icon {
  font-size: 28px;
  color: var(--primary);
}

.logo-text {
  background: var(--gradient-primary);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-links {
  display: flex;
  gap: 4px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
  transition: var(--transition);
}

.nav-link:hover {
  background: var(--primary-bg);
  color: var(--primary);
}

.nav-link.active {
  background: var(--primary-bg);
  color: var(--primary);
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  cursor: pointer;
  transition: var(--transition);
}

.user-avatar:hover {
  transform: scale(1.05);
}

.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}

.footer {
  text-align: center;
  padding: 24px;
  color: var(--text-muted);
  font-size: 13px;
  border-top: 1px solid var(--border-light);
}
</style>
